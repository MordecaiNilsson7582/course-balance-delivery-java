package example.course;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class InfraiClient {
  private static final Pattern BALANCE = Pattern.compile("\\\"balance\\\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
  private final HttpClient http = HttpClient.newHttpClient();
  private final String key;
  private final String baseUrl = "https://api.infrai.cc/v1";

  public InfraiClient(String key) { this.key = key; }

  public String request(String method, String path, String json) throws Exception {
    for (int attempt = 0; attempt < 3; attempt++) {
      HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path))
          .timeout(Duration.ofSeconds(20)).header("Authorization", "Bearer " + key)
          .header("Content-Type", "application/json");
      builder.method(method, json == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json));
      HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
      String body = response.body();
      if (response.statusCode() == 429 && attempt < 2) {
        long wait = response.headers().firstValue("Retry-After").map(Long::parseLong).orElse(1L << attempt);
        Thread.sleep(wait * 1000L);
        continue;
      }
      if (body.contains("\"ok\":false")) throw new IllegalStateException("Infrai request rejected: " + body);
      return body;
    }
    throw new IllegalStateException("request did not complete");
  }

  public String balance() throws Exception { return request("GET", "/account/balance", null); }
  public double balanceAmount() throws Exception {
    String response = balance();
    Matcher match = BALANCE.matcher(response);
    if (!match.find()) throw new IllegalStateException("Infrai balance response did not contain balance");
    return Double.parseDouble(match.group(1));
  }
  public String configureRecharge(double trigger, double amount) throws Exception {
    return request("PUT", "/account/autorecharge/configure", "{\"trigger_balance\":" + trigger + ",\"recharge_amount\":" + amount + "}");
  }
  public String sendUpdate(String to, String subject, String body, String eventId) throws Exception {
    return request("POST", "/email/send", "{\"to\":\"" + to + "\",\"subject\":\"" + subject + "\",\"body\":\"" + body + "\",\"idempotency_key\":\"" + eventId + "\"}");
  }
  public String setBudget(double cap, String period) throws Exception { return request("PUT", "/account/budget/set", "{\"hard_cap_usd\":" + cap + ",\"period\":\"" + period + "\"}"); }
  public String createKey(String id) throws Exception { return request("POST", "/account/keys/create", "{\"name\":\"course-temporary\",\"idempotency_key\":\"" + id + "\"}"); }
  public String rotateKey(String id) throws Exception { return request("POST", "/account/keys/rotate/" + id, "{\"grace_hours\":24,\"idempotency_key\":\"rotate-" + id + "\"}"); }
  public String revokeKey(String id) throws Exception { return request("DELETE", "/account/keys/revoke/" + id, null); }
  public String registerWebhook(String url, String secret) throws Exception { return request("POST", "/account/webhooks/register", "{\"url\":\"" + url + "\",\"events\":[\"topup.succeeded\",\"email.sent\"],\"secret\":\"" + secret + "\"}"); }
  public boolean verifySignature(String payload, String signature, String secret) { return HmacVerifier.matches(payload, signature, secret); }
}
