package zyx.mega;

import zyx.mega.abstraction.ZRobot;
import zyx.mega.gun.Gun;
import zyx.mega.gun.NullGun;
import zyx.mega.movement.Movement;
import zyx.mega.movement.WaveSurfing;
import zyx.mega.radar.LockRadar;
import zyx.mega.radar.Radar;

public class Odin extends ZRobot {
  @Override
  public Radar getRadar() {
    return new LockRadar();
  }

  @Override
  public Gun getGun() {
    return new NullGun();
  }

  @Override
  public Movement getMovement() {
    return new WaveSurfing();
  }
}
