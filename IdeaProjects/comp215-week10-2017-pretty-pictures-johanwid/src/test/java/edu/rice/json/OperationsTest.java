/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.json;

import edu.rice.io.Files;
import edu.rice.list.IList;
import edu.rice.list.KeyValue;
import edu.rice.list.List;
import edu.rice.util.Option;
import org.junit.Test;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;

import static edu.rice.json.Builders.*;
import static edu.rice.json.Operations.*;
import static edu.rice.json.Parser.parseJsonObject;
import static edu.rice.util.Strings.regexToPredicate;
import static org.junit.Assert.*;

@ParametersAreNonnullByDefault
@CheckReturnValue
public class OperationsTest {
  static final Value.JObject BIG_JSON_ALL_CAPS_TITLE =
      parseJsonObject(
          Files.readResource("bigJsonAllCapsTitle.json")
              .getOrElse(""))
      .get();

  static final Value.JObject BIG_JSON_QTY_PLUS_TEN =
      parseJsonObject(
          Files.readResource("bigJsonQtyPlusTen.json")
              .getOrElse(""))
      .get();

  // This JSON file has a collection of puppies that we're going to use for queries
  // and such; it notably has no JSON arrays in it, so the tests against this will
  // work even when the tests against the "bigJson" objects will fail, assuming that
  // you've got objects working and arrays not working yet.
  static final Value.JObject PUPPIES =
      parseJsonObject(
          Files.readResource("puppies.json")
              .getOrElse(""))
      .get();

  @Test
  public void getPathTest() throws Exception {
    // Engineering note: all of these examples are directly calling get() on an Option, without
    // any proof that it's Option.some. That's normally a bad engineering practice for production
    // code, but it's great for test code, because it encodes an assertion that the call will
    // succeed. If it fails, then the test fails.

    assertEquals("$10.00",
        ogetPath(ParserTest.BIG_COMPARISON, "items/0/price").get().asJString().toUnescapedString());
    assertEquals("$5.50",
        ogetPath(ParserTest.BIG_COMPARISON, "items/1/price").get().asJString().toUnescapedString());
    assertFalse(ogetPath(ParserTest.BIG_COMPARISON, "items/2/price").isSome());
    assertFalse(ogetPath(ParserTest.BIG_COMPARISON, "items/green/price").isSome());
    assertFalse(ogetPath(ParserTest.BIG_COMPARISON, "items/1/green").isSome());
    assertEquals((Double) 2.0, (Double) ogetPath(ParserTest.BIG_COMPARISON, "itemCount").get().asJNumber().get());
    assertFalse(ogetPath(ParserTest.BIG_COMPARISON, "itemCount/1").isSome());
  }

  @Test
  public void jObjectGetMatching() throws Exception {
    final Value.JObject puppies = PUPPIES.oget("puppies").get().asJObject();

    assertEquals(
        List.of(KeyValue.make("puppies", puppies)),
        PUPPIES.getMatching(regexToPredicate("puppies")));

    assertEquals(
        List.makeEmpty(),
        PUPPIES.getMatching(regexToPredicate("birds")));

    assertEquals(
        List.of(
            KeyValue.make("Charlie", puppies.oget("Charlie").get())),
        puppies.getMatching(regexToPredicate("Charlie")));

    // make sure our regex helper function is working correctly, while we're at it
    assertTrue(regexToPredicate("Char.*").test("Charlie"));
    assertTrue(regexToPredicate(".*").test("Charlie"));

    assertEquals(
        List.of(
            KeyValue.make("Charlie", puppies.oget("Charlie").get())),
        puppies.getMatching(regexToPredicate("Char.*")));

    assertEquals(
        List.of(
            KeyValue.make("Alice", puppies.oget("Alice").get()),
            KeyValue.make("Charlie", puppies.oget("Charlie").get())),
        puppies.getMatching(regexToPredicate(".*li.*")));

    assertEquals(
        List.of(
          KeyValue.make("Alice", puppies.oget("Alice").get()),
          KeyValue.make("Bob", puppies.oget("Bob").get()),
          KeyValue.make("Charlie", puppies.oget("Charlie").get())),
        puppies.getMatching(regexToPredicate(".*")));
  }

