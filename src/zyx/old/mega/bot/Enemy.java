package zyx.old.mega.bot;

import java.util.ArrayList;

import robocode.Bullet;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import zyx.old.debug.Printer;
import zyx.old.mega.geometry.Geometry;
import zyx.old.mega.movement.TrueWaveSurfingDC;
import zyx.old.mega.movement.WaveSurfing;
import zyx.old.mega.targeting.VGunSystem;
import zyx.old.mega.utils.Config;
import zyx.old.mega.utils.PerformanceTracker;
import zyx.old.mega.utils.Range;
import zyx.old.mega.utils.RollingAverage;
import zyx.old.mega.utils.Snapshot;
import zyx.old.mega.utils.TurnHandler;

public class Enemy extends Bot {
  private static final int NONE = 0;
  private static final int MY_FAULT = 1;
  private static final int HIS_FAULT = 2;
  private static ArrayList<Enemy> enemies_ = new ArrayList<Enemy>();

  public static Enemy Find(String name) {
    for (Enemy enemy : enemies_)
      if (enemy.name_.equals(name))
        return enemy;
    Enemy enemy = new Enemy(name);
    enemies_.add(enemy);
    return enemy;
  }

  public static ArrayList<Enemy> Phonebook() {
    return enemies_;
  }

  public String name_;
  public long scan_time_;
  public double distance_;
  public double my_lateral_velocity_;
  public double my_approaching_velocity_;
  public int my_direction_;
  public VGunSystem gun_;
  // private MeleeGFTargeting melee_gun_;
  public WaveSurfing wave_surfing_;
  // private StopAndGo stop_and_go_;
  // private AntiRammerMovement anti_rammer_;
  public RollingAverage avg_fire_power_;
  public RollingAverage accuracy_;

  public ArrayList<Snapshot> log_;
  public Snapshot _now_;
  public Snapshot _1ago_;
  public Snapshot _2ago_;
  public double damage_taken_;
  public double damage_given_;
  public int orbit_direction_;
  public boolean dead_;
  public int rams_;
  public boolean rammer_ = true;
  public double fire_power_;
  public boolean ready_to_fire_;
  // private boolean RAM_ATT;
  private int hit_robot_;

  public Enemy(String name) {
    name_ = name;
    log_ = new ArrayList<Snapshot>();
    wave_surfing_ = new TrueWaveSurfingDC(this);
    // stop_and_go_ = new StopAndGo(this);
    // anti_rammer_ = new AntiRammerMovement(this);
    gun_ = new VGunSystem(this);
    avg_fire_power_ = new RollingAverage(3);
    accuracy_ = new RollingAverage(101);
    orbit_direction_ = 1;
    rams_ = 0;
    Init();
  }

  public void Init() {
    // RAM_ATT = false;
    time_running_ = time_stopped_ = 0;
    direction_ = my_direction_ = 1;
    _now_ = _1ago_ = _2ago_ = null;
    energy_ = 100;
    dead_ = false;
    wave_surfing_.Init();
    // anti_rammer_.Init();
    gun_.Init();
  }

  public void onBulletHit(BulletHitEvent event) {
    Bullet bullet = event.getBullet();
    energy_ -= Rules.getBulletDamage(bullet.getPower());
    damage_given_ += Rules.getBulletDamage(bullet.getPower());
    gun_.onBulletHit(bullet);
    // Printer.printf(0, "real hit: %.4f %.4f\n", event.getBullet().getX(),
    // event.getBullet().getY());
  }

  public void onBulletHitBullet(long time, Bullet bullet, zyx.old.mega.geometry.Bullet hit_bullet) {
    wave_surfing_.crazy_log_ = "Bullet Hit Bullet\n";
    wave_surfing_.onHitByBullet(time - 1, hit_bullet);
    gun_.onBulletHitBullet(bullet);
  }

