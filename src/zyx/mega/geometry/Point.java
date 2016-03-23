package zyx.mega.geometry;

import zyx.mega.util.ObjectPool;

public class Point {
  public double x;
  public double y;

  public void copyFrom(Point from) {
    x = from.x;
    y = from.y;
  }

  public void setProjected(Point origin, double angle, double distance) {
    x = origin.x;
    y = origin.y;
    move(angle, distance);
  }

  public void move(double angle, double distance) {
    x += Math.sin(angle) * distance;
    y += Math.cos(angle) * distance;
  }

  @Override
  public String toString() {
    return String.format("[%.2f %.2f]", x, y);
  }

  ///// POOLED OBJECT /////
  public static Point newInstance() {
    return newInstance(null);
  }

  public static Point newInstance(Point old) {
    Point object = ObjectPool.getFromPool(Point.class);

    if (object == null) {
      object = new Point();
    }

    if (old != null) {
      object.copyFrom(old);
    }

    return object;
  }

  public void recycle() {
    ObjectPool.recycle(this);
  }

  private Point() {}
  ///// POOLED OBJECT /////
}
