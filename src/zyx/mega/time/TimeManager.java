package zyx.mega.time;

import robocode.AdvancedRobot;
import zyx.mega.abstraction.ZRobot;

public class TimeManager {
  private static TimeManager instance = new TimeManager();
  public static TimeManager getInstance() {
    return instance;
  }

  private TimeManager() {}

  private int trueTime = -1;
  private int round = -1;
  private long time = -1;

  public boolean update() {
    AdvancedRobot robot = ZRobot.getInstance();

    if (round == robot.getRoundNum() && time == robot.getTime()) {
      return false;
    }

    ++trueTime;
    round = robot.getRoundNum();
    time = robot.getTime();

    return true;
  }

  public int time() {
    return trueTime;
  }

  public void init() {
  }

  public long roundTime() {
    return time;
  }

  public int round() {
    return round;
  }
}
