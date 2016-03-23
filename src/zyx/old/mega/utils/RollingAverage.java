package zyx.old.mega.utils;


public class RollingAverage {
  public double average_;
  public double depth_;
  public int count_;
  public RollingAverage(double depth) {
    depth_ = depth;
  }
  public void Roll(double value, double weight) {
    average_ = Roll(average_, value, Math.min(count_++, depth_), weight);
  }
  public static double Roll(double average, double value, double depth, double weight) {
    return (average * depth + value * weight) / (depth + weight);
  }
  public static double Roll(double average, double value, double depth) {
    return (average * depth + value) / (depth + 1);
  }
  public static void Roll(double[] stats, int i, double value, double depth, double weight) {
    stats[i] = Roll(stats[i], value, depth, weight);
  }
  public static void Roll(double[] stats, int i, double value, double depth) {
    stats[i] = Roll(stats[i], value, depth);
  }
}
