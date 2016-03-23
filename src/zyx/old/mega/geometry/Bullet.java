package zyx.old.mega.geometry;


public class Bullet extends Point {
  public double fire_power_;
  public double velocity_;
  public Bullet(robocode.Bullet bullet) {
    super(bullet.getX(), bullet.getY());
    fire_power_ = bullet.getPower();
    velocity_ = bullet.getVelocity();
  }
}
