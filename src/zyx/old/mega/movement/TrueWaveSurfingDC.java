package zyx.old.mega.movement;

import java.util.Arrays;
import java.util.Iterator;

import zyx.old.debug.Printer;
import zyx.old.mega.bot.Enemy;
import zyx.old.mega.geometry.Geometry;
import zyx.old.mega.utils.Range;
import zyx.old.mega.utils.RollingAverage;
import zyx.old.mega.utils.Snapshot;
import zyx.old.mega.utils.Wave;
import zyx.old.mega.utils.WeightedDistancer;
import zyx.old.simonton.utils.Cluster;
import zyx.old.simonton.utils.Distancer;
import zyx.old.simonton.utils.MyTree;

public class TrueWaveSurfingDC extends TrueWaveSurfing {
  private MyTree<Snapshot> flattener_;
  private MyTree<Snapshot> danger_;
  private final WeightedDistancer virtual_distancers_[] = new WeightedDistancer[] {
      /*
      new WeightedDistancer() {
        public void InitWeight() {
          weight_ = new double[Snapshot.ATTRIBUTES];
          weight_[Snapshot.DISTANCE] = 0.1;
          weight_[Snapshot.LATERAL_VELOCITY] = 1.5;
          weight_[Snapshot.ACCELERATION] = 1;
        }
      },
      new WeightedDistancer() {
        public void InitWeight() {
          weight_ = new double[Snapshot.ATTRIBUTES];
          weight_[Snapshot.DISTANCE] = 0.1;
          weight_[Snapshot.LATERAL_VELOCITY] = 1;
          weight_[Snapshot.ACCELERATION] = 1;
          weight_[Snapshot.ROTATION] = 0.1;
          weight_[Snapshot.AHEAD_TICKS] = 1;
          weight_[Snapshot.BACK_TICKS] = 1;
        }
      },
      */
      new WeightedDistancer() {
        public void InitWeight() {
          weight_ = new double[Snapshot.ATTRIBUTES];
          weight_[Snapshot.DISTANCE] = 0.5;
          weight_[Snapshot.LATERAL_VELOCITY] = 1;
          weight_[Snapshot.APPROACHING_VELOCITY] = 1;
          weight_[Snapshot.VELOCITY] = 0.5;
          weight_[Snapshot.ACCELERATION] = 1;
          weight_[Snapshot.ROTATION] = 0.3;
          weight_[Snapshot.AHEAD_TICKS] = 0.7;
          weight_[Snapshot.BACK_TICKS] = 0.7;
          weight_[Snapshot.TIME_STOPPED] = 0.2;
          weight_[Snapshot.TIME_RUNNING] = 0.2;
          weight_[Snapshot.TIME_DIRECTION] = 0.5;
        }
      },
      new WeightedDistancer() {
        public void InitWeight() {
          weight_ = new double[Snapshot.ATTRIBUTES];
          weight_[Snapshot.DISTANCE] = 0.5;
          weight_[Snapshot.LATERAL_VELOCITY] = 1.5;
          weight_[Snapshot.APPROACHING_VELOCITY] = 1.5;
          weight_[Snapshot.VELOCITY] = 0.5;
          weight_[Snapshot.ACCELERATION] = 1;
          weight_[Snapshot.ROTATION] = 0.3;
          weight_[Snapshot.AHEAD_TICKS] = 1;
          weight_[Snapshot.BACK_TICKS] = 1;
          weight_[Snapshot.TIME_STOPPED] = 0.05;
          weight_[Snapshot.TIME_RUNNING] = 0.05;
          weight_[Snapshot.TIME_DIRECTION] = 0.1;
        }
      },
      new WeightedDistancer() {
        public void InitWeight() {
          weight_ = new double[Snapshot.ATTRIBUTES];
          Arrays.fill(weight_, 1);
        }
      },
  };
  private double virtual_scores_[] = new double[virtual_distancers_.length];
  private int virtual_hits_ = 0;
  private static final int DEPTH = 17;
  private static final int DEPTH2 = 5;
  //private static final int DEPTH3 = 101;
  private static final double C = 0.07;
  private static final double K = -1 / (2 * C * C);
  private static final double HOT_FACTOR = 0;
  private static final double ORBITAL_FACTOR = 0.85;
  private int distancer_;
  public Cluster<Snapshot> hit_cluster1_;
  public Cluster<Snapshot> hit_cluster2_;
  public Cluster<Snapshot> flat_cluster1_;
  public Cluster<Snapshot> flat_cluster2_;
  private int last_danger_size_;
  private int last_flattener_size_;
  //private double simple_avg_;
  private double hot_avg_;
  private double linear_avg_;
  private double circular_avg_;
  private double orbital_avg_;
  private double hit_avg_[] = new double[virtual_distancers_.length];
  private double flat_avg_[] = new double[virtual_distancers_.length];
  private double hot_weight_;
  private double linear_weight_;
  private double circular_weight_;
  private double adaptative_weight_;
  private double orbital_weight_;
  private double HIT;
  private double FLAT;
  
