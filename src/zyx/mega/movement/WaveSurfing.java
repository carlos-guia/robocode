package zyx.mega.movement;

import robocode.Bullet;
import robocode.Rules;
import zyx.mega.abstraction.ZRobot;
import zyx.mega.bot.Bot;
import zyx.mega.bot.Enemy;
import zyx.mega.brain.Brain;
import zyx.mega.debug.log.Logger;
import zyx.mega.debug.paint.PaintManager;
import zyx.mega.geometry.Point;
import zyx.mega.time.TimeManager;
import zyx.mega.util.Constants;
import zyx.mega.util.WallSmoothing;
import zyx.mega.util.ZUtils;
import zyx.mega.wave.HitWindow;
import zyx.mega.wave.Wave;

import java.awt.Color;
import java.util.ArrayList;

public class WaveSurfing implements Movement {
  private static final int MAX_TICKS = 150;
  private static final double FLAT_WEIGHT = 0.0;

  private static final double TARGET_DISTANCE = 120;
  private static final double TARGET_DISTANCE_SQ = TARGET_DISTANCE * TARGET_DISTANCE;

  private static final int BINS = 47;
  private static final int BINS_HALF = (BINS - 1) / 2;
  private final double stats[] = new double[BINS];
  private double stats_total = 0;

  private final SurfResult leftResult;
  private final SurfResult rightResult;
  private final SurfResult secondLeftResult;
  private final SurfResult secondRightResult;
  private final ArrayList<Wave> waves;
  private final Wave fakeWave = Wave.newInstance();

  private Wave mainSurfWave;
  private Wave secondSurfWave;
  private SurfResult currentSurfResults;

  @SuppressWarnings("unchecked")
  public WaveSurfing() {
    waves = new ArrayList<Wave>();
    leftResult = new SurfResult(-1);
    rightResult = new SurfResult(1);
    secondLeftResult = new SurfResult(-1);
    secondRightResult = new SurfResult(1);
    fakeWave.bulletPower = Rules.MAX_BULLET_POWER;
    fakeWave.velocity = Rules.getBulletSpeed(Rules.MAX_BULLET_POWER);
    fakeWave.direction = 1;
    fakeWave.maximumEscapeAngle = Math.asin(Rules.MAX_VELOCITY / fakeWave.velocity);
    logHit(0.0, 1 + FLAT_WEIGHT);
  }

  public void init() {
    waves.clear();
    currentSurfResults = null;
  }

  public void execute() {
    updateWaves();

    if (mainSurfWave != null && mainSurfWave != fakeWave) {
      PaintManager.getInstance().paintCircle(mainSurfWave.firePosition,
          mainSurfWave.distanceTraveled, Color.RED);
    }

    if (secondSurfWave != null && secondSurfWave != fakeWave) {
      PaintManager.getInstance().paintCircle(secondSurfWave.firePosition,
          secondSurfWave.distanceTraveled, Color.BLUE);
    }

    if (currentSurfResults == null && mainSurfWave != null) {
      Logger.getInstance().log("Surf %b %b", mainSurfWave == fakeWave, secondSurfWave == fakeWave);
      surf();
    }

    move();
  }

  private void move() {
    if (mainSurfWave == null) {
      return;
    }

    Bot me = Brain.getInstance().me();
    double distanceSq = ZUtils.distanceSq(mainSurfWave.firePosition, me.position());
    double absoluteAngle = ZUtils.angle(mainSurfWave.firePosition, me.position())
        + orbitAngleOffsetSq(distanceSq) * currentSurfResults.direction;
    final double angle =
        WallSmoothing.smoothAngle(me.position(), absoluteAngle, currentSurfResults.direction);

    double distance = --currentSurfResults.ticks < 0 ? 0 : 120;
    ZRobot.getInstance().move(distance, angle);
  }

  private void updateWaves() {
    long time = TimeManager.getInstance().time();
    Bot me = Brain.getInstance().me();
    boolean updated = false;

    for (int i = waves.size() - 1; i >= 0; --i) {
      Wave wave = waves.get(i);
      wave.update(time);

      PaintManager.getInstance().paintCircle(wave.firePosition, wave.distanceTraveled, Color.WHITE);

      if (wave.hit(me.boundingBox(), wave.flattenerWindow)) {
        int low = getIndex(wave.flattenerWindow.low());
        int high = getIndex(wave.flattenerWindow.high());
        while (low <= high) {
          logHit(low++, FLAT_WEIGHT);
        }

        waves.remove(i);
        wave.recycle();
        updateSurfWaves();
        updated = true;
        continue;
      }
    }

    if (waves.size() < 2 && Brain.getInstance().enemy() != null) {
      Enemy enemy = Brain.getInstance().enemy();
      double bearing = ZUtils.angle(me.position(), enemy.bot().position());

      int timeToFire = 0;
      if (enemy.bot().gunHeat() > 0) {
        timeToFire = (int) (enemy.bot().gunHeat() / Constants.GUN_COOLING_RATE) + 2;
      }

      fakeWave.time = TimeManager.getInstance().time() - 1 + timeToFire;
      fakeWave.firePosition.copyFrom(enemy.bot().position());
      fakeWave.bearing = bearing + Math.PI;
      if (enemy.bot().energy() > 0 && waves.isEmpty()
          && ZRobot.getInstance().getDistanceRemaining() == 0) {
        Logger.getInstance().log("eg: %.9f", enemy.bot().energy());
        currentSurfResults = null;
        if (!updated) {
          updateSurfWaves();
        }
      }
    }
  }

