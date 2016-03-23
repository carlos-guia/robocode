package zyx.mega.debug.paint;

import zyx.mega.geometry.Point;

import java.awt.Color;
import java.awt.Graphics2D;

final class RectanglePaintOrder extends PaintOrder {
  final int x, y, size;

  public RectanglePaintOrder(Point center, int sideLength, Color color) {
    super(color);
    x = (int) (center.x - sideLength / 2);
    y = (int) (center.y - sideLength / 2);
    size = sideLength;
  }

  @Override
  public void internalPaint(Graphics2D g) {
    g.drawRect(x, y, size, size);
  }
}
