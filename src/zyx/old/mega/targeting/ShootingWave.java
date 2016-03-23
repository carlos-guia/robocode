package zyx.old.mega.targeting;

import robocode.Bullet;
import zyx.old.mega.utils.Wave;

public class ShootingWave extends Wave {
  public VShot[] virtual_shots_;
  public Bullet bullet_;
  
  public void Update(long time) {
    super.Update(time);
    for (VShot shot : virtual_shots_) if ( !shot.flagged_ ) {
      shot.MoveBullet(velocity_);
      //Painter.Add(0, shot);
    }
  }
}
