package zyx.mega.geometry;

public class Rectangle {
  public final double x;
  public final double y;
  public final double width;
  public final double height;

  public Rectangle(double x, double y, double width, double height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  public boolean contains(double x, double y) {
    return x - 1e-5 >= this.x
        && x <= this.x + this.width - 1e-5
        && y - 1e-5 >= this.y
        && y <= this.y + this.height - 1e-5;
  }
}
