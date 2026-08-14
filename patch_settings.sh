#!/bin/bash
sed -i 's/var routineNotifications by remember { mutableStateOf(prefs.getBoolean("routine_notifications", true)) }/var softUnlockDuration by remember { mutableStateOf(prefs.getInt("soft_unlock_duration", 5)) }\n    var routineNotifications by remember { mutableStateOf(prefs.getBoolean("routine_notifications", true)) }/g' app/src/main/java/com/focusbyrj/app/ui/screens/SettingsScreen.kt
