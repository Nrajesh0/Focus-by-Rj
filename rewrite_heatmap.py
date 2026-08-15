import sys

file_path = "app/src/main/java/com/focusbyrj/app/ui/screens/AnalyticsScreen.kt"

with open(file_path, "r") as f:
    content = f.read()

start_str = "@Composable\nfun HeatmapWidget("
end_str = "                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {"

start_idx = content.find(start_str)
end_idx = content.find(end_str, start_idx)

replacement = """@Composable
fun HeatmapWidget(
    dailyUsage: Map<Int, Long>,
    theme: HeatmapTheme
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderGlass, RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "ACTIVITY HEATMAP",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    theme.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = theme.colors.last()
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "The darker the square, the longer your focus session. Stay consistent and fill the board to build unbreakable momentum!",
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(18.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.height((24 * 7 + 6 * 6).dp)) {
                    val days = listOf("S", "M", "T", "W", "T", "F", "S")
                    days.forEach { d ->
                        Box(modifier = Modifier.height(24.dp), contentAlignment = Alignment.Center) {
                            Text(d, color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                
                val levels = theme.colors
                val todayCalendar = Calendar.getInstance()
                
                for (weekIndex in 4 downTo 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (dayOfWeek in 1..7) {
                            val targetCal = Calendar.getInstance()
                            targetCal.set(Calendar.DAY_OF_WEEK, dayOfWeek)
                            targetCal.add(Calendar.WEEK_OF_YEAR, -weekIndex)
                            
                            val dayOfYear = targetCal.get(Calendar.DAY_OF_YEAR)
                            
                            val isFuture = targetCal.after(todayCalendar) && targetCal.get(Calendar.DAY_OF_YEAR) != todayCalendar.get(Calendar.DAY_OF_YEAR)
                            
                            val usageMs = dailyUsage[dayOfYear] ?: 0L
                            val levelIndex = when {
                                isFuture -> 0
                                usageMs == 0L -> 0
                                usageMs < 15 * 60 * 1000L -> 1
                                usageMs < 30 * 60 * 1000L -> 2
                                usageMs < 60 * 60 * 1000L -> 3
                                else -> 4
                            }
                            
                            val color = levels[levelIndex]
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(color)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Less", color = Color.Gray, fontSize = 10.sp)
                Spacer(modifier = Modifier.width(8.dp))
"""

new_content = content[:start_idx] + replacement + content[end_idx:]

with open(file_path, "w") as f:
    f.write(new_content)
