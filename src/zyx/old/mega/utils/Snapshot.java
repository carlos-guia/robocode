package zyx.old.mega.utils;

import zyx.old.mega.bot.Bot;
import zyx.old.mega.geometry.Geometry;

public class Snapshot {

  public Bot me_;
  public Bot enemy_;
  public double distance_;

  /* Wave Surfing */
  public boolean ws_hit_;
  public double ws_hit_factor_;
  public Range ws_hit_factor_window_;

  /* Guess Factor Targeting */
  //public boolean gf_hit_;
  public static final int SPOT = 0;
  public static final int BOT_HIT = 1;
  public static final int BULLET_HIT = 2;
  public int gf_hit_;
  public double gf_hit_factor_;
  public Range gf_bbox_factor_window_;
  public Range gf_corner_factor_window_;


  public static final int DISTANCE = 0;
  public static final int LATERAL_VELOCITY = 1;
  public static final int ACCELERATION = 2;
  public static final int ROTATION = 3;
  public static final int AHEAD_TICKS = 4;
  public static final int BACK_TICKS = 5;
  public static final int VELOCITY = 6;
  public static final int APPROACHING_VELOCITY = 7;
  public static final int TIME_STOPPED = 8;
  public static final int TIME_RUNNING = 9;
  public static final int TIME_DIRECTION = 10;
  //public static final int BULLET_HIT_TIME = 11;
  public static final int ATTRIBUTES = 11;
  public static final int GF_ATTRIBUTES = 11;
  public static final int MAX_WALL_DISTANCE = 50;
  private static final double MAX_TIME_STOPPED = 20;
  private static final double MAX_TIME_RUNNING = 60;
  private static final double MAX_TIME_DIRECTION = 60;

  public double ws_normal_[];
  public double gf_normal_[];
  
  public void Normalize() {
    /* WaveSurfing */
    ws_normal_ = new double[ATTRIBUTES];
    ws_normal_[DISTANCE] = Range.Normalize(distance_, 0, 800, false);
    ws_normal_[LATERAL_VELOCITY] = Range.Normalize(me_.lateral_velocity_, 0, 8, true);
    ws_normal_[APPROACHING_VELOCITY] = Range.Normalize(me_.approaching_velocity_, -8, 8, false);
    ws_normal_[VELOCITY] = Range.Normalize(me_.velocity_, 0, 8, true);
    ws_normal_[ACCELERATION] = Range.Normalize(me_.acceleration_, -2, 1, false);
    ws_normal_[ROTATION] = Range.Normalize(me_.rotation_, -Geometry.PI_18, Geometry.PI_18, false);
    ws_normal_[AHEAD_TICKS] = Range.Normalize(me_.ahead_ticks_, 1, MAX_WALL_DISTANCE, false);
    ws_normal_[BACK_TICKS] = Range.Normalize(me_.back_ticks_, 1, MAX_WALL_DISTANCE, false);
    ws_normal_[TIME_STOPPED] = Range.Normalize(me_.time_stopped_, 1, MAX_TIME_STOPPED, false);
    ws_normal_[TIME_RUNNING] = Range.Normalize(me_.time_running_, 1, MAX_TIME_RUNNING, false);
    ws_normal_[TIME_DIRECTION] = Range.Normalize(me_.time_direction_, 1, MAX_TIME_DIRECTION, false);
    /* GuessFactoring */
    gf_normal_ = new double[GF_ATTRIBUTES];
    gf_normal_[DISTANCE] = Range.Normalize(distance_, 0, 800, false);
    gf_normal_[LATERAL_VELOCITY] = Range.Normalize(enemy_.lateral_velocity_, 0, 8, true);
    gf_normal_[VELOCITY] = Range.Normalize(enemy_.velocity_, 0, 8, true);
    gf_normal_[ACCELERATION] = Range.Normalize(enemy_.acceleration_, -2, 1, false);
    gf_normal_[ROTATION] = Range.Normalize(enemy_.rotation_, -Geometry.PI_18, Geometry.PI_18, false);
    gf_normal_[AHEAD_TICKS] = Range.Normalize(enemy_.ahead_ticks_, 1, MAX_WALL_DISTANCE, false);
    gf_normal_[BACK_TICKS] = Range.Normalize(enemy_.back_ticks_, 1, MAX_WALL_DISTANCE, false);
    gf_normal_[APPROACHING_VELOCITY] = Range.Normalize(enemy_.approaching_velocity_, -8, 8, false);
    gf_normal_[TIME_STOPPED] = Range.Normalize(enemy_.time_stopped_, 1, MAX_TIME_STOPPED, false);
    gf_normal_[TIME_RUNNING] = Range.Normalize(enemy_.time_running_, 1, MAX_TIME_RUNNING, false);
    gf_normal_[TIME_DIRECTION] = Range.Normalize(enemy_.time_direction_, 1, MAX_TIME_DIRECTION, false);
  }
}
