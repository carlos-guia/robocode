package zyx.old.mega.movement;

import java.util.ArrayList;

import zyx.old.debug.Printer;
import zyx.old.mega.bot.Bot;
import zyx.old.mega.bot.Enemy;
import zyx.old.mega.geometry.Bullet;
import zyx.old.mega.geometry.Geometry;
import zyx.old.mega.utils.PerformanceTracker;
import zyx.old.mega.utils.TurnHandler;
import zyx.old.mega.utils.Wave;
import zyx.old.mega.utils.wave.WaveHit;

public abstract class WaveSurfing extends EnemyDodging {
  //protected static final double PRECISE = 1;
  //protected static final double EXTENDED = 1;
  protected static final double FIRST = 0.8;
  protected static final double SECOND = 1 - FIRST;
  protected static final double THRESHOLD1 = 0.1;
  protected static final double THRESHOLD2 = 0.2;

  public ArrayList<Wave> waves_;

  public Wave surf_wave1_;
  public Wave surf_wave2_;

  public int hits_;
  public String crazy_log_;
  public long last_shot_;

  public WaveSurfing(Enemy enemy) {
    super(enemy);
    hits_ = 0;
  }
  public void Init() {
    waves_ = new ArrayList<Wave>();
    last_shot_ = -10;
  }
  public void onHitByBullet(long time, Bullet bullet) {
    double min_distance = Double.POSITIVE_INFINITY;
    Wave hit_wave = null;
    int hit_i = -1;
    for ( int i = 0; i < waves_.size(); ++i ) {
      Wave wave = waves_.get(i);
      crazy_log_ += String.format("%d) v: %.5f %.5f\n", i, bullet.velocity_, wave.velocity_);
      if ( Math.abs(wave.velocity_ - bullet.velocity_) < 1e-1 + 1e-9 ) {
        wave.Update(time);
        double distance = Math.abs(wave.radius_ - Geometry.Distance(wave, bullet));
        crazy_log_ += String.format("%d) d: %.5f %.5f %.5f\n",
            i, Geometry.Distance(wave, bullet), wave.radius_, distance);
        if ( distance < min_distance ) {
          min_distance = distance;
          hit_wave = wave;
          hit_i  = i;
        }
      }
    }
    if ( hit_wave == null ) Printer.printf(0, "missed wave: %s\n", crazy_log_);
    else {
      hit_wave.snapshot_.ws_hit_ = true;
      hit_wave.snapshot_.ws_hit_factor_ = hit_wave.Factor(bullet);
      //Printer.printf(0, "hit factor: %.4f\n", hit_wave.snapshot_.ws_hit_factor_);
      if ( UpdateDanger(hit_wave) ) {
        waves_.remove(hit_i);
      }
    }
  }
  public void onScannedRobot(boolean new_shot, double power) {
    if ( new_shot ) CreateWave(power);
    //TurnHandler.robot_.out.printf("ws.scan(%b, %.2f)\n", new_shot, power);
    UpdateWaves();
    if ( TurnHandler._moved_ ) return;
    if ( surf_wave1_ == null ) Position();
    else Surf();
    /**
    if ( surf_wave1_ != null ) {
      double distance = 120;
      double angle = Bot.me_.OrbitAngle(surf_wave1_, enemy_.orbit_direction_);
      TurnHandler.Move(distance, angle, true);
    }
    /**/
  }
  private void Position() {
    Bot left = Bot.CloneMe();
    Bot right = Bot.CloneMe();
    boolean try_approach = false;
    if ( enemy_.energy_ * 10 >= Bot.me_.energy_ ) {
      if ( Geometry.Distance(enemy_, Bot.me_) > 400 ) try_approach = true;
    } else if ( enemy_.energy_ < 3 ) {
      try_approach = true;
    }
    for ( int i = 0; i < 40; ++i ) {
      left.OrbitSimple(enemy_, -1, try_approach);
      right.OrbitSimple(enemy_, 1, try_approach);
    }
    int approach = 1;
    if ( Geometry.Distance(left, enemy_) < Geometry.Distance(right, enemy_) ) approach = -1;
    int retreat = -approach;
    double angle = Bot.me_.OrbitAngleSimple(enemy_, try_approach ? approach : retreat, try_approach);
    TurnHandler.Move(enemy_.energy_ < 3 ? 0 : 120, angle, true);
  }
  private Wave FakeWave() {
    if ( enemy_._1ago_ == null ) return null;
    Wave wave = new Wave(enemy_);
    wave.snapshot_ = enemy_._1ago_;
    wave.time_ = TurnHandler.time_;
    wave.bearing_ = wave.snapshot_.me_.bearing_;
    wave.direction_ = wave.snapshot_.me_.direction_;
    wave.SetPower(enemy_.avg_fire_power_.average_);
    return wave;
  }
  private void CreateWave(double fire_power) {
    if ( enemy_._2ago_ == null ) return;
    PerformanceTracker.AddEnemyShot(fire_power);
    Wave wave = new Wave(enemy_._1ago_.enemy_);
    wave.snapshot_ = enemy_._2ago_;
    wave.time_ = TurnHandler.time_ - 1;
    wave.bearing_ = wave.snapshot_.me_.bearing_;
    wave.direction_ = wave.snapshot_.me_.direction_;
    wave.SetPower(fire_power);
    waves_.add(wave);
    /*if ( fire_power > 0.099 )*/ last_shot_ = wave.time_;
  }
  protected void UpdateWaves() {
    surf_wave1_ = surf_wave2_ = null;
    double run_time1 = Double.POSITIVE_INFINITY;
    double run_time2 = Double.POSITIVE_INFINITY;
    for ( int i = 0; i < waves_.size(); ++i ) {
      Wave wave = waves_.get(i);
      wave.Update(TurnHandler.time_);
      //if ( enemy_.dead_ ) 
      //Painter.Add(0, wave);
      WaveHit hit = wave.Hit(Bot.me_);
      //TurnHandler.robot_.out.printf("waves: %.2f : %d\n", wave.radius_, hit.info_);
      boolean as_first = false;
      boolean as_second = false;
      if ( hit.AllIn() ) {
        //Printer.printf(0, "flatten window: %s\n", wave.snapshot_.ws_hit_factor_window_);
        UpdateFlattener(wave);
        if ( !wave.snapshot_.ws_hit_ ) enemy_.accuracy_.Roll(0, 1);
        waves_.remove(i--);
      } else if ( hit.Hitting() ) {
        as_first = true;
        wave.UpdateWS(hit.corners_);
      } else if ( hit.AllOut() ) {
        if ( hit.run_time_ < run_time1 ) {
          as_first = true;
        } else if ( hit.run_time_ < run_time2 ) {
          as_second = true;
        }
      }
      if ( !wave.snapshot_.ws_hit_ ) {
        if ( as_first ) {
          run_time2 = run_time1;
          surf_wave2_ = surf_wave1_;
          run_time1 = hit.run_time_;
          surf_wave1_ = wave;
        } else if ( as_second ) {
          run_time2 = hit.run_time_;
          surf_wave2_ = wave;
        }
      }
    }
    if ( surf_wave1_ == null ) {
      if ( TurnHandler.time_ - last_shot_ < 30 ||
          (TurnHandler.time_ > 25 && TurnHandler.time_ < 31) ) surf_wave1_ = FakeWave();
      else return;
    }
    if ( surf_wave2_ == null ) {
      surf_wave2_ = FakeWave();
    }
    if ( surf_wave1_ != null ) {
      //Painter.Add(0, surf_wave1_);
    }
    if ( surf_wave2_ != null ) {
      //Painter.Add(0, surf_wave2_);
    }
  }
  protected abstract void UpdateFlattener(Wave wave);
  protected abstract boolean UpdateDanger(Wave wave);
  protected abstract void Surf();
}
