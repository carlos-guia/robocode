package zyx.mega.abstraction;

import static zyx.mega.util.Constants.HALF_PI;
import static zyx.mega.util.Constants.PI;

import robocode.AdvancedRobot;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import zyx.mega.brain.Brain;
import zyx.mega.debug.log.Logger;
import zyx.mega.debug.paint.PaintManager;
import zyx.mega.gun.Gun;
import zyx.mega.movement.Movement;
import zyx.mega.radar.Radar;
import zyx.mega.time.TimeManager;

import java.awt.Graphics2D;

public abstract class ZRobot extends AdvancedRobot {
  public static final double EPSILON = 1e-5;

  public abstract Radar getRadar();
  public abstract Gun getGun();
  public abstract Movement getMovement();

  private static ZRobot instance;
  public static ZRobot getInstance() {
    return instance;
  }

  private TimeManager timeManager;
  private Brain brain;
  private Graphics2D g;

  @Override
  public void run() {
    init();

    while (true) {
      update();
      execute();
    }
  }

  @Override
  public void execute() {
    brain.execute();
    Logger.getInstance().execute();

    if (g != null) {
      PaintManager.getInstance().flush(g);
      g = null;
    }

    super.execute();
  }

  private void init() {
    instance = this;
    timeManager = TimeManager.getInstance();
    brain = Brain.getInstance();

    timeManager.init();
    brain.init();
    Logger.getInstance().init();
    setAdjustGunForRobotTurn(true);
    setAdjustRadarForGunTurn(true);
  }

  private void update() {
    if (!timeManager.update()) {
      return;
    }

    brain.update();
  }

  @Override
  public void onBulletHit(BulletHitEvent event) {
    update();
    brain.onBulletHit(event);
  }

  @Override
  public void onHitByBullet(HitByBulletEvent event) {
    update();
    brain.onHitByBullet(event);
  }

  @Override
  public void onBulletHitBullet(BulletHitBulletEvent event) {
    update();
    brain.onBulletHitBullet(event);
  }

  @Override
  public void onScannedRobot(ScannedRobotEvent event) {
    update();
    brain.onScannedRobot(event);
  }

  @Override
  public void onPaint(Graphics2D g) {
    this.g = g;
  }

  public void move(double distance, double angle) {
    angle = Utils.normalRelativeAngle(angle - getHeadingRadians());
    if ( Math.abs(angle) > HALF_PI ) {
      angle = Utils.normalRelativeAngle(angle + PI);
      distance = -distance;
    }
    setTurnRightRadians(angle);
    setAhead(distance);
  }
}
