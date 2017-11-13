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

import edu.rice.io.Files;
import edu.rice.list.KeyValue;
import edu.rice.list.IList;
import edu.rice.list.LazyList;
import edu.rice.util.Option;
import org.junit.Test;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

import static edu.rice.util.Performance.nanoBenchmarkVal;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ParametersAreNonnullByDefault
@CheckReturnValue
public class TreapMapTest {

  @Test
  public void testEquals() throws Exception {
    IMap<String, Integer> map1 = TreapMap.of(
        KeyValue.make("Alice", 1),
        KeyValue.make("Bob", 1),
        KeyValue.make("Charlie", 1),
        KeyValue.make("Dorothy", 1));

    IMap<String, Integer> map2 = TreapMap.of(
        KeyValue.make("Alice", 1),
        KeyValue.make("Bob", 1),
        KeyValue.make("Charlie", 1),
        KeyValue.make("Dorothy", 1));

    assertEquals(map1, map2);
    assertEquals(map1.toString(), map2.toString());
  }

  @Test
  public void testAddRemove() throws Exception {
    IMap<String, Integer> map1 = TreapMap.of(
        KeyValue.make("Alice", 1),
        KeyValue.make("Bob", 1),
        KeyValue.make("Charlie", 1),
        KeyValue.make("Dorothy", 1),
        KeyValue.make("Eve", 1));

    IMap<String, Integer> map2 = TreapMap.of(
        KeyValue.make("Alice", 1),
        KeyValue.make("Bob", 1),
        KeyValue.make("Charlie", 1),
        KeyValue.make("Dorothy", 1));

    IMap<String, Integer> map2eve = map2.add("Eve", 1);
    IMap<String, Integer> map1noeve = map1.remove("Eve");

    assertEquals(map1, map2eve);
    assertEquals(map1noeve, map2);
  }

  @Test
  public void testLookup() throws Exception {
    IMap<String, Integer> map = TreapMap.of(
        KeyValue.make("Alice", 1),
        KeyValue.make("Bob", 2),
        KeyValue.make("Charlie", 3),
        KeyValue.make("Dorothy", 4),
        KeyValue.make("Eve", 5),
        KeyValue.make("Frank", 6),
        KeyValue.make("George", 7));

    assertEquals((Integer) 5, map.oget("Eve").getOrElse(-1));
    assertEquals((Integer) 7, map.oget("George").getOrElse(-1));
    assertTrue(!map.oget("Nobody").isSome());
  }

  @Test
  public void testEmpty() throws Exception {
    IMap<String, Integer> map = TreapMap.of();

    assertTrue(map.empty());
    assertEquals(0, map.size());
  }

  @Test
  public void testUnionIntersection() throws Exception {
    final IMap<String, Integer> map1 = TreapMap.of(
        KeyValue.make("Alice", 1),
        KeyValue.make("Bob", 2),
        KeyValue.make("Charlie", 3),
        KeyValue.make("Dorothy", 4),
        KeyValue.make("Eve", 5),
        KeyValue.make("Frank", 6),
        KeyValue.make("George", 7));

    final IMap<String, Integer> map2 = TreapMap.of(
        KeyValue.make("Alice", 10),
        KeyValue.make("Bob", 20),
        KeyValue.make("Charlie", 30));

    final IMap<String, Integer> intersectionMap = TreapMap.of(
        KeyValue.make("Alice", 11),
        KeyValue.make("Bob", 22),
        KeyValue.make("Charlie", 33));

    final IMap<String, Integer> unionMap = TreapMap.of(
        KeyValue.make("Alice", 11),
        KeyValue.make("Bob", 22),
        KeyValue.make("Charlie", 33),
        KeyValue.make("Dorothy", 4),
        KeyValue.make("Eve", 5),
        KeyValue.make("Frank", 6),
        KeyValue.make("George", 7));

    final IMap<String, Integer> exceptMap = TreapMap.of(
        KeyValue.make("Dorothy", 4),
        KeyValue.make("Eve", 5),
        KeyValue.make("Frank", 6),
        KeyValue.make("George", 7));

    final IMap<String, Integer> union1 = map1.union(map2, (x, y) -> (x + y));
    final IMap<String, Integer> union2 = map2.union(map1, (x, y) -> (x + y));
    final IMap<String, Integer> intersect1 = map1.intersect(map2, (x, y) -> (x + y));
    final IMap<String, Integer> intersect2 = map2.intersect(map1, (x, y) -> (x + y));
    final IMap<String, Integer> except1 = map1.except(map2);

    assertEquals(unionMap, union1);
    assertEquals(unionMap, union2);

    assertEquals(intersectionMap, intersect1);
    assertEquals(intersectionMap, intersect2);

    assertEquals(exceptMap, except1);

    assertEquals(3, intersect1.size());
    assertEquals(7, union1.size());

    assertEquals(Option.some(11), union1.oget("Alice"));
    assertEquals(Option.some(22), union1.oget("Bob"));
    assertEquals(Option.some(33), union1.oget("Charlie"));
    assertEquals(Option.some(4), union1.oget("Dorothy"));
    assertEquals(Option.some(5), union1.oget("Eve"));

    assertEquals(Option.some(11), intersect1.oget("Alice"));
    assertEquals(Option.some(22), intersect1.oget("Bob"));
    assertEquals(Option.some(33), intersect1.oget("Charlie"));
    assertTrue(!intersect1.oget("Dorothy").isSome());
    assertTrue(!intersect1.oget("Eve").isSome());
  }

