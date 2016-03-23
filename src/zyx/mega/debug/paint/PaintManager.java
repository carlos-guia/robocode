package zyx.mega.debug.paint;

import zyx.mega.abstraction.ZRobot;
import zyx.mega.geometry.Point;
import zyx.mega.time.TimeManager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;

public class PaintManager {
  private static PaintManager instance = new PaintManager();
  public static PaintManager getInstance() {
    return instance;
  }

  private long lastPaint;
  private final List<PaintOrder> orders;

  private PaintManager() {
    orders = new LinkedList<PaintOrder>();
    lastPaint = -1;
  }

  public void flush(Graphics2D g) {
    ZRobot.getInstance().out.println("painting: " + orders.size());

    g.setColor(Color.WHITE);

    for (PaintOrder order : orders) {
      order.paint(g);
    }

    orders.clear();
    lastPaint = TimeManager.getInstance().time();
  }

  public void paintCircle(Point center, double radius, Color color) {
    if (enabled()) {
      orders.add(new CirclePaintOrder(center, radius, color));
    }
  }

  public void paintRectangle(Point center, int sideLength, Color color) {
    if (enabled()) {
      orders.add(new RectanglePaintOrder(center, sideLength, color));
    }
  }

  private boolean enabled() {
    return lastPaint >= TimeManager.getInstance().time() - 1;
  }
}