  @Test
  public void regexSearchTest() throws Exception {
    IList<Value> prices = getValuesMatchingPathRegex(ParserTest.BIG_COMPARISON, List.of(".*", ".*", "price"));
    assertEquals(List.of(javaString("$10.00"), javaString("$5.50")), prices);
  }

  @Test
  public void testUpdate() throws Exception {
    Value tmp1 = updatePath(ParserTest.BIG_COMPARISON, "items/0/qty",
        oval -> oval.map(val -> jnumber(val.asJNumber().get() + 10))).get();

    Value tmp2 = updatePath(tmp1, "items/1/qty",
        oval -> oval.map(val -> jnumber(val.asJNumber().get() + 10))).get();

    assertEquals(BIG_JSON_QTY_PLUS_TEN, tmp2);
  }

  @Test
  public void testBogusArrayIndices() throws Exception {
    // if you pass a string that isn't an integer, when dealing with a JArray, the result
    // of a getter should be Option.none(), and the result of a setter should be a no-op.

    assertEquals(Option.none(), ogetPath(ParserTest.BIG_COMPARISON, "items/zero/qty"));

    assertEquals(Option.some(ParserTest.BIG_COMPARISON),
        updatePath(ParserTest.BIG_COMPARISON, "items/zero/qty", oval -> Option.some(jnumber(300))));
  }

  @Test
  public void testUpdateNewDepth() throws Exception {
    Value basics =
        jobject(
            jpair("itemCount", 2),
            jpair("subtotal", "$15.50"));

    Value testVal = updatePath(basics, "a/b/c/d",
        oval -> Option.some(javaString("Hello!")))
        .get();

    Value expected =
        jobject(
            jpair("itemCount", 2),
            jpair("subtotal", "$15.50"),
            jpair("a",
                jobject(
                    jpair("b",
                        jobject(
                            jpair("c",
                                jobject(
                                    jpair("d", "Hello!"))))))));

    assertEquals(expected, testVal);

    // should nuke the whole a/b/c/d stack
    Value nowRemoveItVal = updatePath(expected, "a", oval -> Option.none()).get();
    assertEquals(basics, nowRemoveItVal);
  }

  @Test
  public void testPathSearchesNoArrays() throws Exception {
    assertEquals(
        List.of(
            List.of("puppies", "Alice", "hat"),
            List.of("puppies", "Bob", "hat"),
            List.of("puppies", "Charlie", "hat")),
        getPathsMatching(PUPPIES, List.of(regexToPredicate("puppies"), regexToPredicate(".*"), regexToPredicate("hat")))
    );
  }

  @Test
  public void testUpdatePathMatchesRegex() throws Exception {
    Value testVal = updateValuesMatchingPathRegex(ParserTest.BIG_COMPARISON,
        List.of(".*", ".*", "title"),
        oval -> oval.map(val -> javaString(val.asJString().toUnescapedString().toUpperCase())))
        .get();

    assertEquals(BIG_JSON_ALL_CAPS_TITLE, testVal);

    // nothing should actually match this time, so the result should be unchanged
    Value testVal2 = updateValuesMatchingPathRegex(ParserTest.BIG_COMPARISON,
        List.of("foo", "bar", "baz", "whee"),
        oval -> oval.map(val -> javaString(val.asJString().toUnescapedString().toUpperCase())))
        .get();

    assertEquals(ParserTest.BIG_COMPARISON, testVal2);
  }

  @Test
  public void testMissing() throws Exception {
    // First, we're starting a a simple object having one name/value pair ("name" -> "Alice"). We're going to update this
    // to add another name ("age" -> 10). The first assertion, as part of the update, asserts that there was nothing
    // previously there for the "field" key. The second assertion verifies that the new thing that we inserted landed
    // where we expected.

    Option<Value> obj = updatePath(
        jobject(jpair("name", "Alice")),
        "age",
        val -> {
          assertTrue(!val.isSome());
          return Option.some(jnumber(10));
        });

    assertEquals(
        jobject(
            jpair("name", "Alice"),
            jpair("age", 10)),
        obj.get());
  }
}
