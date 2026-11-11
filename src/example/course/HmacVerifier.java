package example.course;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
public final class HmacVerifier {
  private HmacVerifier() {}
  public static boolean matches(String payload, String signature, String secret) {
    try { Mac mac = Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256")); return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8))).equalsIgnoreCase(signature); }
    catch (Exception e) { return false; }
  }
}
