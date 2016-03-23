package zyx.old.mega.utils;

import java.awt.Graphics2D;

import robocode.AdvancedRobot;
import robocode.BattleEndedEvent;
import robocode.Bullet;
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
import robocode.util.Utils;
import wiki.mc2k7.RaikoGun;
import zyx.old.debug.Printer;
import zyx.old.mega.bot.Bot;
import zyx.old.mega.bot.Enemy;
import zyx.old.mega.geometry.Geometry;


public class TurnHandler {
  private static RaikoGun raiko_gun_;
  public static AdvancedRobot robot_;
  public static Bot me_;
  public static TurnHandler handler_;

  public static int round_;
  public static long time_;
  public static long exact_time_;
  public static boolean _1v1_;
  public static boolean _melee_;
  public static boolean _moved_;
  public static boolean _aimed_;
  public static boolean _fired_;
  //public static Bullet last_bullet_;

  //private Graphics2D g_;

  public TurnHandler(AdvancedRobot robot) {
    robot_ = robot;
    handler_ = this;
    Config.Load(robot_);
    _melee_ = robot_.getOthers() > 1;
    _1v1_ = !_melee_;
    me_ = new Bot();
    Bot.InitStatic();
    //g_ = null;
  }

  public void Update() {
    long time = robot_.getTime();
    if ( time == time_ ) return;
    ++exact_time_;
    time_ = time;
    round_ = robot_.getRoundNum();
    _1v1_ = robot_.getOthers() < 2;
    _moved_ = !Config.movement_enabled_;
    _aimed_ = !Config.targeting_enabled_;
    me_.Update(robot_);
  }
  
  public void run() {
    Update();
    PerformanceTracker.InitRound();
    for (Enemy enemy : Enemy.Phonebook()) {
      enemy.Init();
    }
    robot_.setAdjustGunForRobotTurn(true);
    robot_.setAdjustRadarForGunTurn(true);
    if ( Config._raiko_ ) {
      if ( raiko_gun_ == null ) raiko_gun_ = new RaikoGun(robot_);
      raiko_gun_.run();
    } else {
      time_ = -1;
      while ( true ) {
        Update();
        if ( robot_.getRadarTurnRemainingRadians() == 0 ) {
          robot_.setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
        }
        Fire();
        FinishTurn();
      }
    }
  }

  private void FinishTurn() {
    //Painter.onPaint(g_);
    Printer.onPrint(robot_.out);
    robot_.execute();
  }
  private void Fire() {
    if ( !Config.targeting_enabled_ || _1v1_ ) return;
  }
  
  public static Bullet FireBullet(double power, Enemy enemy) {
    if ( me_.energy_ < 1 || power == 0 ) return null;
    Bullet bullet = robot_.setFireBullet(power);
    if ( bullet != null ) PerformanceTracker.AddShot(power);
    return bullet;
  }
  public static void AimGun(double angle) {
    robot_.setTurnGunRightRadians(Utils.normalRelativeAngle(angle - robot_.getGunHeadingRadians()));
  }

  public void onBattleEnded(BattleEndedEvent event) {
  }
  public void onBulletHit(BulletHitEvent event) {
    Update();
    Enemy enemy = Enemy.Find(event.getName());
    enemy.onBulletHit(event);
    PerformanceTracker.onBulletHit(event);
  }
  public void onBulletHitBullet(BulletHitBulletEvent event) {
    Update();
    Bullet bullet = event.getHitBullet();
    Bullet my_bullet = event.getBullet();
    if ( bullet.getName().equals(robot_.getName()) ) {
      bullet = event.getBullet();
      my_bullet = event.getHitBullet();
      if ( bullet.getName().equals(robot_.getName()) ) {
        PerformanceTracker.RemoveMyShots(event.getBullet().getPower(), event.getHitBullet().getPower());
        return;
      }
    }
    Enemy enemy = Enemy.Find(bullet.getName());
    enemy.onBulletHitBullet(TurnHandler.time_, my_bullet, new zyx.old.mega.geometry.Bullet(bullet));
    PerformanceTracker.RemoveShot(event.getBullet().getPower(), event.getHitBullet().getPower());
  }
  public void onBulletMissed(BulletMissedEvent event) {
  }
  public void onDeath(DeathEvent event) {
    /*
    for (Enemy enemy : Enemy.Phonebook()) if ( !enemy.dead_ ) {
      enemy.Log();
    }
    */
    Update();
    Printer.onPrint(robot_.out);
    PerformanceTracker.onDeath(robot_);
  }
  public void onHitByBullet(HitByBulletEvent event) {
    Update();
    Enemy enemy = Enemy.Find(event.getName());
    enemy.onHitByBullet(event);
    PerformanceTracker.onHitByBullet(event);
  }
  public void onHitRobot(HitRobotEvent event) {
    Update();
    Enemy enemy = Enemy.Find(event.getName());
    enemy.onHitRobot(event);
  }
  public void onHitWall(HitWallEvent event) {
    Update();
    for (Enemy enemy : Enemy.Phonebook()) {
      enemy.onHitWall(event);
    }
  }
  public void onPaint(Graphics2D g) {
    //g_ = g;
  }
  public void onRobotDeath(RobotDeathEvent event) {
    Update();
    PerformanceTracker.onRobotDeath(robot_);
    Enemy enemy = Enemy.Find(event.getName());
    enemy.onRobotDeath(event);
  }
  public void onScannedRobot(ScannedRobotEvent event) {
    if ( raiko_gun_ != null ) raiko_gun_.onScannedRobot(event);
    Update();
    Enemy enemy = Enemy.Find(event.getName());
    enemy.onScannedRobot(event);
    if ( raiko_gun_ != null ) {
      FinishTurn();
    }
  }
  public void onSkippedTurn(SkippedTurnEvent event) {
    Update();
    PerformanceTracker.onSkippedTurn(robot_.out);
  }
  public void onWin(WinEvent event) {
  }

  public static void Move(double distance, double angle, boolean moved) {
    _moved_ = moved;
    angle = Utils.normalRelativeAngle(angle - me_.heading_);
    if ( Math.abs(angle) > Geometry.HALF_PI ) {
      angle = Utils.normalRelativeAngle(angle + Geometry.PI);
      distance = -distance;
    }
    robot_.setTurnRightRadians(angle);
    robot_.setAhead(distance);
  }
}
