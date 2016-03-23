package zyx.old.mega.bot;

import java.awt.Graphics2D;

import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.util.Utils;
import zyx.old.debug.Printer;
import zyx.old.mega.geometry.BoundingSquare;
import zyx.old.mega.geometry.Circle;
import zyx.old.mega.geometry.Geometry;
import zyx.old.mega.geometry.Point;
import zyx.old.mega.geometry.Rectangle;
import zyx.old.mega.utils.Range;
import zyx.old.mega.utils.Snapshot;
import zyx.old.mega.utils.TurnHandler;

public class Bot extends Point {
  private static final double WALL = 18;
  private static final double SMOOTH_WALL = 22;
  private static final double WALL_STICK = 180;
  public static double WIDTH;
  public static double HEIGHT;

  public static Bot me_;
  public static Rectangle smooth_field_;
  public static Rectangle field_;
  public static AdvancedRobot robot_;
  public static Point center_;

  public static void InitStatic() {
    me_ = TurnHandler.me_;
    robot_ = TurnHandler.robot_;
    WIDTH = robot_.getBattleFieldWidth();
    HEIGHT = robot_.getBattleFieldHeight();
    center_ = new Point(WIDTH / 2, HEIGHT / 2);
    smooth_field_ =
        new Rectangle(SMOOTH_WALL, SMOOTH_WALL, WIDTH - SMOOTH_WALL * 2, HEIGHT - SMOOTH_WALL * 2);
    field_ = new Rectangle(WALL, WALL, WIDTH - WALL * 2, HEIGHT - WALL * 2);
  }

  public void onPaint(Graphics2D g) {
    bbox_.onPaint(g);
  }

  /* Basic Values */
  public double energy_;
  public double heading_;
  public double velocity_;
  public double gun_heat_;

  /* Relative to Time */
  public int acceleration_;
  public double rotation_;
  public int time_running_;
  public int time_stopped_;
  public int time_direction_;

  /* Relative to Enemy */
  public double bearing_;
  public double lateral_velocity_;
  public int direction_;
  public double approaching_velocity_;

  /* Position */
  public BoundingSquare bbox_;
  public int ahead_ticks_;
  public int back_ticks_;

  public Bot() {
    energy_ = 100;
    gun_heat_ = 3;
    bbox_ = new BoundingSquare(this, 18);
    time_running_ = time_stopped_ = time_direction_ = 0;
  }

  public Bot(Bot bot) {
    super(bot);
    energy_ = bot.energy_;
    heading_ = bot.heading_;
    velocity_ = bot.velocity_;
    gun_heat_ = bot.gun_heat_;
    acceleration_ = bot.acceleration_;
    rotation_ = bot.rotation_;
    bearing_ = bot.bearing_;
    lateral_velocity_ = bot.lateral_velocity_;
    direction_ = bot.direction_;
    ahead_ticks_ = bot.ahead_ticks_;
    back_ticks_ = bot.back_ticks_;
    bbox_ = new BoundingSquare(this, 18);
  }

  public void Update(AdvancedRobot robot) {
    x_ = robot.getX();
    y_ = robot.getY();
    energy_ = robot.getEnergy();
    heading_ = robot.getHeadingRadians();
    velocity_ = robot.getVelocity();
    gun_heat_ = robot.getGunHeat();
    // Printer.printf(0, "up: %.5f %.5f\n", heading_, robot_.getGunHeadingRadians());
    FinishUpdate();
  }

  protected void FinishUpdate() {
    bbox_.Update();
    UpdateWallDistance();
  }

