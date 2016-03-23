package zyx.mega.wave;

import robocode.util.Utils;
import zyx.mega.geometry.Box;
import zyx.mega.geometry.Point;
import zyx.mega.util.ObjectPool;
import zyx.mega.util.WallSmoothing;
import zyx.mega.util.ZUtils;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Wave {
  private static final int TARGETS = 21;
  private static final int HALF_TARGETS = TARGETS / 2;
  private static final double FACTOR_INCREMENT = 2.0 / TARGETS;

  public int time;
  public final Point firePosition = Point.newInstance();
  public double bearing;
  public double bulletPower;
  public double distanceTraveled;
  public double maximumEscapeAngle;
  public double velocity;
  public int direction;
  private Point[] targets;
  private boolean[] validTarget;

  public final HitWindow flattenerWindow = HitWindow.newInstance();

  public void update(long time) {
    distanceTraveled = time <= this.time ? 0 : ((time - this.time) * velocity);
  }

  public double getGuessFactor(Point position) {
    double angle = ZUtils.angle(firePosition, position) - bearing;
    return Utils.normalRelativeAngle(angle) / maximumEscapeAngle * direction;
  }

  public boolean hit(Box boundingBox, HitWindow window) {
    boolean allIn = true;
    double distanceSq = distanceTraveled * distanceTraveled;

    for (Point corner : boundingBox.corners()) {
      //Logger.getInstance().log("dsq: %s --> %s [%.2f] [%.2f]",
          //firePosition.toString(), corner.toString(), ZUtils.distanceSq(firePosition, corner),
          //distanceSq);
      if (ZUtils.distanceSq(firePosition, corner) <= distanceSq) {
        if (window != null) {
          window.update(getGuessFactor(corner));
        }
      } else {
        allIn = false;
      }
    }

    return allIn;
  }

  public int timeToPass(Box box) {
    double distanceSq = Double.NEGATIVE_INFINITY;
    for (Point c : box.corners()) {
      distanceSq = Math.max(distanceSq, ZUtils.distanceSq(firePosition, c));
    }

    return timeToPass(distanceSq);
  }

  private int timeToPass(double distanceSq) {
    int target = (int) (distanceSq / velocity);
    int low = 1, high = 77;
    while (low + 1 < high) {
      int mid = (low + high) / 2;
      if (mid * mid > target) {
        high = mid + 1;
      } else {
        low = mid;
      }
    }

    return time + low;
  }

  public void createTargets(double distance) {
    if (targets == null) {
      targets = new Point[TARGETS];
      validTarget = new boolean[TARGETS];
      for (int i = 0; i < TARGETS; ++i) {
        targets[i] = Point.newInstance();
      }
    }

    for (int i = 0; i < TARGETS; ++i) {
      double factor = i * FACTOR_INCREMENT - 1;
      double angle = bearing + factor * direction * maximumEscapeAngle;
      targets[i].move(angle, distance);
      validTarget[i] = WallSmoothing.inField(targets[i]);
    }
  }

  private class TargetIterator implements Iterator<Point> {
    private int index;

    TargetIterator() {
      index = 0;
      move();
    }

    private void move() {
      while (index < TARGETS && !validTarget[index]) ++index;
    }

    public boolean hasNext() {
      return index < TARGETS;
    }

    public Point next() {
      if (!hasNext()) {
        throw new NoSuchElementException("TargetIterator: " + index);
      }

      return targets[index];
    }
  }

  public Iterable<Point> targets() {
    return new Iterable<Point>() {
      public Iterator<Point> iterator() {
        return new TargetIterator();
      }
    };
  }

  ///// POOLED OBJECT /////
  public static Wave newInstance() {
    Wave object = ObjectPool.getFromPool(Wave.class);

    if (object == null) {
      return new Wave();
    }

    return object;
  }

  public void recycle() {
    for (Point p : targets) {
      p.recycle();
    }
    targets = null;
    validTarget = null;

    ObjectPool.recycle(this);
  }

  private Wave() {}
  ///// POOLED OBJECT /////
}
