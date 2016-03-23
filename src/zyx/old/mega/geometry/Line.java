package zyx.old.mega.geometry;

import java.awt.Graphics2D;


public class Line extends Point {
  public Point end_;
  public String toString() {
    return String.format("[%.4f %.4f] - [%.4f %.4f]", x_, y_, end_.x_, end_.y_);
  }
  public Line() {
    super();
    end_ = new Point();
  }
  public Line(Point start, Point end) {
    super(start);
    end_ = new Point(end);
  }
  public Line(Point start, double bearing, double distance) {
    super(start);
    end_ = new Point(start, bearing, distance);
  }

  public void onPaint(Graphics2D g) {
    g.drawLine((int)x_, (int)y_, (int)end_.x_, (int)end_.y_);
  }
}
