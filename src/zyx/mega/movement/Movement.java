package zyx.mega.movement;

import robocode.Bullet;
import zyx.mega.abstraction.Executable;
import zyx.mega.abstraction.Initiable;
import zyx.mega.bot.Bot;
import zyx.mega.bot.Enemy;

public interface Movement extends Initiable, Executable {
  void onEnemyBulletFired(Bot me, Enemy enemy, double bulletPower);
  void onHitByBullet(Bullet bullet);
  void onBulletHitBullet(Bullet hitBullet);
}
