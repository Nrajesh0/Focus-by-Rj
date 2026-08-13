import os

os.makedirs('app/src/main/res/drawable', exist_ok=True)
os.makedirs('app/src/main/res/mipmap-anydpi-v26', exist_ok=True)
os.makedirs('fastlane/metadata/android/en-US/images', exist_ok=True)

# 1. Background layer
bg_xml = """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <aapt:attr name="android:fillColor" xmlns:aapt="http://schemas.android.com/aapt">
        <gradient
            android:startX="0"
            android:startY="0"
            android:endX="108"
            android:endY="108"
            android:type="linear">
            <item android:color="#0F172A" android:offset="0.0" />
            <item android:color="#06B6D4" android:offset="1.0" />
        </gradient>
    </aapt:attr>
    <path
        android:pathData="M0,0h108v108h-108z" />
</vector>
"""
with open('app/src/main/res/drawable/ic_launcher_background.xml', 'w') as f:
    f.write(bg_xml)

# 2. Foreground layer
fg_xml = """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    
    <!-- Outer concentric ring -->
    <path
        android:fillColor="#40FFFFFF"
        android:pathData="M54,18 A36,36 0 1,0 90,54 A36.04,36.04 0 0,0 54,18 Z M54,82 A28,28 0 1,1 82,54 A28.03,28.03 0 0,1 54,82 Z"/>
    
    <!-- Center Lock Icon -->
    <group
        android:translateX="30"
        android:translateY="30"
        android:scaleX="2.0"
        android:scaleY="2.0">
        <path
            android:fillColor="#FFFFFF"
            android:pathData="M18,8h-1V6c0-2.76-2.24-5-5-5S7,3.24,7,6v2H6c-1.1,0-2,0.9-2,2v10c0,1.1,0.9,2,2,2h12c1.1,0,2-0.9,2-2V10C20,8.9,19.1,8,18,8zM9,6c0-1.66,1.34-3,3-3s3,1.34,3,3v2H9V6zM12,17c1.1,0,2-0.9,2-2c0-1.1-0.9-2-2-2s-2,0.9-2,2C10,16.1,10.9,17,12,17z"/>
    </group>
</vector>
"""
with open('app/src/main/res/drawable/ic_launcher_foreground.xml', 'w') as f:
    f.write(fg_xml)

# 3. Adaptive wrapper
adaptive_xml = """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
"""
with open('app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml', 'w') as f:
    f.write(adaptive_xml)
with open('app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml', 'w') as f:
    f.write(adaptive_xml)

# 4. SVG for F-Droid PNG generation
svg_content = """<svg width="512" height="512" viewBox="0 0 108 108" xmlns="http://www.w3.org/2000/svg">
  <defs>
    <linearGradient id="grad1" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#0F172A" />
      <stop offset="100%" stop-color="#06B6D4" />
    </linearGradient>
  </defs>
  <rect width="108" height="108" fill="url(#grad1)" />
  <path fill="rgba(255,255,255,0.25)" d="M54,18 A36,36 0 1,0 90,54 A36.04,36.04 0 0,0 54,18 Z M54,82 A28,28 0 1,1 82,54 A28.03,28.03 0 0,1 54,82 Z" />
  <g transform="translate(30, 30) scale(2.0)">
    <path fill="#FFFFFF" d="M18,8h-1V6c0-2.76-2.24-5-5-5S7,3.24,7,6v2H6c-1.1,0-2,0.9-2,2v10c0,1.1,0.9,2,2,2h12c1.1,0,2-0.9,2-2V10C20,8.9,19.1,8,18,8zM9,6c0-1.66,1.34-3,3-3s3,1.34,3,3v2H9V6zM12,17c1.1,0,2-0.9,2-2c0-1.1-0.9-2-2-2s-2,0.9-2,2C10,16.1,10.9,17,12,17z" />
  </g>
</svg>
"""
with open('icon.svg', 'w') as f:
    f.write(svg_content)

print("Icon files generated.")
