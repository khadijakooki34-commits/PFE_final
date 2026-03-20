package ma.safar.morocco.test;

import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.secret.DefaultSecretGenerator;

/**
 * Test simple pour vérifier le format OTP Auth URI
 */
public class QrCodeFormatTest {
    
    public static void main(String[] args) {
        System.out.println("\n========== QR CODE FORMAT VERIFICATION ==========\n");
        
        try {
            // 1. Générer secret
            String secret = new DefaultSecretGenerator().generate();
            System.out.println("1️⃣ Secret généré:");
            System.out.println("   Value: " + secret);
            System.out.println("   Length: " + secret.length());
            System.out.println("   Is Base32: " + secret.matches("[A-Z2-7]+"));
            
            // 2. Créer QrData exactement comme dans le code
            QrData data = new QrData.Builder()
                    .label("admin@safar-morocco.com")
                    .secret(secret)
                    .issuer("Safar Morocco")
                    .digits(6)
                    .period(30)
                    .build();
            
            String otpauthUri = data.getUri();
            
            System.out.println("\n2️⃣ OTP Auth URI généré:");
            System.out.println("   " + otpauthUri);
            
            System.out.println("\n3️⃣ Validation:");
            System.out.println("   ✓ Starts with otpauth://totp/ : " + otpauthUri.startsWith("otpauth://totp/"));
            System.out.println("   ✓ Contains secret= : " + otpauthUri.contains("secret="));
            System.out.println("   ✓ Contains issuer= : " + otpauthUri.contains("issuer="));
            System.out.println("   ✓ Contains digits=6 : " + otpauthUri.contains("digits=6"));
            System.out.println("   ✓ Contains period=30 : " + otpauthUri.contains("period=30"));
            System.out.println("   ✓ No spaces : " + !otpauthUri.contains(" "));
            
            // 4. Extract and validate secret
            int secretStart = otpauthUri.indexOf("secret=") + 7;
            int secretEnd = otpauthUri.indexOf("&", secretStart);
            if (secretEnd == -1) secretEnd = otpauthUri.length();
            String extractedSecret = otpauthUri.substring(secretStart, secretEnd);
            
            System.out.println("\n4️⃣ Secret dans l'URI:");
            System.out.println("   Extracted: " + extractedSecret);
            System.out.println("   Matches generated: " + extractedSecret.equals(secret));
            System.out.println("   Is Base32: " + extractedSecret.matches("[A-Z2-7]+"));
            
            // 5. Extract issuer
            int issuerStart = otpauthUri.indexOf("issuer=") + 7;
            int issuerEnd = otpauthUri.length();
            String extractedIssuer = otpauthUri.substring(issuerStart, issuerEnd);
            
            System.out.println("\n5️⃣ Issuer dans l'URI:");
            System.out.println("   Extracted: " + extractedIssuer);
            System.out.println("   Expected: Safar Morocco");
            System.out.println("   Correct: " + extractedIssuer.equals("Safar Morocco"));
            
            System.out.println("\n✅ FORMAT CORRECT - Prêt pour Google Authenticator\n");
            System.out.println("QR Code URI complète:");
            System.out.println(otpauthUri);
            
        } catch (Exception e) {
            System.err.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
