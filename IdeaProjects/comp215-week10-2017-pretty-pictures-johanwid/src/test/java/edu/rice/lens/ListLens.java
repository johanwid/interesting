/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.lens;

import edu.rice.list.IList;
import edu.rice.list.List;
import edu.rice.util.Option;
import org.junit.Test;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;

import static org.junit.Assert.assertEquals;

/**
 * Make sure our lenses on lists work.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public class ListLens {
  @Test
  public void lensTest() throws Exception {
    final Lens<IList<String>, Option<String>> negfive = IList.lensNth(-5);
    final Lens<IList<String>, Option<String>> zero = IList.lensNth(0);
    final Lens<IList<String>, Option<String>> two = IList.lensNth(2);
    final Lens<IList<String>, Option<String>> five = IList.lensNth(5);

    final IList<String> fourStrings = List.of("Alice", "Bob", "Charlie", "Dorothy");
    final IList<String> charlieToCharles = List.of("Alice", "Bob", "Charles", "Dorothy");
    final IList<String> noCharlie = List.of("Alice", "Bob", "Dorothy");

    assertEquals(Option.none(), negfive.get(fourStrings));
    assertEquals(Option.some("Alice"), zero.get(fourStrings));
    assertEquals(Option.some("Charlie"), two.get(fourStrings));
    assertEquals(Option.none(), five.get(fourStrings));

    assertEquals(charlieToCharles, two.set(fourStrings, Option.some("Charles")));
    assertEquals(noCharlie, two.set(fourStrings, Option.none()));

    // now verify out-of-range updates are no-ops
    assertEquals(fourStrings, negfive.set(fourStrings, Option.some("Nobody")));
    assertEquals(fourStrings, five.set(fourStrings, Option.some("Nobody")));
  }
}
