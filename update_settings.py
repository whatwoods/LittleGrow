import re

with open('app/src/main/java/com/littlegrow/app/ui/screens/SettingsScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Add imports
imports_to_add = """
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.vector.ImageVector
"""
for line in imports_to_add.strip().split('\n'):
    if line not in content:
        content = content.replace("import androidx.compose.material.icons.Icons\n", f"import androidx.compose.material.icons.Icons\n{line}\n")

# Add enum
enum_code = """
enum class SettingsCategory(val title: String, val icon: ImageVector) {
    PROFILE("宝宝资料", Icons.Rounded.Person),
    NOTIFICATIONS("提醒与通知", Icons.Rounded.Notifications),
    APPEARANCE("主题与外观", Icons.Rounded.Palette),
    MODULES("首页模块与看护人", Icons.Rounded.Dashboard),
    DATA("数据与备份", Icons.Rounded.Storage),
    ABOUT("关于", Icons.Rounded.Info)
}
"""
if "enum class SettingsCategory" not in content:
    content += "\n" + enum_code

lazy_column_start = content.find("    LazyColumn(\n        contentPadding = PaddingValues(")
if lazy_column_start == -1:
    print("Could not find LazyColumn start")
    exit(1)

settings_screen_end = content.find("}\n\n@Composable\nprivate fun SettingsSectionTitle")
if settings_screen_end == -1:
    print("Could not find SettingsScreen end")
    exit(1)

lazy_column_code = content[lazy_column_start:settings_screen_end]

def get_block_by_marker(text, marker):
    start_idx = text.find(marker)
    if start_idx == -1: return ""
    component_start = text.find("ElevatedCard(", start_idx)
    if component_start == -1:
        component_start = text.find("Column(", start_idx)
    if component_start == -1: return ""
    
    brace_count = 0
    in_brace = False
    end_idx = component_start
    for i in range(component_start, len(text)):
        if text[i] == '{':
            brace_count += 1
            in_brace = True
        elif text[i] == '}':
            brace_count -= 1
        
        if in_brace and brace_count == 0:
            end_idx = i + 1
            break
            
    return text[component_start:end_idx]

notif_content = get_block_by_marker(lazy_column_code, "SettingsSectionTitle(\"提醒与通知\")")
prof_content = get_block_by_marker(lazy_column_code, "SettingsSectionTitle(\"宝宝资料\")")
app_content = get_block_by_marker(lazy_column_code, "SettingsSectionTitle(\"主题与外观\")")
data_content = get_block_by_marker(lazy_column_code, "SettingsSectionTitle(\"数据与备份\")")
mod_content = get_block_by_marker(lazy_column_code, "SettingsSectionTitle(\"首页模块与看护人\")")
about_content = get_block_by_marker(lazy_column_code, "SettingsSectionTitle(\"关于\")")

new_lazy_column = """
    var currentCategory by rememberSaveable { mutableStateOf<SettingsCategory?>(null) }
    BackHandler(enabled = currentCategory != null) {
        currentCategory = null
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(
            top = contentPadding.calculateTopPadding() + 16.dp,
        )
    ) {
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            alpha = 0.82f,
            shape = RoundedCornerShape(28.dp),
            accentColor = MaterialTheme.colorScheme.secondary,
            shadowElevation = 20.dp,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                if (currentCategory != null) {
                    IconButton(onClick = { currentCategory = null }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "返回", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Text(
                    currentCategory?.title ?: "设置",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        AnimatedContent(
            targetState = currentCategory,
            label = "SettingsTransition",
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            }
        ) { category ->
            LazyColumn(
                contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding() + 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (category == null) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SettingsCategory.entries.forEach { cat ->
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                    onClick = { currentCategory = cat },
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    ListItem(
                                        headlineContent = { Text(cat.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
                                        leadingContent = { 
                                            Box(
                                                modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(cat.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer) 
                                            }
                                        },
                                        trailingContent = { Icon(Icons.Rounded.ChevronRight, contentDescription = "进入", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                                        colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = Color.Transparent),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                    if (errorText != null) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            ElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) { 
                                Text(
                                    errorText!!,
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                } else {
                    item {
                        when (category) {
                            SettingsCategory.PROFILE -> {
                                __PROFILE_CONTENT__
                            }
                            SettingsCategory.NOTIFICATIONS -> {
                                __NOTIFICATIONS_CONTENT__
                            }
                            SettingsCategory.APPEARANCE -> {
                                __APPEARANCE_CONTENT__
                            }
                            SettingsCategory.DATA -> {
                                __DATA_CONTENT__
                            }
                            SettingsCategory.MODULES -> {
                                __MODULES_CONTENT__
                            }
                            SettingsCategory.ABOUT -> {
                                __ABOUT_CONTENT__
                            }
                        }
                    }
                }
            }
        }
    }
"""

new_lazy_column = new_lazy_column.replace("__NOTIFICATIONS_CONTENT__", notif_content)
new_lazy_column = new_lazy_column.replace("__PROFILE_CONTENT__", prof_content)
new_lazy_column = new_lazy_column.replace("__APPEARANCE_CONTENT__", app_content)
new_lazy_column = new_lazy_column.replace("__DATA_CONTENT__", data_content)
new_lazy_column = new_lazy_column.replace("__MODULES_CONTENT__", mod_content)
new_lazy_column = new_lazy_column.replace("__ABOUT_CONTENT__", about_content)

content = content[:lazy_column_start] + new_lazy_column + "\n" + content[settings_screen_end:]

with open('app/src/main/java/com/littlegrow/app/ui/screens/SettingsScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)

print("Settings refactored successfully.")