package ma.safar.morocco.util;

public class LocaleContext {
    private static final ThreadLocal<String> currentLocale = new ThreadLocal<>();

    private LocaleContext() {
        // Private constructor to hide the implicit public one
    }

    public static void setLocale(String locale) {
        currentLocale.set(locale);
    }

    public static String getLocale() {
        return currentLocale.get() != null ? currentLocale.get() : "en";
    }

    public static void clear() {
        currentLocale.remove();
    }
}
