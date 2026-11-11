package example.course;
public final class CourseDeliveryServiceTest {
  public static void main(String[] args) {
    CourseDeliveryService service = new CourseDeliveryService(null);
    CourseAsset asset = new CourseAsset("lesson", "student@example.edu", 1.00, 5.00, "test-event");
    check(service.decide(0.25, asset.triggerBalance()) == CourseDeliveryService.Decision.RECHARGE_THEN_DELIVER);
    check(service.decide(2.00, asset.triggerBalance()) == CourseDeliveryService.Decision.DELIVER);
    System.out.println("course delivery decision test passed");
  }
  private static void check(boolean value) { if (!value) throw new AssertionError("unexpected delivery decision"); }
}
