package zyx.mega.bot;

import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import zyx.mega.util.ObjectPool;

public class Enemy {
  private final Bot bot = Bot.newInstance();
  private double distance;
  private double bearing;

  public Bot bot() {
    return bot;
  }

  public double distance() {
    return distance;
  }

  public double bearing() {
    return bearing;
  }

  public void copyFrom(Enemy from) {
    bot.copyFrom(from.bot);
    distance = from.distance;
  }

  public void update(Bot me, ScannedRobotEvent event, long elapsed) {
    bearing = Utils.normalAbsoluteAngle(me.heading() + event.getBearingRadians());
    distance = event.getDistance();

    bot.update(event);
    bot.position().setProjected(me.position(), bearing, distance);
    bot.updateGunHeat(elapsed);
    bot.updateBoundingBox();
  }

  ///// POOLED OBJECT /////
  public static Enemy newInstance(Enemy old) {
    Enemy object = ObjectPool.getFromPool(Enemy.class);

    if (object == null) {
      object = new Enemy();
    }

    if (old != null) {
      object.copyFrom(old);
    }

    return object;
  }

  public void recycle() {
    ObjectPool.recycle(this);
  }

  private Enemy() {}
  ///// POOLED OBJECT /////
}
