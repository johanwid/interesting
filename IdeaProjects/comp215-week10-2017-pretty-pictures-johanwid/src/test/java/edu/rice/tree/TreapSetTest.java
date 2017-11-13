/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.tree;

import edu.rice.list.IList;
import edu.rice.list.List;
import org.junit.Test;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;

import static org.junit.Assert.*;

@ParametersAreNonnullByDefault
@CheckReturnValue
public class TreapSetTest {

  @Test
  public void fromList() throws Exception {
    IList<String> names = List.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");
    ISet<String> nameSet = TreapSet.fromList(names);

    names.foreach(name -> assertTrue(nameSet.contains(name)));
    assertFalse(nameSet.contains("Frank"));
  }

  @Test
  public void fromSet() throws Exception {
    IList<String> names = List.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");

    java.util.Set<String> nameSet = new java.util.HashSet<>();
    names.foreach(nameSet::add); // mutation!

    ISet<String> ourSet = TreapSet.fromSet(nameSet);

    names.foreach(name -> assertTrue(ourSet.contains(name)));
    assertFalse(ourSet.contains("Frank"));
  }

  @Test
  public void testMap() throws Exception {
    IList<String> names = List.of("Alice", "Bob", "Charlie", "Dorothy", "Charlie", "Eve", "Alice");

    // Engineering note: the type annotations here are redundant for IntelliJ, but javac can't figure them out.
    //noinspection RedundantTypeArguments
    assertEquals(TreapSet.<String>fromList(names.map(String::toLowerCase)),
        TreapSet.<String>fromList(names).map(String::toLowerCase));
  }

  @Test
  public void testFlatMap() throws Exception {
    IList<String> names = List.of("Alice", "Bob", "Charlie", "Dorothy", "Charlie", "Eve", "Alice");

    // we're going to flatmap from a name to a set of names including the name, an all-caps and an all-lowercase version
    // Engineering note: the type annotations here are redundant for IntelliJ, but javac can't figure them out.
    //noinspection RedundantTypeArguments
    assertEquals(TreapSet.<String>fromList(names.flatmap(name -> List.of(name, name.toLowerCase(), name.toUpperCase()))),
        TreapSet.<String>fromList(names).flatmap(name -> TreapSet.of(name, name.toLowerCase(), name.toUpperCase())));
  }
}