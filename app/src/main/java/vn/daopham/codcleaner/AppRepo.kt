package vn.daopham.codcleaner

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

data class AppItem(
    val pkg: String,
    val label: String,
    val isSystem: Boolean,
    val installed: Boolean,
    val enabled: Boolean,
    val risk: Risk,
    val note: String
) {
    /** Đã gỡ khỏi người dùng nhưng còn trong hệ thống → khôi phục được. */
    val removed get() = !installed
    val disabled get() = installed && !enabled
}

object AppRepo {

    fun load(ctx: Context): List<AppItem> {
        val pm = ctx.packageManager
        val flags = PackageManager.MATCH_UNINSTALLED_PACKAGES or PackageManager.MATCH_DISABLED_COMPONENTS
        @Suppress("DEPRECATION")
        val apps: List<ApplicationInfo> = if (android.os.Build.VERSION.SDK_INT >= 33)
            pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(flags.toLong()))
        else pm.getInstalledApplications(flags)

        val guarded = protectedPackages(ctx)

        return apps.mapNotNull { ai ->
            val installed = (ai.flags and ApplicationInfo.FLAG_INSTALLED) != 0
            val isSystem = (ai.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0
            val rule = Catalog.rule(ai.packageName, isSystem)
            val label = try { ai.loadLabel(pm).toString() } catch (_: Throwable) { ai.packageName }
            val niceLabel = when {
                rule?.name.isNullOrBlank() -> label
                label == ai.packageName || label.isBlank() -> rule!!.name
                else -> label
            }
            var risk = rule?.risk ?: if (isSystem) Risk.UNKNOWN else Risk.USER
            var note = rule?.note ?: if (isSystem) "Ứng dụng hệ thống chưa có trong danh sách. Chỉ gỡ khi anh biết rõ."
            else "Ứng dụng anh tự cài hoặc cài sẵn. Gỡ là xoá hẳn, muốn dùng lại phải tải lại."
            if (ai.packageName in guarded) {
                risk = Risk.CRITICAL
                note = "Đang được dùng làm mặc định (bàn phím/màn hình chính). " + (rule?.note ?: "")
            }
            AppItem(ai.packageName, niceLabel, isSystem, installed, ai.enabled, risk, note.trim())
        }.sortedWith(compareBy<AppItem> { it.risk.ordinal }.thenBy { it.label.lowercase() })
    }

    /** Bàn phím và launcher đang dùng: tuyệt đối không cho gỡ. */
    private fun protectedPackages(ctx: Context): Set<String> {
        val set = mutableSetOf(ctx.packageName, Shell.SHIZUKU_PKG)
        try {
            Settings.Secure.getString(ctx.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
                ?.substringBefore('/')?.let { set += it }
        } catch (_: Throwable) {}
        try {
            val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            ctx.packageManager.resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName?.let { if (it != "android") set += it }
        } catch (_: Throwable) {}
        return set
    }

    private val iconCache = LruCache<String, ImageBitmap>(300)

    fun icon(ctx: Context, pkg: String): ImageBitmap? {
        iconCache.get(pkg)?.let { return it }
        return try {
            val pm = ctx.packageManager
            @Suppress("DEPRECATION")
            val ai = pm.getApplicationInfo(pkg, PackageManager.MATCH_UNINSTALLED_PACKAGES)
            val bmp = pm.getApplicationIcon(ai).toBitmap(112, 112).asImageBitmap()
            iconCache.put(pkg, bmp)
            bmp
        } catch (_: Throwable) { null }
    }
}
