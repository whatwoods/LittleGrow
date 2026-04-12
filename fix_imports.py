import os, glob

def fix():
    for f in glob.glob("d:/App/Android/LittleGrow/app/src/main/java/com/littlegrow/app/ui/screens/*.kt"):
        with open(f, "r", encoding="utf-8") as file:
            content = file.read()
        changed = False

        if "import com.littlegrow.app.ui.theme.semantics" in content:
            content = content.replace("import com.littlegrow.app.ui.theme.semantics", "import com.littlegrow.app.ui.theme.semanticColors")
            changed = True
        
        if "colorScheme.semantics" in content:
            content = content.replace("colorScheme.semantics", "semanticColors")
            changed = True

        if "colors.semantics" in content:
            content = content.replace("colors.semantics", "semanticColors")
            changed = True

        if changed:
            with open(f, "w", encoding="utf-8") as file:
                file.write(content)
            print(f"Fixed {f}")
fix()
