
package edu.rice.prettypictures;

public interface argFuncs extends FunctionTree {

  Coord evaluate(double x, double y);

  /*
      creates a coord for target pixel x.
  */
  class VarX implements argFuncs {

    VarX() {
    }

    public static VarX make() {
      return new VarX();
    }

    @Override
    public Coord evaluate(double x, double y) {
      return new Coord(x, x, x);
    }
  }

  /*
      creates a coord for target pixel y.
  */
  class VarY implements argFuncs {

    VarY() {
    }

    public static VarY make() {
      return new VarY();
    }

    @Override
    public Coord evaluate(double x, double y) {
      return new Coord(y, y, y);
    }

  }

  /*
      creates a coord for constants in range [-1, 1]
  */
  class Constant implements argFuncs {
    private final Coord hold;

    Constant(double d) {
      this.hold = new Coord(d, d, d);
    }

    Constant(double a, double b, double c) {
      this.hold = new Coord(a, b, c);
    }

    public static Constant make(double d) {
      return new Constant(d, d, d);
    }

    public static Constant make(double a, double b,
                                double c) {
      return new Constant(a, b, c);
    }

    @Override
    public Coord evaluate(double x, double y) {
      return hold;
    }
  }


  // one arg

  /*
      returns the negation of a coord.
  */
  class Negate implements argFuncs {
    private final argFuncs hold;

    Negate(argFuncs hold) {
      this.hold = hold;
    }

    public static Negate make(argFuncs hold) {
      return new Negate(hold);
    }

    @Override
    public Coord evaluate(double x, double y) {
      double nr = hold.evaluate(x, y).getRed() * -1;
      double ng = hold.evaluate(x, y).getGreen() * -1;
      double nb = hold.evaluate(x, y).getBlue() * -1;
      return new Coord(nr, ng, nb);
    }
  }

  /*
      finds the sine of a coord.
  */
  class Sine implements argFuncs {
    private final argFuncs hold;

    Sine(argFuncs hold) {
      this.hold = hold;
    }

    public static Sine make(argFuncs hold) {
      return new Sine(hold);
    }

    @Override
    public Coord evaluate(double x, double y) {
      double r = hold.evaluate(x, y).getRed();
      double g = hold.evaluate(x, y).getGreen();
      double b = hold.evaluate(x, y).getBlue();
      return new Coord(Math.sin(r), Math.sin(g), Math.sin(b));
    }
  }

  /*
      utilizes math.floor for a coord.
  */
  class RoundDown implements argFuncs {
    private final argFuncs hold;

    RoundDown(argFuncs hold) {
      this.hold = hold;
    }

    public static RoundDown make(argFuncs hold) {
      return new RoundDown(hold);
    }

    @Override
    public Coord evaluate(double x, double y) {
      double r = hold.evaluate(x, y).getRed();
      double g = hold.evaluate(x, y).getGreen();
      double b = hold.evaluate(x, y).getBlue();
      return new Coord(Math.floor(r), Math.floor(g), Math.floor(b));
    }
  }

  /*
      utlizies math.ceil for a coord.
  */
  class RoundUp implements argFuncs {
    private final argFuncs hold;

    RoundUp(argFuncs hold) {
      this.hold = hold;
    }

    public static RoundUp make(argFuncs hold) {
      return new RoundUp(hold);
    }

    @Override
    public Coord evaluate(double x, double y) {
      double r = hold.evaluate(x, y).getRed();
      double g = hold.evaluate(x, y).getGreen();
      double b = hold.evaluate(x, y).getBlue();
      return new Coord(Math.ceil(r), Math.ceil(g), Math.ceil(b));
    }
  }

  /*
      finds cosine of a coord.
  */
  class Cosine implements argFuncs {
    private final argFuncs hold;

    Cosine(argFuncs hold) {
      this.hold = hold;
    }

    public static Cosine make(argFuncs hold) {
      return new Cosine(hold);
    }

    @Override
    public Coord evaluate(double x, double y) {
      double r = hold.evaluate(x, y).getRed();
      double g = hold.evaluate(x, y).getGreen();
      double b = hold.evaluate(x, y).getBlue();
      return new Coord(Math.cos(r), Math.cos(g), Math.cos(b));
    }
  }

