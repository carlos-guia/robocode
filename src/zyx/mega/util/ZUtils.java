package zyx.mega.util;

import zyx.mega.geometry.Point;

public class ZUtils {
  public static double angle(Point source, Point target) {
    return Math.atan2(target.x - source.x, target.y - source.y);
  }

  public static double distance(Point p0, Point p1) {
    return Math.sqrt(distanceSq(p0, p1));
  }

  public static double distanceSq(Point p0, Point p1) {
    double dx = p0.x - p1.x;
    double dy = p0.y - p1.y;
    return dx * dx + dy * dy;
  }

  public static double sigmoid(double t) {
    return 1 / (1 + Math.exp(-t));
  }
}
