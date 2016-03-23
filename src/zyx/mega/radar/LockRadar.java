package zyx.mega.radar;

import robocode.util.Utils;
import zyx.mega.abstraction.ZRobot;
import zyx.mega.bot.Enemy;
import zyx.mega.brain.Brain;

public class LockRadar implements Radar {
  private boolean scanned;

  public void init() {
    scanned = false;
  }

  public void onScannedRobot() {
    scanned = true;
  }

  public void execute() {
    Brain brain = Brain.getInstance();
    Enemy enemy = brain.enemy();
    ZRobot robot = ZRobot.getInstance();

    if (scanned) {
      robot.setTurnRadarRightRadians(
          Utils.normalRelativeAngle(enemy.bearing() - robot.getRadarHeadingRadians()) * 1.999);
    } else {
      robot.setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
    }

    scanned = false;
  }
}
