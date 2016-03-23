package zyx.mega.bot;

import static robocode.Rules.MAX_VELOCITY;
import static zyx.mega.util.Constants.GUN_COOLING_RATE;
import static zyx.mega.util.Constants.HALF_PI;
import static zyx.mega.util.Constants.PI_18;
import static zyx.mega.util.Constants.PI_240;

import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import zyx.mega.debug.log.Logger;
import zyx.mega.geometry.Box;
import zyx.mega.geometry.Point;
import zyx.mega.util.Constants;
import zyx.mega.util.ObjectPool;
import zyx.mega.util.WallSmoothing;
import zyx.mega.util.ZUtils;

public class Bot {
  private final Box boundingBox = Box.newInstance();
  private final Point position = Point.newInstance();
  private double heading;
  private double energy;
  private double velocity;
  private double gunHeat;

  public Box boundingBox() {
    return boundingBox;
  }

  public Point position() {
    return position;
  }

  public double heading() {
    return heading;
  }

  public double energy() {
    return energy;
  }

  public double velocity() {
    return velocity;
  }

  public double gunHeat() {
    return gunHeat;
  }

  public void copyFrom(Bot from) {
    position.copyFrom(from.position);
    heading = from.heading;
    energy = from.energy;
    velocity = from.velocity;
    gunHeat = from.gunHeat;
    updateBoundingBox();
  }

  public void update(AdvancedRobot robot) {
    position.x = robot.getX();
    position.y = robot.getY();
    heading = robot.getHeadingRadians();
    energy = robot.getEnergy();
    velocity = robot.getVelocity();
    gunHeat = robot.getGunHeat();
    updateBoundingBox();
  }

  public void updateBoundingBox() {
    boundingBox.update(position, Constants.BOT_HALF_SIZE);
  }

  public void update(ScannedRobotEvent event) {
    heading = event.getHeadingRadians();
    energy = event.getEnergy();
    velocity = event.getVelocity();
  }

  public void init() {
    gunHeat = 3.0;
    energy = 100;
  }

  void updateGunHeat(long elapsed) {
    gunHeat = Math.max(gunHeat - GUN_COOLING_RATE * elapsed, 0);
  }

  public void onBulletHit(double bulletPower) {
    energy -= Rules.getBulletDamage(bulletPower);
  }

  public void onBulletFired(double bulletPower) {
    gunHeat = Rules.getGunHeat(bulletPower) - GUN_COOLING_RATE;
  }

  public double orbit(double angleOffset, Point reference, int direction, boolean stop) {
    final double absoluteAngle = ZUtils.angle(reference, position) + angleOffset * direction;
    final double angle = WallSmoothing.smoothAngle(position, absoluteAngle, direction);
    final double maxTurn = PI_18 + PI_240 * Math.abs(velocity);
    int moveDirection = 1;
    double turn = Utils.normalRelativeAngle(angle - heading);
    if (Math.abs(turn) > HALF_PI) {
      turn = Utils.normalRelativeAngle(turn + Math.PI);
      moveDirection = -1;
    }
    //Logger.getInstance().log("[A]: %.2f %.2f %.2f %b", position.x, position.y, velocity, stop);
    heading = Utils.normalAbsoluteAngle(heading + Math.max(-maxTurn, Math.min(maxTurn, turn)));
    if (stop) {
      if (velocity > 0) {
        velocity = Math.max(0, velocity - 2);
      } else if (velocity < 0) {
        velocity = Math.min(0, velocity + 2);
      }
    } else {
      velocity += velocity * moveDirection < 0 ? 2 * moveDirection : moveDirection;
      velocity = Math.max(-MAX_VELOCITY, Math.min(MAX_VELOCITY, velocity));
    }
    position.move(heading, velocity);
    boolean hitWall = WallSmoothing.hitWall(position);
    updateBoundingBox();
    if (!hitWall) {
      //Logger.getInstance().log("[B]: %.2f %.2f %.2f %s", position.x, position.y, velocity, boundingBox.toString());
      return 0;
    }

    double wallDamage = Rules.getWallHitDamage(velocity);
    velocity = 0;
    Logger.getInstance().log("[C]: %.2f %.2f %.2f %s %.2f", position.x, position.y, velocity, boundingBox.toString(), wallDamage);
    return wallDamage;
  }

  ///// POOLED OBJECT /////
  public static Bot newInstance() {
    return newInstance(null);
  }

  public static Bot newInstance(Bot old) {
    Bot object = ObjectPool.getFromPool(Bot.class);

    if (object == null) {
      object = new Bot();
    }

    if (old != null) {
      object.copyFrom(old);
    }

    return object;
  }

  public void recycle() {
    ObjectPool.recycle(this);
  }

  private Bot() {
    init();
  }
  ///// POOLED OBJECT /////
}
