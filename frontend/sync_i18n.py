import json
import os

i18n_dir = r'w:\pffe\PFE_final\frontend\src\assets\i18n'
files = ['en.json', 'fr.json', 'ar.json', 'es.json']

def get_keys(data, prefix=''):
    keys = {}
    for k, v in data.items():
        if isinstance(v, dict):
            keys.update(get_keys(v, f"{prefix}.{k}" if prefix else k))
        else:
            keys[f"{prefix}.{k}" if prefix else k] = v
    return keys

def set_key(data, key_path, value):
    parts = key_path.split('.')
    curr = data
    for part in parts[:-1]:
        if part not in curr:
            curr[part] = {}
        curr = curr[part]
    curr[parts[-1]] = value

all_data = {}
all_keys = set()

for f in files:
    path = os.path.join(i18n_dir, f)
    with open(path, 'r', encoding='utf-8') as jf:
        data = json.load(jf)
        all_data[f] = data
        all_keys.update(get_keys(data).keys())

print(f"Total unique keys: {len(all_keys)}")

for f in files:
    data = all_data[f]
    current_keys = get_keys(data).keys()
    missing = all_keys - current_keys
    if missing:
        print(f"File {f} is missing {len(missing)} keys. Adding them...")
        for k in missing:
            en_val = get_keys(all_data['en.json']).get(k, k)
            set_key(data, k, en_val)
    
    with open(os.path.join(i18n_dir, f), 'w', encoding='utf-8') as jf:
        json.dump(data, jf, indent=2, ensure_ascii=False)

print("Synchronization complete.")