  public void onHitByBullet(HitByBulletEvent event) {
    energy_ += Rules.getBulletHitBonus(event.getPower());
    wave_surfing_.crazy_log_ = "Hit By Bullet\n";
    wave_surfing_.onHitByBullet(TurnHandler.time_,
        new zyx.old.mega.geometry.Bullet(event.getBullet()));
    ++wave_surfing_.hits_;
    accuracy_.Roll(1, 1);
    damage_taken_ += Rules.getBulletDamage(event.getPower());
  }

  public void onHitRobot(HitRobotEvent event) {
    if (!event.isMyFault()) {
      hit_robot_ = MY_FAULT;
    } else {
      ++rams_;
      hit_robot_ = HIS_FAULT;
    }
  }

  public void onHitWall(HitWallEvent event) {}

  public void onRobotDeath(RobotDeathEvent event) {
    dead_ = true;
    if (Config.targeting_enabled_)
      gun_.onScannedRobot();
    // Log();
    if (TurnHandler.robot_.getOthers() == 0) {
      KeepSurfing();
      while (true) {
        robot_.setFireBullet(Rules.MIN_BULLET_POWER);
        robot_.turnRight(60);
        robot_.setFireBullet(Rules.MIN_BULLET_POWER);
        robot_.turnLeft(60);
      }
    }
  }

  /**
   * public void Log() { Printer.printf(0, "%-30s%-8s%-8s%-8s\n", "Enemy", "Given", "Taken",
   * "Ratio"); Printer.printf(0, "%-30s%-8.2f%-8.2f%-8.2f\n", name_, damage_given_, damage_taken_,
   * BulletRatio()); } /** public double BulletRatio() { if ( damage_given_ == 0 ) return 0; return
   * damage_given_ / (damage_given_ + damage_taken_); } /
   **/
  private void KeepSurfing() {
    if (Config.movement_enabled_) {
      robot_.setTurnGunRightRadians(Double.POSITIVE_INFINITY);
      while (wave_surfing_.waves_.size() > 0) {
        TurnHandler.handler_.Update();
        wave_surfing_.onScannedRobot(false, 0);
        if (me_.energy_ > 16)
          robot_.setFire(Rules.MIN_BULLET_POWER);
        Printer.onPrint(robot_.out);
        // Painter.onPaint(robot_.getGraphics());
        robot_.execute();
      }
    }
  }

