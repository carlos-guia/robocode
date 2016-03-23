package zyx.mega.geometry;

import zyx.mega.util.ObjectPool;

public class Box {
  private final Point corners[] = new Point[]{
      Point.newInstance(),
      Point.newInstance(),
      Point.newInstance(),
      Point.newInstance(),
  };

  public Point[] corners() {
    return corners;
  }

  public void update(Point center, int halfSize) {
    corners[0].x = center.x - halfSize;
    corners[0].y = center.y - halfSize;
    corners[1].x = center.x - halfSize;
    corners[1].y = center.y + halfSize;
    corners[2].x = center.x + halfSize;
    corners[2].y = center.y + halfSize;
    corners[3].x = center.x + halfSize;
    corners[3].y = center.y - halfSize;
  }

  @Override
  public String toString() {
    return String.format("%s %s %s %s", corners[0], corners[1], corners[2], corners[3]);
  }

  ///// POOLED OBJECT /////
  public static Box newInstance() {
    Box object = ObjectPool.getFromPool(Box.class);

    if (object == null) {
      return new Box();
    }

    return object;
  }

  public void recycle() {
    ObjectPool.recycle(this);
  }

  private Box() {}
  ///// POOLED OBJECT /////
}