  private void UpdateWallDistance() {
    Bot ahead = new Bot(this);
    Bot back = new Bot(this);
    double ahead_angle = heading_;
    double back_angle = heading_ + Geometry.PI;
    if (velocity_ < 0) {
      ahead_angle = back_angle;
      back_angle = heading_;
    }
    ahead_ticks_ = 0;
    back_ticks_ = 0;
    boolean ahead_done = false;
    boolean back_done = false;
    for (int t = 0; t <= Snapshot.MAX_WALL_DISTANCE && (!ahead_done || !back_done); ++t) {
      if (!ahead_done) {
        ++ahead_ticks_;
        ahead.MoveBot(ahead_angle, false);
        ahead_done = !field_.Inside(ahead, false);
      }
      if (!back_done) {
        ++back_ticks_;
        back.MoveBot(back_angle, false);
        back_done = !field_.Inside(back, false);
      }
    }
    /*
     * if ( this != me_ ) { Printer.printf(0, "(%.2f %.2f) %.2f [%.2f, %.2f] -> %d\n", velocity_,
     * ahead.velocity_, Geometry.Distance(this, ahead), ahead.x_, ahead.y_, ahead_ticks_);
     * Printer.printf(0, "(%.2f %.2f) %.2f [%.2f, %.2f] -> %d\n", velocity_, back.velocity_,
     * Geometry.Distance(this, back), back.x_, back.y_, back_ticks_); Painter.Add(0, new Line(this,
     * ahead_angle, Geometry.Distance(this, ahead))); Painter.Add(0, ahead); Painter.Add(0, back); }
     */
  }

  public static Bot CloneMe() {
    return new Bot(me_);
  }

  public void UpdateInTime(Bot bot, long elapsed) {
    if (Math.abs(velocity_ - bot.velocity_) < 1e-9) {
      acceleration_ = 0;
      if (Math.abs(velocity_) < 1) {
        ++time_stopped_;
        time_running_ = 0;
      } else {
        time_stopped_ = 0;
        ++time_running_;
      }
    } else if (Math.abs(velocity_) < Math.abs(bot.velocity_)) {
      acceleration_ = -2;
      time_stopped_ = time_running_ = 0;
    } else {
      acceleration_ = 1;
      time_stopped_ = 0;
      ++time_running_;
    }
    if (acceleration_ == -2)
      time_direction_ = 0;
    else
      ++time_direction_;
    rotation_ = Utils.normalRelativeAngle(heading_ - bot.heading_) / elapsed;
    if (velocity_ < 0)
      rotation_ = -rotation_;
    // else if ( velocity_ == 0 && bot.velocity_ < 0 ) rotation_ = -rotation_;
    if (rotation_ + 1e-9 < -Geometry.PI_18 || rotation_ - 1e-9 > Geometry.PI_18) {
      Printer.printf(0, "rot (%d): %.10f NOT in [+-%.10f] (%d)\n", TurnHandler.time_, rotation_,
          Geometry.PI_18, elapsed);
      /**
       * Printer.onPrint(TurnHandler.robot_.out); ((Bot)null).acceleration_ = 0; /
       **/
    }
  }

  public void UpdateToEnemy(Enemy enemy) {
    bearing_ = enemy.bearing_ + Math.PI;
    direction_ = enemy.my_direction_;
    lateral_velocity_ = enemy.my_lateral_velocity_;
    approaching_velocity_ = enemy.my_approaching_velocity_;
  }

  public void Orbit(Point center, int direction, boolean stop, Enemy enemy) {
    double angle = OrbitAngle(center, direction, enemy);
    MoveBot(angle, stop);
  }

  public void OrbitSimple(Point center, int direction, boolean try_approach) {
    double angle = OrbitAngleSimple(center, direction, try_approach);
    MoveBot(angle, false);
  }

  public void MoveBot(double angle, boolean stop) {
    double turn = Utils.normalRelativeAngle(angle - heading_);
    int direction = 1;
    if (Math.abs(turn) > Geometry.HALF_PI) {
      direction = -1;
      turn = Utils.normalRelativeAngle(turn + Geometry.PI);
    }
    turn = Range.CapCentered(turn, MaxTurn());
    if (stop) {
      if (velocity_ == 0)
        return;
      if (velocity_ < 0)
        velocity_ = Range.CapHigh(velocity_ + 2, 0);
      else
        velocity_ = Range.CapLow(velocity_ - 2, 0);
    } else {
      velocity_ = Range.CapCentered(velocity_ + (velocity_ * direction < 0 ? 2 : 1) * direction,
          Rules.MAX_VELOCITY);
    }
    heading_ = Utils.normalAbsoluteAngle(heading_ + turn);
    MovePoint(heading_, velocity_);
    bbox_.Update();
  }

