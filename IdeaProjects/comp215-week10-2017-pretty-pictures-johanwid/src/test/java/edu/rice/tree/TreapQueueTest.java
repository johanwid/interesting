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

import edu.rice.list.*;
import edu.rice.list.List;
import org.junit.Test;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

import static edu.rice.util.Performance.nanoBenchmarkVal;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("JdkObsolete")
@ParametersAreNonnullByDefault
@CheckReturnValue
public class TreapQueueTest {

  @Test
  public void test1() throws Exception {
    IQueue<Integer> queue1 = TreapQueue.of(5, 2, 9, 7, 3, 10, 100, 4, -2);

    assertEquals("Queue(5, 2, 9, 7, 3, 10, 100, 4, -2)", queue1.toString());

    assertEquals(9, queue1.size());
    assertEquals(5, (int) queue1.head());
    assertEquals(2, (int) queue1.tail().head());
    assertEquals(9, (int) queue1.tail().tail().head());
    assertEquals(7, (int) queue1.tail().tail().tail().head());

    assertEquals(List.of(5,2,9,7,3,10,100,4,-2), queue1.toLazyList());
  }

  @Test
  public void testPerf() throws Exception {
    final int queueSize = 1000000;
    final int fetches = 10000;
    Random random = new Random();
    System.out.println("=========== Priority Queue Performance =========== ");
    final IList<Integer> numberList = LazyList.generate(random::nextInt).limit(queueSize); // one million random numbers

    // causes the random number generator to be called 1M times, so it's not part of the timing
    numberList.force();

    final IList<Integer> resultListBH = nanoBenchmarkVal(
        () -> {
          IPriorityQueue<Integer> priorityQueue = BinaryHeap.make((a, b) -> a.compareTo(b) < 0);
          numberList.foreach(priorityQueue::insert);
          return LazyList.generate(priorityQueue::getMin).limit(fetches).force();
        })
        .match((time, result) -> {
          System.out.println(String.format("BinaryHeap       : %d inserts, %d fetches: %7.3f μs per insert",
              queueSize, fetches, time / (1e3 * queueSize)));

          assertEquals(fetches, result.length());
          assertTrue(result.isSorted());

          return result;
        });

    nanoBenchmarkVal(
        () -> {
          PriorityQueue<Integer> priorityQueue2 = new PriorityQueue<>();
          numberList.foreach(priorityQueue2::add);
          return LazyList.generate(priorityQueue2::poll).limit(fetches).force();
        })
        .consume((time, result) -> {
          System.out.println(String.format("j.u.PriorityQueue: %d inserts, %d fetches: %7.3f μs per insert",
              queueSize, fetches, time / (1e3 * queueSize)));
          assertEquals(fetches, result.length());
          assertTrue(result.isSorted());
          assertEquals(resultListBH, result);
        });

    System.out.println("=========== FIFO Queue Performance =========== ");

    nanoBenchmarkVal(
        () -> {
          IQueue<Integer> resultListQueue = numberList.foldl(ListQueue.makeEmpty(), IQueue::insert);
          return resultListQueue.toLazyList().limit(fetches).force();
        })
        .consume((time, result) -> {
          System.out.println(String.format("ListQueue     : %d inserts, %d fetches: %7.3f μs per insert",
              queueSize, fetches, time / (1e3 * queueSize)));

          // double check that the queue is FIFO
          assertEquals(numberList.limit(fetches), result);
        });

    nanoBenchmarkVal(
        () -> {
          IQueue<Integer> resultQueue2 = numberList.foldl(TreapQueue.makeEmpty(), IQueue::insert);
          return resultQueue2.toLazyList().limit(fetches).force();
        })
        .consume((time, result) -> {
          System.out.println(String.format("TreapQueue    : %d inserts, %d fetches: %7.3f μs per insert",
              queueSize, fetches, time / (1e3 * queueSize)));

          // double check that the queue is FIFO
          assertEquals(numberList.limit(fetches), result);
        });

    // okay, now for good-old-fashioned java.util.List
    nanoBenchmarkVal(
        () -> {
          Queue<Integer> llist = new LinkedList<>();
          numberList.foreach(llist::add);
          return LazyList.generate(llist::remove).limit(fetches).force();
        })
        .consume((time, result) -> {
          System.out.println(String.format("j.u.LinkedList: %d inserts, %d fetches: %7.3f μs per insert",
              queueSize, fetches, time / (1e3 * queueSize)));

          // double check that the queue is FIFO
          assertEquals(numberList.limit(fetches), result);
        });

    // finally, the fancier java.util.ArrayDeque
    nanoBenchmarkVal(
        () -> {
          Queue<Integer> arrayDQ = new ArrayDeque<>();
          numberList.foreach(arrayDQ::add);
          return LazyList.generate(arrayDQ::remove).limit(fetches).force();
        })
        .consume((time, result) -> {
          System.out.println(String.format("j.u.ArrayDeque: %d inserts, %d fetches: %7.3f μs per insert",
              queueSize, fetches, time / (1e3 * queueSize)));

          // double check that the queue is FIFO
          assertEquals(numberList.limit(fetches), result);
        });
  }
}
