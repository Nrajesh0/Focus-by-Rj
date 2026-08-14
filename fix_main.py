import re

path = 'app/src/main/java/com/focusbyrj/app/MainActivity.kt'
with open(path, 'r') as f:
    text = f.read()

old_effect = """    androidx.compose.runtime.LaunchedEffect(Unit) {
        com.focusbyrj.app.service.FocusBlockerService.startService(context)
    }"""

new_effect = """    androidx.compose.runtime.LaunchedEffect(allPermissionsGranted) {
        if (allPermissionsGranted) {
            com.focusbyrj.app.service.FocusBlockerService.startService(context)
        }
    }
    
    // Also restart service when app resumes to ensure it's always running
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                if (com.focusbyrj.app.util.PermissionUtils.hasUsageStatsPermission(context) && 
                    com.focusbyrj.app.util.PermissionUtils.hasOverlayPermission(context)) {
                    com.focusbyrj.app.service.FocusBlockerService.startService(context)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }"""

text = text.replace(old_effect, new_effect)

with open(path, 'w') as f:
    f.write(text)
