package zyx.mega.radar;

import zyx.mega.abstraction.Executable;
import zyx.mega.abstraction.Initiable;

public interface Radar extends Initiable, Executable {
  void onScannedRobot();
}
