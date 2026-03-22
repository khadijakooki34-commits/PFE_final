import json
import os

i18n_dir = r'w:\pffe\PFE_final\frontend\src\assets\i18n'

translations = {
    'fr.json': {
        "COMMON": {
            "EVENT": "Événement",
            "HOTEL": "Hôtel",
            "RESTAURANT": "Restaurant",
            "ACTIVITY": "Activité",
            "SENDING": "Envoi en cours...",
            "PAGE_SELECT": "Sélectionner la page",
            "LOG_IN": "Se connecter",
            "CREATE_ACCOUNT": "Créer un compte",
            "RETURN_HOME": "Retour à l'accueil"
        },
        "LEGAL": {
            "PRIVACY_TITLE": "Politique de confidentialité",
            "TERMS_TITLE": "Conditions d'utilisation",
            "TANDC_TITLE": "Termes et Conditions"
        },
        "HELP": {
            "TITLE": "Centre d'aide",
            "WELCOME": "Bienvenue au centre d'aide de Safar Morocco",
            "SUBTITLE": "Tout ce que vous devez savoir sur l'utilisation de notre plateforme."
        },
        "RECOMMENDATIONS": {
            "TITLE": "Recommandé pour vous",
            "SUBTITLE": "Destinations sélectionnées selon votre activité et vos préférences",
            "LOADING": "Recherche des meilleurs endroits pour vous...",
            "EMPTY": "Aucune recommandation disponible pour le moment.",
            "EXPLORE": "Explorer"
        },
        "AUTH_REQUIRED": {
            "TITLE": "Authentification requise",
            "DESC": "Vous devez vous connecter ou créer un compte pour accéder à ce contenu."
        },
        "CONTACT_PAGE": {
            "HERO_TITLE": "Contactez-nous",
            "HERO_SUBTITLE": "Nous serions ravis de vous entendre.",
            "FORM_TITLE": "Envoyez-nous un message",
            "LABEL_NAME": "Nom complet",
            "LABEL_EMAIL": "Email",
            "LABEL_SUBJECT": "Sujet",
            "LABEL_MESSAGE": "Message",
            "BTN_SEND": "Envoyer le message",
            "PLACEHOLDER_NAME": "Nom complet",
            "PLACEHOLDER_EMAIL": "Email",
            "PLACEHOLDER_SUBJECT": "Sujet",
            "PLACEHOLDER_MESSAGE": "Message"
        },
        "PROFILE": {
            "FRENCH": "Français",
            "ENGLISH": "Anglais",
            "ARABIC": "Arabe",
            "CHANGE_PHOTO": "Changer la photo"
        }
    },
    'es.json': {
        "COMMON": {
            "EVENT": "Evento",
            "HOTEL": "Hotel",
            "RESTAURANT": "Restaurante",
            "ACTIVITY": "Actividad",
            "SENDING": "Enviando...",
            "PAGE_SELECT": "Seleccionar página",
            "LOG_IN": "Iniciar sesión",
            "CREATE_ACCOUNT": "Crear cuenta",
            "RETURN_HOME": "Volver al inicio"
        },
        "LEGAL": {
            "PRIVACY_TITLE": "Política de privacidad",
            "TERMS_TITLE": "Términos de servicio",
            "TANDC_TITLE": "Términos y condiciones"
        },
        "HELP": {
            "TITLE": "Centro de ayuda",
            "WELCOME": "Bienvenido al Centro de Ayuda de Safar Morocco",
            "SUBTITLE": "Todo lo que necesitas saber sobre el uso de nuestra plataforma."
        },
        "RECOMMENDATIONS": {
            "TITLE": "Recomendado para ti",
            "SUBTITLE": "Destinos seleccionados según tu actividad y preferencias",
            "LOADING": "Buscando los mejores lugares para ti...",
            "EMPTY": "No hay recomendaciones disponibles por ahora.",
            "EXPLORE": "Explorar"
        },
        "AUTH_REQUIRED": {
            "TITLE": "Autenticación requerida",
            "DESC": "Debes iniciar sesión o crear una cuenta para acceder a este contenido."
        },
        "CONTACT_PAGE": {
            "HERO_TITLE": "Ponte en contacto",
            "HERO_SUBTITLE": "Nos encantaría saber de ti.",
            "FORM_TITLE": "Envíanos un mensaje",
            "LABEL_NAME": "Nombre completo",
            "LABEL_EMAIL": "Correo electrónico",
            "LABEL_SUBJECT": "Asunto",
            "LABEL_MESSAGE": "Mensaje",
            "BTN_SEND": "Enviar mensaje",
            "PLACEHOLDER_NAME": "Nombre completo",
            "PLACEHOLDER_EMAIL": "Email",
            "PLACEHOLDER_SUBJECT": "Asunto",
            "PLACEHOLDER_MESSAGE": "Mensaje"
        },
        "PROFILE": {
            "FRENCH": "Francés",
            "ENGLISH": "Inglés",
            "ARABIC": "Árabe",
            "CHANGE_PHOTO": "Cambiar foto"
        }
    },
    'ar.json': {
        "COMMON": {
            "EVENT": "فعالية",
            "HOTEL": "فندق",
            "RESTAURANT": "مطعم",
            "ACTIVITY": "نشاط",
            "SENDING": "جاري الإرسال...",
            "PAGE_SELECT": "اختر الصفحة",
            "LOG_IN": "تسجيل الدخول",
            "CREATE_ACCOUNT": "إنشاء حساب",
            "RETURN_HOME": "العودة للرئيسية"
        },
        "LEGAL": {
            "PRIVACY_TITLE": "سياسة الخصوصية",
            "TERMS_TITLE": "شروط الخدمة",
            "TANDC_TITLE": "الأحكام والشروط"
        },
        "HELP": {
            "TITLE": "مركز المساعدة",
            "WELCOME": "مرحباً بكم في مركز مساعدة سفر المغرب",
            "SUBTITLE": "كل ما تحتاج معرفته حول استخدام منصتنا."
        },
        "RECOMMENDATIONS": {
            "TITLE": "موصى به لك",
            "SUBTITLE": "وجهات مختارة بناءً على نشاطك وتفضيلاتك",
            "LOADING": "جاري البحث عن أفضل الأماكن لك...",
            "EMPTY": "لا توجد توصيات متاحة حالياً.",
            "EXPLORE": "استكشف"
        },
        "AUTH_REQUIRED": {
            "TITLE": "مطلوب تسجيل الدخول",
            "DESC": "يجب عليك تسجيل الدخول أو إنشاء حساب للوصول إلى هذا المحتوى."
        },
        "CONTACT_PAGE": {
            "HERO_TITLE": "تواصل معنا",
            "HERO_SUBTITLE": "يسعدنا دائماً سماع آرائكم.",
            "FORM_TITLE": "أرسل لنا رسالة",
            "LABEL_NAME": "الاسم الكامل",
            "LABEL_EMAIL": "البريد الإلكتروني",
            "LABEL_SUBJECT": "الموضوع",
            "LABEL_MESSAGE": "الرسالة",
            "BTN_SEND": "إرسال الرسالة",
            "PLACEHOLDER_NAME": "الاسم الكامل",
            "PLACEHOLDER_EMAIL": "البريد الإلكتروني",
            "PLACEHOLDER_SUBJECT": "الموضوع",
            "PLACEHOLDER_MESSAGE": "الرسالة"
        },
        "PROFILE": {
            "FRENCH": "الفرنسية",
            "ENGLISH": "الإنجليزية",
            "ARABIC": "العربية",
            "CHANGE_PHOTO": "تغيير الصورة"
        }
    }
}

def update_data(data, trans):
    for k, v in trans.items():
        if isinstance(v, dict):
            if k not in data:
                data[k] = {}
            update_data(data[k], v)
        else:
            data[k] = v

for f, trans in translations.items():
    path = os.path.join(i18n_dir, f)
    with open(path, 'r', encoding='utf-8') as jf:
        data = json.load(jf)
    
    update_data(data, trans)
    
    with open(path, 'w', encoding='utf-8') as jf:
        json.dump(data, jf, indent=2, ensure_ascii=False)

print("Translations updated.")
