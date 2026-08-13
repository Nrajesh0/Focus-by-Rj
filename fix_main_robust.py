import re

path = 'app/src/main/java/com/focusbyrj/app/MainActivity.kt'
with open(path, 'r') as f:
    text = f.read()

old_set_content = """    setContent {
      val context = androidx.compose.ui.platform.LocalContext.current
      val prefs = context.getSharedPreferences("focus_prefs", android.content.Context.MODE_PRIVATE)
      var isAuthRequired by androidx.compose.runtime.remember { 
          mutableStateOf(prefs.getBoolean("biometric_lock", false) || prefs.getBoolean("pin_lock_enabled", false)) 
      }
      var isAuthenticated by androidx.compose.runtime.remember { mutableStateOf(false) }
      
      val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
      androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
          val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
              if (event == androidx.lifecycle.Lifecycle.Event.ON_START) {
                  isAuthRequired = prefs.getBoolean("biometric_lock", false) || prefs.getBoolean("pin_lock_enabled", false)
              } else if (event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                  isAuthenticated = false
              }
          }
          lifecycleOwner.lifecycle.addObserver(observer)
          onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
      }

      FocusByRjTheme {
        if (isAuthRequired && !isAuthenticated) {
            com.focusbyrj.app.ui.screens.AuthScreen(onAuthenticated = { isAuthenticated = true })
        } else {
            MainAppScreen(viewModel)
        }
      }
    }"""

new_set_content = """    setContent {
      val context = androidx.compose.ui.platform.LocalContext.current
      val prefs = androidx.compose.runtime.remember { context.getSharedPreferences("focus_prefs", android.content.Context.MODE_PRIVATE) }
      
      var appState by androidx.compose.runtime.remember { 
          androidx.compose.runtime.mutableStateOf("INIT")
      }
      
      val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
      androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
          val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
              if (event == androidx.lifecycle.Lifecycle.Event.ON_START || event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                  val authNeeded = prefs.getBoolean("biometric_lock", false) || prefs.getBoolean("pin_lock_enabled", false)
                  if (authNeeded && appState != "UNLOCKED") {
                      appState = "LOCKED"
                  } else if (!authNeeded) {
                      appState = "UNLOCKED"
                  }
              } else if (event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                  appState = "INIT"
              }
          }
          lifecycleOwner.lifecycle.addObserver(observer)
          onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
      }

      FocusByRjTheme {
        if (appState == "LOCKED") {
            com.focusbyrj.app.ui.screens.AuthScreen(onAuthenticated = { appState = "UNLOCKED" })
        } else {
            MainAppScreen(viewModel)
        }
      }
    }"""

text = text.replace(old_set_content, new_set_content)

with open(path, 'w') as f:
    f.write(text)
