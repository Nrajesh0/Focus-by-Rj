with open('gradle/libs.versions.toml', 'r') as f:
    toml = f.read()

toml = toml.replace('coreKtx = "1.18.0"', 'coreKtx = "1.15.0"')
toml = toml.replace('core = "1.6.1"', 'core = "1.6.1"') # Wait, androidx.test:core is 1.6.1, that's fine. Wait, the error said `androidx.core:core:1.18.0`. So `androidx-core-ktx` brings in `androidx.core:core:1.18.0`. 

with open('gradle/libs.versions.toml', 'w') as f:
    f.write(toml)
