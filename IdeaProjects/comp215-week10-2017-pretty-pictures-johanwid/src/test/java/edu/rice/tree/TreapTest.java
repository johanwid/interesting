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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@CheckReturnValue
public class TreapTest {
  @Rule
  public final ErrorCollector collector = new ErrorCollector();

  @Test
  public void testSuite() throws Exception {
    // insert 1000 sequential numbers, expected 5 <= maxDepth <= 30
    TreeSuite.runAllTests("Treap", collector, Treap.makeEmpty(), Treap.makeEmpty(),
        true,1000, 5, 30);
  }
}