  public void onScannedRobot(ScannedRobotEvent event) {
    // Printer.printf(0, "scan time: %d %d\n", TurnHandler.time_, TurnHandler.robot_.getTime());
    if (_1ago_ == null) {
      gun_heat_ = 3.1 - TurnHandler.time_ * 0.1;
    }
    double energy_drop = energy_ - event.getEnergy();
    double last_velocity = velocity_;
    long elapsed = TurnHandler.time_ - scan_time_;
    scan_time_ = TurnHandler.time_;
    bearing_ = Utils.normalAbsoluteAngle(me_.heading_ + event.getBearingRadians());
    distance_ = event.getDistance();
    energy_ = event.getEnergy();
    heading_ = event.getHeadingRadians();
    velocity_ = event.getVelocity();
    gun_heat_ = Range.CapLow(gun_heat_ - robot_.getGunCoolingRate(), 0);
    ProjectPoint(me_, bearing_, distance_);
    lateral_velocity_ = velocity_ * Math.sin(heading_ - bearing_);
    if (velocity_ != 0)
      direction_ = lateral_velocity_ < 0 ? -1 : 1;
    my_lateral_velocity_ = me_.velocity_ * Math.sin(event.getBearingRadians());
    if (me_.velocity_ != 0)
      my_direction_ = my_lateral_velocity_ < 0 ? -1 : 1;
    approaching_velocity_ = velocity_ * Math.cos(heading_ - bearing_);
    my_approaching_velocity_ = me_.velocity_ * Math.cos(me_.heading_ - bearing_ + Geometry.PI);
    // Printer.printf(0, "e.up: %.5f\n", bearing_);
    // Printer.printf(0, "gh: %.5f %.5f\n", me_.gun_heat_, gun_heat_);
    FinishUpdate();
    // Painter.Add(0, this);
    UpdateFirePower();
    // Printer.printf(0, "fp: %.5f\n", fire_power_);
    CreateSnapshot(elapsed);


    if (Config.movement_enabled_ && TurnHandler._1v1_) {
      if (_1ago_ != null && velocity_ == 0) {
        if (hit_robot_ == NONE) {
          double wall_damage = Math.max(0, Math.abs(last_velocity) / 2 - 1);
          // Printer.printf(0, "You hitted a wall at %.2f for a %.2f damage\n", last_velocity,
          // wall_damage);
          energy_drop -= wall_damage;
        } else {
          double hit_damage = 0.6;
          if (hit_robot_ == MY_FAULT && last_velocity * Math.cos(heading_ - bearing_) < 0) {
            hit_damage += 0.6;
          }
          energy_drop -= hit_damage;
        }
      }
      boolean new_shot =
          gun_heat_ < robot_.getGunCoolingRate() * 2 && energy_drop + 1e-9 > Rules.MIN_BULLET_POWER
              && energy_drop - 1e-9 < Rules.MAX_BULLET_POWER;
      if (new_shot) {
        // Printer.printf(0, "saw a shot\n");
        avg_fire_power_.Roll(energy_drop, 1);
        gun_heat_ = 1 + energy_drop / 5 - robot_.getGunCoolingRate();
      }
      if (!rammer_) {
        rammer_ = (double) rams_ / TurnHandler.exact_time_ > 0.1;
      }
      /**
       * if ( gun_heat_ == 0 ) { Printer.printf(0, "can fire\n"); } Printer.printf(0, "rammer: %b\n"
       * , rammer_); if ( rammer_ ) { anti_rammer_.onScannedRobot(new_shot, energy_drop); } /
       **/
      if (energy_ == 0 && wave_surfing_.waves_.size() == 0) {
        // RAM_ATT = true;
        TurnHandler.Move(1000, bearing_, true);
      }
      wave_surfing_.onScannedRobot(new_shot, energy_drop);
      // stop_and_go_.onScannedRobot(new_shot, energy_drop);
    }
    if (Config.targeting_enabled_) {
      if (TurnHandler._1v1_) {
        gun_.onScannedRobot();
      }
      // if ( TurnHandler._melee_ ) melee_gun_.onScannedRobot();
    }
    if (!Config._raiko_) {
      robot_.setTurnRadarRightRadians(
          Utils.normalRelativeAngle(bearing_ - robot_.getRadarHeadingRadians()) * 1.999);
    }
    hit_robot_ = NONE;
  }

