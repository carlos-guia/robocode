package zyx.old.mega.utils;

public class DangerSwitch implements Comparable<DangerSwitch> {
  public double factor_;
  public double danger_;
  public DangerSwitch(double factor, double weight) {
    factor_ = factor;
    danger_ = weight;
  }

  public int compareTo(DangerSwitch other) {
    if ( Math.abs(factor_ - other.factor_) < 1e-9 ) return (int)Math.signum(danger_ - other.danger_);
    return (int)Math.signum(factor_ - other.factor_);
  }
}
