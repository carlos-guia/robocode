package zyx.old.mega.targeting;

import zyx.old.mega.bot.Enemy;
import zyx.old.mega.utils.Snapshot;
import zyx.old.mega.utils.WeightedDistancer;

public class GFTargetingDC_C extends GFTargetingDC {
  public GFTargetingDC_C(Enemy enemy, VGunSystem gun_system) {
    super(enemy, gun_system);
    distancer_ = new WeightedDistancer() {
      public void InitWeight() {
        weight_ = new double[Snapshot.GF_ATTRIBUTES];
        weight_[Snapshot.DISTANCE] = 0.5;
        weight_[Snapshot.LATERAL_VELOCITY] = 1;
        weight_[Snapshot.VELOCITY] = 0.5;
        weight_[Snapshot.ACCELERATION] = 1;
        weight_[Snapshot.ROTATION] = 0.3;
        weight_[Snapshot.AHEAD_TICKS] = 0.7;
        weight_[Snapshot.BACK_TICKS] = 0.7;
        weight_[Snapshot.APPROACHING_VELOCITY] = 1;
        weight_[Snapshot.TIME_STOPPED] = 0.2;
        weight_[Snapshot.TIME_RUNNING] = 0.2;
        weight_[Snapshot.TIME_DIRECTION] = 0.5;
        //weight_[Snapshot.BULLET_HIT_TIME] = 0.2;
      }
    };
  }
}
