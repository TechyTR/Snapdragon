package com.nevruz.galaxysnapdragon

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Night = Color(0xFF070A12)
private val Panel = Color(0xFF111827)
private val PanelBright = Color(0xFF172238)
private val Cyan = Color(0xFF42E8FF)
private val Blue = Color(0xFF5277FF)
private val Purple = Color(0xFFB16CFF)
private val Muted = Color(0xFF93A4BE)
private val White = Color(0xFFF3F7FF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 741)
        }
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Cyan, onPrimary = Night, secondary = Purple,
                background = Night, surface = Panel, onSurface = White,
                surfaceVariant = PanelBright, onSurfaceVariant = Muted
            )) { SnapdragonApp(this) }
        }
    }
}

@Composable
private fun SnapdragonApp(context: Context) {
    var page by remember { mutableStateOf("home") }
    var gameMode by remember { mutableStateOf(false) }
    var focusMode by remember { mutableStateOf(false) }
    var touchBoost by remember { mutableStateOf(true) }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0B1120), Night, Color(0xFF05070D))))) {
        Box(Modifier.size(280.dp).align(Alignment.TopEnd).drawBehind {
            drawCircle(Brush.radialGradient(listOf(Color(0x263E74FF), Color.Transparent)), radius = size.minDimension * 0.8f)
        })
        when (page) {
            "panel" -> PanelPage({ page = "home" }, gameMode, { gameMode = it }, focusMode, { focusMode = it }, touchBoost, { touchBoost = it })
            "device" -> DevicePage(context) { page = "home" }
            "apps" -> AppSelectionPage(context) { page = "home" }
            else -> HomePage(context, gameMode, { gameMode = it }, focusMode, { focusMode = it }, touchBoost, { touchBoost = it }, { page = "panel" }, { page = "device" }, { page = "apps" })
        }
    }
}

@Composable
private fun HomePage(
    context: Context, gameMode: Boolean, onGameMode: (Boolean) -> Unit,
    focusMode: Boolean, onFocusMode: (Boolean) -> Unit,
    touchBoost: Boolean, onTouchBoost: (Boolean) -> Unit,
    onOpenPanel: () -> Unit, onOpenDevice: () -> Unit, onOpenApps: () -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            GlowIcon(Icons.Rounded.Bolt, Modifier.size(52.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("SNAPDRAGON", fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = White)
                Text("GAMING CONTROL CENTER", fontSize = 9.sp, letterSpacing = 1.6.sp, color = Muted)
            }
            Box(Modifier.clip(CircleShape).background(Color(0xFF15333A)).border(1.dp, Color(0x5542E8FF), CircleShape).padding(horizontal = 12.dp, vertical = 7.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(6.dp).background(Cyan, CircleShape))
                    Text("BETA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Cyan)
                }
            }
        }
        HeroCard(onOpenPanel)
        SectionLabel("HIZLI KONTROLLER")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            QuickToggle("Oyun modu", Icons.Rounded.Gamepad, gameMode, Modifier.weight(1f), onGameMode)
            QuickToggle("Odak modu", Icons.Rounded.Vibration, focusMode, Modifier.weight(1f), onFocusMode)
            QuickToggle("Dokunma+", Icons.Rounded.Speed, touchBoost, Modifier.weight(1f), onTouchBoost)
        }
        SectionLabel("SİSTEM ÖZETİ")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("ANDROID", Build.VERSION.RELEASE ?: "?", "Sürüm", Icons.Rounded.Memory, Modifier.weight(1f))
            MetricCard("API ${Build.VERSION.SDK_INT}", Build.MANUFACTURER.orEmpty().uppercase().ifBlank { "CİHAZ" }, "Platform", Icons.Rounded.MonitorHeart, Modifier.weight(1f))
        }
        SectionLabel("OYUN MERKEZİ")
        ActionCard("Yüzen oyun paneli", "Hızlı araçlar, oyun kısayolları ve kontrol merkezi", Icons.Rounded.Gamepad, Cyan, onOpenPanel)
        ActionCard("Cihaz laboratuvarı", "Bellek, işlemci ve Android bilgilerini incele", Icons.Rounded.Memory, Purple, onOpenDevice)
        ActionCard("Uygulama tetikleyicileri", "Panelin çalışacağı oyunları ve uygulamaları seç", Icons.Rounded.Apps, Blue, onOpenApps)
        Text("Arayüz önizlemesi • Performans ölçümleri ve sistem araçları geliştirme aşamasında", Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 12.dp), textAlign = TextAlign.Center, fontSize = 10.sp, lineHeight = 15.sp, color = Color(0xFF64748B))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp, color = Muted)
}