  private void UpdateFirePower() {
    if (Config._tc_)
      fire_power_ = 3;
    else if (Config._pc_)
      fire_power_ = 0.5;
    else if (Config._mc_)
      fire_power_ = 0;
    else if (Config._raiko_fire_power_)
      SetRaikoFirePower();
    else {
      if (energy_ == 0) {
        if (wave_surfing_.waves_.size() == 0) {
          fire_power_ = 0;
          // RAM_ATT = true;
        } else {
          fire_power_ = Rules.MIN_BULLET_POWER;
        }
      } else {
        if (energy_ + 1e-9 < 4) {
          fire_power_ = energy_ / 4 + 1e-5;
        } else {
          fire_power_ = Math.max(1 + 1e-9, (energy_ + 2) / 10);
        }
        if (distance_ < 80) {
          fire_power_ = Math.min(fire_power_, 3);
        } else {
          if (distance_ < 120) {
            fire_power_ = Math.min(fire_power_, 3);
          } else if (PerformanceTracker.TotalHitRate() > 0.5) {
            fire_power_ = Math.min(fire_power_, 3);
          } else if (PerformanceTracker.TotalHitRate() > 0.3) {
            fire_power_ = Math.min(fire_power_, 2);
          } else {
            double noise =
                Math.random() * Rules.MIN_BULLET_POWER * 1 - Rules.MIN_BULLET_POWER * 0.25;
            fire_power_ = Math.min(fire_power_,
                (PerformanceTracker.TotalHitRate() < 0.01 ? 1.1 : 1.9) + noise);

            if ((me_.energy_ > energy_ + 30 && me_.energy_ > energy_ * 2.2)
                || me_.energy_ > energy_ * 6) {
            } else {
              if ((me_.energy_ > energy_ + 20 && me_.energy_ > energy_ * 1.8)
                  || me_.energy_ > energy_ * 4)
                fire_power_ = Math.min(fire_power_,
                    Math.random() * 4 * Rules.MIN_BULLET_POWER + Rules.MIN_BULLET_POWER);
              else  if (me_.energy_ > energy_ + Rules.getBulletHitBonus(avg_fire_power_.average_) * 2)
                fire_power_ = 0;
              else if (me_.energy_ > energy_ + Rules.getBulletHitBonus(avg_fire_power_.average_))
                fire_power_ = Math.min(fire_power_,
                    Rules.MIN_BULLET_POWER + gun_.BestRating() * Math.random());
              else if (me_.energy_ >= 100 && energy_ == 100)
                fire_power_ = 0;
              else if (me_.energy_ >= 100
                  && energy_ + Rules.getBulletHitBonus(Rules.MAX_BULLET_POWER) < me_.energy_ )
                fire_power_ = Math.random() < 0.9 ? 0 : Rules.MIN_BULLET_POWER;
              
              if (energy_ == 100 && energy_ == 100 && TurnHandler.time_ > 150)
                fire_power_ = Math.random() < 0.9 ? 0 : Rules.MIN_BULLET_POWER;
            }

            double hitRate = PerformanceTracker.HitRate();
            if (hitRate < 0.01 && distance_ > 500)
              fire_power_ = Math.min(fire_power_, 0.8);
            if (hitRate < 0.05 && distance_ > 500)
              fire_power_ = Math.min(fire_power_, 1.1);
            if (hitRate > 0.4 && distance_ < 200)
              fire_power_ = Math.min(fire_power_, 3);
            if (hitRate > 0.5 && distance_ < 350)
              fire_power_ = Math.min(fire_power_, 3);
            if (hitRate > 0.8 && distance_ < 500)
              fire_power_ = Math.min(fire_power_, 3);
            if (damage_taken_ == 0)
              fire_power_ = Math.min(fire_power_, Rules.MIN_BULLET_POWER);
            if (me_.energy_ > energy_)
              fire_power_ = Math.min(fire_power_,
                  Math.max(0, me_.energy_ - energy_ - Rules.MIN_BULLET_POWER));
            // else if ( gun_.BestRatingFast() > 0.4 && me_.energy_ > 60 ) fire_power_ =
            // Math.min(fire_power_, 3);
            // else if ( gun_.BestRatingFast() > 0.3 && me_.energy_ > 70 ) fire_power_ =
            // Math.min(fire_power_, 3);
            // else if ( gun_.BestRatingFast() > 0.4 && me_.energy_ > 80 ) fire_power_ =
            // Math.min(fire_power_, 3);
          }
          fire_power_ = Math.min(fire_power_, me_.energy_ / 6);
        }
      }
    }
    if (TurnHandler.time_ < 50 && distance_ > 80) {
      fire_power_ = Math.min(fire_power_, Rules.MIN_BULLET_POWER);
    }

    if (TurnHandler.time_ % 50 == 0) {
      robot_.out.println("fp: " + fire_power_);
    }
  }

  private void SetRaikoFirePower() {
    fire_power_ = Math.min(me_.energy_ / 4, Math.min(energy_ / 4, distance_ < 140 ? 3 : 2));
    ready_to_fire_ = me_.energy_ > 1 || distance_ < 140;
  }

  private void CreateSnapshot(long elapsed) {
    _2ago_ = _1ago_;
    _1ago_ = _now_;
    log_.add(0, _now_ = new Snapshot());

    _now_.me_ = Bot.CloneMe();
    _now_.me_.UpdateToEnemy(this);
    if (_1ago_ != null) {
      _now_.me_.UpdateInTime(_1ago_.me_, elapsed);
      UpdateInTime(_1ago_.enemy_, elapsed);
    }

    _now_.enemy_ = new Bot(this);
    _now_.distance_ = distance_;
    _now_.Normalize();
  }
}
