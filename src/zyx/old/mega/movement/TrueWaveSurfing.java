package zyx.old.mega.movement;

import zyx.old.mega.bot.Bot;
import zyx.old.mega.bot.Enemy;
import zyx.old.mega.utils.Range;
import zyx.old.mega.utils.TurnHandler;
import zyx.old.mega.utils.Wave;
import zyx.old.mega.utils.wave.WaveHit;

public abstract class TrueWaveSurfing extends WaveSurfing {
  private double surf_danger_[];
  private long surf_time_[];

  public TrueWaveSurfing(Enemy enemy) {
    super(enemy);
    surf_danger_ = new double[2];
    surf_time_ = new long[2];
  }

  protected void Surf() {
    if ( surf_wave1_ == null ) return;
    surf_danger_[0] = surf_danger_[1] = Double.POSITIVE_INFINITY;
    Surf(Bot.CloneMe(), new Wave(surf_wave1_), -1);
    Surf(Bot.CloneMe(), new Wave(surf_wave1_), 1);
    if ( Math.abs(surf_danger_[0] - surf_danger_[1]) > 1e-5 ) {
      if ( surf_danger_[0] < surf_danger_[1] ) {
        enemy_.orbit_direction_ = -1;
      } else {
        enemy_.orbit_direction_ = 1;
      }
    }
    double distance = surf_time_[Math.max(0, enemy_.orbit_direction_)] == 0 ? 0 : 120;
    double angle = Bot.me_.OrbitAngle(surf_wave1_, enemy_.orbit_direction_, enemy_);
    TurnHandler.Move(distance, angle, true);
    /**
    Printer.printf(0, "%.2f %d : %d : %.2f %d\n(%.2f, %.2f)\n",
        surf_danger_[0], surf_time_[0],
        surf_direction_,
        surf_danger_[1], surf_time_[1],
        distance, angle
        );
    /**/
  }

  private void Surf(Bot me, Wave wave, int direction) {
    Range window = new Range();
    WaveHit hit = null;
    long t = 0;
    for ( ; t < 100; ++t, me.Orbit(wave, direction, false, enemy_) ) {
      //Printer.printf(0, "%d %.2f %.2f\n", direction, me.heading_, me.velocity_);
      wave.Update(TurnHandler.time_ + t);
      hit = wave.Hit(me);
      if ( hit.AllIn() ) break;
      else {
        double danger = StopDanger(t, new Bot(me), wave, direction);
        if ( hit.Hitting() ) {
          window.Update(hit.corners_);
        } else if ( hit.AllOut() ) {
          if ( danger - 1e-9 < surf_danger_[Math.max(0, direction)] ) {
            surf_danger_[Math.max(0, direction)] = danger;
            surf_time_[Math.max(0, direction)] = t;
          }
        }
      }
    }
    if ( window.Size() == 0 ) return;
    //Range reach = FindReach(t + (long)hit.wait_time_, me, new Wave(surf_wave2_));
    double danger = Danger(window, surf_wave2_.Hit(me).corners_);
    if ( danger - 1e-9 < surf_danger_[Math.max(0, direction)] ) {
      surf_danger_[Math.max(0, direction)] = danger;
      surf_time_[Math.max(0, direction)] = t;
    }
  }
  private double StopDanger(long time, Bot me, Wave wave, int direction) {
    Range window = new Range();
    WaveHit hit = null;
    long t = time;
    for ( ; t < 100; ++t, me.Orbit(wave, direction, true, enemy_) ) {
      wave.Update(TurnHandler.time_ + t);
      hit = wave.Hit(me);
      if ( me.velocity_ == 0 ) {
        //TurnHandler.robot_.out.printf("%s\n", hit.corners_);
        window.Update(hit.corners_);
        break;
      } else {
        if ( hit.Hitting() ) {
          window.Update(hit.corners_);
        } else if ( hit.AllIn() ) {
          break;
        }
      }
    }
    if ( window.Size() == 0 ) {
      TurnHandler.robot_.out.printf("window size is 0: %d\n", t);
      return 0;
    }
    //Range reach = FindReach(t + (long)hit.wait_time_, me, new Wave(surf_wave2_));
    return Danger(window, surf_wave2_.Hit(me).corners_);
  }
  /**
  private Range FindReach(long time, Bot me, Wave wave) {
    Range reach = new Range();
    Bot left = new Bot(me);
    Bot right = new Bot(me);
    boolean ok = true;
    while ( ok && time < 150 ) {
      wave.Update(time);
      left.Orbit(wave, -1, false);
      right.Orbit(wave, 1, false);
      WaveHit left_hit = wave.Hit(left);
      WaveHit right_hit = wave.Hit(right);
      if ( left_hit.info_ != WaveHit.ALL_IN ) {
        reach.Update(left_hit.corners_);
      }
      if ( right_hit.info_ != WaveHit.ALL_IN ) {
        reach.Update(right_hit.corners_);
      }
      ok = left_hit.info_ != WaveHit.ALL_IN || right_hit.info_ != WaveHit.ALL_IN;
      ++time;
    }
    reach.CapWindow(-1, 1);
    return reach;
  }
  /**/

  protected abstract double Danger(Range window, Range window2);
}
