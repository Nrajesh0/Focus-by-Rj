import sys

file_path = "app/src/main/java/com/focusbyrj/app/ui/screens/DashboardScreen.kt"

with open(file_path, "r") as f:
    content = f.read()

start_str = "@Composable\nfun DistractionsPreventedCard() {"
end_str = "    }\n}"

start_idx = content.find(start_str)
if start_idx == -1:
    print("Start not found")
    sys.exit(1)
    
end_idx = content.find(end_str, start_idx) + len(end_str)

replacement = """@Composable
fun DistractionsPreventedCard() {
    val context = LocalContext.current
    val interceptions by com.focusbyrj.app.util.FocusStatsManager.interceptionsFlow.collectAsStateWithLifecycle()
    val statsFlow = remember { com.focusbyrj.app.util.DeviceStatsHelper.getBatteryStats(context) }
    val initialBatteryInfo = remember {
        com.focusbyrj.app.util.BatteryHealthInfo(
            rawChargePercentage = 80,
            maxCapacityHealthPercent = 88,
            realRemainingCapacityPercent = 70.4f,
            temperatureCelsius = 28.5f,
            voltageMv = 3850,
            healthStatusLabel = "Normal",
            peakPerformanceStatus = "Your battery is currently supporting normal peak performance.",
            isCharging = false
        )
    }
    val stats by statsFlow.collectAsState(initial = initialBatteryInfo)

    Row(
        modifier = Modifier.fillMaxWidth().height(110.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceDark)
                .border(1.dp, BorderGlass, RoundedCornerShape(20.dp))
                .padding(14.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(androidx.compose.material.icons.Icons.Filled.DeviceThermostat, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Temperature", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text("${stats.temperatureCelsius}°C", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                Spacer(modifier = Modifier.height(2.dp))
                Text("${stats.voltageMv} mV", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceDark)
                .border(1.dp, BorderGlass, RoundedCornerShape(20.dp))
                .padding(14.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(androidx.compose.material.icons.Icons.Filled.Lock, contentDescription = null, tint = AccentViolet, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Prevented", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(interceptions.toString(), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Distractions", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}"""

new_content = content[:start_idx] + replacement + content[end_idx:]

with open(file_path, "w") as f:
    f.write(new_content)
