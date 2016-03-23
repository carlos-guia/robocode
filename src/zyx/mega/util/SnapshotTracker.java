package zyx.mega.util;

import java.util.ArrayList;

public class SnapshotTracker<T> {
  private final Class<T> clazz;
  private final ArrayList<T> snapshots;
  private int currentIndex;

  private enum State {
    EMPTY,
    ONE,
    TWO,
    FULL
  }

  @SuppressWarnings("unchecked")
  public SnapshotTracker(Class<T> clazz) {
    this.clazz = clazz;
    this.snapshots = new ArrayList<T>(3);
  }

  public void init() {
  }

  private T newInstance() {
    try {
      return clazz.newInstance();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }

    return null;
  }

  public T getCurrent() {
    return snapshots.get(currentIndex);
  }

  public T getOneAgo() {
    return snapshots.get((currentIndex + 2) % 3);
  }

  public T getTwoAgo() {
    return snapshots.get((currentIndex + 1) % 3);
  }

  public void advance() {
    if (++currentIndex == 3) {
      currentIndex = 0;
    }
  }
}
