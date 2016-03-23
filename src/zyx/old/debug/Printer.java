package zyx.old.debug;

import zyx.old.mega.utils.TurnHandler;

import java.io.PrintStream;

public class Printer {
  public static DBS dbs_ = new DBS();
  public static StringBuilder sb_ = new StringBuilder();
  public static void printf(int level, String format, Object... args) {
    if ( dbs_.Crap(level) ) return;
    sb_.append(TurnHandler.round_ + "." + TurnHandler.time_ + ": " + String.format(format, args));
  }
  public static void onPrint(PrintStream out) {
    //out.printf("printer: %d (%d)\n", TurnHandler.robot_.getTime(), sb_.length());
    out.print(sb_);
    sb_ = new StringBuilder();
  }
}
