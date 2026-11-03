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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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

@Composable
fun LanguageScreen(vm: MainViewModel, contentPadding: PaddingValues) {
    var pick by rememberSaveable {
        mutableStateOf(options.firstOrNull { it.tags == vm.localeTags }?.tags ?: options[0].tags)
    }
    val ready = vm.shizuku == ShizukuState.READY

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = contentPadding.calculateTopPadding() + 4.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SectionCard {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconBubble(Icons.Rounded.Translate, MaterialTheme.colorScheme.onPrimaryContainer, MaterialTheme.colorScheme.primaryContainer, 46.dp)
                    Spacer(Modifier.size(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Ngôn ngữ hệ thống", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(prettyLocale(vm.localeTags), style = MaterialTheme.typography.titleLarge)
                        Text(vm.localeTags, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item { SectionTitle("Chọn ngôn ngữ") }
        options.forEach { o ->
            item(key = o.tags) {
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
        }
        item {
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = { vm.applyLocale(pick) },
                enabled = ready,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) { Text(if (ready) "Áp dụng ngôn ngữ" else "Cần kết nối Shizuku trước", fontSize = 16.sp) }
        }
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
}
