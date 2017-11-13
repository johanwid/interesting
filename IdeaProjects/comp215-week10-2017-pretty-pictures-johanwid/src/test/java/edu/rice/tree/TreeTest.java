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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@ParametersAreNonnullByDefault
@CheckReturnValue
public class TreeTest {
  @Rule
  public final ErrorCollector collector = new ErrorCollector();

  @Test
  public void testSuite() throws Exception {
    // insert 1000 sequential numbers, expected 500 <= maxDepth <= 1001
    TreeSuite.runAllTests("Tree", collector, Tree.makeEmpty(), Tree.makeEmpty(),
        false,1000, 500, 1001);
  }

  @Test
  public void testRotate() throws Exception {
    // We can't have a corresponding test like this for Treap because we have no deterministic way of predicting
    // how the priorities will force rotations.

    ITree<String> tree1 = Tree.of("Bob", "Alice", "Charlie"); // Bob on top, Alice to the left, Charlie to the right
    ITree<String> tree2 = Tree.of("Charlie", "Bob", "Alice"); // Charlie on top, Bob to the left, Alice to the left
    ITree<String> tree3 = Tree.of("Alice", "Bob", "Charlie"); // Alice on top, Bob to the right, Charlie to the right

    if (!(tree1 instanceof Tree.Node<?>)) {
      fail();
    }
    if (!(tree2 instanceof Tree.Node<?>)) {
      fail();
    }
    if (!(tree3 instanceof Tree.Node<?>)) {
      fail();
    }
    ITree<String> tree1Right = ((Tree.Node<String>) tree1).rotateRight();
    ITree<String> tree1Left = ((Tree.Node<String>) tree1).rotateLeft();

    assertEquals(tree2, tree1Left);
    assertEquals(tree3, tree1Right);
  }
}