  @Test
  public void testGreaterThan() throws Exception {
    IMap<String, Integer> map1 = TreapMap.of(
        KeyValue.make("Alice", 1),
        KeyValue.make("Bob", 2),
        KeyValue.make("Charlie", 3),
        KeyValue.make("Dorothy", 4),
        KeyValue.make("Eve", 5),
        KeyValue.make("Frank", 6),
        KeyValue.make("George", 7));

    IMap<String, Integer> map2 = TreapMap.of(
        KeyValue.make("Dorothy", 4),
        KeyValue.make("Eve", 5),
        KeyValue.make("Frank", 6),
        KeyValue.make("George", 7));

    assertEquals(map2, map1.greaterThan("Charlie", false));
  }

  @Test
  public void testLessThan() throws Exception {
    IMap<String, Integer> map1 = TreapMap.of(
        KeyValue.make("Alice", 1),
        KeyValue.make("Bob", 2),
        KeyValue.make("Charlie", 3),
        KeyValue.make("Dorothy", 4),
        KeyValue.make("Eve", 5),
        KeyValue.make("Frank", 6),
        KeyValue.make("George", 7));

    IMap<String, Integer> map2 = TreapMap.of(
        KeyValue.make("Alice", 1),
        KeyValue.make("Bob", 2),
        KeyValue.make("Charlie", 3));

    assertEquals(map2, map1.lessThan("Dorothy", false));
  }

  @Test
  public void testKeys() throws Exception {
    IMap<String, Integer> map1 = TreapMap.of(
        KeyValue.make("Alice", 1),
        KeyValue.make("Bob", 2),
        KeyValue.make("Charlie", 3),
        KeyValue.make("Dorothy", 4),
        KeyValue.make("Eve", 5),
        KeyValue.make("Frank", 6),
        KeyValue.make("George", 7));

    IList<String> keys = LazyList.of("Alice", "Bob", "Charlie", "Dorothy", "Eve", "Frank", "George");
    assertEquals(keys, map1.keys());
  }

  @Test
  public void testValues() throws Exception {
    IMap<String, Integer> map1 = TreapMap.of(
        KeyValue.make("Alice", 1),
        KeyValue.make("Bob", 2),
        KeyValue.make("Charlie", 3),
        KeyValue.make("Dorothy", 4),
        KeyValue.make("Eve", 5),
        KeyValue.make("Frank", 6),
        KeyValue.make("George", 7));

    IList<Integer> values = LazyList.of(1, 2, 3, 4, 5, 6, 7);
    assertEquals(values, map1.values());
  }

  @Test
  public void testMapReduce() throws Exception {
    final String baconIpsum = Files.readResource("baconIpsum.txt").getOrElse("failed to read file!");
    final String[] baconWords = baconIpsum.split(" ");

    final IMap<String, Integer> empty = TreapMap.makeEmpty();

    final IMap<String, Integer> freqCount = LazyList.fromArray(baconWords)
        .map(String::toLowerCase)
        .map(x -> KeyValue.make(x, 1))
        .foldl(empty, (map, elem) -> map.merge(elem.getKey(), elem.getValue(), (x, y) -> x + y));

    // let's test mapkv and the merging fromList creator, while we're at it
    final IList<String> baconList = LazyList.fromArray(baconWords);
    final IList<String> lowerCaseBacon = baconList.map(String::toLowerCase);

    final IMap<String, Integer> freqCount2 =
        TreapMap.fromList(lowerCaseBacon, x -> 1, (x, y) -> x + y);

    assertEquals(5, (int) freqCount.oget("bacon").getOrElse(-1));
    assertEquals(3, (int) freqCount.oget("andouille").getOrElse(-1));
    assertEquals(3, (int) freqCount.oget("ham").getOrElse(-1));

    assertEquals(5, (int) freqCount2.oget("bacon").getOrElse(-1));
    assertEquals(3, (int) freqCount2.oget("andouille").getOrElse(-1));
    assertEquals(3, (int) freqCount2.oget("ham").getOrElse(-1));
  }