  private double MaxTurn() {
    return Geometry.PI_18 - Geometry.PI_240 * Math.abs(velocity_);
  }

  public double OrbitAngle(Point center, int direction, Enemy enemy) {
    double p = energy_ > enemy.energy_ ? 0.51 : 0.485;
    double distance = Geometry.Distance(this, center);
    int test_distance[] =
        energy_ > enemy.energy_
            ? new int[] {100, 150, 200, 250, 300, 850, 750, 650, 500}
            : new int[] {150, 200, 250, 300, 350, 800, 700, 600, 450};
    if (distance < test_distance[0])
      p = 0.2;
    else if (distance < test_distance[1])
      p = 0.30;
    else if (distance < test_distance[2])
      p = 0.35;
    else if (distance < test_distance[3])
      p = 0.40;
    else if (distance < test_distance[4])
      p = 0.45;
    else if (distance > test_distance[5])
      p = 0.85;
    else if (distance > test_distance[6])
      p = 0.75;
    else if (distance > test_distance[7])
      p = 0.65;
    else if (distance > test_distance[8])
      p = 0.55;
    return Smooth(center, Geometry.Angle(center, this) + direction * p * Geometry.PI);
  }

  public double OrbitAngleSimple(Point center, int direction, boolean try_approach) {
    double p = try_approach ? 0.51 : 0.45;
    return Smooth(center, Geometry.Angle(center, this) + direction * p * Geometry.PI);
  }

  public double Smooth(Point center, double angle) {
    // Painter.Add(0, field_);
    if (smooth_field_.Inside(new Point(this, angle, WALL_STICK), true))
      return angle;
    angle = Utils.normalRelativeAngle(angle);
    double smooth_angle = angle;
    double best_error = Double.POSITIVE_INFINITY;
    double best_error2 = Double.POSITIVE_INFINITY;
    Circle shield = new Circle(this, WALL_STICK);
    for (Point point : shield.Intersection(smooth_field_)) {
      double pangle = Geometry.Angle(this, point);
      double error = Geometry.AngleDifference(pangle, angle);
      double error2 = Geometry.Distance(point, center);
      // TurnHandler.robot_.out.printf("point: %.4f %.4f\n", pangle, error);
      if (error < best_error || (Math.abs(error - best_error) < 1e-5 && error2 < best_error2)) {
        best_error = error;
        best_error2 = error2;
        smooth_angle = pangle;
      }
    }
    // TurnHandler.robot_.out.printf("smooth: %.4f %.4f\n", Utils.normalRelativeAngle(angle),
    // Utils.normalRelativeAngle(smooth_angle));
    return smooth_angle;
  }

  /*
   * public static Bot NextMe() { Bot next_me = new Bot(me_); int direction =
   * (int)Math.signum(robot_.getDistanceRemaining()); if ( direction == 0 ) direction =
   * (int)Math.signum(-next_me.velocity_); next_me.heading_ =
   * Utils.normalAbsoluteAngle(next_me.heading_ +
   * Range.CapCentered(robot_.getTurnRemainingRadians(), next_me.MaxTurn())); next_me.velocity_ =
   * Range.CapCentered(next_me.velocity_ + (next_me.velocity_ * direction < 0 ? -2 : 1) * direction,
   * Rules.MAX_VELOCITY); next_me.MovePoint(next_me.heading_, next_me.velocity_); return next_me; }
   */
  public static double NextTurn() {
    return Range.CapCentered(robot_.getTurnRemainingRadians(), me_.MaxTurn());
  }
}
