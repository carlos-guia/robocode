package zyx.mega.debug.paint;

import java.awt.Color;
import java.awt.Graphics2D;

abstract class PaintOrder {
  private final Color color;

  protected PaintOrder(Color color) {
    this.color = color;
  }

  void paint(Graphics2D g) {
    g.setColor(color);
    internalPaint(g);
  }

  protected abstract void internalPaint(Graphics2D g);
}
