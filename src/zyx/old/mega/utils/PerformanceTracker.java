package zyx.old.mega.utils;

import java.io.PrintStream;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.Rules;

public class PerformanceTracker {
  public static double total_damage_given_, damage_given_;
  public static double total_damage_taken_, damage_taken_;
  public static double total_power_shot_, power_shot_;
  public static double total_power_hitted_, power_hitted_;
  public static double total_enemy_power_shot_, enemy_power_shot_;
  public static double total_enemy_power_hitted_, enemy_power_hitted_;
  public static int total_shots_, shots_;
  public static int total_hits_, hits_;
  public static int total_enemy_shots_, enemy_shots_;
  public static int total_enemy_hits_, enemy_hits_;
  public static int total_skipped_, skipped_;
  public static int wins_, losses_;

  public static void InitRound() {
    damage_given_ = damage_taken_ = power_shot_ = power_hitted_ = enemy_power_shot_ = enemy_power_hitted_ = 0;
    shots_ = hits_ = enemy_shots_ = enemy_hits_ = skipped_ = 0;
  }
  public static void AddShot(double power) {
    ++total_shots_; ++shots_;
    total_power_shot_ += power;
    power_shot_ += power;
  }
  public static void AddEnemyShot(double power) {
    ++total_enemy_shots_; ++enemy_shots_;
    total_enemy_power_shot_ += power;
    enemy_power_shot_ += power;
  }
  public static void RemoveShot(double power, double enemy_power) {
    --total_shots_; --shots_;
    --total_enemy_shots_; --enemy_shots_;
    total_power_shot_ -= power;
    power_shot_ -= power;
    total_enemy_power_shot_ -= enemy_power;
    enemy_power_shot_ -= enemy_power;
  }
  public static void RemoveMyShots(double power, double power2) {
    --total_shots_; --shots_;
    --total_shots_; --shots_;
    total_power_shot_ -= power + power2;
    power_shot_ -= power + power2;
  }
  public static void onBulletHit(BulletHitEvent event) {
    double power = event.getBullet().getPower();
    double damage = Rules.getBulletDamage(power);
    damage_given_ += damage;
    total_damage_given_ += damage;
    ++hits_; ++total_hits_;
    total_power_hitted_ += power;
    power_hitted_ += power;
  }
  public static void onHitByBullet(HitByBulletEvent event) {
    double power = event.getBullet().getPower();
    double damage = Rules.getBulletDamage(power);
    damage_taken_ += damage;
    total_damage_taken_ += damage;
    ++enemy_hits_; ++total_enemy_hits_;
    total_enemy_power_hitted_ += power;
    enemy_power_hitted_ += power;
  }
  public static double HitRate() {
    if ( shots_ == 0 ) return 0;
    return (double)hits_ / shots_;
  }
  public static double TotalHitRate() {
    if ( total_shots_ == 0 ) return 0;
    return (double)total_hits_ / total_shots_;
  }
  public static double PowerHitRate() {
    if ( power_shot_ == 0 ) return 0;
    return power_hitted_ / power_shot_;
  }
  public static double TotalPowerHitRate() {
    if ( total_power_shot_ == 0 ) return 0;
    return total_power_hitted_ / total_power_shot_;
  }
  public static double EnemyHitRate() {
    if ( enemy_shots_ == 0 ) return 0;
    return (double)enemy_hits_ / enemy_shots_;
  }
  public static double TotalEnemyHitRate() {
    if ( total_enemy_shots_ == 0 ) return 0;
    return (double)total_enemy_hits_ / total_enemy_shots_;
  }
  public static double EnemyPowerHitRate() {
    if ( enemy_power_shot_ == 0 ) return 0;
    return (double)enemy_power_hitted_ / enemy_power_shot_;
  }
  public static double TotalEnemyPowerHitRate() {
    if ( total_enemy_power_shot_ == 0 ) return 0;
    return (double)total_enemy_power_hitted_ / total_enemy_power_shot_;
  }
  public static void onRobotDeath(AdvancedRobot robot) {
    ++wins_;
    LogData(robot);
  }
  public static void onDeath(AdvancedRobot robot) {
    ++losses_;
    LogData(robot);
  }
  public static void LogData(AdvancedRobot robot) {
    double hr = HitRate();
    double thr = TotalHitRate();
    double phr = PowerHitRate();
    double tphr = TotalPowerHitRate();
    double ehr = EnemyHitRate();
    double tehr = TotalEnemyHitRate();
    double ephr = EnemyPowerHitRate();
    double tephr = TotalEnemyPowerHitRate();
    int round = robot.getRoundNum() + 1;
    robot.out.printf(
        "Skipped turns: %d (%d)\n" +
        "\tper round: %.2f (%.2f projected to 35 rounds)\n" +
        "My Hit Damage: %.2f (%.2f)\n" +
        "\tper round: %.2f (%.2f projected to 35 rounds)\n" +
        "Enemy Hit Damage: %.2f (%.2f)\n" +
        "\tper round: %.2f (%.2f projected to 35 rounds)\n" +
        "Damage ratio: %.2f (%.2f)\n" +
        "Win/Loss -> ratio: %d/%d -> %.2f\n" +
        "My Hit Rate: %.4f (%.4f)\n" +
        "Enemy Hit Rate: %.4f (%.4f)\n" +
        "My Power Hit Rate: %.4f (%.4f)\n" +
        "Enemy Power Hit Rate: %.4f (%.4f)\n" +
        "",
        total_skipped_, skipped_, (double)total_skipped_/ round, 35.0 * total_skipped_/ round,
        total_damage_given_, damage_given_, total_damage_given_ / round, 35 * total_damage_given_ / round,
        total_damage_taken_, damage_taken_, total_damage_taken_ / round, 35 * total_damage_taken_ / round,
        total_damage_given_ / (total_damage_given_ + total_damage_taken_), damage_given_ / (damage_given_ + damage_taken_),
        wins_, losses_, (double)wins_/round,
        thr, hr, tehr, ehr,
        tphr, phr, tephr, ephr
    );
  }
  public static void onSkippedTurn(PrintStream out) {
    out.printf("URGENT: Skipped turn: %d, %d\n", ++skipped_, ++total_skipped_);
  }
}
