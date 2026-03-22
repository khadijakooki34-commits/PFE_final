import os
import re

app_dir = r'w:\pffe\PFE_final\frontend\src\app'
results = []

# Regex for text nodes in HTML: >Some Text<
# Excluding those that are already translated or empty
text_node_regex = re.compile(r'>([^<{|\n\s][^<{|\n]*)<')

# Regex for attributes that should be translated
attr_regex = re.compile(r'\b(placeholder|title|label|aria-label)="([^"{}]*)"')

for root, dirs, files in os.walk(app_dir):
    for f in files:
        if f.endswith('.html'):
            path = os.path.join(root, f)
            with open(path, 'r', encoding='utf-8') as hf:
                content = hf.read()
                
                # Check text nodes
                for match in text_node_regex.finditer(content):
                    text = match.group(1).strip()
                    if text and not (text.startswith('{{') and text.endswith('}}')):
                        results.append(f"{path}: Hardcoded Text: '{text}'")
                
                # Check attributes
                for match in attr_regex.finditer(content):
                    attr = match.group(1)
                    val = match.group(2).strip()
                    if val and not (val.startswith('{{') and val.endswith('}}')):
                        results.append(f"{path}: Hardcoded {attr}: '{val}'")

with open(r'w:\pffe\PFE_final\frontend\hardcoded_scan.txt', 'w', encoding='utf-8') as rf:
    rf.write('\n'.join(results))

print(f"Found {len(results)} potential items. Saved to hardcoded_scan.txt")
