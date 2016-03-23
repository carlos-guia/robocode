package zyx.mega.util;

import static zyx.mega.util.Constants.BOT_HALF_SIZE;
import static zyx.mega.util.Constants.BOT_SIZE;

import zyx.mega.geometry.Point;
import zyx.mega.geometry.Rectangle;

public class WallSmoothing {
  private static final boolean ENABLED = true;
  private static double WALL_STICK = 140;
  private static double WIDTH = 800;
  private static double HEIGHT = 600;
  private static Rectangle FIELD =
      new Rectangle(BOT_HALF_SIZE, BOT_HALF_SIZE, WIDTH - BOT_SIZE, HEIGHT - BOT_SIZE);
  private static int SMOOTH_TOWARDS_ENEMY = 1;

  private WallSmoothing() {}

  public static double smoothAngle(Point start, double angle, int direction) {
    if (!ENABLED) {
      return angle;
    }

    angle += 4 * Math.PI;

    double x = start.x + Math.sin(angle) * WALL_STICK;
    double y = start.y + Math.cos(angle) * WALL_STICK;
    double wallDistanceX = Math.min(start.x - BOT_HALF_SIZE, WIDTH - start.x - BOT_HALF_SIZE);
    double wallDistanceY = Math.min(start.y - BOT_HALF_SIZE, HEIGHT - start.y - BOT_HALF_SIZE);
    double testDistanceX = Math.min(x - BOT_HALF_SIZE, WIDTH - x - BOT_HALF_SIZE);
    double testDistanceY = Math.min(y - BOT_HALF_SIZE, HEIGHT - y - BOT_HALF_SIZE);

    double adjacent = 0;
    int g = 0; // because I'm paranoid about potential infinite loops

    while (!FIELD.contains(x, y) && g++ < 25) {
      if (testDistanceY < 0 && testDistanceY < testDistanceX) {
        angle = ((int) ((angle + (Math.PI / 2)) / Math.PI)) * Math.PI;
        adjacent = Math.abs(wallDistanceY);
      } else if (testDistanceX < 0 && testDistanceX <= testDistanceY) {
        angle = (((int) (angle / Math.PI)) * Math.PI) + (Math.PI / 2);
        adjacent = Math.abs(wallDistanceX);
      }

      angle +=
          SMOOTH_TOWARDS_ENEMY * direction * (Math.abs(Math.acos(adjacent / WALL_STICK)) + 1e-1);

      x = start.x + (Math.sin(angle) * WALL_STICK);
      y = start.y + (Math.cos(angle) * WALL_STICK);
      testDistanceX = Math.min(x - 18, WIDTH - x - 18);
      testDistanceY = Math.min(y - 18, HEIGHT - y - 18);
    }

    return angle;
  }

  public static boolean hitWall(Point position) {
    if (FIELD.contains(position.x, position.y)) {
      return false;
    }
    position.x = Math.max(FIELD.x, Math.min(FIELD.x + FIELD.width, position.x));
    position.y = Math.max(FIELD.y, Math.min(FIELD.y + FIELD.height, position.y));
    return true;
  }

  public static boolean inField(Point position) {
    return FIELD.contains(position.x, position.y);
  }
}
