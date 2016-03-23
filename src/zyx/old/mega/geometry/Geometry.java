package zyx.old.mega.geometry;

import robocode.util.Utils;


public class Geometry {
  public static final double PI = Math.PI;
  public static final double HALF_PI = Math.PI / 2;
  public static final double PI_4 = Math.PI / 4;
  public static final double PI_8 = Math.PI / 8;
  public static final double PI_9 = Math.PI / 9;
  public static final double PI_18 = Math.PI / 18;
  public static final double PI_240 = Math.PI / 240;
  public static final double PI2 = Math.PI * 2;

  public static double Square(double x) { return x * x; }
  public static double Distance(Point p0, Point p1) {
    return Math.sqrt(Square(p1.x_ - p0.x_) + Square(p1.y_ - p0.y_));
  }
  public static double Angle(Point p0, Point p1) {
    return Math.atan2(p1.x_ - p0.x_, p1.y_ - p0.y_);
  }
  public static double AngleDifference(double alpha, double beta) {
    return Math.abs(Utils.normalRelativeAngle(alpha - beta));
  }
}
