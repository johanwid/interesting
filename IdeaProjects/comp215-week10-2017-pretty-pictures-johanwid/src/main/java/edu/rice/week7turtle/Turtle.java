/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.week7turtle;

import edu.rice.json.Value;
import edu.rice.lens.Lens;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;

import static edu.rice.json.Builders.jobject;
import static edu.rice.json.Builders.jpair;
import static edu.rice.lens.Lens.lens;
import static java.lang.Math.*;

/**
 * "Turtle graphics" example to demonstrate how lenses can allow for functional updates. Of note, to the
 * client of this Turtle class, the lenses are <i>available</i> but easily ignored, while all the
 * methods that might do turtle operations can still take advantage of them.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public class Turtle {
  // Lots more information on turtle graphics:
  //     https://en.wikipedia.org/wiki/Turtle_graphics

  // A set of slides showing how to do lenses and turtle graphics (in Scala):
  //     https://docs.google.com/presentation/d/1jpo-glo9DU5SA57gBslLm2wFR-8kQJtEnoDT-WSsYgY/edit#slide=id.p

  // DATA DEFINITION! A "Turtle" is:
  //
  // a location (x, y), color (integer RGB, 8-bits each), heading (radians), and pen-state (up or down)
  //
  // We're treating the location as a separate class (Point, defined below) mostly to show you how these
  // things can compose. Seems like overkill here, but in a "real" implementation, you would probably have
  // "2D vector" as a general-purpose data type with lots of other methods on it.

  /**
   * A simple representation of a two-dimensional (x, y) point.
   */
  public static class Point {
    public final double x;
    public final double y;

    public static final Lens<Point, Double> X = lens(
        p -> p.x,
        (p, x) -> new Point(x, p.y));

    public static final Lens<Point, Double> Y = lens(
        p -> p.y,
        (p, y) -> new Point(p.x, y));

    public Point(double x, double y) {
      this.x = x;
      this.y = y;
    }

    /**
     * Returns a JSON representation of the point.
     */
    public Value toJson() {
      return jobject(jpair("x", x), jpair("y", y));
    }

    @Override
    public String toString() {
      // we could do this many other ways, but it's awfully convenient to just use the JSON routines
      return toJson().toString();
    }

    @Override
    public int hashCode() {
      return Double.hashCode(x) * 31 + Double.hashCode(y);
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Point)) {
        return false;
      }

      Point other = (Point) o;

      return other.x == x && other.y == y;
    }
  }

  public final boolean penDown;
  public final int color; // RGB, 8 bits each
  public final Point location;
  public final double heading; // radians

  public static final Lens<Turtle, Boolean> PenDown = lens(
      t -> t.penDown,
      (t, penDown) -> new Turtle(penDown, t.color, t.location, t.heading));

  public static final Lens<Turtle, Integer> Color = lens(
      t -> t.color,
      (t, color) -> new Turtle(t.penDown, color, t.location, t.heading));

  public static final Lens<Turtle, Point> Location = lens(
      t -> t.location,
      (t, location) -> new Turtle(t.penDown, t.color, location, t.heading));

  // useful function to clean up a heading, making sure it's in [0, 2pi)
  private static double normalizeHeading(double heading) {
    if (heading < 0) {
      double multiple = -floor(heading / (2 * PI));
      return heading + multiple * 2.0 * PI;
    } else if (heading < PI * 2) {
      return heading;
    } else {
      double multiple = floor(heading / (2 * PI));
      return heading - multiple * 2.0 * PI;
    }
  }

  public static final Lens<Turtle, Double> Heading = lens(
      t -> t.heading,
      (t, heading) -> new Turtle(t.penDown, t.color, t.location, normalizeHeading(heading)));

  private Turtle(boolean penDown, int color, Point location, double heading) {
    this.penDown = penDown;
    this.color = color;
    this.location = location;
    this.heading = heading;
  }

  /**
   * Public constructor makes a Turtle with reasonable default values (penDown = true, color = white,
   * location = (0,0), heading = 0). You can then use other methods on a Turtle object to update
   * these values.
   */
  public Turtle() {
    this.penDown = true;
    this.color = 0xffffff;
    this.location = new Point(0,0);
    this.heading = 0;
  }


  // and now, for some convenience methods that internally use these lenses

  /**
   * Returns a new turtle with the same state, except the pen is down.
   */
  public Turtle penDown() {
    return PenDown.set(this, true);
  }

  /**
   * Returns a new turtle with the same state, except the pen is up.
   */
  public Turtle penUp() {
    return PenDown.set(this, false);
  }

  /**
   * Returns a new turtle with the same state, except the color is updaed.
   */
  public Turtle setColor(int color) {
    return Color.set(this, color);
  }

  /**
   * Returns a new turtle with the same state, except the heading has rotated
   * left by the given number of degrees.
   */
  public Turtle leftDegrees(double delta) {
    return Heading.update(this, old -> old + delta * PI / 180.0);
  }

  /**
   * Returns a new turtle with the same state, except the heading has rotated
   * right by the given number of degrees.
   */
  public Turtle rightDegrees(double delta) {
    return leftDegrees(-delta);
  }

  /**
   * Returns a new turtle with the same state, except the heading is set to
   * the given angle, in degrees. Zero degrees means the turtle is pointed to the right.
   */
  public Turtle setHeadingDegrees(double degrees) {
    return Heading.set(this, degrees * PI / 180.0);
  }

  // compose some lenses together to make our lives easier
  public static final Lens<Turtle, Double> LocationX = Location.andThen(Point.X);
  public static final Lens<Turtle, Double> LocationY = Location.andThen(Point.Y);

  /**
   * Returns a new turtle with the same state, except the location of
   * the turtle has moved forward by the given distance, based on the heading of the turtle.
   */
  public Turtle forward(double distance) {
    // Engineering note: Our lenses force us to update X and Y separately.
    // This isn't particularly efficient, but it's clean to view. You could
    // imagine, alternatively, using Location.update() directly and extracting
    // the old x and y values from the original location. The code would be
    // modestly more complicated, but you could then use the code here to
    // verify it via unit tests.

    final Turtle x1 = LocationX.update(this, x0 -> x0 + distance * cos(heading));
    return LocationY.update(x1, y0 -> y0 + distance * sin(heading));
  }

  /**
   * Returns a new turtle with the same state, except the location of
   * the turtle is set to the given x, y coordinates.
   */
  public Turtle moveTo(double x, double y) {
    return Location.set(this, new Point(x, y));
  }

  /**
   * Returns a JSON representation of the turtle's state.
   */
  public Value toJson() {
    return jobject(
        jpair("penDown", penDown),
        jpair("color", Integer.toHexString(color)),
        jpair("location", location.toJson()),
        jpair("heading", heading));
  }

  @Override
  public String toString() {
    // we could do this many other ways, but it's awfully convenient to just use the JSON routines
    return toJson().toString();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Turtle)) {
      return false;
    }

    Turtle other = (Turtle) o;

    return other.penDown == penDown &&
        other.color == color &&
        other.heading == heading &&
        location.equals(other.location);
  }

  @Override
  public int hashCode() {
    // There's no strictly correct to build something like this, but multiplying each of the components by a prime
    // number and then combining them is a generally accepted practice.
    return Double.hashCode(heading) * 53 +
        location.hashCode() * 31 +
        Integer.hashCode(color) * 7 +
        Boolean.hashCode(penDown);
  }

  // Engineering note: Wouldn't it be nice if we didn't have to write all this boilerplate: toJson, toString, equals?
  // We could leave it out, but it's all exceptionally useful for debugging. Yes, you can get IntelliJ to write
  // some of it for you automatically, but in the "real world", how could you tell whether you were looking at
  // "standard" boilerplate versus something that the developer customized? What if there was a bug in all that
  // boilerplate code somewhere? That means you're going to need a lot more unit tests, which isn't much fun.
  // Wouldn't it be nice if all the boilerplate (including the lenses!) could all be automatically generated for
  // you unless you needed to do something special, as we do for normalizing the turtle's heading?

  // In the world of Java, lenses aren't widely enough used for there to be automated tools to generate them
  // for you, but for JSON support, there's a tool called Jackson which automatically generates conversions
  // to and from JSON, along with special annotations you can add for how individual fields should appear in
  // the resulting JSON. https://github.com/FasterXML/jackson

  // Also of note, there's a separate tool called Lombok (https://projectlombok.org/) that can also auto-generate
  // lots of boilerplate for you, but alas it similarly knows neither of lenses nor of JSON. (Jackson does know
  // how to get along with Lombok though.) The demo video at the Lombok home page helps make the argument that
  // programmers shouldn't have to write so much boilerplate.

  // At some point, you might say "enough already, this belongs directly in the programming language" and, sure
  // enough, many newer languages have all this and more; you declare a "data class" and all the rest happens
  // for you. Suffice to say that Comp215 makes you spell all this out so you know how all the machinery works.
  // In the future, you'll appreciate what's happening when it's happening automatically.
}
