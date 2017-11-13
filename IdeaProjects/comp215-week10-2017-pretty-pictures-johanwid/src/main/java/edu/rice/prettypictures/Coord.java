package edu.rice.prettypictures;
import java.awt.Color;

/*
	* foreach coordinate (x, y), this creates
	* a custom class to get the rgb values at
	* that point.
*/
class Coord {
  private final double r;
  private final double g;
  private final double b;

  Coord(double r, double g, double b) {
    this.r = r;
    this.g = g;
    this.b = b;
  }

  public double getRed() {
    return this.r;
  }
  public double getGreen() {
    return this.g;
  }

  public double getBlue() {
    return this.b;
  }

  public int getRGB() {
    int hr = (int) Math.ceil(127.5 * (r + 1));
    int hg = (int) Math.ceil(127.5 * (g + 1));
    int hb = (int) Math.ceil(127.5 * (b + 1));
    return new Color(hr, hg, hb).getRGB();
  }

  // public Coord do(Function<Double, Double> holdF) {
  // 	return new Coord(holdF.apply(r), holdF.apply(g), holdF.apply(b));
  // }
}