  /*
      finds arctan of a coord.
  */
  class Arctan implements argFuncs {
    private final argFuncs hold;

    Arctan(argFuncs hold) {
      this.hold = hold;
    }

    public static Arctan make(argFuncs hold) {
      return new Arctan(hold);
    }

    @Override
    public Coord evaluate(double x, double y) {
      double r = hold.evaluate(x, y).getRed();
      double g = hold.evaluate(x, y).getGreen();
      double b = hold.evaluate(x, y).getBlue();
      return new Coord(Math.atan(r), Math.atan(g), Math.atan(b));
    }
  }

  /*
      finds e^n_i of a coord.
  */
  class Expon implements argFuncs {
    private final argFuncs hold;

    Expon(argFuncs hold) {
      this.hold = hold;
    }

    public static Expon make(argFuncs hold) {
      return new Expon(hold);
    }

    public double clipper(double n) {
      return (n > 1) ? 1 : (n < -1) ? -1 : n;
    }

    @Override
    public Coord evaluate(double x, double y) {
      double r = hold.evaluate(x, y).getRed();
      double g = hold.evaluate(x, y).getGreen();
      double b = hold.evaluate(x, y).getBlue();
      return new Coord(
          clipper(Math.exp(r)),
          clipper(Math.exp(g)),
          clipper(Math.exp(b)));
    }
  }

  /*
      finds ln(n_i) of a coord.
  */
  class Logrm implements argFuncs {
    private final argFuncs hold;

    Logrm(argFuncs hold) {
      this.hold = hold;
    }

    public static Logrm make(argFuncs hold) {
      return new Logrm(hold);
    }

    @Override
    public Coord evaluate(double x, double y) {
      double r = hold.evaluate(x, y).getRed();
      double g = hold.evaluate(x, y).getGreen();
      double b = hold.evaluate(x, y).getBlue();
      return new Coord(Math.log(r), Math.log(g), Math.log(b));
    }
  }

  /*
      finds absval of a coord.
  */
  class AbsVal implements argFuncs {
    private final argFuncs hold;

    AbsVal(argFuncs hold) {
      this.hold = hold;
    }

    public static AbsVal make(argFuncs hold) {
      return new AbsVal(hold);
    }

    public double avhelper(double c) {
      double av = (c >= 0) ? c : (c * -1);
      return av;
    }

    @Override
    public Coord evaluate(double x, double y) {
      double r = hold.evaluate(x, y).getRed();
      double g = hold.evaluate(x, y).getGreen();
      double b = hold.evaluate(x, y).getBlue();
      // double ar = (r >= 0) ? r : (r * -1);
      double ar = avhelper(r);
      double ag = avhelper(g);
      double ab = avhelper(b);
      return new Coord(Math.log(ar), Math.log(ag), Math.log(ab));
    }
  }

  /*
      clips the value of coord elemnts to [-1, 1].
  */
  class Clip implements argFuncs {
    private final argFuncs hold;

    Clip(argFuncs hold) {
      this.hold = hold;
    }

    public static Clip make(argFuncs hold) {
      return new Clip(hold);
    }

    public double cliphelper(double n) {
      double cl = (n > 1) ? 1 : (n < -1) ? -1 : n;
      return cl;
    }

    @Override
    public Coord evaluate(double x, double y) {
      double r = hold.evaluate(x, y).getRed();
      double g = hold.evaluate(x, y).getGreen();
      double b = hold.evaluate(x, y).getBlue();
      // double cr = (r > 1) ? 1 : (r < -1) ? -1 : r;
      double cr = cliphelper(r);
      double cg = cliphelper(g);
      double cb = cliphelper(b);
      return new Coord(cr, cg, cb);
    }
  }

  /*
      wraps elements of coord ot [-1, 1].
  */
  class Wrap implements argFuncs {
    private final argFuncs hold;

    Wrap(argFuncs hold) {
      this.hold = hold;
    }

    public static Wrap make(argFuncs hold) {
      return new Wrap(hold);
    }

