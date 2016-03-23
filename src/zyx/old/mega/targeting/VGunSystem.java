package zyx.old.mega.targeting;

import java.util.ArrayList;

import robocode.Bullet;
import zyx.old.debug.Printer;
import zyx.old.mega.bot.Bot;
import zyx.old.mega.bot.Enemy;
import zyx.old.mega.utils.Snapshot;
import zyx.old.mega.utils.TurnHandler;
import zyx.old.mega.utils.wave.WaveHit;
import zyx.old.simonton.utils.MyTree;

public class VGunSystem {
  private Enemy enemy_;
  private VGun[] guns_;
  private ArrayList<ShootingWave> waves_;
  public ShootingWave next_wave_;
  MyTree<Snapshot> tree_;
  MyTree<Snapshot> hit_tree_;
  private int last_gun_;
  private long rating_time_;
  private double best_rating_;
  private long rating_time_fast_;
  private double best_rating_fast_;
  private int last_mode_;

  public VGunSystem(Enemy enemy) {
    enemy_ = enemy;
    guns_ = new VGun[] {
        new GFTargetingDC_C(enemy_, this),
        new GFTargetingDC_A(enemy_, this),
        new GFTargetingDC_B(enemy_, this),
    };
    tree_ = new MyTree<Snapshot>(Snapshot.ATTRIBUTES, 10, 1, 500);
    hit_tree_ = new MyTree<Snapshot>(Snapshot.ATTRIBUTES, 10, 1, 500);
  }

  public void Init() {
    waves_ = new ArrayList<ShootingWave>();
    next_wave_ = null;
    for (VGun gun : guns_) {
      gun.Init();
    }
  }

  public void onScannedRobot() {
    FireWave();
    UpdateWaves();
    CreateWave();
  }

  private void CreateWave() {
    next_wave_ = new ShootingWave();
    next_wave_.snapshot_ = enemy_._now_;
    Bot enemy = next_wave_.snapshot_.enemy_;
    next_wave_.bearing_ = enemy.bearing_;
    next_wave_.direction_ = enemy.direction_;
    next_wave_.SetPower(enemy_.fire_power_);
    //enemy_._now_.gf_normal_[Snapshot.BULLET_HIT_TIME] = Range.Normalize(enemy_._now_.distance_ / next_wave_.velocity_, 3, 80, false);
    VShot[] shots = next_wave_.virtual_shots_ = new VShot[guns_.length * 2];
    int i = 0;
    double tank_turn = Bot.NextTurn();
    double gun_heading = Bot.robot_.getGunHeadingRadians();
    double angle = enemy_.bearing_;
    double best = -1;
    int k = 0;
    for (VGun gun : guns_) {
      gun.Update();
      /* Anti-Surfer gun */ {
        shots[i] = new VShot(gun, tank_turn, gun_heading, true);
        //Printer.printf(0, "[%d %.5f %.5f %.5f]\n", i, gun.RatingAS(), gun.RatingFastAS(), gun.ComposedRatingAS());
        double rating = gun.ComposedRatingAS();
        if ( rating > best ) {
          angle = gun.as_aim_angle_;
          best = rating;
          k = i;
        }
        ++i;
      }
      /* Normal gun */ {
        shots[i] = new VShot(gun, tank_turn, gun_heading, false);
        //Printer.printf(0, "[%d %.5f %.5f %.5f]\n", i, gun.Rating(), gun.RatingFast(), gun.ComposedRating());
        double rating = gun.ComposedRating();
        if ( rating > best ) {
          angle = gun.aim_angle_;
          best = rating;
          k = i;
        }
        ++i;
      }
    }
    int gun = k / 2;
    int mode = k % 2;
    if ( last_gun_ != gun || last_mode_ != mode ) {
      Printer.printf(0, "Current gun: %d %s\n", gun, mode == 0 ? "Anti-Surfer" : "Normal");
    }
    last_gun_ = gun;
    last_mode_ = mode;
    TurnHandler.AimGun(angle);
  }

