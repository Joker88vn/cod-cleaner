package vn.daopham.codcleaner

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class Action(val verb: String, val done: String) {
    UNINSTALL("Gỡ bỏ", "Đã gỡ"),
    DISABLE("Vô hiệu hoá", "Đã tắt"),
    RESTORE("Khôi phục", "Đã khôi phục"),
    ENABLE("Bật lại", "Đã bật lại")
}

data class Progress(val action: Action, val total: Int, val done: Int, val current: String)
data class BatchResult(val action: Action, val ok: List<String>, val failed: List<Pair<String, String>>)
data class LogEntry(val time: String, val text: String)

enum class Filter(val label: String) {
    JUNK("Nên dọn"), CAUTION("Cẩn thận"), ALL("Tất cả"), USER("Đã cài"),
    UNKNOWN("Chưa rõ"), CRITICAL("Hệ thống")
}

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("cod", Context.MODE_PRIVATE)

    var shizuku by mutableStateOf(ShizukuState.NOT_RUNNING)
        private set
    var apps by mutableStateOf<List<AppItem>>(emptyList())
        private set
    var loading by mutableStateOf(true)
        private set
    var progress by mutableStateOf<Progress?>(null)
        private set
    var result by mutableStateOf<BatchResult?>(null)
    var toast by mutableStateOf<String?>(null)

    var expert by mutableStateOf(prefs.getBoolean("expert", false))
        private set

    var query by mutableStateOf("")
    var filter by mutableStateOf(Filter.JUNK)
    val selected = mutableStateListOf<String>()
    val log = mutableStateListOf<LogEntry>()

    var localeTags by mutableStateOf(currentLocales())
        private set

    init {
        loadLog()
        refreshShizuku()
        reload()
    }

    fun refreshShizuku() {
        shizuku = Shell.state(getApplication())
    }

    fun setExpertMode(on: Boolean) {
        expert = on
        prefs.edit().putBoolean("expert", on).apply()
        if (!on) selected.removeAll { pkg -> apps.find { it.pkg == pkg }?.risk == Risk.CRITICAL }
    }

    fun reload() {
        viewModelScope.launch {
            loading = apps.isEmpty()
            val list = withContext(Dispatchers.IO) { AppRepo.load(getApplication()) }
            apps = list
            selected.retainAll(list.filter { it.installed }.map { it.pkg }.toSet())
            loading = false
            localeTags = currentLocales()
        }
    }

    // ---------- Thống kê ----------
    val installedApps get() = apps.filter { it.installed }
    val junkLeft get() = apps.filter { it.installed && it.risk == Risk.SAFE }
    val junkRemoved get() = apps.count { it.removed && it.risk == Risk.SAFE }
    val removedApps get() = apps.filter { it.removed }
    val disabledApps get() = apps.filter { it.disabled }

    /** Điểm gọn gàng 0..1 dựa trên ứng dụng rác đã dọn. */
    val cleanScore: Float
        get() {
            val total = junkLeft.size + junkRemoved
            return if (total == 0) 1f else junkRemoved.toFloat() / total
        }

    fun visibleApps(): List<AppItem> {
        val q = query.trim().lowercase()
        return installedApps.filter { a ->
            val f = when (filter) {
                Filter.JUNK -> a.risk == Risk.SAFE
                Filter.CAUTION -> a.risk == Risk.CAUTION
                Filter.ALL -> true
                Filter.USER -> a.risk == Risk.USER
                Filter.UNKNOWN -> a.risk == Risk.UNKNOWN
                Filter.CRITICAL -> a.risk == Risk.CRITICAL
            }
            f && (q.isEmpty() || a.label.lowercase().contains(q) || a.pkg.lowercase().contains(q))
        }
    }

    fun canSelect(a: AppItem) = a.installed && (a.risk != Risk.CRITICAL || expert) &&
        a.pkg != getApplication<Application>().packageName && a.pkg != Shell.SHIZUKU_PKG

    fun toggle(a: AppItem) {
        if (!canSelect(a)) {
            toast = if (a.risk == Risk.CRITICAL) "Ứng dụng hệ thống quan trọng – bật Chế độ chuyên gia nếu thật sự cần."
            else "Không thể chọn ứng dụng này."
            return
        }
        if (a.pkg in selected) selected.remove(a.pkg) else selected.add(a.pkg)
    }

    fun selectAllVisible() {
        val list = visibleApps().filter { canSelect(it) }.map { it.pkg }
        if (list.isNotEmpty() && selected.containsAll(list)) selected.removeAll(list.toSet())
        else list.forEach { if (it !in selected) selected.add(it) }
    }

    fun clearSelection() = selected.clear()

    // ---------- Thực thi ----------
    fun run(action: Action, pkgs: List<String>) {
        if (pkgs.isEmpty() || progress != null) return
        refreshShizuku()
        if (shizuku != ShizukuState.READY) {
            toast = "Cần kết nối Shizuku trước."
            return
        }
        viewModelScope.launch {
            val ok = mutableListOf<String>()
            val failed = mutableListOf<Pair<String, String>>()
            pkgs.forEachIndexed { i, pkg ->
                val name = apps.find { it.pkg == pkg }?.label ?: pkg
                progress = Progress(action, pkgs.size, i, name)
                val r = withContext(Dispatchers.IO) {
                    when (action) {
                        Action.UNINSTALL -> Shell.uninstall(pkg)
                        Action.DISABLE -> Shell.disable(pkg)
                        Action.RESTORE -> Shell.restore(pkg)
                        Action.ENABLE -> Shell.enable(pkg)
                    }
                }
                if (r.ok) ok += pkg else failed += pkg to r.message.ifBlank { "Lỗi không rõ" }
            }
            progress = null
            selected.removeAll(ok.toSet())
            addLog("${action.done} ${ok.size} ứng dụng" + if (failed.isNotEmpty()) " (${failed.size} lỗi)" else "")
            result = BatchResult(action, ok, failed)
            reload()
        }
    }

    // ---------- Ngôn ngữ ----------
    fun applyLocale(tags: String) {
        refreshShizuku()
        if (shizuku != ShizukuState.READY) { toast = "Cần kết nối Shizuku trước."; return }
        viewModelScope.launch {
            val r = withContext(Dispatchers.IO) { Shell.setSystemLocales(tags) }
            toast = when {
                r.ok && r.out == "reboot" -> "Đã lưu. Khởi động lại máy để áp dụng ngôn ngữ."
                r.ok -> "Đã đổi ngôn ngữ hệ thống. Nếu vài app chưa đổi, hãy khởi động lại máy."
                else -> "Không đổi được: ${r.message}"
            }
            if (r.ok) addLog("Đổi ngôn ngữ hệ thống → $tags")
            localeTags = tags
        }
    }

    private fun currentLocales(): String =
        Resources.getSystem().configuration.locales.toLanguageTags()

    // ---------- Sao lưu / nhập ----------
    fun exportTo(uri: Uri) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                try {
                    val json = JSONObject()
                        .put("app", "COD Cleaner")
                        .put("time", stamp())
                        .put("removed", JSONArray(removedApps.map { it.pkg }))
                        .put("disabled", JSONArray(disabledApps.map { it.pkg }))
                    getApplication<Application>().contentResolver.openOutputStream(uri, "wt")?.use {
                        it.write(json.toString(2).toByteArray())
                    }
                    true
                } catch (_: Throwable) { false }
            }
            toast = if (ok) "Đã lưu danh sách (${removedApps.size} gỡ, ${disabledApps.size} tắt)." else "Lưu thất bại."
        }
    }

    /** Đọc file sao lưu, trả về các gói cần gỡ/tắt lại (đang còn cài). */
    fun importFrom(uri: Uri, onReady: (List<String>, List<String>) -> Unit) {
        viewModelScope.launch {
            val parsed = withContext(Dispatchers.IO) {
                try {
                    val text = getApplication<Application>().contentResolver.openInputStream(uri)
                        ?.bufferedReader()?.readText() ?: return@withContext null
                    val o = JSONObject(text)
                    fun arr(k: String) = o.optJSONArray(k)?.let { a -> List(a.length()) { a.getString(it) } } ?: emptyList()
                    arr("removed") to arr("disabled")
                } catch (_: Throwable) { null }
            }
            if (parsed == null) { toast = "File không hợp lệ."; return@launch }
            val installed = installedApps.filter { canSelect(it) || it.risk == Risk.CRITICAL && expert }.map { it.pkg }.toSet()
            val rm = parsed.first.filter { it in installed }
            val dis = parsed.second.filter { it in installed && it !in rm }
            if (rm.isEmpty() && dis.isEmpty()) toast = "Máy đã gọn như bản sao lưu, không cần làm gì thêm."
            else onReady(rm, dis)
        }
    }

    fun runImport(rm: List<String>, dis: List<String>) {
        if (rm.isNotEmpty()) run(Action.UNINSTALL, rm)
        // tắt các app còn lại sau khi gỡ xong
        if (dis.isNotEmpty()) viewModelScope.launch {
            while (progress != null) kotlinx.coroutines.delay(300)
            run(Action.DISABLE, dis)
        }
    }

    // ---------- Nhật ký ----------
    private fun stamp() = SimpleDateFormat("dd/MM HH:mm", Locale("vi")).format(Date())

    private fun addLog(text: String) {
        log.add(0, LogEntry(stamp(), text))
        while (log.size > 30) log.removeAt(log.lastIndex)
        val arr = JSONArray()
        log.forEach { arr.put(JSONObject().put("t", it.time).put("x", it.text)) }
        prefs.edit().putString("log", arr.toString()).apply()
    }

    private fun loadLog() {
        try {
            val arr = JSONArray(prefs.getString("log", "[]"))
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                log.add(LogEntry(o.getString("t"), o.getString("x")))
            }
        } catch (_: Throwable) {}
    }
}
