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

import edu.rice.list.List;
import org.junit.Test;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.Assert.*;

@ParametersAreNonnullByDefault
@CheckReturnValue
public class OptionTest {
  @Test
  public void some() throws Exception {
    Option<String> ostring = Option.some("Hello");
    assertTrue(ostring.isSome());
    assertEquals("Hello", ostring.get());
  }

  @Test(expected = NoSuchElementException.class)
  public void ofNullable() throws Exception {
    Option<String> ostring = Option.ofNullable("Hello");
    Option<String> ostring2 = Option.ofNullable(null);
    assertTrue(ostring.isSome());
    assertEquals("Hello", ostring.get());
    assertFalse(ostring2.isSome());

    String noValue = ostring2.get(); // should throw NoSuchElementException
    assertEquals("fail", noValue);
    fail(); // control flow shouldn't ever get here
  }

  @Test(expected = NoSuchElementException.class)
  public void none() throws Exception {
    Option<String> ostring = Option.none();
    assertFalse(ostring.isSome());

    String noValue = ostring.get(); // should throw NoSuchElementException
    assertEquals("fail", noValue);
    fail(); // control flow shouldn't ever get here
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  @Test
  public void fromOptional() throws Exception {
    // we're testing Java8's java.util.Optional vs. our edu.comp215.util.Option
    Optional<String> ostring = Optional.of("Hello");
    Option<String> ostring2 = Option.fromOptional(ostring);
    assertTrue(ostring.isPresent());
    assertTrue(ostring2.isSome());
    assertEquals("Hello", ostring.get());
    assertEquals("Hello", ostring2.get());
  }

  @Test
  public void toList() throws Exception {
    assertEquals(List.of("Hello"), Option.some("Hello").toList());
    assertEquals(List.makeEmpty(), Option.none().toList());
  }

  @Test
  public void match() throws Exception {
    assertEquals("HelloWorld", Option.some("Hello").match(() -> "Nope", str -> str + "World"));
    assertEquals("Empty", Option.none().match(() -> "Empty", str -> "Fail"));
  }

  @Test
  public void getOrElse() throws Exception {
    assertEquals("Correct", Option.none().getOrElse("Correct"));
    assertEquals("Correct", Option.some("Correct").getOrElse("Wrong"));
  }

  @Test
  public void orElse() throws Exception {
    assertEquals(Option.some("A"), Option.some("A").orElse(Option.some("B")));
    assertEquals(Option.some("A"), Option.some("A").orElse(Option.none()));
    assertEquals(Option.some("B"), Option.<String>none().orElse(Option.some("B")));
    assertEquals(Option.<String>none(), Option.<String>none().orElse(Option.none()));

    assertEquals(Option.some("A"), Option.some("A").orElse(() -> Option.some("B")));
    assertEquals(Option.some("A"), Option.some("A").orElse(() -> {
      fail("This lambda should never be executed!");
      return Option.some("B");
    }));
  }

  @Test(expected = RuntimeException.class)
  public void orElseThrow() throws Exception {
    RuntimeException re = new RuntimeException("test exception");

    assertEquals("Correct", Option.some("Correct").getOrElseThrow(() -> re));

    String notUsed = Option.<String>none().getOrElseThrow(() -> re); // should throw re
    assertEquals("fail", notUsed);
    fail(); // control flow shouldn't ever get here
  }

  @Test
  public void filter() throws Exception {
    assertEquals(Option.some("Hello"), Option.some("Hello").filter(str -> str.contains("H")));
    assertEquals(Option.none(), Option.some("World").filter(str -> str.contains("H")));
  }

  @Test
  public void map() throws Exception {
    assertEquals(Option.some("hello"), Option.some("Hello").map(String::toLowerCase));
    assertEquals(Option.<String>none(), Option.<String>none().map(String::toLowerCase));
  }

  @Test
  public void flatmap() throws Exception {
    Function<String,Option<String>> mapFunc = str -> Option.some(str.toLowerCase());

    assertEquals(Option.some("hello"), Option.some("Hello").flatmap(mapFunc));
    assertEquals(Option.<String>none(), Option.<String>none().flatmap(mapFunc));
  }

  @Test
  public void testEquals() throws Exception {
    assertEquals(Option.some("Hello"), Option.some("He" + "llo"));

    // also testing the alternate version of making an Option.none, where the type parameter is inferred
    // from the argument, but the argument isn't used on the inside...
    assertEquals(Option.<String>none(), Option.none("Ignored"));
  }

  @Test
  public void testHashcode() throws Exception {
    assertEquals(Option.some("Hello").hashCode(), Option.some("He" + "llo").hashCode());
    assertEquals(Option.<String>none().hashCode(), Option.<String>none().hashCode());
  }

  @Test
  public void testToString() throws Exception {
    assertEquals("Option.Some(\"Hello\")", Option.some("Hello").toString());
    assertEquals("Option.None()", Option.none().toString());
  }
}