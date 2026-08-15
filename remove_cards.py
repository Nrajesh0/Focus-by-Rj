import sys

file_path = "app/src/main/java/com/focusbyrj/app/ui/screens/DashboardScreen.kt"

with open(file_path, "r") as f:
    content = f.read()

# Remove the invocation
invoke_str = "            DistractionsPreventedCard()\n            Spacer(modifier = Modifier.height(12.dp))\n"
content = content.replace(invoke_str, "")

# Remove the composable definition
start_str = "@Composable\nfun DistractionsPreventedCard() {"
start_idx = content.find(start_str)

if start_idx != -1:
    end_str = "    }\n}\n"
    end_idx = content.find(end_str, start_idx) + len(end_str)
    content = content[:start_idx] + content[end_idx:]

with open(file_path, "w") as f:
    f.write(content)

