package zyx.old.mega.utils.wave;

import zyx.old.mega.utils.Range;

public class WaveHit {
  public static final int UNKOWN = -1;
  public static final int ALL_OUT = 0;
  public static final int HITTING = 1;
  public static final int ALL_IN = 2;

  public Range corners_;
  public Range bbox_;
  public double run_time_;
  public double wait_time_;
  public int info_ = UNKOWN;
  
  public WaveHit() {
    bbox_ = new Range();
    corners_ = new Range();
    run_time_ = Double.POSITIVE_INFINITY;
  }

  public boolean AllIn() {
    return info_ == ALL_IN;
  }
  public boolean AllOut() {
    return info_ == ALL_OUT;
  }
  public boolean Hitting() {
    return info_ == HITTING;
  }
  public boolean Unkown() {
    return info_ == UNKOWN;
  }
}
