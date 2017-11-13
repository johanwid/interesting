/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.util;

import org.junit.Test;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

/**
 * This particular test is meant more to stress out the Java8 and IntelliJ type inference system,
 * than it's especially useful for testing Option. Still, this code wouldn't compile properly without
 * the type annotations on Option.some() and/or Option.none(), shown below.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public class OptionTest2 {
  interface Multi {
    String yo();

    default <R> R match(Function<? super T1,? extends R> t1func,
                       Function<? super T2,? extends R> t2func,
                       Function<? super T3,? extends R> t3func) {
      if (this instanceof T1) {
        return t1func.apply((T1) this);
      } else if (this instanceof T2) {
        return t2func.apply((T2) this);
      } else if (this instanceof T3) {
        return t3func.apply((T3) this);
      } else {
        throw new RuntimeException("Whoa!");
      }
    }

    class T1 implements Multi {
      @Override
      public String yo() {
        return "T1";
      }
    }

    class T2 implements Multi {
      @Override
      public String yo() {
        return "T2";
      }
    }

    class T3 implements Multi {
      @Override
      public String yo() {
        return "T3";
      }
    }
  }

  @Test
  public void madnessOption() throws Exception {
    String result = new Multi.T1().match(
        Option::some,
        Option::some,
        t3 -> Option.<Multi>none()) // if you don't declare a type here, neither javac nor IntelliJ can infer it
        .map(Multi::yo).getOrElse("Nope");

    assertEquals("T1", result);


    // Similar to the above, but uses a different variant of Option.none, which has one input and infers its output
    // type parameter from the input type parameter, so therefore need to declare it as above.
    assertEquals("T1",
        new Multi.T1().match(
            Option::some,
            Option::some,
            Option::none).map(Multi::yo).getOrElse("Nope"));


    // This works now, but didn't work as of April 2016...
    // https://youtrack.jetbrains.com/issue/IDEA-153999
    assertEquals("2", new Multi.T2().match(
        t1 -> "1",
        t2 -> "2",
        Multi.T3::yo));
  }
}
