import json
import os

i18n_dir = r'w:\pffe\PFE_final\frontend\src\assets\i18n'

legal_help_trans = {
    'fr.json': {
        "LEGAL": {
            "PRIVACY": {
                "SECTION1_DESC": "Nous collectons les informations que vous nous fournissez directement, par exemple lorsque vous créez un compte, effectuez une réservation ou nous contactez pour obtenir de l'aide.",
                "SECTION2_DESC": "Nous utilisons les informations que nous collectons pour fournir, maintenir et améliorer nos services, y compris le traitement de vos réservations et l'envoi de mises à jour de voyage.",
                "SECTION3_DESC": "Vos détails de réservation et vos informations personnelles sont traités avec une confidentialité stricte et ne sont partagés qu'avec les partenaires concernés pour faciliter vos réservations.",
                "SECTION4_DESC": "Les avis publics et les notes que vous fournissez seront visibles par les autres utilisateurs de la plateforme pour les aider à prendre des décisions de voyage éclairées.",
                "SECTION5_DESC": "Nous mettons en œuvre une variété de mesures de sécurité pour maintenir la sécurité de vos informations personnelles lorsque vous saisissez, soumettez ou accédez à vos informations personnelles."
            },
            "TERMS": {
                "SECTION1_DESC": "En accédant et en utilisant Safar Morocco, vous acceptez d'être lié par ces conditions d'utilisation et par toutes les lois et réglementations applicables.",
                "SECTION2_DESC": "La permission est accordée de télécharger temporairement une copie du matériel sur le site Web de Safar Morocco pour un visionnage transitoire personnel et non commercial uniquement.",
                "SECTION3_DESC": "Les utilisateurs sont responsables du maintien de la confidentialité de leurs comptes et de toutes les activités qui se déroulent sous leur compte.",
                "SECTION4_DESC": "Les utilisateurs peuvent publier des avis et des photos. Ce faisant, vous accordez à Safar Morocco une licence non exclusive et libre de redevance pour utiliser, reproduire et afficher ce contenu.",
                "SECTION5_DESC": "Safar Morocco peut réviser ces conditions d'utilisation de son site Web à tout moment sans préavis."
            }
        },
        "HELP": {
            "NAVIGATE": {
                "TITLE": "Comment naviguer sur la plateforme",
                "DESC": "Notre plateforme est conçue pour être intuitive. Utilisez la barre de navigation pour explorer les destinations, trouver des événements à venir ou consulter vos itinéraires personnalisés sur la carte interactive."
            },
            "ITINERARIES": {
                "TITLE": "Création d'itinéraires",
                "DESC": "Accédez à la section \"Itinéraires\" pour commencer à planifier votre voyage. Vous pouvez ajouter des destinations, définir des dates et organiser facilement vos activités quotidiennes."
            },
            "BOOKING": {
                "TITLE": "Réservation d'événements",
                "DESC": "Découvrez les festivals locaux, les ateliers et les visites dans la section \"Événements\". Sélectionnez simplement un événement et suivez le processus de réservation pour garantir votre place."
            },
            "RESERVATIONS": {
                "TITLE": "Gestion des réservations",
                "DESC": "Accédez à votre profil pour consulter et gérer toutes vos réservations actives, vos voyages passés et vos destinations enregistrées."
            }
        }
    },
    'es.json': {
        "LEGAL": {
            "PRIVACY": {
                "SECTION1_DESC": "Recopilamos la información que nos proporciona directamente, como cuando crea una cuenta, realiza una reserva o se comunica con nosotros para obtener ayuda.",
                "SECTION2_DESC": "Utilizamos la información que recopilamos para proporcionar, mantener y mejorar nuestros servicios, incluido el procesamiento de sus reservas y el envío de actualizaciones de viaje.",
                "SECTION3_DESC": "Los detalles de su reserva y su información personal se manejan con estricta confidencialidad y solo se comparten con los socios relevantes para facilitar sus reservas.",
                "SECTION4_DESC": "Las reseñas y calificaciones públicas que proporcione serán visibles para otros usuarios de la plataforma para ayudarlos a tomar decisiones de viaje informadas.",
                "SECTION5_DESC": "Implementamos una variedad de medidas de seguridad para mantener la seguridad de su información personal cuando ingresa, envía o accede a su información personal."
            },
            "TERMS": {
                "SECTION1_DESC": "Al acceder y utilizar Safar Morocco, usted acepta estar sujeto a estos Términos de servicio y a todas las leyes y regulaciones aplicables.",
                "SECTION2_DESC": "Se concede permiso para descargar temporalmente una copia de los materiales en el sitio web de Safar Morocco únicamente para visualización transitoria personal y no comercial.",
                "SECTION3_DESC": "Los usuarios son responsables de mantener la confidencialidad de sus cuentas y de todas las actividades que ocurran bajo su cuenta.",
                "SECTION4_DESC": "Los usuarios pueden publicar reseñas y fotos. Al hacerlo, le otorga a Safar Morocco una licencia no exclusiva y libre de regalías para usar, reproducir y mostrar dicho contenido.",
                "SECTION5_DESC": "Safar Morocco puede revisar estos términos de servicio para su sitio web en cualquier momento sin previo aviso."
            }
        },
        "HELP": {
            "NAVIGATE": {
                "TITLE": "Cómo navegar por la plataforma",
                "DESC": "Nuestra plataforma está diseñada para ser intuitiva. Utilice la barra de navegación para explorar destinos, buscar próximos eventos o ver sus itinerarios personalizados en el mapa interactivo."
            },
            "ITINERARIES": {
                "TITLE": "Creación de itinerarios",
                "DESC": "Vaya a la sección \"Itinerarios\" para comenzar a planificar su viaje. Puede agregar destinos, establecer fechas y organizar sus actividades diarias fácilmente."
            },
            "BOOKING": {
                "TITLE": "Reserva de eventos",
                "DESC": "Descubra festivales locales, talleres y recorridos en la sección \"Eventos\". Simplemente seleccione un evento y siga el proceso de reserva para asegurar su lugar."
            },
            "RESERVATIONS": {
                "TITLE": "Gestión de reservas",
                "DESC": "Acceda a su perfil para ver y gestionar todas sus reservas activas, viajes pasados y destinos guardados."
            }
        }
    },
    'ar.json': {
        "LEGAL": {
            "PRIVACY": {
                "SECTION1_DESC": "نحن نجمع المعلومات التي تقدمها لنا مباشرة، مثل عند إنشاء حساب، أو إجراء حجز، أو الاتصال بنا للحصول على الدعم.",
                "SECTION2_DESC": "نستخدم المعلومات التي نجمعها لتوفير خدماتنا وصيانتها وتحسينها، بما في ذلك معالجة حجوزاتك وإرسال تحديثات السفر إليك.",
                "SECTION3_DESC": "يتم التعامل مع تفاصيل حجزك ومعلوماتك الشخصية بسرية تامة ولا يتم مشاركتها إلا مع الشركاء ذوي الصلة لتسهيل حجوزاتك.",
                "SECTION4_DESC": "المراجعات والتقييمات العامة التي تقدمها ستكون مرئية للمستخدمين الآخرين للمنصة لمساعدتهم في اتخاذ قرارات سفر مدروسة.",
                "SECTION5_DESC": "نحن نطبق مجموعة متنوعة من التدابير الأمنية للحفاظ على سلامة معلوماتك الشخصية عند إدخال معلوماتك الشخصية أو إرسالها أو الوصول إليها."
            },
            "TERMS": {
                "SECTION1_DESC": "من خلال الوصول إلى سفر المغرب واستخدامه، فإنك توافق على الالتزام بشروط الخدمة هذه وجميع القوانين واللوائح المعمول بها.",
                "SECTION2_DESC": "يُمنح الإذن لتنزيل نسخة واحدة مؤقتًا من المواد الموجودة على موقع سفر المغرب للعرض الانتقالي الشخصي وغير التجاري فقط.",
                "SECTION3_DESC": "يتحمل المستخدمون مسؤولية الحفاظ على سرية حساباتهم وعن جميع الأنشطة التي تحدث تحت حساباتهم.",
                "SECTION4_DESC": "يجوز للمستخدمين نشر المراجعات والصور. من خلال القيام بذلك، فإنك تمنح سفر المغرب ترخيصًا غير حصري وخالٍ من حقوق الملكية لاستخدام هذا المحتوى وإعادة إنتاجه وعرضه.",
                "SECTION5_DESC": "قد يقوم سفر المغرب بمراجعة شروط الخدمة هذه لموقعه على الويب في أي وقت دون إشعار مسبق."
            }
        },
        "HELP": {
            "NAVIGATE": {
                "TITLE": "كيفية التنقل في المنصة",
                "DESC": "منصتنا مصممة لتكون بديهية. استخدم شريط التنقل لاستكشاف الوجهات، أو العثور على الفعاليات القادمة، أو عرض مسارات رحلاتك المخصصة على الخريطة التفاعلية."
            },
            "ITINERARIES": {
                "TITLE": "إنشاء مسارات الرحلة",
                "DESC": "انتقل إلى قسم \"مسارات الرحلة\" لبدء التخطيط لرحلتك. يمكنك إضافة وجهات وتحديد المواعيد وتنظيم أنشطتك اليومية بسهولة."
            },
            "BOOKING": {
                "TITLE": "حجز الفعاليات",
                "DESC": "اكتشف المهرجانات المحلية وورش العمل والجولات في قسم \"الفعاليات\". ما عليك سوى اختيار فعالية واتباع عملية الحجز لتأمين مكانك."
            },
            "RESERVATIONS": {
                "TITLE": "إدارة الحجوزات",
                "DESC": "ادخل إلى ملفك الشخصي لعرض وإدارة جميع حجوزاتك النشطة ورحلاتك السابقة ووجهاتك المحفوظة."
            }
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

for f, trans in legal_help_trans.items():
    path = os.path.join(i18n_dir, f)
    with open(path, 'r', encoding='utf-8') as jf:
        data = json.load(jf)
    
    update_data(data, trans)
    
    with open(path, 'w', encoding='utf-8') as jf:
        json.dump(data, jf, indent=2, ensure_ascii=False)

print("Legal and Help translations updated.")
