package mu.node.rexweather.app.Helpers;

public class PointDouble {
  public double x, y;

  public PointDouble(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public String toString() {
    return "x=" + x + ", y=" + y;
  }
}
