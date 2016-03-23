package zyx.old.mega.geometry;

import zyx.old.debug.painter.IPaintable;

import java.awt.Graphics2D;


public class Point implements IPaintable {
  public double x_;
  public double y_;
  
  public Point() {}
  public Point(double x, double y) {
    SetPoint(x, y);
  }
  public Point(Point point) {
    SetPoint(point.x_, point.y_);
  }
  public Point(double x, double y, double angle, double distance) {
    SetPoint(x, y);
    MovePoint(angle, distance);
  }
  public Point(Point start, double angle, double distance) {
    ProjectPoint(start, angle, distance);
  }
  public void SetPoint(double x, double y) {
    x_ = x;
    y_ = y;
  }
  public void SetPoint(Point point) {
    x_ = point.x_;
    y_ = point.y_;
  }
  public void MovePoint(double angle, double distance) {
    x_ += Math.sin(angle) * distance;
    y_ += Math.cos(angle) * distance;
  }
  public void ProjectPoint(Point start, double bearing, double distance) {
    SetPoint(start.x_, start.y_);
    MovePoint(bearing, distance);
  }


  public void onPaint(Graphics2D g) {
  }
}
