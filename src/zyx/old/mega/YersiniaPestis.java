package zyx.old.mega;

import java.awt.Color;
import java.awt.Graphics2D;

import robocode.AdvancedRobot;
import robocode.BattleEndedEvent;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.SkippedTurnEvent;
import robocode.WinEvent;
import zyx.old.mega.utils.TurnHandler;

public class YersiniaPestis extends AdvancedRobot {
  private TurnHandler game_turn_;
  
  //private ConsolePrinter cp_ = new ConsolePrinter();
  
  public void run() {
    System.gc(); System.gc();
    setColors(Color.BLACK, new Color(0, 128, 0), Color.GREEN, Color.WHITE, Color.WHITE);
    game_turn_ = new TurnHandler(this);
    game_turn_.run();
  }

  public void onBattleEnded(BattleEndedEvent event) {
    game_turn_.onBattleEnded(event);
  }
  public void onBulletHit(BulletHitEvent event) {
    game_turn_.onBulletHit(event);
  }
  public void onBulletHitBullet(BulletHitBulletEvent event) {
    game_turn_.onBulletHitBullet(event);
  }
  public void onBulletMissed(BulletMissedEvent event) {
    game_turn_.onBulletMissed(event);
  }
  public void onDeath(DeathEvent event) {
    game_turn_.onDeath(event);
  }
  public void onHitByBullet(HitByBulletEvent event) {
    game_turn_.onHitByBullet(event);
  }
  public void onHitRobot(HitRobotEvent event) {
    game_turn_.onHitRobot(event);
  }
  public void onHitWall(HitWallEvent event) {
    game_turn_.onHitWall(event);
  }
  public void onPaint(Graphics2D g) {
    game_turn_.onPaint(g);
  }
  public void onRobotDeath(RobotDeathEvent event) {
    game_turn_.onRobotDeath(event);
  }
  public void onScannedRobot(ScannedRobotEvent event) {
    game_turn_.onScannedRobot(event);
  }
  public void onSkippedTurn(SkippedTurnEvent event) {
    out.println("Skipped turn");
    game_turn_.onSkippedTurn(event);
  }
  public void onWin(WinEvent event) {
    game_turn_.onWin(event);
  }
}
