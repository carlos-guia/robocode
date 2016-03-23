package zyx.old.mega.utils;

import java.util.Arrays;

public class Range {
  public double window_[];
  public String toString() {
    if ( window_ == null ) return "null";
    return String.format("[%.4f %.4f]", window_[0], window_[1]);
  }
  public Range() {}
  public Range(double value) {
    SetWindow(value);
  }
  public Range(double low, double high) {
    SetWindow(low, high, false);
  }
  public Range(double a, double b, boolean centered) {
    SetWindow(a, b, centered);
  }
  public Range(double[] window) {
    SetWindow(window, true);
  }
  public void SetWindow(double value) {
    window_ = new double[] { value, value };
  }
  public void SetWindow(double a, double b, boolean centered) {
    if ( centered ) {
      window_ = new double[] { a - b, a + b };
    } else {
      window_ = new double[] { a, b };
    }
  }
  public void SetWindow(double[] window, boolean copy) {
    if ( copy && window != null ) window_ = Arrays.copyOf(window, 2);
    else window_ = window;
  }
  public void SetWindow(Range window, boolean copy) {
    SetWindow(window.window_, copy);
  }
  public void Update(double value) {
    if ( window_ == null ) SetWindow(value);
    else {
      window_[0] = Math.min(window_[0], value);
      window_[1] = Math.max(window_[1], value);
    }
  }
  public void Update(Range range) {
    if ( range == null || range.window_ == null ) return;
    if ( window_ == null ) SetWindow(range.window_, true);
    else {
      window_[0] = Math.min(window_[0], range.window_[0]);
      window_[1] = Math.max(window_[1], range.window_[1]);
    }
  }
  public boolean Inside(double v) {
    if ( window_ == null ) return false;
    return v + 1e-9 >= window_[0] && v - 1e-9 <= window_[1];
  }

  
  public static double CapLow(double value, double min) {
    return Math.max(value, min);
  }
  public static double CapHigh(double value, double max) {
    return Math.min(value, max);
  }
  public static double CapLowHigh(double value, double min, double max) {
    return CapHigh(CapLow(value, min), max);
  }
  public static double CapCentered(double value, double absolute_max) {
    return CapLowHigh(value, -absolute_max, absolute_max);
  }
  public double Size() {
    if ( window_ == null ) return 0;
    return window_[1] - window_[0] + 1e-5;
  }
  public static double Normalize(double value, double min, double max, boolean absolute) {
    if ( absolute ) value = Math.abs(value);
    return CapLowHigh((value - min) / (max - min), 0 , 1);
  }
  public Range Intersection(Range window) {
    if ( window == null || window.window_ == null || window_ == null ) return new Range();
    double low = Math.max(window_[0], window.window_[0]);
    double high = Math.min(window_[1], window.window_[1]);
    if ( low < high ) return new Range(low, high);
    return new Range();
  }
  public void CapWindow(double min, double max) {
    if ( window_ == null ) return;
    window_[0] = Math.max(window_[0], min);
    window_[1] = Math.min(window_[1], max);
  }
  public double Center() {
    if ( window_ == null ) return 0;
    return Math.max(1e-9, (window_[1] + window_[0]) / 2);
  }
}