    public double wrapper(double n) {
      // return (n < -1) ? (n % 1) : (n > 1) ? (n % 1) * -1 : n;
      return -1 + (n + 1) % 2;
    }

    @Override
    public Coord evaluate(double x, double y) {
      double r = hold.evaluate(x, y).getRed();
      double g = hold.evaluate(x, y).getGreen();
      double b = hold.evaluate(x, y).getBlue();
      return new Coord(wrapper(r), wrapper(g),
          wrapper(b));
    }
  }

  /*
      converts from rgb coloring format to
      ycrcb format.
  */
  class RGBtoYCrCb implements argFuncs {
    private final argFuncs hold;

    RGBtoYCrCb(argFuncs hold) {
      this.hold = hold;
    }

    public static RGBtoYCrCb make(argFuncs hold) {
      return new RGBtoYCrCb(hold);
    }

    @Override
    public Coord evaluate(double x, double y) {
      double r = hold.evaluate(x, y).getRed();
      double g = hold.evaluate(x, y).getGreen();
      double b = hold.evaluate(x, y).getBlue();
      double y1 = 0.299, y2 = 0.587, y3 = 0.114;
      double cr1 = 0.500, cr2 = -0.419, cr3 = -0.081;
      double cb1 = -0.619, cb2 = -0.331, cb3 = 0.500;
      double yo = (r * y1) + (g * y2) + (b * y3);
      double cr = (r * cr1) + (g * cr2) + (b * cr3);
      double cb = (r * cb1) + (g * cb2) + (b * cb3);
      return new Coord(yo, cr, cb);
    }
  }

  /*
      converts from ycrcb coloring format to
      rgb format.
  */
  class YCrCbtoRGB implements argFuncs {
    private final argFuncs hold;

    YCrCbtoRGB(argFuncs hold) {
      this.hold = hold;
    }

    public static YCrCbtoRGB make(argFuncs hold) {
      return new YCrCbtoRGB(hold);
    }

    @Override
    public Coord evaluate(double x, double y) {
      double yo = hold.evaluate(x, y).getRed();
      double cr = hold.evaluate(x, y).getGreen();
      double cb = hold.evaluate(x, y).getBlue();
      double r1 = 1.000, r2 = 1.400, r3 = 0.000;
      double g1 = 1.000, g2 = -0.711, g3 = -0.343;
      double b1 = 1.000, b2 = 0.000, b3 = 1.765;
      double r = (yo * r1) + (yo * r2) + (yo * r3);
      double g = (cr * g1) + (cr * g2) + (cr * g3);
      double b = (cb * b1) + (cb * b2) + (cb * b3);
      return new Coord(r, g, b);
    }
  }

  // two args

  /*
      adds the elements of two coords.
  */
  class Add implements argFuncs {
    private final argFuncs one;
    private final argFuncs two;

    Add(argFuncs one, argFuncs two) {
      this.one = one;
      this.two = two;
    }

    public static Add make(argFuncs one, argFuncs two) {
      return new Add(one, two);
    }

    public double clipper(double n) {
      return (n > 1) ? 1 : (n < -1) ? -1 : n;
    }

    @Override
    public Coord evaluate(double x, double y) {
      double r1 = one.evaluate(x, y).getRed();
      double g1 = one.evaluate(x, y).getGreen();
      double b1 = one.evaluate(x, y).getBlue();
      double r2 = two.evaluate(x, y).getRed();
      double g2 = two.evaluate(x, y).getGreen();
      double b2 = two.evaluate(x, y).getBlue();
      return new Coord(
          clipper(r1 + r2),
          clipper(g1 + g2),
          clipper(b1 + b2));
    }
  }

  /*
      subtracts the elements of two coords.
  */
  class Subtract implements argFuncs {
    private final argFuncs one;
    private final argFuncs two;

    Subtract(argFuncs one, argFuncs two) {
      this.one = one;
      this.two = two;
    }

    public static Subtract make(argFuncs one, argFuncs two) {
      return new Subtract(one, two);
    }

    public double clipper(double n) {
      return (n > 1) ? 1 : (n < -1) ? -1 : n;
    }

