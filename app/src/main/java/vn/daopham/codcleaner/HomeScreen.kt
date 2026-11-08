package vn.daopham.codcleaner

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(vm: MainViewModel, contentPadding: PaddingValues, onQuickClean: () -> Unit, goTo: (Int) -> Unit) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = contentPadding.calculateTopPadding() + 4.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { HeroCard(vm, onQuickClean) }
        item { ShizukuCard(vm) }
        item {
            val r = LocalRisk.current
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("${vm.installedApps.size}", "Đang cài", Icons.Rounded.Apps, r.neutral, r.neutralBg, Modifier.weight(1f))
                StatTile("${vm.removedApps.size}", "Đã gỡ", Icons.Rounded.CleaningServices, r.safe, r.safeBg, Modifier.weight(1f))
                StatTile("${vm.disabledApps.size}", "Đã tắt", Icons.Rounded.Block, r.caution, r.cautionBg, Modifier.weight(1f))
            }
        }
        item {
            val adLeft = vm.adServicesLeft.size
            ShortcutRow(
                Icons.Rounded.Shield, "Chặn quảng cáo",
                (if (vm.adDns == Shell.ADGUARD_DNS) "Đang chặn toàn máy" else "Chưa bật chặn toàn máy") +
                    if (adLeft > 0) " · còn $adLeft dịch vụ quảng cáo" else ""
            ) { goTo(3) }
        }
        if (vm.rom.isChina) item {
            ShortcutRow(
                Icons.Rounded.Translate, "Chuyển sang Tiếng Việt",
                "Hiện tại: ${prettyLocale(vm.localeTags)}"
            ) { goTo(3) }
        }
        item {
            ShortcutRow(
                Icons.Rounded.Inventory2, "Khôi phục & sao lưu",
                "Cài lại app đã gỡ, lưu danh sách để dọn lại sau khi reset"
            ) { goTo(2) }
        }
        if (vm.log.isNotEmpty()) {
            item { SectionTitle("Hoạt động gần đây") }
            item {
                SectionCard {
                    Column(Modifier.padding(vertical = 6.dp)) {
                        vm.log.take(6).forEach { e ->
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.History, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.size(12.dp))
                                Text(e.text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                Text(e.time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroCard(vm: MainViewModel, onQuickClean: () -> Unit) {
    val r = LocalRisk.current
    val score by animateFloatAsState(vm.cleanScore, tween(900), label = "score")
    val left = vm.junkLeft.size
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(r.heroStart, r.heroEnd)))
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(104.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.size(104.dp)) {
                        val sw = 10.dp.toPx()
                        val inset = sw / 2
                        val arcSize = Size(size.width - sw, size.height - sw)
                        drawArc(Color.White.copy(alpha = 0.18f), 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(sw))
                        drawArc(Color.White, -90f, 360f * score, false, Offset(inset, inset), arcSize, style = Stroke(sw, cap = StrokeCap.Round))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${(score * 100).toInt()}%", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        Text("gọn gàng", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                    }
                }
                Spacer(Modifier.size(18.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (vm.loading) "Đang quét…" else if (left == 0) "Máy đã sạch!" else "$left ứng dụng rác",
                        color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (left == 0) "Không còn ứng dụng rác nào trong danh sách an toàn."
                        else (if (vm.rom.isChina) "Quảng cáo, theo dõi và app Trung Quốc cài sẵn" else "Quảng cáo, theo dõi và app cài sẵn") + " — gỡ an toàn, khôi phục lại được.",
                        color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onQuickClean,
                enabled = left > 0 && !vm.loading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White, contentColor = r.heroStart,
                    disabledContainerColor = Color.White.copy(alpha = 0.25f), disabledContentColor = Color.White
                )
            ) {
                Icon(Icons.Rounded.AutoAwesome, null)
                Spacer(Modifier.size(8.dp))
                Text(if (left > 0) "Dọn nhanh $left ứng dụng" else "Đã dọn xong", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ShizukuCard(vm: MainViewModel) {
    val ctx = LocalContext.current
    val r = LocalRisk.current
    val st = vm.shizuku
    var open by rememberSaveable { mutableStateOf(false) }
    val ready = st == ShizukuState.READY
    SectionCard {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(enabled = !ready) { open = !open },
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (ready) IconBubble(Icons.Rounded.CheckCircle, r.safe, r.safeBg)
                else IconBubble(Icons.Rounded.LinkOff, r.caution, r.cautionBg)
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        when (st) {
                            ShizukuState.READY -> "Đã kết nối Shizuku"
                            ShizukuState.NO_PERMISSION -> "Shizuku chưa cấp quyền"
                            ShizukuState.NOT_RUNNING -> "Shizuku chưa chạy"
                            ShizukuState.NOT_INSTALLED -> "Chưa cài Shizuku"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        when (st) {
                            ShizukuState.READY -> "Sẵn sàng dọn dẹp, không cần root."
                            ShizukuState.NO_PERMISSION -> "Bấm \"Cấp quyền\" và chọn Cho phép."
                            ShizukuState.NOT_RUNNING -> "Mở Shizuku và bấm Start (xem hướng dẫn)."
                            ShizukuState.NOT_INSTALLED -> "Cần cài Shizuku để app có quyền gỡ ứng dụng."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!ready) Icon(if (open) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, null)
            }
            if (!ready) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (st) {
                        ShizukuState.NO_PERMISSION -> Button(onClick = { Shell.requestPermission() }, shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Rounded.Link, null, Modifier.size(18.dp)); Spacer(Modifier.size(6.dp)); Text("Cấp quyền")
                        }
                        ShizukuState.NOT_RUNNING -> Button(onClick = {
                            ctx.packageManager.getLaunchIntentForPackage(Shell.SHIZUKU_PKG)?.let { ctx.startActivity(it) }
                        }, shape = RoundedCornerShape(12.dp)) { Text("Mở Shizuku") }
                        ShizukuState.NOT_INSTALLED -> Button(onClick = {
                            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/RikkaApps/Shizuku/releases/latest")))
                        }, shape = RoundedCornerShape(12.dp)) { Text("Tải Shizuku") }
                        else -> {}
                    }
                    OutlinedButton(onClick = { vm.refreshShizuku() }, shape = RoundedCornerShape(12.dp)) { Text("Kiểm tra lại") }
                }
                AnimatedVisibility(open || st == ShizukuState.NOT_INSTALLED) {
                    Column(Modifier.padding(top = 12.dp)) {
                        Text("Cách bật (làm 1 lần):", style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(6.dp))
                        BulletStep(1, "Cài Shizuku (bấm \"Tải Shizuku\", chọn file .apk).")
                        BulletStep(2, "Cài đặt → Giới thiệu điện thoại → bấm 7 lần vào \"Phiên bản MIUI\" (hoặc \"Phiên bản OS\") để mở Tùy chọn nhà phát triển.")
                        BulletStep(3, "Cài đặt → Cài đặt bổ sung → Tùy chọn nhà phát triển: bật \"Gỡ lỗi USB\", \"Gỡ lỗi USB (Cài đặt bảo mật)\" và \"Gỡ lỗi không dây\".")
                        BulletStep(4, "Mở Shizuku → \"Ghép nối\" → vào Gỡ lỗi không dây → \"Ghép nối thiết bị bằng mã\". Kéo thanh thông báo, vuốt xuống trên thông báo Shizuku để hiện ô nhập mã 6 số.")
                        BulletStep(5, "Quay lại Shizuku bấm \"Bắt đầu\", rồi trở về đây bấm \"Cấp quyền\".")
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Mỗi lần khởi động lại máy cần mở Shizuku bấm \"Bắt đầu\" lại (cần bật Wi-Fi).",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShortcutRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, sub: String, onClick: () -> Unit) {
    SectionCard {
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBubble(icon, MaterialTheme.colorScheme.onPrimaryContainer, MaterialTheme.colorScheme.primaryContainer)
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

fun prettyLocale(tags: String): String {
    val first = tags.split(',').firstOrNull()?.trim().orEmpty()
    return when {
        first.startsWith("vi") -> "Tiếng Việt"
        first.startsWith("zh") -> "Tiếng Trung"
        first.startsWith("en") -> "Tiếng Anh"
        first.isBlank() -> "Không rõ"
        else -> first
    }
}

