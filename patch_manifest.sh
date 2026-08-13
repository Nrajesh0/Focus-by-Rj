#!/bin/bash
set -e
sed -i 's/android:allowBackup="true"/android:allowBackup="false"/g' app/src/main/AndroidManifest.xml
sed -i '/android:dataExtractionRules/d' app/src/main/AndroidManifest.xml
sed -i '/android:fullBackupContent/d' app/src/main/AndroidManifest.xml
