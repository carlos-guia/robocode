package zyx.mega.wave;

import zyx.mega.util.ObjectPool;

public class HitWindow {
  private double low;
  private double high;

  public boolean empty() {
    return low == Double.POSITIVE_INFINITY;
  }

  public double low() {
    return low;
  }

  public double high() {
    return high;
  }

  void init() {
    low = Double.POSITIVE_INFINITY;
    high = Double.NEGATIVE_INFINITY;
  }

  private void copyFrom(HitWindow old) {
    low = old.low;
    high = old.high;
  }

  public void update(double factor) {
    //Logger.getInstance().log("update window: %.2f %.2f :: %.2f", low, high, factor);
    low = Math.min(low, factor);
    high = Math.max(high, factor);
  }

  ///// POOLED OBJECT /////
  public static HitWindow newInstance() {
    return newInstance(null);
  }

  public static HitWindow newInstance(HitWindow old) {
    HitWindow object = ObjectPool.getFromPool(HitWindow.class);

    if (object == null) {
      object = new HitWindow();
    }

    if (old != null) {
      object.copyFrom(old);
    } else {
      object.init();
    }

    return object;
  }

  public void recycle() {
    ObjectPool.recycle(this);
  }

  private HitWindow() {}
  ///// POOLED OBJECT /////
}
