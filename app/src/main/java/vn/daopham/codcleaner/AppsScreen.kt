package vn.daopham.codcleaner

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(vm: MainViewModel, contentPadding: PaddingValues, onAction: (Action, List<String>) -> Unit) {
    var detail by remember { mutableStateOf<AppItem?>(null) }
    val list = vm.visibleApps()

    Column(Modifier.fillMaxSize().padding(top = contentPadding.calculateTopPadding())) {
        // Thanh tìm kiếm
        TextField(
            value = vm.query,
            onValueChange = { vm.query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(54.dp),
            placeholder = { Text("Tìm ứng dụng hoặc tên gói…") },
            leadingIcon = { Icon(Icons.Rounded.Search, null) },
            trailingIcon = {
                if (vm.query.isNotEmpty()) IconButton(onClick = { vm.query = "" }) { Icon(Icons.Rounded.Close, null) }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            )
        )
        // Bộ lọc
        val counts = remember(vm.apps) {
            val inst = vm.installedApps
            mapOf(
                Filter.JUNK to inst.count { it.risk == Risk.SAFE },
                Filter.CAUTION to inst.count { it.risk == Risk.CAUTION },
                Filter.ALL to inst.size,
                Filter.USER to inst.count { it.risk == Risk.USER },
                Filter.UNKNOWN to inst.count { it.risk == Risk.UNKNOWN },
                Filter.CRITICAL to inst.count { it.risk == Risk.CRITICAL }
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(Filter.entries.toList()) { f ->
                FilterChip(
                    selected = vm.filter == f,
                    onClick = { vm.filter = f },
                    label = { Text("${f.label} · ${counts[f] ?: 0}") },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    border = null
                )
            }
        }
        // Thanh chọn tất cả
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${list.size} ứng dụng" + if (vm.selected.isNotEmpty()) " · đã chọn ${vm.selected.size}" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            if (vm.selected.isNotEmpty()) TextButton(onClick = { vm.clearSelection() }) { Text("Bỏ chọn") }
            TextButton(onClick = { vm.selectAllVisible() }, enabled = list.any { vm.canSelect(it) }) { Text("Chọn tất cả") }
        }

        if (list.isEmpty() && !vm.loading) {
            Column(
                Modifier.fillMaxWidth().padding(top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Rounded.SearchOff, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                Text(
                    if (vm.filter == Filter.JUNK && vm.query.isBlank()) "Không còn ứng dụng rác. Máy đã sạch!" else "Không có ứng dụng nào",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp, top = 4.dp,
                bottom = contentPadding.calculateBottomPadding() + if (vm.selected.isNotEmpty()) 96.dp else 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(list, key = { it.pkg }) { a ->
                AppRow(
                    a, selected = a.pkg in vm.selected, selectable = vm.canSelect(a),
                    onClick = { vm.toggle(a) }, onInfo = { detail = a }
                )
            }
        }
    }

    detail?.let { a ->
        ModalBottomSheet(onDismissRequest = { detail = null }, containerColor = MaterialTheme.colorScheme.surfaceContainerLowest) {
            AppDetail(a, vm.canSelect(a)) { act ->
                detail = null
                if (act != null) onAction(act, listOf(a.pkg))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppRow(a: AppItem, selected: Boolean, selectable: Boolean, onClick: () -> Unit, onInfo: () -> Unit) {
    val bg by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
        label = "rowbg"
    )
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(bg)
            .combinedClickable(onClick = onClick, onLongClick = onInfo)
            .padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CheckDot(selected, selectable)
        Spacer(Modifier.size(12.dp))
        AppIcon(a.pkg)
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    a.label, style = MaterialTheme.typography.titleSmall, maxLines = 1,
                    overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(Modifier.size(6.dp))
                RiskBadge(a.risk)
            }
            Text(
                a.note, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onInfo) {
            Icon(Icons.Rounded.Info, "Chi tiết", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CheckDot(checked: Boolean, enabled: Boolean) {
    val c = MaterialTheme.colorScheme
    Box(
        Modifier.size(22.dp).clip(CircleShape)
            .background(if (checked) c.primary else Color.Transparent)
            .border(2.dp, if (checked) c.primary else if (enabled) c.outline else c.outlineVariant, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (checked) Icon(Icons.Rounded.Check, null, tint = c.onPrimary, modifier = Modifier.size(15.dp))
    }
}

@Composable
fun AppDetail(a: AppItem, canAct: Boolean, onDone: (Action?) -> Unit) {
    val ctx = LocalContext.current
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(a.pkg, 56.dp)
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(a.label, style = MaterialTheme.typography.titleLarge)
                Text(a.pkg, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RiskBadge(a.risk)
            Box(
                Modifier.clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    if (a.isSystem) "Hệ thống" else "Người dùng", fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(a.note, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(6.dp))
        Text(
            if (a.isSystem) "Ứng dụng hệ thống · gỡ xong vẫn khôi phục lại được trong tab Khôi phục."
            else "Ứng dụng thường · gỡ là xoá hẳn khỏi máy.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(18.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(18.dp))
        if (a.installed) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { onDone(Action.UNINSTALL) }, enabled = canAct,
                    modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(14.dp)
                ) { Text("Gỡ bỏ") }
                if (a.enabled) OutlinedButton(
                    onClick = { onDone(Action.DISABLE) }, enabled = canAct,
                    modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(14.dp)
                ) { Text("Vô hiệu hoá") }
                else OutlinedButton(
                    onClick = { onDone(Action.ENABLE) },
                    modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(14.dp)
                ) { Text("Bật lại") }
            }
            if (!canAct) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Đây là thành phần quan trọng. Muốn thao tác, bật Chế độ chuyên gia ở biểu tượng cài đặt.",
                    style = MaterialTheme.typography.bodySmall, color = LocalRisk.current.critical
                )
            }
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = {
                ctx.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${a.pkg}")))
                onDone(null)
            }) { Text("Mở trang thông tin ứng dụng") }
        } else {
            Button(
                onClick = { onDone(Action.RESTORE) },
                modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(14.dp)
            ) { Text("Khôi phục") }
        }
    }
}
