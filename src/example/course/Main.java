package example.course;
public final class Main {
  public static void main(String[] args) throws Exception {
    String key = System.getenv("INFRAI_API_KEY");
    if (key == null || key.isBlank()) throw new IllegalArgumentException("INFRAI_API_KEY is required");
    if (args.length != 1 || args[0].isBlank()) throw new IllegalArgumentException("recipient email is required");
    InfraiClient infrai = new InfraiClient(key);
    CourseAsset asset = new CourseAsset("fractions-week-3.pdf", args[0], 1.00, 10.00, "lesson-week-3");
    CourseDeliveryService service = new CourseDeliveryService(infrai);
    CourseDeliveryService.DeliveryResult result = service.deliver(asset, infrai.balanceAmount());
    System.out.println("delivery decision: " + result.decision());
    System.out.println("delivery receipt: " + result.receipt());
    System.out.println("subscriber update queued");
  }
}
