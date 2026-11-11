package example.course;
public final class CourseDeliveryService {
  public enum Decision { RECHARGE_THEN_DELIVER, DELIVER }
  public record DeliveryResult(Decision decision, String receipt) {}
  private final InfraiClient infrai;
  public CourseDeliveryService(InfraiClient infrai) { this.infrai = infrai; }
  public Decision decide(double balance, double trigger) { return balance <= trigger ? Decision.RECHARGE_THEN_DELIVER : Decision.DELIVER; }
  public DeliveryResult deliver(CourseAsset asset, double balance) throws Exception {
    Decision decision = decide(balance, asset.triggerBalance());
    if (decision == Decision.RECHARGE_THEN_DELIVER) infrai.configureRecharge(asset.triggerBalance(), asset.rechargeAmount());
    String receipt = infrai.sendUpdate(asset.subscriber(), "Your next lesson is ready", "Digital asset: " + asset.name(), asset.eventId());
    return new DeliveryResult(decision, receipt);
  }
}
