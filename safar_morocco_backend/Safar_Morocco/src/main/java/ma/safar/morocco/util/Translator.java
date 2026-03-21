package ma.safar.morocco.util;

import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.Method;

@Slf4j
public class Translator {

    public static String translate(Object entity, String fieldName) {
        if (entity == null) return null;
        
        String locale = LocaleContext.getLocale();
        // Capitalize first letter for getter: 'name' -> 'Name'
        String capitalizedField = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        
        // Try to get language-specific field first (e.g., getNameFr)
        String langMethodName = "get" + capitalizedField + locale.substring(0, 1).toUpperCase() + locale.substring(1);
        
        try {
            Method langMethod = entity.getClass().getMethod(langMethodName);
            String value = (String) langMethod.invoke(entity);
            if (value != null && !value.isEmpty()) {
                return value;
            }
        } catch (Exception e) {
            // Method might not exist, ignore and fallback
        }
        
        // Fallback to English (e.g., getNameEn)
        if (!locale.equals("en")) {
            String enMethodName = "get" + capitalizedField + "En";
            try {
                Method enMethod = entity.getClass().getMethod(enMethodName);
                String value = (String) enMethod.invoke(entity);
                if (value != null && !value.isEmpty()) {
                    return value;
                }
            } catch (Exception e) {
                // Ignore
            }
        }
        
        // Final fallback to generic getter (e.g., getName or getNom)
        try {
            Method genericMethod = entity.getClass().getMethod("get" + capitalizedField);
            return (String) genericMethod.invoke(entity);
        } catch (Exception e) {
            // If getField fails, try getNom (French convention used in some entities)
            if (fieldName.equals("name")) {
                try {
                    Method nomMethod = entity.getClass().getMethod("getNom");
                    return (String) nomMethod.invoke(entity);
                } catch (Exception ex) {
                    // Ignore
                }
            }
        }
        
        return null;
    }
}
