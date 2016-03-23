package zyx.old.mega.geometry;




public class BoundingSquare extends Rectangle {
  private Point center_;
  private double size_;
  public Point[] corners_;

  public BoundingSquare(Point center, double size) {
    center_ = center;
    size_ = size;
    corners_ = new Point[]{ new Point(), new Point(), new Point(), new Point() };
    Update();
  }

  public void Update() {
    x_ = center_.x_ - size_;
    y_ = center_.y_ - size_;
    width_ = height_ = 2 * size_;
    corners_[0].SetPoint(center_.x_ - size_, center_.y_ - size_);  
    corners_[1].SetPoint(center_.x_ + size_, center_.y_ - size_);  
    corners_[2].SetPoint(center_.x_ + size_, center_.y_ + size_);  
    corners_[3].SetPoint(center_.x_ - size_, center_.y_ + size_);  
  }

  public boolean Interescts(BoundingSquare rect) {
    if ( true ) return false;
    for (Point point : rect.corners_)
      if ( Inside(point, true) )
        return true;
    return false;
  }

}