  private void updateResult(SurfResult result, double danger, int ticks) {
    String which = "";
    if (result == leftResult) {
      which += "Left";
    }
    if (result == rightResult) {
      which += "Right";
    }
    if (result == secondLeftResult) {
      which += "Second left";
    }
    if (result == secondRightResult) {
      which += "Second right";
    }
    Logger.getInstance().log("[%s] %.2f %.2f %d", which, result.danger, danger, ticks);
    if (danger < result.danger) {
      result.danger = danger;
      result.ticks = ticks;
    }
  }

  private double danger(Wave wave, Point position, HitWindow window, double wallDamage,
      double secondDanger) {
    if (wallDamage != 0) {
      Logger.getInstance().log("WD: " + wallDamage);
    }
    double distanceSq = ZUtils.distanceSq(wave.firePosition, position);
    double closenessDanger =
        distanceSq < TARGET_DISTANCE_SQ ? 1 - distanceSq / TARGET_DISTANCE_SQ : 0;
    Logger.getInstance().log("danger: %s [%.2f, %.2f] %s %.2f %.2f %.2f",
        wave == mainSurfWave ? "MAIN" : "SECOND",
        position.x, position.y,
        window.empty() ? "EMPTY" : String.format("%.2f %.2f", window.low(), window.high()),
        wallDamage, secondDanger, closenessDanger);
    if (stats_total == 0 || window.empty()) {
      return wallDamage + closenessDanger + secondDanger;
    }

    int low = getIndex(window.low());
    int high = getIndex(window.high());
    double danger = closenessDanger * 0.1;
    while (low <= high) {
      danger += stats[low] / stats_total * Rules.getBulletDamage(wave.bulletPower) + wallDamage;
      ++low;
    }

    return danger + secondDanger;
  }

  private static int getIndex(double factor) {
    return (int) (Math.max(-1, Math.min(1, factor)) * BINS_HALF + BINS_HALF);
  }

  private static class SurfResult {
    final int direction;
    int ticks;
    double danger;

    SurfResult(int direction) {
      this.direction = direction;
    }

    void init() {
      danger = Double.POSITIVE_INFINITY;
      ticks = 0;
    }
  }

  private void logHit(Wave wave, Point where, double weight) {
    logHit(wave.getGuessFactor(where), weight);
  }

  private void logHit(double factor, double weight) {
    logHit(getIndex(factor), weight);
  }

  private void logHit(int index, double weight) {
    stats_total += weight;
    stats[index] += weight;
  }

  public void onHitByBullet(Bullet bullet) {
    removeWaveFromBullet(bullet, true);
  }

  public void onBulletHitBullet(Bullet bullet) {
    removeWaveFromBullet(bullet, false);
  }

  /**
   * @param realHit
   */
  public void removeWaveFromBullet(Bullet bullet, boolean realHit) {
    long time = TimeManager.getInstance().time();
    Point where = Point.newInstance();
    where.x = bullet.getX();
    where.y = bullet.getY();
    for (Wave wave : waves) {
      wave.update(time);
      if (Math.abs(bullet.getPower() - wave.bulletPower) < 1e-5
          && Math.abs(ZUtils.distanceSq(wave.firePosition, where)
              - wave.distanceTraveled * wave.distanceTraveled) < 18 * 18) {
        logHit(wave, where, 1);
        waves.remove(wave);
        wave.recycle();
        updateSurfWaves();
        break;
      }
    }
  }

  public void onEnemyBulletFired(Bot me, Enemy enemy, double bulletPower) {
    double bearing = ZUtils.angle(me.position(), enemy.bot().position());
    double lateralVelocity = me.velocity() * Math.sin(enemy.bot().heading() - bearing);

    Wave wave = Wave.newInstance();
    wave.time = TimeManager.getInstance().time() - 1;
    wave.firePosition.x = enemy.bot().position().x;
    wave.firePosition.y = enemy.bot().position().y;
    wave.bearing = bearing + Math.PI;
    wave.bulletPower = bulletPower;
    wave.velocity = Rules.getBulletSpeed(bulletPower);
    wave.direction = lateralVelocity >= 0 ? 1 : -1;
    wave.maximumEscapeAngle = Math.asin(Rules.MAX_VELOCITY / wave.velocity);
    wave.createTargets();
    waves.add(wave);

    updateSurfWaves();
  }

