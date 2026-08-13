#!/bin/bash
awk '
BEGIN { skip=0 }
/var isBackupEnabled/ { next }
/\/\/ Backup Section/ { skip=1 }
/Spacer\(modifier = Modifier\.height\(32\.dp\)\)/ {
    if (skip == 1) {
        skip=0
        next
    }
}
{
    if (skip == 0) {
        print $0
    }
}
' app/src/main/java/com/focusbyrj/app/ui/screens/BackupSecurityScreen.kt > temp.kt
mv temp.kt app/src/main/java/com/focusbyrj/app/ui/screens/BackupSecurityScreen.kt
