import glob

for f in glob.glob("d:/App/Android/LittleGrow/app/src/main/java/com/littlegrow/app/ui/screens/*.kt"):
    with open(f, "r", encoding="utf-8") as file: content = file.read()
    content = content.replace(" semanticColors.", " MaterialTheme.semanticColors.")
    with open(f, "w", encoding="utf-8") as file: file.write(content)
