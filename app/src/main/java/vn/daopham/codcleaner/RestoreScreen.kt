package vn.daopham.codcleaner

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun RestoreScreen(
    vm: MainViewModel,
    contentPadding: PaddingValues,
    onAction: (Action, List<String>) -> Unit,
    onImport: (List<String>, List<String>) -> Unit
) {
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) vm.exportTo(uri)
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.importFrom(uri, onImport)
    }
    val removed = vm.removedApps
    val disabled = vm.disabledApps

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = contentPadding.calculateTopPadding() + 4.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            SectionCard {
                Column(Modifier.padding(16.dp)) {
                    Text("Sao lưu danh sách dọn", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Lưu danh sách app đã gỡ/tắt ra file. Sau khi reset máy, nhập lại file để dọn lại toàn bộ chỉ với 1 lần bấm.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilledTonalButton(
                            onClick = { exportLauncher.launch("COD-Cleaner-danh-sach.json") },
                            enabled = removed.isNotEmpty() || disabled.isNotEmpty(),
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.FileDownload, null, Modifier.size(18.dp)); Spacer(Modifier.size(6.dp)); Text("Lưu file")
                        }
                        FilledTonalButton(
                            onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.FileUpload, null, Modifier.size(18.dp)); Spacer(Modifier.size(6.dp)); Text("Nhập file")
                        }
                    }
                }
            }
        }

        item {
            SectionTitle("Đã gỡ · ${removed.size}") {
                if (removed.isNotEmpty()) TextButton(onClick = { onAction(Action.RESTORE, removed.map { it.pkg }) }) {
                    Text("Khôi phục tất cả")
                }
            }
        }
        if (removed.isEmpty()) item { EmptyHint("Chưa gỡ ứng dụng nào.") }
        items(removed, key = { "r_" + it.pkg }) { a ->
            RestoreRow(a, "Khôi phục") { onAction(Action.RESTORE, listOf(a.pkg)) }
        }

        item {
            SectionTitle("Đã vô hiệu hoá · ${disabled.size}") {
                if (disabled.isNotEmpty()) TextButton(onClick = { onAction(Action.ENABLE, disabled.map { it.pkg }) }) {
                    Text("Bật lại tất cả")
                }
            }
        }
        if (disabled.isEmpty()) item { EmptyHint("Không có ứng dụng nào bị tắt.") }
        items(disabled, key = { "d_" + it.pkg }) { a ->
            RestoreRow(a, "Bật lại") { onAction(Action.ENABLE, listOf(a.pkg)) }
        }
    }
}

@Composable
private fun RestoreRow(a: AppItem, label: String, onClick: () -> Unit) {
    SectionCard {
        Row(Modifier.padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            AppIcon(a.pkg, 40.dp)
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(a.label, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(a.pkg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            FilledTonalButton(onClick = onClick, shape = RoundedCornerShape(12.dp)) { Text(label) }
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    SectionCard {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Inventory2, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.size(12.dp))
            Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
