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

import static org.junit.Assert.*;

@ParametersAreNonnullByDefault
@CheckReturnValue
public class ListQueueTest {
  @Test
  public void testEmpty() throws Exception {
    // write a test that creates an empty queue and verifies that it's empty
    IQueue<Integer> emptyQueue = ListQueue.makeEmpty();
    assertTrue(emptyQueue.empty());

    // fail("testEmpty not implemented yet");
  }

  @Test
  public void testEmptySize() throws Exception {
    // write a test that creates an empty queue and verifies that it's length is zero
    IQueue<Integer> emptyQueue = ListQueue.makeEmpty();
    assertEquals(0, emptyQueue.size());

    // fail("testEmptySize not implemented yet");
  }

  @Test
  public void testSizeEight() throws Exception {
    // write a test that creates a queue with eight *integers* in it, using ListQueue.of(...) and verifies that the size is 8
    IQueue<Integer> queue = ListQueue.of(1, 2, 3, 4, 5, 6, 7, 8);
    assertEquals(8, queue.size());

    // fail("testSizeEight not implemented yet");
  }

  @Test
  public void testToStringEight() throws Exception {
    // Make a queue using ListQueue.of(), as above, convert it to a string with the toString() method (provided for you),
    // and test that you get the string you were expecting.

    IQueue<Integer> queue = ListQueue.of(1, 2, 3, 4, 5, 6, 7, 8);
    assertEquals("Queue(1, 2, 3, 4, 5, 6, 7, 8)", queue.toString());

    // fail("testToStringEight not implemented yet");
  }

  @Test
  public void testSizeInsertOne() throws Exception {
    // write a test that creates an empty queue, adds one thing to it with the insert() method, then verifies the size
    IQueue<Integer> queue = ListQueue.<Integer>makeEmpty().insert(5);
    assertEquals(1, queue.size());

    // fail("testSizeInsertOne not implemented yet");
  }

  @Test
  public void testHeadOfThreeInserts() throws Exception {
    // write a test that creates an empty queue, adds three things to it with the insert() method, then verifies the head() is
    // the first thing that went in

    IQueue<String> queue = ListQueue.<String>makeEmpty().insert("Hello").insert("Rice").insert("Owls");
    assertEquals("Hello", queue.head());

    // fail("testHeadOfThreeInserts() not implemented yet");
  }

  @Test
  public void testHeadAndTailOfThreeInserts() throws Exception {
    // write a test that creates an empty queue, adds three things to it with the insert() method, testing the head() as above,
    // but also takes the tail() of the queue and verifies the head() of the result, doing this for each entry until you get
    // an empty queue (and verifying that the queue is indeed empty).

    IQueue<String> queue = ListQueue.<String>makeEmpty().insert("Hello").insert("Rice").insert("Owls");
    assertEquals("Hello", queue.head());
    assertEquals("Rice", queue.tail().head());
    assertEquals("Owls", queue.tail().tail().head());
    assertTrue(queue.tail().tail().tail().empty());

    // fail("testHeadAndTailOfThreeInserts() not implemented yet");
  }

  @Test
  public void testSingularEmptyQueue() throws Exception {
    // write a test that creates an empty queue, adds one thing, and then removes it. Verify that the resulting empty queue
    // and the original empty queue point to the identical object in memory
    // (i.e., use assertTrue(a == b) rather than assertEquals(a, b)).

    IQueue<String> emptyQueue = ListQueue.makeEmpty();
    IQueue<String> emptyQueue2 = emptyQueue.insert("Hello").tail();

    assertTrue(emptyQueue == emptyQueue2);

    // fail("testSingularEmptyQueue() not implemented yet");
  }

  @Test
  public void testToLazyList() throws Exception {
    // Write a test that exercises IQueue's toLazyList method. This test should verify correct behavior for empty queues
    // and non-empty queues. Try to be clever and make sure that the queue you're converting to a lazy list has something
    // in its inbox and its outbox, which means that you'll also be exercising your rebalancing logic.

    IQueue<Integer> queue = ListQueue.of(1, 2, 3, 4, 5, 6, 7, 8).insert(9).insert(10);
    assertEquals(LazyList.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), queue.toLazyList());

    // fail("testToLazyList() not implemented yet");
  }

  @Test
  public void testEqualsAndHashcode() throws Exception {
    IQueue<Integer> queue1 = ListQueue.of(1, 2, 3, 4, 5, 6, 7, 8);
    IQueue<Integer> queue2 = ListQueue.of(3, 4, 5, 6, 7, 8);
    assertTrue(queue1.tail().tail().equals(queue2));
    assertTrue(queue1.tail().tail().tail().equals(queue2.tail()));
    assertTrue(queue1.tail().tail().tail().insert(9).equals(queue2.tail().insert(9)));
    assertFalse(queue1.equals(queue2));
    assertFalse(queue1.tail().equals(queue2));
    assertFalse(queue1.tail().tail().insert(9).equals(queue2));

    assertEquals(queue1.tail().tail().hashCode(), queue2.hashCode());
    assertEquals(queue1.tail().tail().tail().hashCode(), queue2.tail().hashCode());
    assertEquals(queue1.tail().tail().tail().insert(9).hashCode(), queue2.tail().insert(9).hashCode());

    // hashCodes *might* collide, but it's going to be highly unlikely
    assertNotEquals(queue1.hashCode(), queue2.hashCode());
    assertNotEquals(queue1.tail().tail().hashCode(), queue2.tail().hashCode());
    assertNotEquals(queue1.tail().tail().insert(9).hashCode(), queue2.tail().insert(9).hashCode());
  }

  @Test
  public void testEverything() throws Exception {
    // this test, provided for you, does a bunch of inserts, then fetches, then more inserts, over and over,
    // to exercise the inbox/outbox reversing logic.

    IList<Integer> testNumbers = LazyList.rangeInt(0, 9); // numbers 0 through 9 inclusive
    IQueue<Integer> testQueue = ListQueue.makeEmpty();
    IQueue<Integer> resultQueue = ListQueue.makeEmpty();

    // Engineering note: you might look at this code and say "ah ha! mutation!" and indeed, our local variables
    // for testQueue and resultQueue are changing, but the underlying queues they point to are still functional.
    // We could certainly reengineer this test, itself, to be purely functional, but would that make it easier
    // or harder for you to read?

    for (int i = 0; i < 5; i++) {
      // first, insert the numbers 0 through 9
      testQueue = testNumbers.foldl(testQueue, IQueue::insert);

      // now, extract the first three numbers from the queue
      for (int j = 0; j < 3; j++) {
        resultQueue = resultQueue.insert(testQueue.head());
        testQueue = testQueue.tail();
      }
    }

    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4), resultQueue.toLazyList());
  }

}