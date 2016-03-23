package zyx.old.mega.targeting;

import robocode.util.Utils;
import zyx.old.mega.geometry.Geometry;
import zyx.old.mega.geometry.Line;
import zyx.old.mega.utils.Range;

public class VShot extends Line {
  public VGun gun_;
  public double angle_;
  public boolean flagged_;
  public boolean anti_surfer_;
  public VShot(VGun gun, double tank_turn, double gun_heading, boolean anti_surfer) {
    gun_ = gun;
    anti_surfer_ = anti_surfer;
    double gun_turn = Range.CapLowHigh(Utils.normalRelativeAngle((anti_surfer ? gun.as_aim_angle_ : gun.aim_angle_) - gun_heading),
        tank_turn - Geometry.PI_9,
        tank_turn + Geometry.PI_9);
    angle_ = Utils.normalAbsoluteAngle(gun_heading + gun_turn);
    /**
    Printer.printf(0, "vs: %.5f - %.5f = %.5f | %.5f +- %.5f = %.5f : %.5f\n",
        gun.aim_angle_, gun_heading, Utils.normalRelativeAngle(gun.aim_angle_ - gun_heading),
        tank_turn, Geometry.PI_9, gun_turn, angle_);
    /**/
    flagged_ = false;
  }
  public void MoveBullet(double velocity) {
    end_.SetPoint(this);
    MovePoint(angle_, velocity);
  }
}
