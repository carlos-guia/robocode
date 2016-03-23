package zyx.old.mega.geometry;

import zyx.old.mega.utils.Range;

import java.awt.Graphics2D;
import java.util.ArrayList;

public class Circle extends Point {
  public double radius_;
  public Circle() {}
  public Circle(Point center) {
    super(center);
    radius_ = 0;
  }
  public Circle(Point center, double radius) {
    super(center);
    radius_ = radius;
  }
  
  public Circle(Circle circle) {
    super(circle);
    radius_ = circle.radius_;
  }
  
  public boolean Inside(Point point) {
    return Geometry.Distance(this, point) <= radius_;
  }
  
  public void onPaint(Graphics2D g) {
    double side = 2 * radius_;
    g.drawOval((int)(x_ - radius_), (int)(y_ - radius_), (int) side, (int) side);
  }
  public ArrayList<Point> Intersection(Rectangle rect) {
    ArrayList<Point> inter = new ArrayList<Point>();
    Range x_range = new Range(rect.x_, rect.x_ + rect.width_);
    Range y_range = new Range(rect.y_, rect.y_ + rect.height_);
    for (double y : GetY(rect.x_) ) if ( y_range.Inside(y) ){
      inter.add(new Point(rect.x_, y));
    }
    for (double y : GetY(rect.x_ + rect.width_) ) if ( y_range.Inside(y) ){
      inter.add(new Point(rect.x_ + rect.width_, y));
    }
    for (double x : GetX(rect.y_) ) if ( x_range.Inside(x) ){
      inter.add(new Point(x, rect.y_));
    }
    for (double x : GetX(rect.y_ + rect.height_) ) if ( x_range.Inside(x) ){
      inter.add(new Point(x, rect.y_ + rect.height_));
    }
    return inter;
  }
  private double[] GetY(double x) {
    double det = Geometry.Square(radius_) - Geometry.Square(x - x_);
    if ( det < 0 ) return new double[]{};
    if ( det == 0 ) return new double[]{ y_ };
    double factor = Math.sqrt(det);
    return new double[] { y_ - factor, y_ + factor };
  }
  private double[] GetX(double y) {
    double det = Geometry.Square(radius_) - Geometry.Square(y - y_);
    if ( det < 0 ) return new double[]{};
    if ( det == 0 ) return new double[]{ x_ };
    double factor = Math.sqrt(det);
    return new double[] { x_ - factor, x_ + factor };
  }
}