    @Override
    public Coord evaluate(double x, double y) {
      double r1 = one.evaluate(x, y).getRed();
      double g1 = one.evaluate(x, y).getGreen();
      double b1 = one.evaluate(x, y).getBlue();
      double r2 = two.evaluate(x, y).getRed();
      double g2 = two.evaluate(x, y).getGreen();
      double b2 = two.evaluate(x, y).getBlue();
      return new Coord(
          clipper(r1 - r2),
          clipper(g1 - g2),
          clipper(b1 - b2));
    }
  }

  /*
      multiplies the elements of two coords.
  */
  class Multiply implements argFuncs {
    private final argFuncs one;
    private final argFuncs two;

    Multiply(argFuncs one, argFuncs two) {
      this.one = one;
      this.two = two;
    }

    public static Multiply make(argFuncs one, argFuncs two) {
      return new Multiply(one, two);
    }

    public double clipper(double n) {
      return (n > 1) ? 1 : (n < -1) ? -1 : n;
    }

    @Override
    public Coord evaluate(double x, double y) {
      double r1 = one.evaluate(x, y).getRed();
      double g1 = one.evaluate(x, y).getGreen();
      double b1 = one.evaluate(x, y).getBlue();
      double r2 = two.evaluate(x, y).getRed();
      double g2 = two.evaluate(x, y).getGreen();
      double b2 = two.evaluate(x, y).getBlue();
      return new Coord(
          clipper(r1 * r2),
          clipper(g1 * g2),
          clipper(b1 * b2));
    }
  }

  /*
      divides the elements of two coords.
  */
  class Divide implements argFuncs {
    private final argFuncs one;
    private final argFuncs two;

    Divide(argFuncs one, argFuncs two) {
      this.one = one;
      this.two = two;
    }

    public static Divide make(argFuncs one, argFuncs two) {
      return new Divide(one, two);
    }

    public double clipper(double n) {
      return (n > 1) ? 1 : (n < -1) ? -1 : n;
    }

    @Override
    public Coord evaluate(double x, double y) {
      double r1 = one.evaluate(x, y).getRed();
      double g1 = one.evaluate(x, y).getGreen();
      double b1 = one.evaluate(x, y).getBlue();
      double r2 = two.evaluate(x, y).getRed();
      double g2 = two.evaluate(x, y).getGreen();
      double b2 = two.evaluate(x, y).getBlue();
      double d1 = r2 == 0 ? 0 : r1 / r2;
      double d2 = g2 == 0 ? 0 : g1 / g2;
      double d3 = b2 == 0 ? 0 : b1 / b2;
      return new Coord(
          clipper(d1),
          clipper(d2),
          clipper(d3));
    }
  }

  /*
      dotproduct of the two coords.
  */
  class DotProduct implements argFuncs {
    private final argFuncs one;
    private final argFuncs two;

    DotProduct(argFuncs one, argFuncs two) {
      this.one = one;
      this.two = two;
    }

    public static DotProduct make(argFuncs one, argFuncs two) {
      return new DotProduct(one, two);
    }

    public double clipper(double n) {
      return (n > 1) ? 1 : (n < -1) ? -1 : n;
    }

    @Override
    public Coord evaluate(double x, double y) {
      double r1 = one.evaluate(x, y).getRed();
      double g1 = one.evaluate(x, y).getGreen();
      double b1 = one.evaluate(x, y).getBlue();
      double r2 = two.evaluate(x, y).getRed();
      double g2 = two.evaluate(x, y).getGreen();
      double b2 = two.evaluate(x, y).getBlue();
      double c = (r1 * r2) + (g1 * g2) + (b1 * b2);
      return new Coord(
          clipper(c),
          clipper(c),
          clipper(c));
    }
  }

  /*
      not implemented.
  */
  class External implements argFuncs {
    private final argFuncs one;
    private final argFuncs two;

    External(argFuncs one, argFuncs two) {
      this.one = one;
      this.two = two;
    }

    public static External make(argFuncs one, argFuncs two) {
      return new External(one, two);
    }

