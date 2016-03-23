package zyx.old.mega.geometry;

import zyx.old.mega.targeting.VShot;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

public class Rectangle extends Point {
  public double width_;
  public double height_;

  public Rectangle() {}

  public Rectangle(double x, double y, double w, double h) {
    super(x, y);
    width_ = w;
    height_ = h;
  }

  @Override
  public void onPaint(Graphics2D g) {
    g.drawRect((int) x_, (int) y_, (int) width_, (int) height_);
  }

  public boolean Inside(Point point, boolean in) {
    double t = in ? 1e-9 : -1e-9;
    return point.x_ + t >= x_ && point.x_ - t <= x_ + width_ && point.y_ + t >= y_
        && point.y_ - t <= y_ + height_;
  }

  public boolean Inside(double x, double y, boolean in) {
    double t = in ? 1e-9 : -1e-9;
    return x + t >= x_ && x - t <= x_ + width_ && y + t >= y_ && y - t <= y_ + height_;
  }

  public boolean Interescts(VShot shot) {
    return new Rectangle2D.Double(x_, y_, width_, height_)
        .intersectsLine(new Line2D.Double(shot.x_, shot.y_, shot.end_.x_, shot.end_.y_));
  }

}
