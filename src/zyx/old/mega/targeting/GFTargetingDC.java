package zyx.old.mega.targeting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import zyx.old.mega.bot.Enemy;
import zyx.old.mega.utils.DangerSwitch;
import zyx.old.mega.utils.Snapshot;
import zyx.old.simonton.utils.Cluster;
import zyx.old.simonton.utils.Distancer;
import zyx.old.simonton.utils.MyTree;

public abstract class GFTargetingDC extends VGun {
  private static final double HIT_THRESHOLD = 0.11;
  //private static final double HIT_WEIGHT = 0.1;
  private static final double BBOX_WEIGHT = 1;
  private static final double CORNER_WEIGHT = 0.2;
  private static final double BULLET_BBOX_WEIGHT = 0.5;
  private static final double BULLET_CORNER_WEIGHT = 0.05;
  private static final double AS_HIT_WEIGHT = -2;
  private static final double AS_BBOX_WEIGHT = -1;
  private static final double AS_BULLET_HIT_WEIGHT = -1;
  private static final double AS_CORNER_WEIGHT = -0.2;
  private VGunSystem gun_system_;
  protected Distancer distancer_;
  public GFTargetingDC(Enemy enemy, VGunSystem gun_system) {
    super(enemy);
    gun_system_ = gun_system;
  }
  public void GunUpdate() {
    Cluster<Snapshot> cluster = gun_system_.tree_.buildCluster(enemy_._now_.gf_normal_, ClusterSize(gun_system_.tree_), distancer_);
    ArrayList<DangerSwitch> danger_array = new ArrayList<DangerSwitch>();
    ArrayList<DangerSwitch> as_danger_array = new ArrayList<DangerSwitch>();
    for ( Iterator<Snapshot> it = cluster.getValues().iterator(); it.hasNext(); ) {
      Snapshot snapshot = it.next();
      /* normal */
      danger_array.add(new DangerSwitch(snapshot.gf_bbox_factor_window_.window_[0], BBOX_WEIGHT));
      danger_array.add(new DangerSwitch(snapshot.gf_bbox_factor_window_.window_[1], -BBOX_WEIGHT));
      danger_array.add(new DangerSwitch(snapshot.gf_corner_factor_window_.window_[0], CORNER_WEIGHT));
      danger_array.add(new DangerSwitch(snapshot.gf_corner_factor_window_.window_[1], -CORNER_WEIGHT));
      /* anti-surfer */
      as_danger_array.add(new DangerSwitch(snapshot.gf_bbox_factor_window_.window_[0], BBOX_WEIGHT));
      as_danger_array.add(new DangerSwitch(snapshot.gf_bbox_factor_window_.window_[1], -BBOX_WEIGHT));
      as_danger_array.add(new DangerSwitch(snapshot.gf_corner_factor_window_.window_[0], CORNER_WEIGHT));
      as_danger_array.add(new DangerSwitch(snapshot.gf_corner_factor_window_.window_[1], -CORNER_WEIGHT));
    }
    cluster = gun_system_.hit_tree_.buildCluster(enemy_._now_.gf_normal_, HitClusterSize(gun_system_.hit_tree_), distancer_);
    for ( Iterator<Snapshot> it = cluster.getValues().iterator(); it.hasNext(); ) {
      Snapshot snapshot = it.next();
      if ( snapshot.gf_hit_ == Snapshot.BOT_HIT ) {
        /* normal */
        //danger_array.add(new DangerSwitch(snapshot.gf_hit_factor_ - HIT_THRESHOLD, HIT_WEIGHT));
        //danger_array.add(new DangerSwitch(snapshot.gf_hit_factor_ + HIT_THRESHOLD, -HIT_WEIGHT));
        danger_array.add(new DangerSwitch(snapshot.gf_bbox_factor_window_.window_[0], BBOX_WEIGHT));
        danger_array.add(new DangerSwitch(snapshot.gf_bbox_factor_window_.window_[1], -BBOX_WEIGHT));
        danger_array.add(new DangerSwitch(snapshot.gf_corner_factor_window_.window_[0], CORNER_WEIGHT));
        danger_array.add(new DangerSwitch(snapshot.gf_corner_factor_window_.window_[1], -CORNER_WEIGHT));
        /* anti-surfer */
        as_danger_array.add(new DangerSwitch(snapshot.gf_hit_factor_ - HIT_THRESHOLD, AS_HIT_WEIGHT));
        as_danger_array.add(new DangerSwitch(snapshot.gf_hit_factor_ + HIT_THRESHOLD, -AS_HIT_WEIGHT));
        as_danger_array.add(new DangerSwitch(snapshot.gf_bbox_factor_window_.window_[0], AS_BBOX_WEIGHT));
        as_danger_array.add(new DangerSwitch(snapshot.gf_bbox_factor_window_.window_[1], -AS_BBOX_WEIGHT));
        as_danger_array.add(new DangerSwitch(snapshot.gf_corner_factor_window_.window_[0], AS_CORNER_WEIGHT));
        as_danger_array.add(new DangerSwitch(snapshot.gf_corner_factor_window_.window_[1], -AS_CORNER_WEIGHT));
      } else if ( snapshot.gf_hit_ == Snapshot.BULLET_HIT ) {
        /* normal */
        danger_array.add(new DangerSwitch(snapshot.gf_bbox_factor_window_.window_[0], BULLET_BBOX_WEIGHT));
        danger_array.add(new DangerSwitch(snapshot.gf_bbox_factor_window_.window_[1], -BULLET_BBOX_WEIGHT));
        danger_array.add(new DangerSwitch(snapshot.gf_corner_factor_window_.window_[0], BULLET_CORNER_WEIGHT));
        danger_array.add(new DangerSwitch(snapshot.gf_corner_factor_window_.window_[1], -BULLET_CORNER_WEIGHT));
        /* anti-surfer */
        as_danger_array.add(new DangerSwitch(snapshot.gf_hit_factor_ - HIT_THRESHOLD, AS_BULLET_HIT_WEIGHT));
        as_danger_array.add(new DangerSwitch(snapshot.gf_hit_factor_ + HIT_THRESHOLD, -AS_BULLET_HIT_WEIGHT));
      }
    }
    aim_angle_ = as_aim_angle_ = enemy_.bearing_;
    if ( danger_array.size() > 0 ) {
      Collections.sort(danger_array);
      double danger = 0;
      double best = 0;
      double size = danger_array.get(0).factor_ + 1;
      double factor = (danger_array.get(0).factor_ - 1) / 2;
      for (int i = 0; i < danger_array.size(); ++i ) {
        DangerSwitch ds = danger_array.get(i);
        danger += ds.danger_;
        double next = -1;
        if ( i + 1 < danger_array.size() ) {
          next = danger_array.get(i + 1).factor_;
        }
        double s = next - ds.factor_;
        double diff = Math.abs(danger - best);
        if ( (diff > 1e-5 && danger > best) || (diff < 1e-5 && s < size) ) {
          size = s;
          best = danger;
          factor = (next + ds.factor_) / 2; 
        }
      }
      aim_angle_ += factor * gun_system_.next_wave_.direction_ * gun_system_.next_wave_.mae_;
    }
    if ( as_danger_array.size() > 0 ) {
      Collections.sort(as_danger_array);
      double danger = 0;
      double best = 0;
      double size = as_danger_array.get(0).factor_ + 1;
      double factor = (as_danger_array.get(0).factor_ - 1) / 2;
      for (int i = 0; i < as_danger_array.size(); ++i ) {
        DangerSwitch ds = as_danger_array.get(i);
        danger += ds.danger_;
        double next = -1;
        if ( i + 1 < as_danger_array.size() ) {
          next = as_danger_array.get(i + 1).factor_;
        }
        double s = next - ds.factor_;
        double diff = Math.abs(danger - best);
        if ( (diff > 1e-5 && danger > best) || (diff < 1e-5 && s < size) ) {
          size = s;
          best = danger;
          factor = (next + ds.factor_) / 2; 
        }
      }
      as_aim_angle_ += factor * gun_system_.next_wave_.direction_ * gun_system_.next_wave_.mae_;
    }
  }
  private int ClusterSize(MyTree<Snapshot> tree) {
    if ( tree.size() <= 2 ) return tree.size();
    return Math.max(Math.min(1, tree.size()), Math.min((int)Math.ceil(Math.sqrt(tree.size())), 100));
  }
  private int HitClusterSize(MyTree<Snapshot> tree) {
    /**/
    if ( tree.size() <= 2 ) return tree.size();
    if ( tree.size() < 12 ) return Math.max(Math.min(1, tree.size()), Math.min((int)Math.ceil(Math.sqrt(tree.size())), 80));
    return Math.max(Math.min(1, tree.size()), Math.min((int)Math.ceil(Math.sqrt(tree.size() / 2)), 80));
    /**
    if ( tree.size() < 3 ) return tree.size();
    return Math.max(Math.min(1, tree.size()), Math.min((int)Math.ceil(Math.sqrt(tree.size() / 2)), 20));
    /**/
  }
}
