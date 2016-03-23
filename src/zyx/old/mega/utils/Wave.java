package zyx.old.mega.utils;

import robocode.Rules;
import robocode.util.Utils;
import zyx.old.mega.bot.Bot;
import zyx.old.mega.geometry.Circle;
import zyx.old.mega.geometry.Geometry;
import zyx.old.mega.geometry.Point;
import zyx.old.mega.geometry.Rectangle;
import zyx.old.mega.utils.wave.WaveHit;

public class Wave extends Circle {

  public long time_;
  public double bearing_;
  public double fire_power_;
  public double velocity_;
  public double mae_;
  public int direction_;

  public Snapshot snapshot_;
  // public boolean sync_;
  public double linear_factor_;
  public double circular_factor_;

  public Wave() {}

  public Wave(Point location) {
    super(location);
  }

  public Wave(Wave wave) {
    super(wave);
    time_ = wave.time_;
    bearing_ = wave.bearing_;
    fire_power_ = wave.fire_power_;
    velocity_ = wave.velocity_;
    mae_ = wave.mae_;
    direction_ = wave.direction_;
  }

  public void Update(long time) {
    radius_ = (time - time_) * velocity_;
  }

  public void SetPower(double fire_power) {
    fire_power_ = fire_power;
    velocity_ = Rules.getBulletSpeed(fire_power);
    mae_ = Math.asin(Rules.MAX_VELOCITY / velocity_);
    LinearFactor(snapshot_.me_, snapshot_.me_.heading_, snapshot_.me_.velocity_, Bot.field_);
    CircularFactor(snapshot_.me_, snapshot_.me_.heading_, snapshot_.me_.velocity_,
        snapshot_.me_.rotation_, Bot.field_);
  }

  private void LinearFactor(Point location, double heading, double velocity, Rectangle field) {
    int deltaTime = 0;
    Point predicted = new Point(location.x_, location.y_);
    while ((++deltaTime) * velocity_ < Geometry.Distance(this, predicted)) {
      predicted.x_ += Math.sin(heading) * velocity;
      predicted.y_ += Math.cos(heading) * velocity;
      if (!field.Inside(predicted, true)) {
        predicted.x_ = Math.min(Math.max(18.0, predicted.x_), field.width_ - 18.0);
        predicted.y_ = Math.min(Math.max(18.0, predicted.y_), field.height_ - 18.0);
        break;
      }
    }
    linear_factor_ = Factor(Geometry.Angle(this, predicted));
  }

  private void CircularFactor(Point location, double heading, double velocity, double rotation,
      Rectangle field) {
    int deltaTime = 0;
    Point predicted = new Point(location.x_, location.y_);
    while ((++deltaTime) * velocity_ < Geometry.Distance(this, predicted)) {
      predicted.x_ += Math.sin(heading) * velocity;
      predicted.y_ += Math.cos(heading) * velocity;
      heading += rotation;
      if (!field.Inside(predicted, true)) {
        predicted.x_ = Math.min(Math.max(18.0, predicted.x_), field.width_ - 18.0);
        predicted.y_ = Math.min(Math.max(18.0, predicted.y_), field.height_ - 18.0);
        break;
      }
    }
    circular_factor_ = Factor(Geometry.Angle(this, predicted));
  }

  public double Factor(Point point) {
    double offset = Utils.normalRelativeAngle(Geometry.Angle(this, point) - bearing_);
    return direction_ * offset / mae_;
  }

  public double Factor(double angle) {
    double offset = Utils.normalRelativeAngle(angle - bearing_);
    return direction_ * offset / mae_;
  }

  public WaveHit Hit(Bot bot) {
    WaveHit hit = new WaveHit();
    for (Point corner : bot.bbox_.corners_) {
      hit.corners_.Update(Factor(corner));
      double t = Math.ceil((Geometry.Distance(this, corner) - radius_) / velocity_);
      if (t < 1e-9) {
        if (hit.Unkown()) {
          hit.info_ = WaveHit.ALL_IN;
        } else if (hit.info_ == WaveHit.ALL_OUT) {
          hit.info_ = WaveHit.HITTING;
        }
      } else {
        hit.run_time_ = Math.min(hit.run_time_, t);
        hit.wait_time_ = Math.max(hit.wait_time_, t);
        if (hit.info_ == WaveHit.UNKOWN) {
          hit.info_ = WaveHit.ALL_OUT;
        } else if (hit.info_ == WaveHit.ALL_IN) {
          hit.info_ = WaveHit.HITTING;
        }
      }
    }
    for (Point border : Intersection(bot.bbox_)) {
      hit.bbox_.Update(Factor(border));
    }
    return hit;
  }

  public void UpdateWS(Range window) {
    if (snapshot_.ws_hit_factor_window_ == null)
      snapshot_.ws_hit_factor_window_ = window;
    else
      snapshot_.ws_hit_factor_window_.Update(window);
  }

  public void UpdateGF(Range window, Range window2) {
    if (snapshot_.gf_bbox_factor_window_ == null)
      snapshot_.gf_bbox_factor_window_ = window;
    else
      snapshot_.gf_bbox_factor_window_.Update(window);
    if (snapshot_.gf_corner_factor_window_ == null)
      snapshot_.gf_corner_factor_window_ = window2;
    else
      snapshot_.gf_corner_factor_window_.Update(window2);
  }

  public double AbsoluteAngle(double factor) {
    return bearing_ + direction_ * factor * mae_;
  }
}
