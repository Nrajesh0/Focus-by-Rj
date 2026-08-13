import re

with open('gradle/libs.versions.toml', 'r') as f:
    content = f.read()

# Remove firebase versions
content = re.sub(r'firebaseBom = ".*"\n', '', content)
content = re.sub(r'googleServices = ".*"\n', '', content)
content = re.sub(r'credentials = ".*"\n', '', content)
content = re.sub(r'googleid = ".*"\n', '', content)
content = re.sub(r'playServicesLocation = ".*"\n', '', content)

# Remove firebase libraries
content = re.sub(r'firebase-bom = \{.*\}\n', '', content)
content = re.sub(r'firebase-ai = \{.*\}\n', '', content)
content = re.sub(r'firebase-appcheck-recaptcha = \{.*\}\n', '', content)
content = re.sub(r'firebase-firestore = \{.*\}\n', '', content)
content = re.sub(r'firebase-auth = \{.*\}\n', '', content)
content = re.sub(r'androidx-credentials = \{.*\}\n', '', content)
content = re.sub(r'androidx-credentials-play-services = \{.*\}\n', '', content)
content = re.sub(r'googleid = \{.*\}\n', '', content)
content = re.sub(r'play-services-location = \{.*\}\n', '', content)

# Remove google-services plugin
content = re.sub(r'google-services = \{.*\}\n', '', content)

with open('gradle/libs.versions.toml', 'w') as f:
    f.write(content)
print("Cleaned libs.versions.toml")
