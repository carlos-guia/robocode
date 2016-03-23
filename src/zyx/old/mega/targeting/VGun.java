package zyx.old.mega.targeting;

import zyx.old.mega.bot.Enemy;
import zyx.old.mega.utils.Snapshot;
import zyx.old.mega.utils.TurnHandler;

public abstract class VGun {
  //private static final double RATING_DEPTH = 1023;
  //private static final double FAST_RATING_DEPTH = 11;
  protected Enemy enemy_;
  //public RollingAverage rating_;
  //public RollingAverage as_rating_;
  //public RollingAverage fast_rating_;
  //public RollingAverage fast_as_rating_;

  public double aim_angle_;
  public double as_aim_angle_;
  private int hits_;
  private int bullets_;
  private int as_hits_;
  private int as_bullets_;
  private int round_hits_;
  private int round_bullets_;
  private int round_as_hits_;
  private int round_as_bullets_;

  public VGun(Enemy enemy) {
    enemy_ = enemy;
    //rating_ = new RollingAverage(RATING_DEPTH);
    //as_rating_ = new RollingAverage(RATING_DEPTH);
    //fast_rating_ = new RollingAverage(FAST_RATING_DEPTH);
    //fast_as_rating_ = new RollingAverage(FAST_RATING_DEPTH);
  }
  public void LogHit(Snapshot snapshot) {
    ++hits_;
    ++bullets_;
    ++round_hits_;
    ++round_bullets_;
  }
  public void LogMiss(Snapshot snapshot) {
    ++bullets_;
    ++round_bullets_;
  }
  public void LogHitAS(Snapshot snapshot) {
    ++as_hits_;
    ++as_bullets_;
    ++round_as_hits_;
    ++round_as_bullets_;
  }
  public void LogMissAS(Snapshot snapshot) {
    ++as_bullets_;
    ++round_as_bullets_;
  }

  public final void Update() {
    if ( enemy_.energy_ == 0 ) aim_angle_ = as_aim_angle_ = enemy_.bearing_;
    else GunUpdate();
  }
  protected abstract void GunUpdate();
  public double Rating() {
    if ( bullets_ == 0 ) return 0;
    return (double)hits_ / bullets_;
  }
  public double RatingAS() {
    if ( as_bullets_ == 0 ) return 0;
    return (double)as_hits_ / as_bullets_;
  }
  public double RatingFast() {
    if ( round_bullets_ == 0 ) return 0;
    return (double)round_hits_ / round_bullets_;
  }
  public double RatingFastAS() {
    if ( round_as_bullets_ == 0 ) return 0;
    return (double)round_as_hits_ / round_as_bullets_;
  }
  public double ComposedRating() {
    return ComposedRating(Rating(), RatingFast());
  }
  public double ComposedRatingAS() {
    return ComposedRating(RatingAS(), RatingFastAS());
  }
  //private static final double GUN_WEIGHT = 5;
  private double ComposedRating(double rating, double fast_rating) {
    final double GUN_WEIGHT = Math.max(Math.min(TurnHandler.round_ + 1, 16) / 2, 1);
    return (rating * GUN_WEIGHT + fast_rating) / (GUN_WEIGHT + 1);
    //return rating;
  }
  public void Init() {
    /**
    if ( TurnHandler.round_ > 2 ) {
      hits_ -= round_hits_;
      bullets_ -= round_bullets_;
      as_hits_ -= round_as_hits_;
      as_bullets_ -= round_as_bullets_;
    }
    /**/
    round_hits_ = round_bullets_ = round_as_hits_ = round_as_bullets_ = 0;
    /**/
  }
}
