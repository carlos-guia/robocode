package zyx.mega.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ObjectPool {
  private static final ObjectPool instance = new ObjectPool();

  private Map<String, LinkedList<Object>> objects = new HashMap<String, LinkedList<Object>>();

  @SuppressWarnings("unchecked")
  public static <T> T getFromPool(Class<T> clazz) {
    LinkedList<T> list = (LinkedList<T>) instance.objects.get(clazz.getName());
    if (list == null) {
      list = new LinkedList<T>();
      instance.objects.put(clazz.getName(), (LinkedList<Object>) list);
    }

    if (!list.isEmpty()) {
      //ZRobot.getInstance().out.println("pool hit " + clazz.getName());
      return list.pop();
    }

    //ZRobot.getInstance().out.println("pool miss " + clazz.getName());
    return null;
  }

  public static <T> void recycle(T old) {
    instance.objects.get(old.getClass().getName()).add(old);
  }
}
