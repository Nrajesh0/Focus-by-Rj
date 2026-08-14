import re
path = 'app/src/main/java/com/focusbyrj/app/ui/screens/SettingsScreen.kt'
with open(path, 'r') as f:
    text = f.read()

replacement = """                Slider(
                    value = softLockDuration.toFloat(),
                    onValueChange = { softLockDuration = it.toInt() },
                    onValueChangeFinished = {
                        prefs.edit().putInt("soft_lock_duration", softLockDuration).apply()
                    },
                    valueRange = 5f..60f,
                    steps = 10,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary,
                        activeTrackColor = MaterialTheme.colorScheme.secondary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surface
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Soft Unlock Duration",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "How many minutes to unlock an app in Soft Mode.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${softUnlockDuration}m",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Slider(
                    value = softUnlockDuration.toFloat(),
                    onValueChange = { softUnlockDuration = it.toInt() },
                    onValueChangeFinished = {
                        prefs.edit().putInt("soft_unlock_duration", softUnlockDuration).apply()
                    },
                    valueRange = 1f..60f,
                    steps = 58,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary,
                        activeTrackColor = MaterialTheme.colorScheme.secondary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surface
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Routine Notifications",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Notify when a scheduled routine starts or ends.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = routineNotifications,
                        onCheckedChange = { 
                            routineNotifications = it
                            prefs.edit().putBoolean("routine_notifications", it).apply()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.secondary,
                            checkedTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                        )
                    )
                }"""

text = re.sub(r'                Slider\(\n                    value = softLockDuration\.toFloat\(\),\n                    onValueChange = \{ softLockDuration = it\.toInt\(\) \},\n                    onValueChangeFinished = \{\n                        prefs\.edit\(\)\.putInt\("soft_lock_duration", softLockDuration\)\.apply\(\)\n                    \},\n                    valueRange = 5f\.\.60f,\n                    steps = 10,\n                    modifier = Modifier\.fillMaxWidth\(\),\n                    colors = SliderDefaults\.colors\(\n                        thumbColor = MaterialTheme\.colorScheme\.secondary,\n                        activeTrackColor = MaterialTheme\.colorScheme\.secondary,\n                        inactiveTrackColor = MaterialTheme\.colorScheme\.surface\n                    \)\n                \)', replacement, text, flags=re.DOTALL)

with open(path, 'w') as f:
    f.write(text)
