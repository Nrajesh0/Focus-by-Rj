import re

path1 = 'app/src/main/java/com/focusbyrj/app/util/PermissionUtils.kt'
path2 = 'app/src/main/java/com/focusbyrj/app/util/UsageStatsHelper.kt'

replacement = """    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }"""

for path in [path1, path2]:
    with open(path, 'r') as f:
        text = f.read()

    # Find the hasUsageStatsPermission function and replace it
    text = re.sub(r'    fun hasUsageStatsPermission\(context: Context\): Boolean \{.*?return mode == AppOpsManager\.MODE_ALLOWED\n    \}', replacement, text, flags=re.DOTALL)
    
    # Ensure Build is imported
    if "import android.os.Build" not in text:
        text = text.replace("import android.os.Process", "import android.os.Build\nimport android.os.Process")

    with open(path, 'w') as f:
        f.write(text)
