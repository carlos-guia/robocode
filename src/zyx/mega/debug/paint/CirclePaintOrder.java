package zyx.mega.debug.paint;

import zyx.mega.geometry.Point;

import java.awt.Color;
import java.awt.Graphics2D;

final class CirclePaintOrder extends PaintOrder {
  private final int x;
  private final int y;
  private final int side;

  public CirclePaintOrder(Point center, double radius, Color color) {
    super(color);
    x = (int) (center.x - radius);
    y = (int) (center.y - radius);
    side = (int) (2 * radius);
  }

  @Override
  public void internalPaint(Graphics2D g) {
    g.drawOval(x, y, side, side);
  }
}