  public TrueWaveSurfingDC(Enemy enemy) {
    super(enemy);
    danger_ = new MyTree<Snapshot>(Snapshot.ATTRIBUTES, 8, 1, 500);
    flattener_ = new MyTree<Snapshot>(Snapshot.ATTRIBUTES, 8, 1, 500);
    hot_weight_ = 0.8;
    linear_weight_ = 0.05;
    circular_weight_ = 0.10;
    orbital_weight_ = 0.05;
    adaptative_weight_ = 0;
    HIT = 0;
    FLAT = 0;
    /*
    Snapshot snapshot = new Snapshot();
    snapshot.ws_normal_ = new double[Snapshot.ATTRIBUTES];
    snapshot.ws_hit_factor_ = -0.003;
    danger_.add(snapshot.ws_normal_, snapshot);
    snapshot = new Snapshot();
    snapshot.ws_normal_ = new double[Snapshot.ATTRIBUTES];
    snapshot.ws_hit_factor_ = 0.003;
    danger_.add(snapshot.ws_normal_, snapshot);
    */
  }
  public void Init() {
    super.Init();
    Printer.printf(0, "danger status: %d\n" +
        "hot: %.4f (%.4f)\n" + 
        "linear: %.4f (%.4f)\n" + 
        "circular: %.4f (%.4f)\n" + 
        "orbital: %.4f (%.4f)\n" + 
        "adaptative: %.4f\n" + 
        "HIT: %.4f : %.4f (%.4f)\n" + 
        "FLAT: %.4f : %.4f (%.4f)\n",
      distancer_,
      hot_weight_, hot_avg_,
      linear_weight_, linear_avg_,
      circular_weight_, circular_avg_,
      orbital_weight_, orbital_avg_,
      adaptative_weight_,
      HIT * adaptative_weight_, HIT, hit_avg_[distancer_],
      FLAT * adaptative_weight_, FLAT, flat_avg_[distancer_]
     );
  }
  protected void UpdateFlattener(Wave wave) {
    flattener_.add(wave.snapshot_.ws_normal_, wave.snapshot_);
  }
  protected boolean UpdateDanger(Wave wave) {
    UpdateWeights(wave);
    danger_.add(wave.snapshot_.ws_normal_, wave.snapshot_);
    return true;
  }
  private void UpdateWeights(Wave wave) {
    Range window1 = new Range(wave.snapshot_.ws_hit_factor_, THRESHOLD1, true);
    Range window2 = new Range(wave.snapshot_.ws_hit_factor_, THRESHOLD2, true);
    int depth = Math.min(virtual_hits_, DEPTH);
    int depth2 = Math.min(virtual_hits_++, DEPTH2);
    int best = distancer_ = 0;
    //double simple = Math.max(Math.max(hot, linear), Math.max(circular, orbital));
    //simple_avg_ = RollingAverage.Roll(simple_avg_, simple, depth3);
    for (int i = 0; i < virtual_distancers_.length; ++i ) {
      int j = (i + distancer_) % virtual_distancers_.length;
      /*if ( i > 0 )*/ BuildDanger(wave.snapshot_, virtual_distancers_[j]);
      double hit_danger = HitDanger(window1, window2);
      double flat_danger = FlatDanger(window1, window2);
      RollingAverage.Roll(hit_avg_, j, hit_danger, depth2);
      RollingAverage.Roll(flat_avg_, j, flat_danger, depth2);
      RollingAverage.Roll(virtual_scores_, j, hit_danger, depth);
      if ( virtual_scores_[j] > virtual_scores_[best] ) {
        best = j;
      }
    }
    /*
    final double SIMPLE = 0.7;
    if ( simple_avg_ > SIMPLE ) {
      if ( danger_.size() > 20 ) adaptative_avg = 3;
      else if ( danger_.size() > 10 ) adaptative_avg = 1.5;
      else if ( danger_.size() > 5 ) adaptative_avg = 0.5;
      adaptative_avg = 0;
    }
    else if ( danger_.size() > 10 ) adaptative_avg = 3;
    else if ( danger_.size() > 5 ) adaptative_avg = 1.5;
    else if ( danger_.size() > 2 ) adaptative_avg = 0.5;
    */
    hot_avg_ = RollingAverage.Roll(hot_avg_, Gauss(wave.snapshot_.ws_hit_factor_, HOT_FACTOR), depth);
    linear_avg_ = RollingAverage.Roll(linear_avg_, Gauss(wave.snapshot_.ws_hit_factor_, wave.linear_factor_), depth);
    circular_avg_ = RollingAverage.Roll(circular_avg_, Gauss(wave.snapshot_.ws_hit_factor_, wave.circular_factor_), depth);
    orbital_avg_ = RollingAverage.Roll(orbital_avg_, Gauss(wave.snapshot_.ws_hit_factor_, ORBITAL_FACTOR), depth);
    distancer_ = best;
    if ( hot_avg_ > 0.9 ) {
      hot_weight_ = 0.44;
      linear_weight_ = orbital_weight_ = 0.03;
      circular_weight_ = 0.05;
      adaptative_weight_ = 0.45;
      HIT = 1;
      FLAT = 0;
    } else {
      double simple_avg = hot_avg_ + linear_avg_ + circular_avg_ + orbital_avg_;
      double adaptative_avg = Math.max(1 + Math.min(danger_.size() / 10, 4) - simple_avg, 0);
      double total = simple_avg + adaptative_avg;
      hot_weight_ = hot_avg_ / total;
      linear_weight_ = linear_avg_ / total;
      circular_weight_ = circular_avg_ / total;
      orbital_weight_ = orbital_avg_ / total;
      adaptative_weight_ = adaptative_avg / total;
      total = hit_avg_[best] + flat_avg_[best];
      /*if ( simple_avg_ > SIMPLE ) {
        HIT = 1;
      } else */if ( total < 1e-5 ) {
        HIT = simple_avg;
      } else {
        HIT = hit_avg_[best] / total;
      }
      FLAT = 1 - HIT;
      //Printer.printf(0, "simple vs adaptative: %.4f %.4f\n", simple_avg, adaptative_avg);
    }
    /*
    Printer.printf(0, "danger status: %d\n" +
        "hot: %.4f (%.4f)\n" + 
        "linear: %.4f (%.4f)\n" + 
        "circular: %.4f (%.4f)\n" + 
        "orbital: %.4f (%.4f)\n" + 
        "adaptative: %.4f\n" + 
        "HIT: %.4f : %.4f (%.4f)\n" + 
        "FLAT: %.4f : %.4f (%.4f)\n",
      distancer_,
      hot_weight_, hot_avg_,
      linear_weight_, linear_avg_,
      circular_weight_, circular_avg_,
      orbital_weight_, orbital_avg_,
      adaptative_weight_,
      HIT * adaptative_weight_, HIT, hit_avg_[best],
      FLAT * adaptative_weight_, FLAT, flat_avg_[best]
     );
     */
  }
  private double Gauss(double value, double center) {
    return Math.exp(K * Geometry.Square(value - center));
  }
  /*
  protected boolean UpdateDanger(Wave wave) {
    if ( danger_.size() > 5 || (danger_.size() > 0 && flattener_.size() > 20) ) {
      Range window1 = new Range(wave.snapshot_.ws_hit_factor_, THRESHOLD1, true);
      Range window2 = new Range(wave.snapshot_.ws_hit_factor_, THRESHOLD2, true);
      int bj[] = new int[2];
      int depth = Math.min(virtual_hits_++, DEPTH);
      boolean was_flat = flat_;
      if ( TurnHandler.time_ - last_print_ > 200 ) {
        last_print_ = TurnHandler.time_;
      }
      int j = last_distancer_;
      flat_ = true;
      int fc = 0;
      for (WeightedDistancer distancer : virtual_distancers_) {
        BuildDanger(wave.snapshot_, distancer);
        double hit_danger = HitDanger(window1, window2);
        double flat_danger = FlatDanger(window1, window2);
        if ( flat_danger > 1 * hit_danger ) ++fc;
        RollingAverage.Roll(virtual_scores_[0], j, hit_danger, depth, 1);
        RollingAverage.Roll(virtual_scores_[1], j, hit_danger * HIT + flat_danger * FLAT, depth, 1);
        //Printer.printf(0, "new dangers: %.2f %.2f -> %.2f\n", hit_danger, flat_danger, hit_danger * HIT + flat_danger * FLAT);
        //Printer.printf(0, "sc: %.2f%%\n", virtual_scores_[j]);
        //Printer.printf(0, "%.4f   %.4f\n", virtual_scores_[0][j], virtual_scores_[1][j]);
        if ( virtual_scores_[0][j] > virtual_scores_[0][bj[0]] - 1e-6 ) {
          bj[0] = j;
        }
        if ( virtual_scores_[1][j] > virtual_scores_[1][bj[1]] - 1e-6 ) {
          bj[1] = j;
        }
        j = (j + 1) % virtual_distancers_.length;
      }
      //flatten_index_ = RollingAverage.Roll(flatten_index_, (double)fc / virtual_distancers_.length, depth, 1);
      //Printer.printf(0, "flatten_index: %.4f\n", flatten_index_);
      //Printer.printf(0, "acc: %.4f   %.2f%%\n", enemy_.accuracy_.average_, enemy_.accuracy_.average_ * 100);
      //Printer.printf(0, "fi: %.4f   %.2f%%\n", flatten_index_, flatten_index_ * 100);
      flat_ = virtual_scores_[0][bj[0]] < virtual_scores_[1][bj[1]];// - 1e-6;
      Distancer ndistancer = virtual_distancers_[bj[flat_ ? 1 : 0]];
      last_distancer_ = bj[flat_ ? 1 : 0];
      if ( was_flat != flat_ ) Printer.printf(0, "Flattener is on: %b\n", flat_);
      if ( distancer_ != ndistancer ) {
        distancer_ = ndistancer;
        //Printer.printf(0, "switching to distancer: %d (%b)\n", bj[flat_ ? 1 : 0], flat_);
      } else if ( TurnHandler.time_ == last_print_ ) {
        //Printer.printf(0, "refreshing distancer: %d (%b)\n", bj[flat_ ? 1 : 0], flat_);
      }
    }
    danger_.add(wave.snapshot_.ws_normal_, wave.snapshot_);
    return true;
  }
  */
  protected double Danger(Range window, Range window2) {
    return
    HotDanger(window, window2) * hot_weight_ +
    LinearDanger(window, window2) * linear_weight_ +
    CircularDanger(window, window2) * circular_weight_ +
    OrbitalDanger(window, window2) * orbital_weight_ +
    adaptative_weight_ * (HitDanger(window, window2) * HIT + FlatDanger(window, window2) * FLAT);
  }
  private double HotDanger(Range window, Range window2) {
    return SimpleDanger(window, HOT_FACTOR) * FIRST + SimpleDanger(window2, HOT_FACTOR) * SECOND;
  }
  private double OrbitalDanger(Range window, Range window2) {
    return SimpleDanger(window, ORBITAL_FACTOR) * FIRST + SimpleDanger(window2, ORBITAL_FACTOR) * SECOND;
  }
  private double LinearDanger(Range window, Range window2) {
    return SimpleDanger(window, surf_wave1_.linear_factor_) * FIRST + SimpleDanger(window2, surf_wave2_.linear_factor_) * SECOND;
  }
  private double CircularDanger(Range window, Range window2) {
    return SimpleDanger(window, surf_wave1_.circular_factor_) * FIRST + SimpleDanger(window2, surf_wave2_.circular_factor_) * SECOND;
  }
  private double SimpleDanger(Range window, double factor) {
    if ( window.Inside(factor) ) return 1;
    else if ( factor > window.window_[1] ) return Gauss(window.window_[1], factor);
    else return Gauss(window.window_[0], factor);
  }
  /**
  private double HitDanger(Range window, Range window2) {
    if ( hit_cluster1_.size() == 0 ) return 0;
    Range extended = new Range(window.window_[0] - THRESHOLD1, window.window_[1] + THRESHOLD1);
    Range extended2 = new Range(window2.window_[0] - THRESHOLD2, window2.window_[1] + THRESHOLD2);
    Iterator<Snapshot> it2 = hit_cluster2_.getValues().iterator();
    double hits = 0;
    double second_hits = 0;
    for ( Iterator<Snapshot> it = hit_cluster1_.getValues().iterator(); it.hasNext(); ) {
      Snapshot snapshot = it.next();
      //hits += SimpleDanger(window, snapshot.ws_hit_factor_);
      hits += SimpleDanger(extended, snapshot.ws_hit_factor_);
      snapshot = it2.next();
      //second_hits += SimpleDanger(window2, snapshot.ws_hit_factor_);
      second_hits += SimpleDanger(extended2, snapshot.ws_hit_factor_);
    }
    double first = hits / hit_cluster1_.size();
    double second = second_hits / hit_cluster1_.size();
    //Printer.printf(0, "hit_danger: %s %.4f (%d/%d) [%.4f]\n", window, first, hits, total, second);
    //Printer.printf(0, "second_danger: [%.2f, %.2f] %.4f (%d/%d) [%.4f]\n", low, high, second, second_hits, second_total, delta);
    return first * FIRST + second * SECOND;
  }
  /**/
  private double HitDanger(Range window, Range window2) {
    Range extended = new Range(window.window_[0] - THRESHOLD1, window.window_[1] + THRESHOLD1);
    Range extended2 = new Range(window2.window_[0] - THRESHOLD2, window2.window_[1] + THRESHOLD2);
    double hits = 0;
    double total = 0;
    Iterator<Snapshot> it2 = hit_cluster2_.getValues().iterator();
    double second_hits = 0;
    double second_total = 0;
    for ( Iterator<Snapshot> it = hit_cluster1_.getValues().iterator(); it.hasNext(); ) {
      Snapshot snapshot = it.next();
      if ( window.Inside(snapshot.ws_hit_factor_) ) hits += 1;
      if ( extended.Inside(snapshot.ws_hit_factor_) ) hits += 1;
      hits += (SimpleDanger(window, snapshot.ws_hit_factor_) + SimpleDanger(extended, snapshot.ws_hit_factor_)) / 2;
      total += 1;
      snapshot = it2.next();
      if ( window2.Inside(snapshot.ws_hit_factor_) ) second_hits  += 1;
      if ( extended2.Inside(snapshot.ws_hit_factor_) ) second_hits += 1;
      hits += (SimpleDanger(window2, snapshot.ws_hit_factor_) + SimpleDanger(extended2, snapshot.ws_hit_factor_)) / 2;
      second_total += 1;
    }
    final int per_snap = 3;
    double first = hits / Math.max(1.0, total * per_snap);
    double second = second_hits / Math.max(1.0, second_total * per_snap);
    //Printer.printf(0, "hit_danger: %s %.4f (%d/%d) [%.4f]\n", window, first, hits, total, second);
    //Printer.printf(0, "second_danger: [%.2f, %.2f] %.4f (%d/%d) [%.4f]\n", low, high, second, second_hits, second_total, delta);
    return first * FIRST + second * SECOND;
  }
  /**/
  private double FlatDanger(Range window, Range window2) {
    //Printer.printf(0, "%.2f, %.2f\n", window.window_[0] - THRESHOLD1, window.window_[1] + THRESHOLD1);
    Range extended = new Range(window.window_[0] - THRESHOLD1, window.window_[1] + THRESHOLD1);
    Range extended2 = new Range(window2.window_[0] - THRESHOLD2, window2.window_[1] + THRESHOLD2);
    double hits = 0;
    double total = 0;
    Iterator<Snapshot> it2 = flat_cluster2_.getValues().iterator();
    double second_hits = 0;
    double second_total = 0;
    //Printer.printf(0, "flat danger1: hit_window: [%s] [%s]\n", window, extended);
    //Printer.printf(0, "flat danger2: hit_window: [%s] [%s]\n", window2, extended2);
    for ( Iterator<Snapshot> it = flat_cluster1_.getValues().iterator(); it.hasNext(); ) {
      /* First Wave */
      Snapshot snapshot = it.next();
      double size = Math.min(window.Size(), snapshot.ws_hit_factor_window_ == null ? 0 : snapshot.ws_hit_factor_window_.Size());
      double e_size = Math.min(extended.Size(), snapshot.ws_hit_factor_window_ == null ? 0 : snapshot.ws_hit_factor_window_.Size());
      Range inter = window.Intersection(snapshot.ws_hit_factor_window_);
      Range inter2 = extended.Intersection(snapshot.ws_hit_factor_window_);
      hits += inter.Size() + inter2.Size();
      total += size + e_size;
      //Printer.printf(0, "flat1: %s  [%s] [%s]\n%.4f %.4f : %.4f %.4f : %.4f\n",
        //  snapshot.ws_hit_factor_window_ == null ? null : snapshot.ws_hit_factor_window_,
          //    inter, inter2, size, e_size,
            //  inter.Size(), inter2.Size(),
              //PRECISE * inter.Size() + EXTENDED * inter2.Size());
      /* Second Wave */
      snapshot = it2.next();
      size = Math.min(window2.Size(), snapshot.ws_hit_factor_window_ == null ? 0 : snapshot.ws_hit_factor_window_.Size());
      e_size = Math.min(extended2.Size(), snapshot.ws_hit_factor_window_ == null ? 0 : snapshot.ws_hit_factor_window_.Size());
      inter = window2.Intersection(snapshot.ws_hit_factor_window_);
      inter2 = extended2.Intersection(snapshot.ws_hit_factor_window_);
      second_hits += inter.Size() + inter2.Size();
      second_total += size + e_size;
      //Printer.printf(0, "flat2: %s  [%s] [%s]\n%.4f %.4f : %.4f %.4f : %.4f\n",
        //  snapshot.ws_hit_factor_window_ == null ? null : snapshot.ws_hit_factor_window_,
          //    inter, inter2, size, e_size,
            //  inter.Size(), inter2.Size(),
              //PRECISE * inter.Size() + EXTENDED * inter2.Size());
    }
    double first = total == 0 ? 0 : (hits / total);
    double second = total == 0 ? 0 : (second_hits / second_total);
    //Printer.printf(0, "flat danger: %.2f(%.2f %.2f) %.2f(%.2f %.2f)\n", first, hits, total, second, second_hits, second_total);
    //Printer.printf(0, "hit_danger: %s %.4f (%d/%d) [%.4f]\n", window, first, hits, total, second);
    //Printer.printf(0, "second_danger: [%.2f, %.2f] %.4f (%d/%d) [%.4f]\n", low, high, second, second_hits, second_total, delta);
    return first * FIRST + second * SECOND;
  }
  protected void UpdateWaves() {
    super.UpdateWaves();
    if ( surf_wave1_ == null ) return;
    BuildDanger();
  }
  private void BuildDanger() {
    int danger_size = ClusterSize(danger_);
    int flattener_size = ClusterSize(flattener_);
    if ( danger_size > last_danger_size_ + 5 || flattener_size > last_flattener_size_ + 10 ) {
      //Printer.printf(0, "using %d (%d) %d (%d) clusters\n", danger_size, danger_.size(), flattener_size, flattener_.size());
      last_danger_size_ = danger_size;
      last_flattener_size_ = flattener_size; 
    }
    Distancer distancer = virtual_distancers_[distancer_];
    hit_cluster1_ = danger_.buildCluster(surf_wave1_.snapshot_.ws_normal_, danger_size, distancer);
    hit_cluster2_ = danger_.buildCluster(surf_wave2_.snapshot_.ws_normal_, danger_size, distancer);
    flat_cluster1_ = flattener_.buildCluster(surf_wave1_.snapshot_.ws_normal_, flattener_size, distancer);
    flat_cluster2_ = flattener_.buildCluster(surf_wave2_.snapshot_.ws_normal_, flattener_size, distancer);
  }
  private void BuildDanger(Snapshot snapshot, Distancer distancer) {
    int danger_size = ClusterSize(danger_);
    int flattener_size = ClusterSize(flattener_);
    last_danger_size_ = danger_size;
    last_flattener_size_ = flattener_size; 
    hit_cluster1_ = hit_cluster2_ = danger_.buildCluster(snapshot.ws_normal_, danger_size, distancer);
    flat_cluster1_ = flat_cluster2_ = flattener_.buildCluster(snapshot.ws_normal_, flattener_size, distancer);
  }
  private int ClusterSize(MyTree<Snapshot> tree) {
    //Printer.printf(0, "sizes: %d %d\n",
      //  Math.max(Math.min(1, tree.size()), Math.min((int)Math.ceil(Math.sqrt(tree.size() / 2)), 100)), tree.size());
    if ( tree.size() == 1 ) return 1;
    return Math.max(Math.min(1, tree.size()), Math.min((int)Math.ceil(Math.sqrt(tree.size())), 100));
  }
}
