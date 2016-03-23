package zyx.mega.debug.log;

import zyx.mega.abstraction.Executable;
import zyx.mega.abstraction.Initiable;
import zyx.mega.abstraction.ZRobot;
import zyx.mega.time.TimeManager;

import java.util.LinkedList;

public class Logger implements Initiable, Executable {
  private static final boolean ENABLED = true;

  private static Logger instance = new Logger();
  public static Logger getInstance() {
    return instance;
  }

  private final LinkedList<LogItem> items;

  private Logger() {
    items = new LinkedList<Logger.LogItem>();
  }

  public void init() {
    items.clear();
  }

  public void execute() {
    if (items.isEmpty()) {
      return;
    }

    long time = TimeManager.getInstance().time();
    long roundTime = TimeManager.getInstance().roundTime();
    int round = TimeManager.getInstance().round();
    StringBuilder builder = new StringBuilder();
    builder.append("log at: ");
    builder.append(round); builder.append(' ');
    builder.append(roundTime); builder.append(' ');
    builder.append(time); builder.append('\n');
    for (LogItem item : items) {
      builder.append(item.toString());
      builder.append('\n');
    }
    items.clear();

    ZRobot.getInstance().out.println(builder);
  }

  public void log(String format) {
    if (ENABLED) {
      items.add(new LogItem(format));
    }
  }

  public void log(String format, Object... args) {
    if (ENABLED) {
      items.add(new LogItem(format, args));
    }
  }

  private static class LogItem {
    final String format;
    final Object[] args;

    LogItem(String format) {
      this.format = format;
      this.args = null;
    }

    LogItem(String format, Object[] args) {
      this.format = format;
      this.args = args;
    }

    @Override
    public String toString() {
      if (args == null) {
        return format;
      }

      return String.format(format, args);
    }
  }
}
