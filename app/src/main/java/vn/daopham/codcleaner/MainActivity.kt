package vn.daopham.codcleaner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    private val binderReceived = Shizuku.OnBinderReceivedListener { runOnUiThread { vm.refreshShizuku() } }
    private val binderDead = Shizuku.OnBinderDeadListener { runOnUiThread { vm.refreshShizuku() } }
    private val permResult = Shizuku.OnRequestPermissionResultListener { _, _ -> runOnUiThread { vm.refreshShizuku() } }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceived)
            Shizuku.addBinderDeadListener(binderDead)
            Shizuku.addRequestPermissionResultListener(permResult)
        } catch (_: Throwable) {}
        setContent { CodTheme { App(vm) } }
    }

    override fun onDestroy() {
        try {
            Shizuku.removeBinderReceivedListener(binderReceived)
            Shizuku.removeBinderDeadListener(binderDead)
            Shizuku.removeRequestPermissionResultListener(permResult)
        } catch (_: Throwable) {}
        super.onDestroy()
    }
}

private data class Tab(val title: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("COD Cleaner", "Tổng quan", Icons.Rounded.Dashboard),
    Tab("Ứng dụng", "Ứng dụng", Icons.Rounded.Apps),
    Tab("Khôi phục", "Khôi phục", Icons.Rounded.Restore),
    Tab("Tiện ích", "Tiện ích", Icons.Rounded.Shield)
)

private data class Pending(val action: Action, val pkgs: List<String>, val thenDisable: List<String> = emptyList())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun App(vm: MainViewModel) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var pending by remember { mutableStateOf<Pending?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    val snack = remember { SnackbarHostState() }

    // Làm mới khi quay lại app (ví dụ vừa bật Shizuku)
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val obs = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_RESUME) { vm.refreshShizuku(); vm.reload() }
        }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }
    LaunchedEffect(vm.toast) {
        vm.toast?.let { snack.showSnackbar(it); vm.toast = null }
    }

    val ask: (Action, List<String>) -> Unit = { a, p -> if (p.isNotEmpty()) pending = Pending(a, p) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(tabs[tab].title, style = MaterialTheme.typography.headlineSmall)
                        if (tab == 0) Text(
                            "${vm.rom.model} · ${vm.rom.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { vm.refreshShizuku(); vm.reload() }) { Icon(Icons.Rounded.Refresh, "Làm mới") }
                    IconButton(onClick = { showSettings = true }) { Icon(Icons.Rounded.Settings, "Cài đặt") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            Column {
                AnimatedVisibility(
                    visible = tab == 1 && vm.selected.isNotEmpty(),
                    enter = slideInVertically { it }, exit = slideOutVertically { it }
                ) {
                    SelectionBar(
                        count = vm.selected.size,
                        onDisable = { ask(Action.DISABLE, vm.selected.toList()) },
                        onUninstall = { ask(Action.UNINSTALL, vm.selected.toList()) }
                    )
                }
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest, tonalElevation = 0.dp) {
                    tabs.forEachIndexed { i, t ->
                        NavigationBarItem(
                            selected = tab == i,
                            onClick = { tab = i },
                            icon = { Icon(t.icon, null) },
                            label = { Text(t.label) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { pad ->
        Crossfade(tab, label = "tab") { t ->
            when (t) {
                0 -> HomeScreen(vm, pad, onQuickClean = { ask(Action.UNINSTALL, vm.junkLeft.map { it.pkg }) }, goTo = { tab = it })
                1 -> AppsScreen(vm, pad, onAction = ask)
                2 -> RestoreScreen(vm, pad, onAction = ask, onImport = { rm, dis ->
                    pending = if (rm.isNotEmpty()) Pending(Action.UNINSTALL, rm, dis) else Pending(Action.DISABLE, dis)
                })
                else -> ToolsScreen(vm, pad, onAction = ask)
            }
        }
    }

    pending?.let { p ->
        ConfirmDialog(vm, p, onDismiss = { pending = null }) {
            pending = null
            if (p.thenDisable.isNotEmpty()) vm.runImport(p.pkgs, p.thenDisable) else vm.run(p.action, p.pkgs)
        }
    }
    vm.progress?.let { ProgressDialog(it) }
    vm.result?.let { r -> ResultDialog(vm, r) { vm.result = null } }
    if (showSettings) SettingsDialog(vm) { showSettings = false }
}

@Composable
private fun SelectionBar(count: Int, onDisable: () -> Unit, onUninstall: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLowest, shadowElevation = 8.dp) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Đã chọn $count", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            OutlinedButton(onClick = onDisable, shape = RoundedCornerShape(12.dp)) { Text("Tắt") }
            Button(onClick = onUninstall, shape = RoundedCornerShape(12.dp)) { Text("Gỡ bỏ") }
        }
    }
}

@Composable
private fun ConfirmDialog(vm: MainViewModel, p: Pending, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val items = p.pkgs.mapNotNull { pkg -> vm.apps.find { it.pkg == pkg } }
    val risky = items.filter { it.risk == Risk.CAUTION || it.risk == Risk.UNKNOWN || it.risk == Risk.CRITICAL }
    val userApps = items.filter { !it.isSystem }
    val destructive = p.action == Action.UNINSTALL || p.action == Action.DISABLE
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(if (risky.isNotEmpty() && destructive) Icons.Rounded.WarningAmber else Icons.Rounded.CheckCircle, null) },
        title = { Text("${p.action.verb} ${p.pkgs.size} ứng dụng?") },
        text = {
            Column(Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState())) {
                when (p.action) {
                    Action.UNINSTALL -> Text("Ứng dụng hệ thống sẽ được gỡ khỏi người dùng và có thể khôi phục lại bất cứ lúc nào ở tab Khôi phục.")
                    Action.DISABLE -> Text("Ứng dụng sẽ bị tắt (vẫn nằm trong máy). Bật lại được ở tab Khôi phục.")
                    Action.RESTORE -> Text("Cài lại các ứng dụng đã gỡ.")
                    Action.ENABLE -> Text("Bật lại các ứng dụng đã tắt.")
                }
                if (p.thenDisable.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text("Sau đó tắt thêm ${p.thenDisable.size} ứng dụng theo file sao lưu.")
                }
                if (destructive && risky.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Có ${risky.size} ứng dụng cần cẩn thận:", color = LocalRisk.current.caution,
                        fontWeight = FontWeight.SemiBold
                    )
                    risky.take(8).forEach { Text("• ${it.label}", style = MaterialTheme.typography.bodySmall) }
                    if (risky.size > 8) Text("• …và ${risky.size - 8} ứng dụng khác", style = MaterialTheme.typography.bodySmall)
                }
                if (p.action == Action.UNINSTALL && userApps.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "${userApps.size} ứng dụng không phải hệ thống sẽ bị xoá hẳn (muốn dùng lại phải tải lại).",
                        color = LocalRisk.current.critical, style = MaterialTheme.typography.bodySmall
                    )
                }
                if (items.size <= 12) {
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(6.dp))
                    items.forEach { Text("• ${it.label}", style = MaterialTheme.typography.bodySmall) }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = if (destructive && risky.any { it.risk == Risk.CRITICAL })
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                else ButtonDefaults.buttonColors()
            ) { Text(p.action.verb) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Huỷ") } }
    )
}

