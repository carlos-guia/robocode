package zyx.mega.brain;

import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import zyx.mega.abstraction.Executable;
import zyx.mega.abstraction.ZRobot;
import zyx.mega.bot.Bot;
import zyx.mega.bot.Enemy;
import zyx.mega.gun.Gun;
import zyx.mega.movement.Movement;
import zyx.mega.radar.Radar;
import zyx.mega.time.TimeManager;

public class Brain implements Executable {
  private static Brain instance = new Brain();

  public static Brain getInstance() {
    return instance;
  }

  private final Bot me;
  private final Bot meOneAgo;
  private final Bot meTwoAgo;
  private Enemy enemy;
  private long lastScan;

  private final Radar radar;
  private final Movement movement;
  private final Gun gun;

  private Brain() {
    me = Bot.newInstance();
    meOneAgo = Bot.newInstance();
    meTwoAgo = Bot.newInstance();

    ZRobot robot = ZRobot.getInstance();
    radar = robot.getRadar();
    movement = robot.getMovement();
    gun = robot.getGun();
  }

  public void init() {
    if (enemy != null) {
      enemy.recycle();
      enemy = null;
    }

    radar.init();
    movement.init();
    gun.init();
    lastScan = 0;
  }

  public void update() {
    meTwoAgo.copyFrom(meOneAgo);
    meOneAgo.copyFrom(me);
    me.update(ZRobot.getInstance());
  }

  public void onBulletHit(BulletHitEvent event) {
    if (enemy != null) {
      enemy.bot().onBulletHit(event.getBullet().getPower());
    }
  }

  public void onHitByBullet(HitByBulletEvent event) {
    movement.onHitByBullet(event.getBullet());
  }

  public void onBulletHitBullet(BulletHitBulletEvent event) {
    boolean bothAreMine = event.getHitBullet().getName().equals(ZRobot.getInstance().getName());

    if (!bothAreMine) {
      movement.onBulletHitBullet(event.getHitBullet());
    }
  }

  public void onScannedRobot(ScannedRobotEvent event) {
    Enemy old = enemy;
    enemy = Enemy.newInstance(old);
    long elapsed = TimeManager.getInstance().roundTime() - lastScan;
    lastScan = TimeManager.getInstance().roundTime();
    enemy.update(me, event, elapsed);

    radar.onScannedRobot();

    //Logger.getInstance().log("gunHeat: %.2f", enemy.bot().gunHeat());

    if (old != null) {
      double bulletPower = getBulletPower(enemy, old);
      if (bulletPower != 0) {
        enemy.bot().onBulletFired(bulletPower);
        movement.onEnemyBulletFired(meTwoAgo, old, bulletPower);
      }
      old.recycle();
    }
  }

  private static double getBulletPower(Enemy now, Enemy oneAgo) {
    double energyDrop = oneAgo.bot().energy() - now.bot().energy();
    //Logger.getInstance().log("energyDrop: %.2f", energyDrop);
    if (oneAgo.bot().gunHeat() > ZRobot.EPSILON || energyDrop == 0) {
      return 0;
    }

    if (energyDrop < Rules.MIN_BULLET_POWER
        && oneAgo.bot().energy() - ZRobot.EPSILON > Rules.MIN_BULLET_POWER) {
      return 0;
    }

    return Math.min(energyDrop, Rules.MAX_BULLET_POWER);
  }

  public void execute() {
    radar.execute();
    movement.execute();
    gun.execute();
  }

  public Enemy enemy() {
    return enemy;
  }

  public Bot me() {
    return me;
  }
}
