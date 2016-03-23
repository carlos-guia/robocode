package zyx.old.mega.targeting;

import java.util.Arrays;

import zyx.old.mega.bot.Enemy;
import zyx.old.mega.utils.Snapshot;
import zyx.old.mega.utils.WeightedDistancer;

public class GFTargetingDC_B extends GFTargetingDC {
  public GFTargetingDC_B(Enemy enemy, VGunSystem gun_system) {
    super(enemy, gun_system);
    distancer_ = new WeightedDistancer() {
      public void InitWeight() {
        weight_ = new double[Snapshot.GF_ATTRIBUTES];
        Arrays.fill(weight_, 1);
      }
    };
  }
}