@Composable
private fun HeroCard(onOpenPanel: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "heroGlow")
    val glow by transition.animateFloat(0.28f, 0.72f, infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "glow")
    Card(Modifier.fillMaxWidth().shadow(22.dp, RoundedCornerShape(30.dp), ambientColor = Blue.copy(alpha = glow * 0.32f), spotColor = Cyan.copy(alpha = glow * 0.22f)), shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Box(Modifier.background(Brush.linearGradient(listOf(Color(0xFF19345A), Color(0xFF172342), Color(0xFF25163F)))).padding(22.dp)) {
            Box(Modifier.size(150.dp).align(Alignment.TopEnd).drawBehind {
                drawCircle(Brush.radialGradient(listOf(Cyan.copy(alpha = glow * 0.2f), Color.Transparent)), radius = size.minDimension / 1.5f)
            })
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.clip(CircleShape).background(Color(0x3327E8FF)).border(1.dp, Color(0x6642E8FF), CircleShape).padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text("GAMING SUITE", color = Cyan, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Rounded.Bolt, null, tint = Cyan, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.height(20.dp))
                Text("Oyuna odaklan.", fontSize = 30.sp, lineHeight = 34.sp, fontWeight = FontWeight.Black, color = White)
                Text("Kontrol sende kalsın.", fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8DBDFF))
                Spacer(Modifier.height(10.dp))
                Text("Kısayollarını tek dokunuşla aç, oyun alanını kendi tarzına göre düzenle.", fontSize = 12.sp, lineHeight = 18.sp, color = Color(0xFFD0DCF2))
                Spacer(Modifier.height(22.dp))
                Button(onClick = onOpenPanel, shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Night), modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Icon(Icons.Rounded.PlayArrow, null, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("PANELİ KEŞFET", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Rounded.ChevronRight, null)
                }
            }
        }
    }
}