    @Override
    public Coord evaluate(double x, double y) {
      double r1 = one.evaluate(x, y).getRed();
      double g1 = one.evaluate(x, y).getGreen();
      double b1 = one.evaluate(x, y).getBlue();
      double r2 = two.evaluate(x, y).getRed();
      double g2 = two.evaluate(x, y).getGreen();
      double b2 = two.evaluate(x, y).getBlue();
      return new Coord(g1, g1, g1);
    }
  }

  /*
      creates grayscale perlin based on two coords.
  */
  class GSPerlin implements argFuncs {
    private final argFuncs one;
    private final argFuncs two;

    GSPerlin(argFuncs one, argFuncs two) {
      this.one = one;
      this.two = two;
    }

    public static GSPerlin make(argFuncs one, argFuncs two) {
      return new GSPerlin(one, two);
    }

    public int conv255to1(double n) {
      return (int) Math.ceil(n / 127.5) - 1;
    }

    @Override
    public Coord evaluate(double x, double y) {
      double r1 = one.evaluate(x, y).getRed();
      double g1 = one.evaluate(x, y).getGreen();
      double b1 = one.evaluate(x, y).getBlue();
      double no = ImprovedNoise.noise(r1, g1, b1);
      return new Coord(conv255to1(no), conv255to1(no),
          conv255to1(no));
    }
  }

  /*
      creates perlin based on two coords.
  */
  class Perlin implements argFuncs {
    private final argFuncs one;
    private final argFuncs two;

    Perlin(argFuncs one, argFuncs two) {
      this.one = one;
      this.two = two;
    }

    public static Perlin make(argFuncs one, argFuncs two) {
      return new Perlin(one, two);
    }

    public int conv255to1(double n) {
      return (int) Math.ceil(n / 127.5) - 1;
    }

    @Override
    public Coord evaluate(double x, double y) {
      double r1 = one.evaluate(x, y).getRed();
      double g1 = one.evaluate(x, y).getGreen();
      double b1 = one.evaluate(x, y).getBlue();
      double r2 = two.evaluate(x, y).getRed();
      double g2 = two.evaluate(x, y).getGreen();
      double b2 = two.evaluate(x, y).getBlue();
      double n1 = ImprovedNoise.noise(g1, b2, r2);
      double n2 = ImprovedNoise.noise(b1, r2, b2);
      double n3 = ImprovedNoise.noise(g1, r1, r1);
      return new Coord(conv255to1(n1), conv255to1(n2),
          conv255to1(n3));
    }
  }

  // three args

  /*
      dissolves based on three coords.
  */
  class Dissolve implements argFuncs {
    private final argFuncs one;
    private final argFuncs two;
    private final argFuncs tri;

    Dissolve(argFuncs one, argFuncs two, argFuncs tri) {
      this.one = one;
      this.two = two;
      this.tri = tri;
    }

    public static Dissolve make(argFuncs one, argFuncs two,
                                argFuncs tri) {
      return new Dissolve(one, two, tri);
    }

    // to clip t to [-1, 1]
    public double cliphelper(double n) {
      return (n > 1) ? 1 : (n < -1) ? -1 : n;
    }

    public double dissolver(double a, double b,
                            double c) {
      return (1 - c) * a + (c * b);
    }

    @Override
    public Coord evaluate(double x, double y) {
      double r1 = one.evaluate(x, y).getRed();
      double g1 = one.evaluate(x, y).getGreen();
      double b1 = one.evaluate(x, y).getBlue();
      double r2 = two.evaluate(x, y).getRed();
      double g2 = two.evaluate(x, y).getGreen();
      double b2 = two.evaluate(x, y).getBlue();
      double r3h = tri.evaluate(x, y).getRed();
      double g3h = tri.evaluate(x, y).getGreen();
      double b3h = tri.evaluate(x, y).getBlue();
      double r3 = cliphelper(r3h);
      double g3 = cliphelper(g3h);
      double b3 = cliphelper(b3h);
      return new Coord(dissolver(r1, r2, r3), dissolver(g1, g2, g3),
          dissolver(b1, b2, b3));
    }
  }
}