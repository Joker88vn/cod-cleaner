package vn.daopham.codcleaner

import android.os.Build

/** Thông tin máy & loại ROM Xiaomi (nội địa Trung Quốc hay quốc tế). */
data class RomInfo(
    val model: String,
    val osName: String,
    val region: String,
    val isGlobal: Boolean
) {
    val isChina get() = !isGlobal
    val label get() = (if (isGlobal) "Bản quốc tế" else "Bản nội địa") + " · " + osName
}

object Device {

    private fun prop(key: String): String = try {
        val c = Class.forName("android.os.SystemProperties")
        (c.getMethod("get", String::class.java).invoke(null, key) as? String).orEmpty().trim()
    } catch (_: Throwable) {
        try {
            Runtime.getRuntime().exec(arrayOf("getprop", key)).inputStream.bufferedReader().readText().trim()
        } catch (_: Throwable) { "" }
    }

    val info: RomInfo by lazy {
        val market = prop("ro.product.marketname").ifBlank { prop("ro.product.vendor.marketname") }
        val model = market.ifBlank { "${Build.MANUFACTURER} ${Build.MODEL}".trim() }
        val modDevice = prop("ro.product.mod_device").lowercase()
        val region = prop("ro.miui.region").uppercase()
        val hyper = prop("ro.mi.os.version.name")
        val miui = prop("ro.miui.ui.version.name")
        val osName = when {
            hyper.isNotBlank() -> "HyperOS ${hyper.removePrefix("OS")}".trim()
            miui.isNotBlank() -> "MIUI " + miui.removePrefix("V").let { v ->
                if (v.length == 3 && v.all { it.isDigit() }) v.substring(0, 2) + "." + v.substring(2) else v
            }
            else -> "Android ${Build.VERSION.RELEASE}"
        }
        val isGlobal = when {
            modDevice.contains("_global") -> true
            region.isNotBlank() -> region != "CN"
            modDevice.isNotBlank() -> false
            else -> true
        }
        RomInfo(model, osName, region, isGlobal)
    }
}