@Composable
private fun QuickToggle(title: String, icon: ImageVector, checked: Boolean, modifier: Modifier, onToggle: (Boolean) -> Unit) {
    val accent = if (checked) Cyan else Color(0xFF718096)
    val container by animateColorAsState(if (checked) Color(0xFF15313D) else Color(0xFF111827), label = "toggleColor")
    Column(modifier.clip(RoundedCornerShape(22.dp)).background(container).border(1.dp, accent.copy(alpha = if (checked) 0.5f else 0.15f), RoundedCornerShape(22.dp)).clickable { onToggle(!checked) }.padding(vertical = 15.dp, horizontal = 5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(42.dp).shadow(if (checked) 12.dp else 0.dp, CircleShape, ambientColor = accent.copy(alpha = 0.3f)).background(accent.copy(alpha = 0.13f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(9.dp))
        Text(title, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = White, textAlign = TextAlign.Center)
        Spacer(Modifier.height(5.dp))
        Text(if (checked) "AÇIK" else "KAPALI", fontSize = 8.sp, letterSpacing = 1.sp, color = accent, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MetricCard(title: String, value: String, caption: String, icon: ImageVector, modifier: Modifier) {
    Row(modifier.clip(RoundedCornerShape(22.dp)).background(Brush.linearGradient(listOf(PanelBright, Panel))).border(1.dp, Color(0xFF26364D), RoundedCornerShape(22.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(38.dp).background(Color(0x2242E8FF), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Cyan, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, fontSize = 9.sp, color = Muted, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text(value, fontSize = 16.sp, color = White, fontWeight = FontWeight.ExtraBold, maxLines = 1)
            Text(caption, fontSize = 9.sp, color = Muted)
        }
    }
}

@Composable
private fun ActionCard(title: String, description: String, icon: ImageVector, accent: Color, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Brush.linearGradient(listOf(Color(0xFF182338), Color(0xFF101724)))).border(1.dp, Color(0xFF26364D), RoundedCornerShape(24.dp)).clickable(onClick = onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(52.dp).shadow(12.dp, RoundedCornerShape(18.dp), ambientColor = accent.copy(alpha = 0.22f)).background(accent.copy(alpha = 0.13f), RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(27.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, color = White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(description, fontSize = 11.sp, lineHeight = 16.sp, color = Muted)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = accent)
    }
}

@Composable
private fun PanelPage(onBack: () -> Unit, gameMode: Boolean, onGameMode: (Boolean) -> Unit, focusMode: Boolean, onFocusMode: (Boolean) -> Unit, touchBoost: Boolean, onTouchBoost: (Boolean) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        PageHeader("Yüzen panel", "OYUN KONTROLÜ", onBack)
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
            Column(Modifier.background(Brush.linearGradient(listOf(Color(0xFF193A55), Color(0xFF241A42)))).padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GlowIcon(Icons.Rounded.Gamepad, Modifier.size(58.dp))
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("SNAP PANEL", fontSize = 21.sp, color = White, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Text("Kompakt oyun araçları", fontSize = 12.sp, color = Color(0xFFB9C9E2))
                    }
                }
                Spacer(Modifier.height(22.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PanelButton("Oyun", Icons.Rounded.Gamepad, gameMode, Modifier.weight(1f)) { onGameMode(!gameMode) }
                    PanelButton("Odak", Icons.Rounded.Vibration, focusMode, Modifier.weight(1f)) { onFocusMode(!focusMode) }
                    PanelButton("Dokunma+", Icons.Rounded.Speed, touchBoost, Modifier.weight(1f)) { onTouchBoost(!touchBoost) }
                }
                Spacer(Modifier.height(18.dp))
                Text("Panel tasarım önizlemesi", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(Modifier.height(5.dp))
                Text("Bu ekran uygulama içi demodur. Kenardan açılan sistem üstü paneli sonraki aşamada servis ve izinlerle bağlayacağız.", color = Color(0xFFD0DCF2), fontSize = 11.sp, lineHeight = 17.sp)
            }
        }
        SectionLabel("HIZLI ARAÇLAR")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ToolTile("Performans", Icons.Rounded.MonitorHeart, Cyan, Modifier.weight(1f), "FPS ölçümü daha sonra bağlanacak")
            ToolTile("Sıcaklık", Icons.Rounded.Thermostat, Purple, Modifier.weight(1f), "Sensör erişimi cihaza bağlı")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ToolTile("Hızlı ayarlar", Icons.Rounded.Tune, Blue, Modifier.weight(1f), "Kısayolların tasarım alanı")
            ToolTile("Bellek", Icons.Rounded.Memory, Cyan, Modifier.weight(1f), "RAM verileri cihaz ekranında")
        }
    }
}

@Composable
private fun DevicePage(context: Context, onBack: () -> Unit) {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memory = ActivityManager.MemoryInfo()
    manager.getMemoryInfo(memory)
    val totalRam = memory.totalMem / (1024L * 1024L * 1024L)
    val availableRam = memory.availMem / (1024L * 1024L * 1024L)
    val usedRam = (totalRam - availableRam).coerceAtLeast(0)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PageHeader("Cihaz laboratuvarı", "SİSTEM BİLGİLERİ", onBack)
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = PanelBright)) {
            Column(Modifier.padding(22.dp)) {
                Text(Build.MODEL.orEmpty(), color = White, fontSize = 23.sp, fontWeight = FontWeight.Black)
                Text("${Build.MANUFACTURER.orEmpty()} • Android ${Build.VERSION.RELEASE} • API ${Build.VERSION.SDK_INT}", color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(22.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(92.dp)) {
                        CircularProgressIndicator(progress = { if (totalRam > 0) (usedRam.toFloat() / totalRam).coerceIn(0f, 1f) else 0f }, modifier = Modifier.fillMaxSize(), color = Cyan, trackColor = Color(0xFF29364C), strokeWidth = 8.dp, strokeCap = StrokeCap.Round)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${usedRam}G", color = White, fontWeight = FontWeight.Black, fontSize = 21.sp)
                            Text("kullanım", color = Muted, fontSize = 9.sp)
                        }
                    }
                    Spacer(Modifier.width(18.dp))
                    Column {
                        Text("Bellek durumu", color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Yaklaşık ${usedRam} GB / ${totalRam} GB kullanımda", color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
                        Text("Android'in raporladığı sistem belleği", color = Color(0xFF64748B), fontSize = 10.sp)
                    }
                }
            }
        }
        InfoRow("Üretici", Build.MANUFACTURER.orEmpty())
        InfoRow("Cihaz kod adı", Build.DEVICE.orEmpty())
        InfoRow("İşlemci donanımı", Build.HARDWARE.orEmpty())
        InfoRow("Ürün", Build.PRODUCT.orEmpty())
        InfoRow("Android sürümü", Build.VERSION.RELEASE.orEmpty())
        InfoRow("SDK seviyesi", Build.VERSION.SDK_INT.toString())
        Text("Bazı işlemci ve sıcaklık ayrıntıları Android tarafından uygulamalara açılmayabilir.", color = Muted, fontSize = 11.sp, lineHeight = 16.sp)
    }
}

@Composable
private fun PageHeader(title: String, subtitle: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.size(46.dp).clip(CircleShape).background(PanelBright).border(1.dp, Color(0xFF2B3D58), CircleShape).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Close, "Geri", tint = White)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, color = White, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
            Text(subtitle, color = Cyan, fontSize = 9.sp, letterSpacing = 1.8.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PanelButton(title: String, icon: ImageVector, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val accent = if (active) Cyan else Muted
    Column(modifier.clip(RoundedCornerShape(20.dp)).background(if (active) Color(0xFF153943) else Color(0xFF151D2E)).border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(20.dp)).clickable(onClick = onClick).padding(vertical = 16.dp, horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = accent, modifier = Modifier.size(25.dp))
        Spacer(Modifier.height(8.dp))
        Text(title, color = White, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(5.dp))
        Text(if (active) "AÇIK" else "KAPALI", color = accent, fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ToolTile(title: String, icon: ImageVector, accent: Color, modifier: Modifier, note: String) {
    Column(modifier.clip(RoundedCornerShape(24.dp)).background(Panel).border(1.dp, Color(0xFF25334A), RoundedCornerShape(24.dp)).padding(16.dp)) {
        Box(Modifier.size(46.dp).background(accent.copy(alpha = 0.12f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text(title, color = White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(5.dp))
        Text(note, color = Muted, fontSize = 10.sp, lineHeight = 15.sp)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Panel).border(1.dp, Color(0xFF202D41), RoundedCornerShape(18.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Muted, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value.ifBlank { "Bilinmiyor" }, color = White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End, modifier = Modifier.weight(1.1f))
    }
}

@Composable
private fun GlowIcon(icon: ImageVector, modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "iconPulse")
    val pulse by transition.animateFloat(0.45f, 0.9f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "iconPulseAlpha")
    Box(modifier.shadow(18.dp, RoundedCornerShape(19.dp), ambientColor = Cyan.copy(alpha = pulse * 0.4f), spotColor = Cyan.copy(alpha = pulse * 0.25f)).background(Brush.linearGradient(listOf(Cyan.copy(alpha = 0.22f), Blue.copy(alpha = 0.16f))), RoundedCornerShape(19.dp)).border(1.dp, Cyan.copy(alpha = 0.45f), RoundedCornerShape(19.dp)), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = Cyan, modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun AppSelectionPage(context: Context, onBack: () -> Unit) {
    val prefs = remember { context.getSharedPreferences("snapdragon_settings", Context.MODE_PRIVATE) }
    var selected by remember { mutableStateOf(prefs.getStringSet("selected_apps", emptySet())?.toSet() ?: emptySet()) }
    val apps = remember {
        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_LAUNCHER)
        context.packageManager.queryIntentActivities(intent, 0).map { it.activityInfo }.filter { it.packageName != context.packageName }.distinctBy { it.packageName }.sortedBy { it.loadLabel(context.packageManager).toString().lowercase() }
    }
    val usageGranted = remember { hasUsageAccess(context) }
    val overlayGranted = remember { android.provider.Settings.canDrawOverlays(context) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        PageHeader("Uygulama seçimi", "OTOMATİK OYUN TETİKLEYİCİSİ", onBack)
        Text("Yalnızca seçtiğin uygulamalar öndeyken Snapdragon bildirimi ve panel tetiklenir.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) { StatusChip("Kullanım erişimi", usageGranted); StatusChip("Üstte göster", overlayGranted) }
        Button(onClick = {
            when {
                !hasUsageAccess(context) -> context.startActivity(android.content.Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS))
                !android.provider.Settings.canDrawOverlays(context) -> context.startActivity(android.content.Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:" + context.packageName)))
                else -> { val i = android.content.Intent(context, SnapdragonMonitorService::class.java); if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(i) else context.startService(i) }
            }
        }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(18.dp)) { Icon(Icons.Rounded.PlayArrow, null); Spacer(Modifier.width(8.dp)); Text(if (!usageGranted) "Kullanım erişimini ver" else if (!overlayGranted) "Panel iznini ver" else "Otomatik algılamayı başlat", fontWeight = FontWeight.Bold) }
        Text("SEÇİLİ UYGULAMALAR · ${selected.size}", color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
        apps.forEach { app ->
            val packageName = app.packageName
            val label = app.loadLabel(context.packageManager).toString()
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Panel).border(1.dp, Color(0xFF26364D), RoundedCornerShape(18.dp)).clickable { selected = if (packageName in selected) selected - packageName else selected + packageName; prefs.edit().putStringSet("selected_apps", selected).apply() }.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(label, color = White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp); Text(packageName, color = Muted, fontSize = 9.sp, maxLines = 1) }
                Checkbox(checked = packageName in selected, onCheckedChange = { selected = if (it) selected + packageName else selected - packageName; prefs.edit().putStringSet("selected_apps", selected).apply() })
            }
        }
        Text("Android, kullanım erişimi ve diğer uygulamaların üzerinde gösterme izinlerini senden ister. Algılama açıkken Snapdragon servisi bildirim alanında görünür.", color = Muted, fontSize = 10.sp, lineHeight = 15.sp)
    }
}

@Composable
private fun StatusChip(title: String, enabled: Boolean) {
    val tint = if (enabled) Cyan else Purple
    Row(Modifier.clip(CircleShape).background(tint.copy(alpha = 0.12f)).border(1.dp, tint.copy(alpha = 0.35f), CircleShape).padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) { Box(Modifier.size(6.dp).background(tint, CircleShape)); Text(title + if (enabled) " hazır" else " gerekli", color = tint, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
}

private fun hasUsageAccess(context: Context): Boolean {
    val ops = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= 29) ops.unsafeCheckOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), context.packageName) else { @Suppress("DEPRECATION") ops.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), context.packageName) }
    return mode == android.app.AppOpsManager.MODE_ALLOWED
}
