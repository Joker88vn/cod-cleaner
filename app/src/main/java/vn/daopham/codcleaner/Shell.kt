package vn.daopham.codcleaner

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.IBinder
import android.os.LocaleList
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.ShizukuRemoteProcess
import rikka.shizuku.SystemServiceHelper
import java.lang.reflect.InvocationTargetException

enum class ShizukuState { NOT_INSTALLED, NOT_RUNNING, NO_PERMISSION, READY }

data class CmdResult(val code: Int, val out: String, val err: String) {
    val ok get() = code == 0 && !out.contains("Failure", true) && !err.contains("Failure", true) &&
        !out.contains("Exception", true)
    val message get() = (err.ifBlank { out }).trim().lineSequence().firstOrNull().orEmpty()
}

object Shell {
    const val SHIZUKU_PKG = "moe.shizuku.privileged.api"
    const val REQ_CODE = 7001

    fun state(ctx: Context): ShizukuState {
        val running = try { Shizuku.pingBinder() } catch (_: Throwable) { false }
        if (!running) {
            val installed = try {
                ctx.packageManager.getPackageInfo(SHIZUKU_PKG, 0); true
            } catch (_: Throwable) { false }
            return if (installed) ShizukuState.NOT_RUNNING else ShizukuState.NOT_INSTALLED
        }
        return try {
            if (Shizuku.isPreV11()) ShizukuState.NOT_RUNNING
            else if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) ShizukuState.READY
            else ShizukuState.NO_PERMISSION
        } catch (_: Throwable) { ShizukuState.NOT_RUNNING }
    }

    fun requestPermission() {
        try { Shizuku.requestPermission(REQ_CODE) } catch (_: Throwable) {}
    }

    private val newProcess by lazy {
        Shizuku::class.java.getDeclaredMethod(
            "newProcess",
            Array<String>::class.java, Array<String>::class.java, String::class.java
        ).apply { isAccessible = true }
    }

    /** Chạy lệnh với quyền shell (qua Shizuku). Gọi từ luồng nền. */
    fun run(cmd: String): CmdResult = try {
        val p = newProcess.invoke(null, arrayOf("sh", "-c", cmd), null, null) as ShizukuRemoteProcess
        val out = p.inputStream.bufferedReader().readText()
        val err = p.errorStream.bufferedReader().readText()
        val code = p.waitFor()
        CmdResult(code, out, err)
    } catch (e: Throwable) {
        val cause = (e as? InvocationTargetException)?.targetException ?: e
        CmdResult(-1, "", cause.message ?: cause.javaClass.simpleName)
    }

    fun uninstall(pkg: String) = run("pm uninstall --user 0 $pkg")
    fun restore(pkg: String): CmdResult {
        val r = run("cmd package install-existing --user 0 $pkg")
        return if (r.ok) r else run("pm install-existing --user 0 $pkg")
    }
    fun disable(pkg: String) = run("pm disable-user --user 0 $pkg")

    // ---------- Chặn quảng cáo bằng DNS riêng tư ----------
    const val ADGUARD_DNS = "dns.adguard-dns.com"

    /** Trả về tên máy chủ DNS riêng tư đang dùng, hoặc rỗng nếu không bật. */
    fun privateDns(): String {
        val mode = run("settings get global private_dns_mode").out.trim()
        if (mode != "hostname") return ""
        return run("settings get global private_dns_specifier").out.trim().takeIf { it != "null" }.orEmpty()
    }

    fun setPrivateDns(host: String?): CmdResult =
        if (host.isNullOrBlank()) run("settings put global private_dns_mode opportunistic")
        else run("settings put global private_dns_specifier $host && settings put global private_dns_mode hostname")
    fun enable(pkg: String) = run("pm enable --user 0 $pkg")

    /**
     * Đổi ngôn ngữ hệ thống, ví dụ "vi-VN,en-US".
     * Dùng IActivityManager qua binder của Shizuku (quyền shell có CHANGE_CONFIGURATION).
     */
    fun setSystemLocales(tags: String): CmdResult {
        val errors = mutableListOf<String>()
        var applied = false
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) HiddenApiBypass.addHiddenApiExemptions("")
            val binder: IBinder = ShizukuBinderWrapper(SystemServiceHelper.getSystemService("activity"))
            val iface = Class.forName("android.app.IActivityManager")
            val stub = Class.forName("android.app.IActivityManager\$Stub")
            val am = stub.getMethod("asInterface", IBinder::class.java).invoke(null, binder)
            val config = Configuration(iface.getMethod("getConfiguration").invoke(am) as Configuration)
            config.setLocales(LocaleList.forLanguageTags(tags))
            try {
                Configuration::class.java.getField("userSetLocale").setBoolean(config, true)
            } catch (_: Throwable) {}
            applied = try {
                iface.getMethod(
                    "updatePersistentConfigurationWithAttribution",
                    Configuration::class.java, String::class.java, String::class.java
                ).invoke(am, config, "com.android.shell", null)
                true
            } catch (_: NoSuchMethodException) {
                iface.getMethod("updatePersistentConfiguration", Configuration::class.java).invoke(am, config)
                true
            }
        } catch (e: Throwable) {
            val cause = (e as? InvocationTargetException)?.targetException ?: e
            errors += (cause.message ?: cause.javaClass.simpleName)
        }
        // Lưu thêm vào Settings để giữ sau khi khởi động lại
        val s = run("settings put system system_locales $tags")
        if (!applied && !s.ok) errors += s.message
        return if (applied) CmdResult(0, "ok", "")
        else if (s.ok) CmdResult(0, "reboot", "")
        else CmdResult(1, "", errors.joinToString("; "))
    }
}
