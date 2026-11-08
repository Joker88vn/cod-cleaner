package vn.daopham.codcleaner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class LangOpt(val tags: String, val flag: String, val title: String, val sub: String)

private val options = listOf(
    LangOpt("vi-VN,en-US", "VI", "Tiếng Việt  (khuyên dùng)", "Phần nào chưa có tiếng Việt sẽ hiện Tiếng Anh thay vì Tiếng Trung."),
    LangOpt("vi-VN,zh-CN", "VI", "Tiếng Việt  + Tiếng Trung", "Phần chưa dịch giữ Tiếng Trung như máy gốc."),
    LangOpt("en-US", "EN", "English", "Toàn bộ Tiếng Anh."),
    LangOpt("zh-CN", "中", "简体中文", "Trở về ngôn ngữ gốc của máy.")
)

/** Tab "Tiện ích": chặn quảng cáo + ngôn ngữ (ngôn ngữ chỉ cần cho máy nội địa). */
@Composable
fun ToolsScreen(vm: MainViewModel, contentPadding: PaddingValues, onAction: (Action, List<String>) -> Unit) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = contentPadding.calculateTopPadding() + 4.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        adBlockItems(vm, onAction)
        if (vm.rom.isChina) languageItems(vm)
    }
}

private fun LazyListScope.adBlockItems(vm: MainViewModel, onAction: (Action, List<String>) -> Unit) {
    item { SectionTitle("Chặn quảng cáo") }

    // 1. DNS chặn quảng cáo toàn máy
    item {
        val r = LocalRisk.current
        val ready = vm.shizuku == ShizukuState.READY
        val dns = vm.adDns
        val on = dns == Shell.ADGUARD_DNS
        SectionCard {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBubble(Icons.Rounded.Shield, if (on) r.safe else r.neutral, if (on) r.safeBg else r.neutralBg, 44.dp)
                    Spacer(Modifier.size(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Chặn quảng cáo toàn máy", style = MaterialTheme.typography.titleMedium)
                        Text(
                            when {
                                !ready -> "Cần kết nối Shizuku"
                                on -> "Đang bật · AdGuard DNS"
                                !dns.isNullOrBlank() -> "Đang dùng DNS khác: $dns"
                                else -> "Đang tắt"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (on) r.safe else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = on, enabled = ready, onCheckedChange = { vm.setAdBlock(it) })
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "Dùng DNS riêng tư của AdGuard để chặn quảng cáo và theo dõi trong hầu hết ứng dụng và trình duyệt, " +
                        "kể cả quảng cáo trong app hệ thống MIUI. Không tốn pin, không cần VPN.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Nếu một trang web/app nào đó không tải được, tắt công tắc này là trở lại bình thường.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // 2. Gỡ dịch vụ quảng cáo của MIUI
    item {
        val r = LocalRisk.current
        val left = vm.adServicesLeft
        SectionCard {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (left.isEmpty()) IconBubble(Icons.Rounded.CheckCircle, r.safe, r.safeBg, 44.dp)
                    else IconBubble(Icons.Rounded.Block, r.caution, r.cautionBg, 44.dp)
                    Spacer(Modifier.size(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Dịch vụ quảng cáo của Xiaomi", style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (left.isEmpty()) "Đã gỡ hết" else "Còn ${left.size} dịch vụ quảng cáo/theo dõi",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (left.isEmpty()) r.safe else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "Gỡ MSA (máy chủ quảng cáo), Analytics, Facebook/Amazon cài ngầm… Quảng cáo trong Quản lý tệp, Bảo mật, " +
                        "Giao diện, Nhạc sẽ giảm hẳn. Khôi phục lại được ở tab Khôi phục.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (left.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { onAction(Action.UNINSTALL, left) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Gỡ ${left.size} dịch vụ quảng cáo") }
                }
            }
        }
    }

    // 3. Mẹo tắt gợi ý trong app hệ thống
    item {
        SectionCard {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Lightbulb, null, tint = LocalRisk.current.caution, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Tắt thêm gợi ý (làm tay, 1 lần)", style = MaterialTheme.typography.titleSmall)
                }
                Spacer(Modifier.height(8.dp))
                BulletStep(1, "Cài đặt → Mật khẩu & bảo mật → Quyền riêng tư → Quảng cáo: tắt \"Đề xuất quảng cáo được cá nhân hóa\".")
                BulletStep(2, "Bảo mật, Quản lý tệp, Giao diện, Nhạc, Tải xuống: vào ⚙ Cài đặt của từng app, tắt \"Nhận đề xuất\".")
                BulletStep(3, "Khi cài app mới, ở màn hình quét an toàn bấm ⚙ và tắt \"Đề xuất\".")
                BulletStep(4, "Màn hình chính → giữ ngón trên vùng trống → Cài đặt → tắt \"Đề xuất ứng dụng\" trong ngăn kéo/thư mục.")
            }
        }
    }
}

private fun LazyListScope.languageItems(vm: MainViewModel) {
    item { SectionTitle("Ngôn ngữ hệ thống") }
    item {
        SectionCard {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconBubble(Icons.Rounded.Translate, MaterialTheme.colorScheme.onPrimaryContainer, MaterialTheme.colorScheme.primaryContainer, 46.dp)
                Spacer(Modifier.size(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Đang dùng", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(prettyLocale(vm.localeTags), style = MaterialTheme.typography.titleLarge)
                    Text(vm.localeTags, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
    item { LanguagePicker(vm) }
    item {
        SectionCard {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Lightbulb, null, tint = LocalRisk.current.caution, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Lưu ý", style = MaterialTheme.typography.titleSmall)
                }
                Spacer(Modifier.height(8.dp))
                BulletStep(1, "Bản ROM nội địa không có sẵn Tiếng Việt cho mọi chỗ: phần lõi Android và app có tiếng Việt (Google, Zalo, Chrome…) sẽ đổi, một số app riêng của Xiaomi vẫn tiếng Anh/Trung.")
                BulletStep(2, "Nếu vài chỗ chưa đổi, hãy khởi động lại máy.")
                BulletStep(3, "Nếu vào Cài đặt → Ngôn ngữ chọn lại ngôn ngữ khác, Tiếng Việt sẽ bị ghi đè – chỉ cần vào đây áp dụng lại.")
            }
        }
    }
}

@Composable
private fun LanguagePicker(vm: MainViewModel) {
    var pick by rememberSaveable {
        mutableStateOf(options.firstOrNull { it.tags == vm.localeTags }?.tags ?: options[0].tags)
    }
    val ready = vm.shizuku == ShizukuState.READY
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEach { o ->
            val sel = pick == o.tags
            val c = MaterialTheme.colorScheme
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                    .background(if (sel) c.primaryContainer else c.surfaceContainerLowest)
                    .border(1.5.dp, if (sel) c.primary else Color.Transparent, RoundedCornerShape(18.dp))
                    .clickable { pick = o.tags }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(if (sel) c.primary else c.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(o.flag, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (sel) c.onPrimary else c.onSurfaceVariant)
                }
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(o.title, style = MaterialTheme.typography.titleSmall)
                    Text(o.sub, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant)
                }
                RadioButton(selected = sel, onClick = { pick = o.tags })
            }
        }
        Spacer(Modifier.height(2.dp))
        FilledTonalButton(
            onClick = { vm.applyLocale(pick) },
            enabled = ready,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) { Text(if (ready) "Áp dụng ngôn ngữ" else "Cần kết nối Shizuku trước", fontSize = 16.sp) }
    }
}
