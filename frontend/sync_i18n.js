const fs = require('fs');
const path = require('path');

const i18nPath = path.join(__dirname, 'src/assets/i18n');
const en = JSON.parse(fs.readFileSync(path.join(i18nPath, 'en.json'), 'utf8'));

const sync = (obj, target) => {
  const result = { ...target };
  for (const key in obj) {
    if (typeof obj[key] === 'object' && obj[key] !== null) {
      result[key] = sync(obj[key], target[key] || {});
    } else {
      if (target[key] === undefined) {
        result[key] = obj[key]; // Fallback to English
      } else {
        result[key] = target[key];
      }
    }
  }
  return result;
};

['fr.json', 'ar.json', 'es.json'].forEach(file => {
  const filePath = path.join(i18nPath, file);
  const target = JSON.parse(fs.readFileSync(filePath, 'utf8'));
  const synced = sync(en, target);
  fs.writeFileSync(filePath, JSON.stringify(synced, null, 2), 'utf8');
  console.log(`Synced ${file}`);
});
