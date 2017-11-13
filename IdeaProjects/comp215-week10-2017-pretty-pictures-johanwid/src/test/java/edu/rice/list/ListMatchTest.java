/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.list;

import org.junit.Test;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@ParametersAreNonnullByDefault
@CheckReturnValue
public class ListMatchTest {
  private final IList<String> list0 = List.makeEmpty();
  private final IList<String> list1 = List.of("Hello");
  private final IList<String> list2 = List.of("Hello", "Rice");
  private final IList<String> list3 = List.of("Hello", "Rice", "Owls");

  @Test
  public void testMatch2() throws Exception {
    final Function<IList<String>, String> uberMatcher =
        list -> list.match(
            emptyList -> "empty",
            (head, tail) -> head + "/" + tail.toString());

    assertEquals("empty", uberMatcher.apply(list0));
    assertEquals("Hello/List()", uberMatcher.apply(list1));
    assertEquals("Hello/List(\"Rice\")", uberMatcher.apply(list2));
    assertEquals("Hello/List(\"Rice\", \"Owls\")", uberMatcher.apply(list3));
  }

  @Test
  public void testMatch3() throws Exception {
    final Function<IList<String>, String> uberMatcher =
        list -> list.match(
            emptyList -> "empty",
            (head, tail) -> head + "/" + tail.toString(),
            (head, second, tail) -> head + "/" + second + "/" + tail.toString());

    assertEquals("empty", uberMatcher.apply(list0));
    assertEquals("Hello/List()", uberMatcher.apply(list1));
    assertEquals("Hello/Rice/List()", uberMatcher.apply(list2));
    assertEquals("Hello/Rice/List(\"Owls\")", uberMatcher.apply(list3));
  }

  @Test
  public void testConsume2() throws Exception {
    // the consume methods work just like the match methods, but return nothing; this means that
    // we need to put assertions inside the lambdas to make sure the right things are showing up

    list3.consume(
        empty -> fail(),
        (head, tail) -> {
          assertEquals("Hello", head);
          assertEquals(List.of("Rice", "Owls"), tail);
        });

    list2.consume(
        empty -> fail(),
        (head, tail) -> {
          assertEquals("Hello", head);
          assertEquals(List.of("Rice"), tail);
        });

    list1.consume(
        empty -> fail(),
        (head, tail) -> {
          assertEquals("Hello", head);
          assertTrue(tail.empty());
        });


    list0.consume(
        shouldBeEmpty -> assertTrue(shouldBeEmpty.empty()),
        (head, tail) -> fail());
  }

  @Test
  public void testConsume3() throws Exception {
    // the consume methods work just like the match methods, but return nothing; this means that
    // we need to put assertions inside the lambdas to make sure the right things are showing up

    list3.consume(
        empty -> fail(),
        (head, tail) -> fail(),
        (head, second, tail) -> {
          assertEquals("Hello", head);
          assertEquals("Rice", second);
          assertEquals(List.of("Owls"), tail);
        });

    list2.consume(
        empty -> fail(),
        (head, tail) -> fail(),
        (head, second, tail) -> {
          assertEquals("Hello", head);
          assertEquals("Rice", second);
          assertTrue(tail.empty());
        });

    list1.consume(
        empty -> fail(),
        (head, tail) -> {
          assertEquals("Hello", head);
          assertTrue(tail.empty());
        },
        (head, second, tail) -> fail());

    list0.consume(
        shouldBeEmpty -> assertTrue(shouldBeEmpty.empty()),
        (head, tail) -> fail(),
        (head, second, tail) -> fail());
  }
}