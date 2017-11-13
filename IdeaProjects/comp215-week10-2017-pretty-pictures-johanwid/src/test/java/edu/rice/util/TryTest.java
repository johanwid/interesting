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
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

@ParametersAreNonnullByDefault
@CheckReturnValue
public class TryTest {
  @Test(expected = NoSuchElementException.class)
  public void ofNullable() throws Exception {
    String whatever = "Whatever";

    Try<String> tValue = Try.ofNullable(whatever);
    assertTrue(tValue.isSuccess());
    assertEquals(whatever, tValue.get());


    //noinspection ThrowableResultOfMethodCallIgnored
    Throwable th = tValue.getException();
    assertTrue(th != null);
    fail(); // control flow shouldn't have gotten here
  }

  @Test(expected = NoSuchElementException.class)
  public void ofNullable2() throws Exception {
    Try<String> tFailure = Try.ofNullable(null);
    assertTrue(tFailure.isFailure());

    //noinspection ThrowableResultOfMethodCallIgnored
    Throwable th = tFailure.getException();
    assertTrue(th instanceof NullPointerException);

    String notThere = tFailure.get(); // should throw an exception
    assertEquals("fail", notThere);
    fail(); // control flow shouldn't have gotten here
  }

  @Test(expected = NullPointerException.class)
  public void ofNullable3() throws Throwable {
    Try<String> tFailure = Try.ofNullable(null);
    assertTrue(tFailure.isFailure());

    String notThere = tFailure.getOrElseThrow(); // should throw an exception
    assertEquals("fail", notThere);
    fail(); // should have thrown an exception
  }

  @Test
  public void of() throws Exception {
    String whatever = "Whatever";

    RuntimeException rte = new RuntimeException("Oh no!");

    Try<String> tValue = Try.of(() -> whatever);
    assertEquals(Try.success(whatever), tValue);

    Try<String> tFailure = Try.of(
        () -> {
          throw rte;
        });
    assertEquals(Try.failure(rte), tFailure);
  }

  @Test
  public void fromOption() throws Exception {
    Option<String> oString = Option.some("Whatever");
    Try<String> tString = Try.fromOption(oString, () -> new RuntimeException("Oh no!"));
    assertTrue(tString.isSuccess());
    assertEquals("Whatever", tString.get());
  }
}