@Composable
private fun ProgressDialog(p: Progress) {
    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        title = { Text("Đang ${p.action.verb.lowercase()}…") },
        text = {
            Column {
                Text("${p.done + 1}/${p.total} · ${p.current}", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(14.dp))
                LinearProgressIndicator(
                    progress = { (p.done + 1f) / p.total },
                    modifier = Modifier.fillMaxWidth().height(8.dp)
                )
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun ResultDialog(vm: MainViewModel, r: BatchResult, onDismiss: () -> Unit) {
    val name: (String) -> String = { pkg -> vm.apps.find { it.pkg == pkg }?.label ?: pkg }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                if (r.failed.isEmpty()) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline, null,
                tint = if (r.failed.isEmpty()) LocalRisk.current.safe else LocalRisk.current.caution,
                modifier = Modifier.size(36.dp)
            )
        },
        title = { Text(if (r.failed.isEmpty()) "Hoàn tất!" else "Xong, có ${r.failed.size} lỗi") },
        text = {
            Column(Modifier.heightIn(max = 340.dp).verticalScroll(rememberScrollState())) {
                Text("${r.action.done} ${r.ok.size} ứng dụng.")
                if (r.failed.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    r.failed.forEach { (pkg, msg) ->
                        Text("• ${name(pkg)}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                        Text("  $msg", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Đóng") } }
    )
}

@Composable
private fun SettingsDialog(vm: MainViewModel, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cài đặt") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Chế độ chuyên gia", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Cho phép chọn cả ứng dụng hệ thống quan trọng. Dễ gây lỗi máy – chỉ bật khi biết rõ.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.size(10.dp))
                    Switch(checked = vm.expert, onCheckedChange = { vm.setExpertMode(it) })
                }
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                Text("COD Cleaner 1.2", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Gỡ bằng lệnh \"pm uninstall --user 0\" qua Shizuku: không cần root, không xoá khỏi phân vùng hệ thống. " +
                        "Nếu reset máy, ứng dụng sẽ quay lại – dùng file sao lưu để dọn lại.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Xong") } }
    )
}