  @Test
  public void testUpdate() throws Exception {
    final IMap<String, Integer> map1 = TreapMap.of(
        KeyValue.make("Alice", 1),
        KeyValue.make("Bob", 2),
        KeyValue.make("Charlie", 3),
        KeyValue.make("Dorothy", 4),
        KeyValue.make("Eve", 5),
        KeyValue.make("Frank", 6),
        KeyValue.make("George", 7));

    final IMap<String, Integer> map2 = map1
        .update("Eve", x -> x.map(y -> y + 10)) // updates something in place
        .update("Charlie", x -> Option.none()) // removes something that's there
        .update("Zed", x -> Option.some(100)) // creates something that's *not* there
        .update("Nobody", x -> Option.none()); // removes something that's not there

    final IMap<String, Integer> mapCheck = TreapMap.of(
        KeyValue.make("Alice", 1),
        KeyValue.make("Bob", 2),
        KeyValue.make("Dorothy", 4),
        KeyValue.make("Eve", 15),
        KeyValue.make("Frank", 6),
        KeyValue.make("George", 7),
        KeyValue.make("Zed", 100));

    assertEquals(mapCheck, map2);
  }

  @Test
  public void testPerformance() throws Exception {
    System.out.println("======================= Map Insert & Query Performance ======================= ");
    Random random = new Random();
    final int mapSize =    10000;
    final int numQueries = 10000;
    final IList<Integer> randomList = LazyList.generate(random::nextInt).limit(mapSize);
    final IList<Integer> queryList = randomList.limit(numQueries);
    final IList<Integer> referenceResult = queryList.map(x -> x * x);
    final IList<KeyValue<Integer, Integer>> squareList = IList.mapkv(randomList, key -> key * key);

    // all of these have the side-effect of fully evaluating the lazy lists
    assertEquals(mapSize, randomList.length());
    assertEquals(mapSize, squareList.length());
    assertEquals(numQueries, queryList.length());

    // first, do it for our TreapMap

    nanoBenchmarkVal(
        () -> {
          IMap<Integer, Integer> treapMap = TreapMap.fromList(squareList);
          return randomList.limit(numQueries).map(key -> treapMap.oget(key).getOrElse(-1)).force();
        })
        .consume((time, result) -> {
          assertEquals(referenceResult, result);

          System.out.println(String.format("TreapMap   : %d inserts, %d queries; %7.3f μs per insert",
              mapSize, numQueries, time / (1e3 * mapSize)));
        });

    // okay, now do it for java.util.TreeMap (actually a red-black tree)
    nanoBenchmarkVal(
        () -> {
          TreeMap<Integer, Integer> treeMap = new TreeMap<>();
          squareList.foreach(kv -> treeMap.put(kv.getKey(), kv.getValue())); // insert everything
          return randomList.limit(numQueries).map(treeMap::get).force();
        })
        .consume((time, result) -> {
          assertEquals(referenceResult, result);

          System.out.println(String.format("j.u.TreeMap: %d inserts, %d queries; %7.3f μs per insert",
              mapSize, numQueries, time / (1e3 * mapSize)));
        });

    //and lastly, let's do a HashMap
    nanoBenchmarkVal(
        () -> {
          Map<Integer, Integer> hashMap = new HashMap<>();
          squareList.foreach(kv -> hashMap.put(kv.getKey(), kv.getValue())); // insert everything
          return randomList.limit(numQueries).map(hashMap::get).force();
        })
        .consume((time, result) -> {
          assertEquals(referenceResult, result);

          System.out.println(String.format("j.u.HashMap: %d inserts, %d queries; %7.3f μs per insert",
              mapSize, numQueries, time / (1e3 * mapSize)));
        });

    System.out.println("======================= Map Range Query Performance ======================= ");

    //
    // First, build up full data structures that we'll use for subsequent queries. TreapMap is our
    // functional implementation. TreeMap comes from java.util, and uses an efficient red-black tree.
    //
    final IMap<Integer, Integer> treapMap = TreapMap.fromList(squareList);
    final TreeMap<Integer, Integer> treeMap = new TreeMap<>();
    squareList.foreach(kv -> treeMap.put(kv.getKey(), kv.getValue()));

    nanoBenchmarkVal(
        () -> queryList.map(query -> treapMap.greaterThan(query, true)).force())
        .consume((time, result) -> {
          assertEquals(numQueries, result.length());

          System.out.println(String.format("TreapMap   : %d range queries, %7.3f μs per query",
              numQueries, time / (1e3 * numQueries)));
        });

    nanoBenchmarkVal(
        () -> queryList.map(query -> treeMap.tailMap(query, true)).force())
        .consume((time, result) -> {
          assertEquals(numQueries, result.length());

          System.out.println(String.format("j.u.TreeMap: %d range queries, %7.3f μs per query",
              numQueries, time / (1e3 * numQueries)));
        });
  }
}
