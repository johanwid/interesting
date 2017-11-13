/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.stream;

import edu.rice.list.IList;
import edu.rice.list.KeyValue;
import edu.rice.tree.IMap;
import edu.rice.tree.ISet;
import edu.rice.tree.TreapMap;
import edu.rice.tree.TreapSet;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static edu.rice.list.LazyList.fromIterator;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toList;

/**
 * These static methods supply adapters to go from {@link IList}, {@link ISet}, and {@link IMap} to Java8's {@link Stream},
 * allowing for parallel computation with those stream interfaces.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public interface Adapters {
  /**
   * Given an {@link IList}, return a {@link Stream} of the same type; you may choose whether or not you want it to
   * be a parallel stream. The non-parallel version will happen in the original order. The parallel
   * version makes no ordering guarantees.
   */
  static <T> Stream<T> listToStream(IList<T> list, boolean parallel) {
    // possible characteristics are: ORDERED, DISTINCT, SORTED, SIZED, NONNULL, IMMUTABLE, CONCURRENT, and SUBSIZED.
    int characteristics = Spliterator.NONNULL | Spliterator.IMMUTABLE |
        ((parallel) ? 0 : Spliterator.ORDERED); // if not parallel, then we'll try to preserve the ordering

    return StreamSupport.stream(spliteratorUnknownSize(listToIterator(list), characteristics), parallel);
  }

  /**
   * Creates a standard Java iterator from an {@link IList}.
   */
  static <T> Iterator<T> listToIterator(IList<? extends T> list) {
    // Engineering note: this thing below is something that we haven't really talked about in class called
    // an "anonymous inner class". It's basically the same as creating a regular class that implements an
    // interface, except we're just not bothering to give it a name. Before Java8, these sorts of things
    // were the way that you made lambdas, and if you look at coding examples for things like Android,
    // you'll see these sorts of anonymous inner classes used ubiquitously. Java8 basically made them
    // obsolete, so we won't be talking about them in class.

    return new Iterator<T>() {
      private IList<T> state = IList.narrow(list); // mutating handle to the current location of the iterator

      @Override
      public boolean hasNext() {
        return !state.empty();
      }

      @Override
      public T next() {
        T result = state.head();
        state = state.tail(); // mutation!
        return result;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("can't remove elements from an IList -- they're mutation free!");
      }
    };

    // Engineering note: there's a whole other thing called a "spliterator" that hypothetically allows for
    // improved parallelism by making it easy to partition the underlying data and then operate independently
    // on each partition. Coding that up is more difficult, and it's unclear that the streams implementation
    // is good enough that it's worth the bother. Instead, by converting from our functional classes to
    // a Java iterator (which, as you can see above, does nothing particularly fancy), we'll be counting on
    // the "limited parallelism" which Streams already support. In particular, since we're stating with the
    // various "characteristics" below that, when parallel, we don't care about ordering, the Stream implementation
    // is free to dispatch the computations on the stream in any order.

    // We get fancier below, where we copy from the original list to an ArrayList, which is then amenable to
    // better parallelism.
  }

  /**
   * Given an {@link IList}, return a {@link Stream} of the same type; you may choose whether or not you want it to
   * be a parallel stream. The non-parallel version will happen in the original order. The parallel
   * version makes no ordering guarantees. This method internally copies everything from the IList into
   * an {@link ArrayList} before making it into a Stream. <b>This significantly improves
   * parallelism performance, despite the copying overhead</b>.
   */
  static <T> Stream<T> listToArrayStream(IList<? extends T> list, boolean parallel) {
    int listLength = list.length();

    final java.util.List<T> alist = new ArrayList<>(listLength);
    final Iterator<T> iter = listToIterator(list);
    for (int i = 0; iter.hasNext(); i++) {
      T val = iter.next();
      alist.add(i, val);
    }

    if (parallel) {
      return alist.parallelStream();
    } else {
      return alist.stream();
    }
  }

  /**
   * Given an {@link ISet}, return a {@link Stream} of the same type; you may choose whether or not you want it to
   * be a parallel stream. The non-parallel version will happen in sorted order. The parallel
   * version makes no ordering guarantees.
   */
  static <T extends Comparable<T>> Stream<T> setToStream(ISet<T> set, boolean parallel) {
    // possible characteristics are: ORDERED, DISTINCT, SORTED, SIZED, NONNULL, IMMUTABLE, CONCURRENT, and SUBSIZED.
    int characteristics = Spliterator.NONNULL | Spliterator.IMMUTABLE | Spliterator.DISTINCT |
        (parallel ? 0 : Spliterator.ORDERED | Spliterator.SORTED); // if not parallel, then we'll try to preserve the ordering

    return StreamSupport.stream(spliteratorUnknownSize(setToIterator(set, parallel), characteristics), parallel);
  }

  /**
   * Creates a standard Java {@link Iterator} from an {@link ISet}.
   *
   * @param set
   *     The set being iterated.
   * @param parallel
   *     If false, the set will be processed in sorted order. If true, no guarantees are made on the
   *     ordering.
   * @param <T>
   *     Type of the set.
   * @return An iterator over the set.
   */
  static <T extends Comparable<T>> Iterator<T> setToIterator(ISet<T> set, boolean parallel) {
    // there's a hypothetical performance improvement to be had here, since we don't care about ordering when  going in parallel
    return listToIterator((parallel) ? set.toList() : set.toSortedList());
  }

  /**
   * Given an {@link IMap}, return a {@link Stream} of the same type; you may choose whether or not you want it to
   * be a parallel stream. The non-parallel version will happen in key-sorted order. The parallel
   * version makes no ordering guarantees.
   */
  static <K extends Comparable<K>, V> Stream<KeyValue<K, V>> mapToStream(IMap<K, V> map, boolean parallel) {
    return setToStream(map.getSet(), parallel);
  }

  /**
   * Creates a standard Java {@link Iterator} from an {@link IMap}.
   *
   * @param map
   *     The map being iterated.
   * @param parallel
   *     If false, the map will be processed in sorted order. If true, no guarantees are made on the
   *     ordering.
   * @param <K>
   *     Key type for the map.
   * @param <V>
   *     Value type for the map.
   * @return An iterator over the map.
   */
  static <K extends Comparable<K>, V> Iterator<KeyValue<K, V>> mapToIterator(IMap<K, V> map, boolean parallel) {
    // there's a hypothetical performance improvement to be had here, since we don't care about ordering when going in parallel
    return listToIterator((parallel) ? map.toList() : map.toSortedList());
  }

  /**
   * Given a {@link Stream}, converts it to an {@link IList} of the same type. Runs fast, but not lazily.
   */
  static <T> IList<T> streamToEagerList(Stream<T> stream) {
    // The "collection" process will wind its way through the entire stream and extract a java.util.List
    // of unknown concrete type. Experimentally, if the stream is parallel(), this will run at full blast.
    // We're then wrapping that list with our own LazyList, which *is* lazy in reading out the java.util.List.
    return fromIterator(stream.collect(toList()).iterator());
  }

  /**
   * Given a {@link Stream}, converts it to an {@link IList} of the same type. Runs lazily, but not fast.
   */
  static <T> IList<T> streamToList(Stream<T> stream) {
    // Experimentally, asking a Stream for its iterator results in lazy behavior. Computation doesn't happen
    // on the stream until you read its iterator, and it's single-threaded, regardless of whether the Stream
    // is parallel.
    return fromIterator(stream.iterator());
  }

  /**
   * Given a {@link Stream}, converts it to an {@link ISet} of the same type.
   */
  static <T extends Comparable<T>> ISet<T> streamToSet(Stream<T> stream) {
    return TreapSet.fromList(streamToList(stream));
  }

  /**
   * Given a {@link Stream} of {@link KeyValue} tuples, converts it to an {@link IMap} of the same type.
   */
  static <K extends Comparable<K>, V> IMap<K, V> streamToMap(Stream<KeyValue<K, V>> stream) {
    return TreapMap.fromList(streamToList(stream));
  }
}