  private void UpdateWaves() {
    for ( int i = 0; i < waves_.size(); ++i ) {
      ShootingWave wave = waves_.get(i);
      wave.Update(TurnHandler.time_);
      //Painter.Add(0, wave);
      WaveHit hit = wave.Hit(enemy_);
      UpdateVirtualHits(wave);
      if ( !hit.AllOut() ) {
        if ( hit.AllIn() ) {
          wave.UpdateGF(hit.bbox_, hit.corners_);
          UpdateVirtualMisses(wave);
          UpdateDanger(wave.snapshot_);
          waves_.remove(i--);
        } else if ( hit.Hitting() ) {
          wave.UpdateGF(hit.bbox_, hit.corners_);
        }
      }
    }
  }
  
  private void UpdateDanger(Snapshot snapshot) {
    if ( snapshot.gf_bbox_factor_window_.window_ == null ) {
      snapshot.gf_bbox_factor_window_.SetWindow(snapshot.gf_corner_factor_window_, true);
    }
    if ( snapshot.gf_hit_ == Snapshot.SPOT ) tree_.add(snapshot.gf_normal_, snapshot);
    else hit_tree_.add(snapshot.gf_normal_, snapshot);
    /**
    Printer.printf(0, "up danger: [%s] [%s] [%b %.4f]\n",
        snapshot.gf_bbox_factor_window_,
        snapshot.gf_corner_factor_window_,
        snapshot.gf_hit_, snapshot.gf_hit_factor_);
    /**/
  }

  private void UpdateVirtualHits(ShootingWave wave) {
    for (VShot shot : wave.virtual_shots_) if ( !shot.flagged_ ) {
      //if ( shot.anti_surfer_ ) Painter.Add(0, new Circle(shot, 3));
      if ( enemy_.bbox_.Interescts(shot) ) {
        shot.flagged_ = true;
        if ( shot.anti_surfer_ ) {
          shot.gun_.LogHitAS(wave.snapshot_);
        } else {
          shot.gun_.LogHit(wave.snapshot_);
        }
      }
    }
  }

  private void UpdateVirtualMisses(ShootingWave wave) {
    for (VShot shot : wave.virtual_shots_) if ( !shot.flagged_ ) {
      if ( shot.anti_surfer_ ) {
        shot.gun_.LogMissAS(wave.snapshot_);
      } else {
        shot.gun_.LogMiss(wave.snapshot_);
      }
    }
  }

  public void FireWave() {
    if ( next_wave_ != null ) {
      if ( (next_wave_.bullet_ = TurnHandler.FireBullet(next_wave_.fire_power_, enemy_)) != null ) {
        if ( next_wave_.fire_power_ > 1.8 ) {
          next_wave_.time_ = TurnHandler.time_;
          next_wave_.SetPoint(Bot.me_);
          for (VShot shot : next_wave_.virtual_shots_) {
            shot.SetPoint(Bot.me_);
          }
          waves_.add(next_wave_);
        }
      }
      next_wave_ = null;
    }
  }

  public double BestRating() {
    if ( rating_time_ == TurnHandler.time_ ) return best_rating_;
    rating_time_ = TurnHandler.time_;
    best_rating_ = -1;
    for (VGun gun : guns_) {
      double g_rating = Math.max(gun.Rating(), gun.RatingAS());
      if ( g_rating > best_rating_ ) {
        best_rating_ = g_rating;
      }
    }
    return best_rating_;
  }
  public double BestRatingFast() {
    if ( rating_time_fast_ == TurnHandler.time_ ) return best_rating_fast_;
    rating_time_fast_ = TurnHandler.time_;
    best_rating_fast_ = -1;
    for (VGun gun : guns_) {
      double g_rating = Math.max(gun.RatingFast(), gun.RatingFastAS());
      if ( g_rating > best_rating_fast_ ) {
        best_rating_fast_ = g_rating;
      }
    }
    return best_rating_fast_;
  }

  public void onBulletHit(Bullet bullet) {
    for (ShootingWave wave : waves_) if ( wave.bullet_ == bullet ) {
      wave.snapshot_.gf_hit_ = Snapshot.BOT_HIT;
      wave.snapshot_.gf_hit_factor_ = wave.Factor(new zyx.old.mega.geometry.Bullet(bullet));
      break;
    }
  }
  public void onBulletHitBullet(Bullet bullet) {
    for (ShootingWave wave : waves_) if ( wave.bullet_ == bullet ) {
      wave.snapshot_.gf_hit_ = Snapshot.BULLET_HIT;
      wave.snapshot_.gf_hit_factor_ = wave.Factor(new zyx.old.mega.geometry.Bullet(bullet));
      break;
    }
  }
}
