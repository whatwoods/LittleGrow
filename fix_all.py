import os
import glob

def fix_files():
    for f_path in glob.glob("d:/App/Android/LittleGrow/app/src/main/java/com/littlegrow/app/ui/screens/*.kt") + glob.glob("d:/App/Android/LittleGrow/app/src/main/java/com/littlegrow/app/ui/components/*.kt") + glob.glob("d:/App/Android/LittleGrow/app/src/main/java/com/littlegrow/app/ui/theme/*.kt"):
        with open(f_path, "r", encoding="utf-8") as f:
            content = f.read()
        
        changed = False
        if "itemsIndexed" in content and "import androidx.compose.foundation.lazy.itemsIndexed" not in content:
            content = content.replace("import androidx.compose.foundation.lazy.items", "import androidx.compose.foundation.lazy.items\nimport androidx.compose.foundation.lazy.itemsIndexed")
            changed = True
        
        if "colorScheme.semantics" in content:
            content = content.replace("colorScheme.semantics", "semanticColors")
            changed = True

        if changed:
            with open(f_path, "w", encoding="utf-8") as f:
                f.write(content)
            print(f"Fixed {f_path}")

fix_files()
