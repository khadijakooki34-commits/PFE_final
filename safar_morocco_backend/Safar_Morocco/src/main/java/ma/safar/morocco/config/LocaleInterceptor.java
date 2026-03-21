package ma.safar.morocco.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ma.safar.morocco.util.LocaleContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LocaleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String lang = request.getHeader("Accept-Language");
        if (lang == null || lang.isEmpty()) {
            lang = "en";
        } else {
            // Take the first part of the language header (e.g., 'en-US' -> 'en')
            lang = lang.split(",")[0].split("-")[0].toLowerCase();
        }
        
        // Ensure it's one of the supported languages
        if (!isSupported(lang)) {
            lang = "en";
        }
        
        LocaleContext.setLocale(lang);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LocaleContext.clear();
    }

    private boolean isSupported(String lang) {
        return lang.equals("en") || lang.equals("fr") || lang.equals("ar") || lang.equals("es");
    }
}
