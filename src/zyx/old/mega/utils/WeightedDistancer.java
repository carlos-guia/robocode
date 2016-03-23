package zyx.old.mega.utils;

import zyx.old.mega.geometry.Geometry;
import zyx.old.simonton.utils.Distancer;

public abstract class WeightedDistancer implements Distancer {
  protected double weight_[];
  public WeightedDistancer() {
    InitWeight();
  }
  public double getDistance(double[] d1, double[] d2) {
    double distance = 0;
    double sum = 0;
    for ( int i = 0; i < d1.length; ++i ) {
      distance += Geometry.Square(d1[i] - d2[i]) * weight_[i];
      sum += weight_[i];
    }
    return distance / Math.max(1, sum);
  }
  public abstract void InitWeight();
}
