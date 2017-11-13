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
import edu.rice.list.LazyList;
import edu.rice.list.List;
import edu.rice.util.Pair;
import org.junit.rules.ErrorCollector;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

import static edu.rice.util.Performance.nanoBenchmarkVal;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;

/**
 * Static methods useful for testing Trees, Treaps, or anything else that implements ITree.
 * Typically you pass these the empty tree that's used for subsequent work.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
class TreeSuite {
  private final String name;
  private final ITree<String> emptyStringTree;
  private final ITree<Integer> emptyIntTree;

  // Engineering note: JUnit4 has the idea of an "error collector" which allows you to
  // run a bunch of tests, where we just want to remember the errors but keep on going.
  // We'll use this to run basicTests() on each of our prime number generators.
  private final ErrorCollector collector;

  private TreeSuite(String name, ErrorCollector collector, ITree<String> emptyStringTree, ITree<Integer> emptyIntTree) {
    this.name = name;
    this.collector = collector;
    this.emptyStringTree = emptyStringTree;
    this.emptyIntTree = emptyIntTree;
  }

  private void checkEqual(String reason, Object o1, Object o2) {
    // how to use an ErrorCollector to check if one thing is equal to another
    collector.checkThat(reason, o1, equalTo(o2));
  }

  private void checkNotEqual(String reason, Object o1, Object o2) {
    // how to use an ErrorCollector to check if one thing is not equal to another
    collector.checkThat(reason, o1, not(o2));
  }

  private void checkTrue(String reason, boolean b) {
    // how to use an ErrorCollector to check if a thing is true
    collector.checkThat(reason, b, equalTo(Boolean.TRUE));
  }

  private void checkFalse(String reason, boolean b) {
    // how to use an ErrorCollector to check if a thing is true
    collector.checkThat(reason, b, equalTo(Boolean.FALSE));
  }

  void testInsertSimple() {
    // Write a unit test that inserts two strings into a tree, then queries the tree to see whether those
    // values are present. Also, very that a third string is absent from the tree.

    ITree<String> testTree = emptyStringTree.insert("Alice").insert("Bob");
    checkTrue("insert simple", testTree.find("Alice").isSome());
    checkTrue("insert simple", testTree.find("Bob").isSome());
    checkTrue("insert simple", testTree.find("Charlie").isNone());

//    throw new RuntimeException("testInsert not implemented yet (project 4)");
  }

  void testRemoveSimple() {
    // Write a unit test that inserts two strings into a tree, then removes one of them. Verify that
    // both strings are still present in the original tree, and that the post-removal tree is indeed
    // missing the value you removed.

    ITree<String> testTree = emptyStringTree.insert("Alice").insert("Bob").remove("Bob");
    checkTrue("remove simple", testTree.find("Alice").isSome());
    checkTrue("remove simple", testTree.find("Bob").isNone());
    checkTrue("remove simple", testTree.find("Charlie").isNone());

//    throw new RuntimeException("testInsert not implemented yet (project 4)");
  }

  void testGreaterThanSimple() {
    // Write a unit test that inserts five strings into the tree, then make two queries against the
    // tree, against one of those strings. Use "inclusive" for one query and don't use it for the other.
    // Verify that the results have the correct members.

    final IList<String> testVectorList = List.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");
    final ITree<String> testTree = testVectorList.foldl(emptyStringTree, ITree::insert);
    final ITree<String> geqTree = testTree.greaterThan("Charlie", true);
    final ITree<String> gtrTree = testTree.greaterThan("Charlie", false);

    checkTrue("greater than simple", geqTree.find("Alice").isNone());
    checkTrue("greater than simple", geqTree.find("Bob").isNone());
    checkTrue("greater than simple", geqTree.find("Charlie").isSome());
    checkTrue("greater than simple", geqTree.find("Dorothy").isSome());
    checkTrue("greater than simple", geqTree.find("Eve").isSome());

    checkTrue("greater than simple", gtrTree.find("Alice").isNone());
    checkTrue("greater than simple", gtrTree.find("Bob").isNone());
    checkTrue("greater than simple", gtrTree.find("Charlie").isNone());
    checkTrue("greater than simple", gtrTree.find("Dorothy").isSome());
    checkTrue("greater than simple", gtrTree.find("Eve").isSome());

//    throw new RuntimeException("testInsert not implemented yet (project 4)");
  }

  void testLessThanSimple() {
    // Same as GreaterThan, but do it for LessThan.

    final IList<String> testVectorList = List.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");
    final ITree<String> testTree = testVectorList.foldl(emptyStringTree, ITree::insert);
    final ITree<String> leqTree = testTree.lessThan("Charlie", true);
    final ITree<String> lessTree = testTree.lessThan("Charlie", false);

    checkTrue("less than simple", leqTree.find("Alice").isSome());
    checkTrue("less than simple", leqTree.find("Bob").isSome());
    checkTrue("less than simple", leqTree.find("Charlie").isSome());
    checkTrue("less than simple", leqTree.find("Dorothy").isNone());
    checkTrue("less than simple", leqTree.find("Eve").isNone());

    checkTrue("less than simple", lessTree.find("Alice").isSome());
    checkTrue("less than simple", lessTree.find("Bob").isSome());
    checkTrue("less than simple", lessTree.find("Charlie").isNone());
    checkTrue("less than simple", lessTree.find("Dorothy").isNone());
    checkTrue("less than simple", lessTree.find("Eve").isNone());

//    throw new RuntimeException("testInsert not implemented yet (project 4)");
  }

  void testInsertList() {
    IList<String> testVectorList = List.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");
    ITree<String> tree1 = emptyStringTree.insert("Alice").insert("Bob").insert("Charlie").insert("Dorothy").insert("Eve");
    ITree<String> tree2 = emptyStringTree.insertList(testVectorList);

    // we have to convert to a list, first, because treaps might have different memory layouts, but toList() does an
    // in-order traversal, which we expect to give us consistent results.
    checkEqual("insert list", tree1.toList().toString(), tree2.toList().toString());

    checkTrue("insert list", tree1.toList().equals(tree2.toList()));
    checkTrue("insert list", tree2.toList().equals(tree1.toList()));

    ITree<String> tree3 = tree1.removeList(List.of("Alice", "Charlie"));

    checkEqual("insert list", List.of("Bob", "Dorothy", "Eve"), tree3.toList());
  }


  void testInorder() {
    IList<String> testVectorList = List.of("Charlie", "Eve", "Bob", "Alice", "Dorothy");
    ITree<String> tree = emptyStringTree.insertList(testVectorList);

    StringBuilder result = new StringBuilder();
    tree.inorder(result::append);

    checkEqual("test inorder", "AliceBobCharlieDorothyEve", result.toString());
  }

  void testToList() {
    final IList<String> testVectorList1 = List.of("Charlie", "Eve", "Bob", "Alice", "Dorothy");
    final IList<String> testVectorList2 = List.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");
    final IList<String> testVectorList3 = List.of("Eve", "Bob", "Alice", "Dorothy");
    final ITree<String> tree1 = emptyStringTree.insertList(testVectorList1);
    final ITree<String> tree2 = emptyStringTree.insertList(testVectorList2);
    final ITree<String> tree3 = emptyStringTree.insertList(testVectorList3);

    final IList<String> elist = emptyStringTree.toList();
    final IList<String> list1 = tree1.toList();
    final IList<String> list2 = tree2.toList();
    final IList<String> list3 = tree3.toList();

    final IList<String> lelist = emptyStringTree.toLazyList();
    final IList<String> llist1 = tree1.toLazyList();
    final IList<String> llist2 = tree2.toLazyList();
    final IList<String> llist3 = tree3.toLazyList();

    // regular lists should be equal
    checkEqual("to list", list1, list2);
    checkEqual("to list", list2, list1);
    checkEqual("to list", elist, elist);
    checkNotEqual("to list", list1, elist);
    checkNotEqual("to list", list1, list3);
    checkNotEqual("to list", list3, list1);

    // lazy lists should also be equal
    checkEqual("to list", llist1, llist2);
    checkEqual("to list", llist2, llist1);
    checkEqual("to list", lelist, lelist);
    checkNotEqual("to list", llist1, lelist);
    checkNotEqual("to list", llist1, llist3);
    checkNotEqual("to list", llist3, llist1);

    // and the lists should equal each other
    checkEqual("to list", elist, lelist);
    checkEqual("to list", list1, llist1);
    checkEqual("to list", list2, llist2);
    checkEqual("to list", list3, llist3);
  }

  void testRemove() {
    final IList<String> testVectorList = List.of("Charlie", "Hao", "Eve", "Gerald", "Bob", "Alice", "Frank", "Dorothy");
    final ITree<String> tree = emptyStringTree.insertList(testVectorList);
    final ITree<String> treeR1 = tree.remove("Alice");
    final ITree<String> treeR2 = tree.remove("Bob");
    final ITree<String> treeR3 = tree.remove("Gerald");

    checkEqual("remove", List.of("Bob", "Charlie", "Dorothy", "Eve", "Frank", "Gerald", "Hao"), treeR1.toList());
    checkEqual("remove", List.of("Alice", "Charlie", "Dorothy", "Eve", "Frank", "Gerald", "Hao"), treeR2.toList());
    checkEqual("remove", List.of("Alice", "Bob", "Charlie", "Dorothy", "Eve", "Frank", "Hao"), treeR3.toList());
    checkEqual("remove", emptyStringTree, emptyStringTree.remove("Alice"));
    checkEqual("remove", tree, tree.remove("Nobody"));

    checkTrue("remove", emptyStringTree.valid());
    checkTrue("remove", tree.valid());
    checkTrue("remove", treeR1.valid());
    checkTrue("remove", treeR2.valid());
    checkTrue("remove", treeR3.valid());
  }

  void testRange() {
    // first, do it for trees
    IList<String> testVectorList = List.of("Charlie", "Hao", "Eve", "Gerald", "Bob", "Alice", "Frank", "Dorothy");
    ITree<String> tree = emptyStringTree.insertList(testVectorList);

    checkEqual("range",
        List.of("Alice", "Bob", "Charlie"), tree.lessThan("Charlie", true).toList());
    checkEqual("range",
        List.of("Alice", "Bob"), tree.lessThan("Charlie", false).toList());
    checkEqual("range",
        List.of("Charlie", "Dorothy", "Eve", "Frank", "Gerald", "Hao"),
        tree.greaterThan("Charlie", true).toList());
    checkEqual("range",
        List.of("Dorothy", "Eve", "Frank", "Gerald", "Hao"), tree.greaterThan("Charlie", false).toList());
    checkEqual("range",
        List.of("Dorothy", "Eve", "Frank"),
        tree.greaterThan("Charlie", false).lessThan("Frank", true).toList());

    ITree<String> tmp;
    tmp = tree.lessThan("Charlie", true);
    checkEqual("range", List.of("Alice", "Bob", "Charlie"), tmp.toList());
    checkTrue("range", tmp.valid());

    tmp = tree.lessThan("Charlie", false);
    checkEqual("range", List.of("Alice", "Bob"), tmp.toList());
    checkTrue("range", tmp.valid());

    tmp = tree.greaterThan("Charlie", true);
    checkEqual("range", List.of("Charlie", "Dorothy", "Eve", "Frank", "Gerald", "Hao"), tmp.toList());
    checkTrue("range", tmp.valid());

    tmp = tree.greaterThan("Charlie", false);
    checkEqual("range", List.of("Dorothy", "Eve", "Frank", "Gerald", "Hao"), tmp.toList());
    checkTrue("range", tmp.valid());

    tmp = tree.greaterThan("Charlie", false).lessThan("Frank", true);
    checkEqual("range", List.of("Dorothy", "Eve", "Frank"), tmp.toList());
    checkTrue("range", tmp.valid());
  }

  void testEquals() {
    final IList<String> testVectorList1 = List.of("Charlie", "Eve", "Bob", "Alice", "Dorothy");
    final IList<String> testVectorList2 = List.of("Charlie", "Eve", "Bob", "Alice", "Dorothy");
    final IList<String> testVectorList3 = List.of("Eve", "Bob", "Alice", "Dorothy");
    final ITree<String> tree1 = emptyStringTree.insertList(testVectorList1);
    final ITree<String> tree2 = emptyStringTree.insertList(testVectorList2);
    final ITree<String> tree3 = emptyStringTree.insertList(testVectorList3);

    @SuppressWarnings({"SelfEquals", "EqualsWithItself"})
    boolean equalsTestResult = emptyStringTree.equals(emptyStringTree);

    checkTrue("equals", equalsTestResult);
    checkTrue("equals", tree1.equals(tree2));
    checkTrue("equals", tree2.equals(tree1));
    checkFalse("equals", emptyStringTree.equals(tree1));
    checkFalse("equals", tree1.equals(emptyStringTree));
    checkFalse("equals", tree1.equals(tree3));
  }

  void testSize() {
    IList<String> testVectorList = List.of("Charlie", "Eve", "Bob", "Alice", "Dorothy");
    ITree<String> tree = emptyStringTree.insertList(testVectorList);

    checkEqual("size", emptyStringTree.size(), 0);
    checkEqual("size", tree.size(), 5);
  }

  void testToString() {
    IList<String> testVectorList = List.of("Charlie", "Eve", "Bob", "Alice", "Dorothy");
    ITree<String> tree = emptyStringTree.insertList(testVectorList);

    checkEqual("toString",
        "Tree(Tree(Tree(\"Alice\"), \"Bob\", Tree()), \"Charlie\", Tree(Tree(\"Dorothy\"), \"Eve\", Tree()))",
        tree.toString());
  }

  void testRemoveMin() {
    ITree<String> tree = emptyStringTree.insertList(List.of("Charlie", "Eve", "Bob", "Alice", "Dorothy"));
    Pair<String, ITree<String>> failure = new Pair<>("Fail", emptyStringTree);

    ITree<String> resultTree = tree.removeMin().getOrElse(failure)
        .match((minVal, remainingTree) -> {
          checkEqual("remove min", "Alice", minVal);
          checkFalse("remove min", remainingTree.empty());
          return remainingTree;
        })
        .removeMin().getOrElse(failure)
        .match((minVal, remainingTree) -> {
          checkEqual("remove min", "Bob", minVal);
          checkFalse("remove min", remainingTree.empty());
          return remainingTree;
        })
        .removeMin().getOrElse(failure)
        .match((minVal, remainingTree) -> {
          checkEqual("remove min", "Charlie", minVal);
          checkFalse("remove min", remainingTree.empty());
          return remainingTree;
        })
        .removeMin().getOrElse(failure)
        .match((minVal, remainingTree) -> {
          checkEqual("remove min", "Dorothy", minVal);
          checkFalse("remove min", remainingTree.empty());
          return remainingTree;
        })
        .removeMin().getOrElse(failure)
        .match((minVal, remainingTree) -> {
          checkEqual("remove min", "Eve", minVal);
          checkTrue("remove min", remainingTree.empty());
          return remainingTree;
        });

    checkFalse("remove min", resultTree.removeMin().isSome());
  }

  void testMaxDepth() {
    checkEqual("max depth",0, emptyStringTree.maxDepth());
    ITree<String> oneElem = emptyStringTree.insert("Hello");
    checkEqual("max depth",1, oneElem.maxDepth());
    ITree<String> twoElem = oneElem.insert("Rice");
    checkEqual("max depth",2, twoElem.maxDepth());

    // once we insert a third element, we might have a two-level tree or we might have a three-level tree, depending
    // on how the balancing went
    ITree<String> threeElem = twoElem.insert("Owls!");
    int depth = threeElem.maxDepth();
    checkTrue("max depth",depth == 2 || depth == 3);
  }

  void testMaxDepth2(int ninserts, int minDepth, int maxDepth) {
    ITree<Integer> tree = emptyIntTree.insertList(LazyList.rangeInt(1, ninserts));

    int depth = tree.maxDepth();
    checkTrue("If this test fails, then your tree depth is too small; something is very wrong with your tree",
        depth >= minDepth);
    checkTrue("If this test fails, then your tree depth is too large; your tree rebalancing isn't working",
        depth <= maxDepth);
  }

  void testPerformance() {
    System.out.println("=========== " + name + " performance =========== ");
    // first, we'll insert random numbers; performance should be similar
    final Random random = new Random();
    final IList<Integer>
        numberList =
        LazyList.generate(random::nextInt).limit(100000).force(); // 100K random numbers
    final IList<Integer> numberList10K = numberList.limit(10000).force();
    final IList<Integer> integers1K = LazyList.rangeInt(0, 999).force();

    System.out.println(String.format("100K random inserts:     %7.3f μs per insert",
        1e-8 * nanoBenchmarkVal(() -> emptyIntTree.insertList(numberList)).a));

    System.out.println(String.format(" 10K random inserts:     %7.3f μs per insert",
        1e-7 * nanoBenchmarkVal(() -> emptyIntTree.insertList(numberList10K)).a));

    nanoBenchmarkVal(
        () -> emptyIntTree.insertList(integers1K))
        .consume((time, result) -> {
          System.out.println(String.format("  1K sequential inserts: %7.3f μs per insert", 1e-6 * time));
          checkTrue("performance", result.valid());
        });
  }

  /**
   * Run every tree-related test that we've got.
   * @param name Name of the algorithm (e.g., "Treap")
   * @param collector an ErrorCollector for reporting failures
   * @param emptyTree An empty tree of String
   * @param emptyIntTree An empty tree of Integer
   * @param randomized If a tree is randomized, then we shouldn't run certain equality tests
   * @param ninserts for the tree depth test, how many sequential inserts to conduct
   * @param minDepth expected minimum depth for the depth test
   * @param maxDepth expected maximum depth for the depth test
   */
  public static void runAllTests(String name, ErrorCollector collector,
                                 ITree<String> emptyTree, ITree<Integer> emptyIntTree,
                                 boolean randomized, int ninserts, int minDepth, int maxDepth) {
    TreeSuite suite = new TreeSuite(name, collector, emptyTree, emptyIntTree);
    suite.testInsertSimple();
    suite.testRemoveSimple();
    suite.testGreaterThanSimple();
    suite.testLessThanSimple();
    suite.testInsertList();
    suite.testInorder();
    suite.testToList();
    suite.testRemove();
    suite.testRange();
    suite.testSize();
    suite.testRemoveMin();
    suite.testMaxDepth();
    suite.testMaxDepth2(ninserts, minDepth, maxDepth);

    if (!randomized) {
      suite.testEquals();
      suite.testToString();
    }

    suite.testPerformance();
  }

}
