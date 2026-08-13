import re
with open('gradle/wrapper/gradle-wrapper.properties', 'r') as f:
    props = f.read()
props = re.sub(r'gradle-8\.\d+-bin\.zip', 'gradle-8.9-bin.zip', props)
with open('gradle/wrapper/gradle-wrapper.properties', 'w') as f:
    f.write(props)
