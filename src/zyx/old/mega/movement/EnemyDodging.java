package zyx.old.mega.movement;

import zyx.old.mega.bot.Enemy;

public abstract class EnemyDodging {
  protected Enemy enemy_;
  public EnemyDodging(Enemy enemy) {
    enemy_ = enemy;
  }
  public abstract void Init();
  public abstract void onScannedRobot(boolean new_shot, double power);
}