  private void updateSurfWaves() {
    Bot me = Brain.getInstance().me();
    Wave newMainSurfWave = fakeWave;
    Wave newSecondSurfWave = null;
    int mainTime = Integer.MAX_VALUE;
    int secondTime = Integer.MAX_VALUE;
    for (Wave wave : waves) {
      int time = wave.timeToPass(me.boundingBox());
      Logger.getInstance().log("usw: %d %d [%d] [%d]", wave.time, time, mainTime, secondTime);
      if (time < mainTime) {
        secondTime = mainTime;
        newSecondSurfWave = newMainSurfWave;
        mainTime = time;
        newMainSurfWave = wave;
      } else if (time < secondTime) {
        secondTime = time;
        newSecondSurfWave = wave;
      }
    }

    if (newMainSurfWave != mainSurfWave || newSecondSurfWave != secondSurfWave
        || newMainSurfWave == fakeWave || newSecondSurfWave == fakeWave) {
      mainSurfWave = newMainSurfWave;
      secondSurfWave = newSecondSurfWave;
      currentSurfResults = null;
    }
  }

  private void surf() {
    Bot me = Brain.getInstance().me();
    int time = TimeManager.getInstance().time();

    Logger.getInstance().log("SurfLEFT: %.2f %.2f", me.position().x, me.position().y);
    surf(time, me, mainSurfWave, leftResult);
    Logger.getInstance().log("SurfRIGHT: %.2f %.2f", me.position().x, me.position().y);
    surf(time, me, mainSurfWave, rightResult);

    Logger.getInstance().log("SurfResult.LEFT : %.2f [%d]", leftResult.danger, leftResult.ticks);
    Logger.getInstance().log("SurfResult.RIGHT: %.2f [%d]", rightResult.danger, rightResult.ticks);

    currentSurfResults = leftResult.danger < rightResult.danger ? leftResult : rightResult;
  }

  private void surf(int time, Bot initialMe, Wave wave, SurfResult result) {
    Bot me = Bot.newInstance(initialMe);
    HitWindow window = HitWindow.newInstance();

    Logger.getInstance().log("surf: %s (%d) [%.2f %.2f] %s",
        wave == mainSurfWave ? "MAIN" : "SECOND",
        time,
        me.position().x, me.position().y,
        window.empty() ? "EMPTY" : String.format("%.2f %.2f", window.low(), window.high()));

    double wallDamage = 0;
    result.init();

    final int max_ticks = wave == secondSurfWave ? MAX_TICKS / 2 : MAX_TICKS;
    int ticks = 0;

    for (; ticks < max_ticks; ++ticks, ++time) {
      if (wave != fakeWave) {
        stopSurf(ticks, time, me, wave, result, wallDamage, window);
      }

      wave.update(time);
      if (wave.hit(me.boundingBox(), window)) {
        break;
      }

      double distanceSq = ZUtils.distanceSq(wave.firePosition, me.position());
      wallDamage +=
          me.orbit(orbitAngleOffsetSq(distanceSq), wave.firePosition, result.direction, false);
    }


    double secondDanger = 0;
    if (shouldSurfSecond(wave)) {
      surf(time, me, secondSurfWave, secondLeftResult);
      surf(time, me, secondSurfWave, secondRightResult);
      secondDanger = Math.min(secondLeftResult.danger, secondRightResult.danger);
    }
    updateResult(result, danger(wave, me.position(), window, wallDamage, secondDanger), ticks + 1);

    window.recycle();
    me.recycle();
  }

  private void stopSurf(int ticks, int time, Bot initialMe, Wave wave, SurfResult result,
      double wallDamage, HitWindow initialWindow) {
    Bot me = Bot.newInstance(initialMe);
    HitWindow window = HitWindow.newInstance(initialWindow);

    Logger.getInstance().log("stopSurf: %s (%d) (%d) [%.2f %.2f] %.2f %s",
        wave == mainSurfWave ? "MAIN" : "SECOND",
        ticks, time,
        me.position().x, me.position().y,
        wallDamage,
        window.empty() ? "EMPTY" : String.format("%.2f %.2f", window.low(), window.high()));
    for (; me.velocity() != 0; ++time) {
      wave.update(time);
      if (wave.hit(me.boundingBox(), window)) {
        break;
      }
      double distanceSq = ZUtils.distanceSq(wave.firePosition, me.position());
      wallDamage +=
          me.orbit(orbitAngleOffsetSq(distanceSq), wave.firePosition, result.direction, true);
    }
    if (me.velocity() == 0) {
      for (Point corner : me.boundingBox().corners()) {
        window.update(wave.getGuessFactor(corner));
      }
      double secondDanger = 0;
      if (shouldSurfSecond(wave)) {
        int timeToHit = wave.timeToPass(me.boundingBox());
        surf(timeToHit, me, secondSurfWave, secondLeftResult);
        surf(timeToHit, me, secondSurfWave, secondRightResult);
        secondDanger = Math.min(secondLeftResult.danger, secondRightResult.danger);
      }
      updateResult(result, danger(wave, me.position(), window, wallDamage, secondDanger), ticks);
    }

    window.recycle();
    me.recycle();
  }

  private boolean shouldSurfSecond(Wave wave) {
    return wave != secondSurfWave && secondSurfWave != fakeWave && secondSurfWave != null;
  }

  private double orbitAngleOffsetSq(double distanceSq) {
    return Math.PI * ZUtils.sigmoid(distanceSq / TARGET_DISTANCE_SQ - 1);
  }
}
