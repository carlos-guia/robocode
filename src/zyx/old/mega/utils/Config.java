package zyx.old.mega.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import robocode.AdvancedRobot;

public class Config {
  public static boolean _raiko_;
  public static boolean _mc_;
  public static boolean _tc_;
  public static boolean movement_enabled_;
  public static boolean targeting_enabled_;
  public static boolean _raiko_fire_power_;
  public static boolean _pc_;

  public static void Load(AdvancedRobot robot) {
    Properties properties = new Properties();
    try {
      properties.load(new FileInputStream(robot.getDataFile("config.properties")));
    } catch (IOException e) {
      e.printStackTrace();
    }
    _raiko_fire_power_ = Boolean.parseBoolean(properties.getProperty("raiko.fire_power", "false"));
    String test = properties.getProperty("test", "none");
    if ( test.equalsIgnoreCase("raiko") ) {
      _raiko_ = true;
    } else if ( test.equalsIgnoreCase("mc") ) {
      _mc_ = true;
    } else if ( test.equalsIgnoreCase("tc") ) {
      _tc_ = true;
    } else if ( test.equalsIgnoreCase("pc") ) {
      _pc_ = true;
    }
    targeting_enabled_ = !(_raiko_ || _mc_);
    movement_enabled_ = !(_tc_ || _pc_);
    /**
    paint_level_ = properties.getProperty("paint.level", "GAME");
    print_level_ = properties.getProperty("print.level", "GAME");
    /**/
  }
}
