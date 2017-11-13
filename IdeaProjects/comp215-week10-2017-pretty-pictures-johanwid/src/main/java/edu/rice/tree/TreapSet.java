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
import edu.rice.util.Option;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * TreapSet implements the ISet interface, providing a functional set implementation
 * backed by a Treap.
 * @see ISet
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public interface TreapSet<T extends Comparable<? super T>> extends ISet<T> {
  //
  // Data definition:
  //
  // A TreapSet can be one of two things: empty or non-empty.
  // These are represented as TreapSet.Empty and TreapSet.NonEmptySet
  // These are just wrappers around Treap. More details there.
  //


  /**
   * Create an empty set fo the given type parameter.
   * @param <T> any comparable type
   */
  static <T extends Comparable<? super T>> ISet<T> makeEmpty() {
    @SuppressWarnings("unchecked")
    ISet<T> typedEmpty = (ISet<T>) Empty.SINGLETON;
    return typedEmpty;
  }

  /**
   * Given a bunch of values passed as varargs to this function, return a set with those values.
   */
  @SuppressWarnings("varargs")
  @SafeVarargs
  static <T extends Comparable<? super T>> ISet<T> of(@Nullable T... values) {
    return TreapSet.<T>makeEmpty().addList(LazyList.fromArray(values));
  }

  /**
   * Given a list of values, return a set with those values.
   */
  static <T extends Comparable<? super T>> ISet<T> fromList(IList<T> list) {
    return TreapSet.<T>makeEmpty().addList(list);
  }

  /**
   * Given a list of values, return a set with those values. If two elements in the list have the same value, the
   * resulting set will use the mergeOp to combine them.
   */
  static <T extends Comparable<? super T>> ISet<T> fromList(IList<T> list, BinaryOperator<T> mergeOp) {
    return TreapSet.<T>makeEmpty().addListMerge(list, mergeOp);
  }

  /**
   * Given a java.util.Set (hashset, etc.), get back a functional set stored in our treap structure.
   */
  static <K extends Comparable<? super K>> ISet<K> fromSet(java.util.Set<K> inSet) {
    return fromList(LazyList.fromIterator(inSet.iterator()));
  }

  @Override
  default <R extends Comparable<? super R>> ISet<R> map(Function<? super T,? extends R> mapFunc) {
    // we're converting the set to a list, doing the map, and then converting back to a set again
    return fromList(IList.narrow(toList().map(mapFunc)));
  }

  @Override
  default <R extends Comparable<? super R>> ISet<R> flatmap(Function<? super T,? extends ISet<? extends R>> mapFunc) {
    // We're converting the set to a list, doing the map, and then folding all the resulting sets together.
    // It's a bit annoying to have to map the ISet::narrow function, since it's really a no-op, but this
    // makes the types come out properly. Also of note, Java8 doesn't require the type annotation on ISet::narrow
    // but IntelliJ does. Type inference is fun.
    return IList.narrow(toList().map(mapFunc).map(ISet::<R>narrow))
        .foldl(makeEmpty(), ISet::union);
  }

  class NonEmptySet<T extends Comparable<? super T>> implements TreapSet<T> {
    private final ITree<T> treap;

    // not for external use; start from the empty TreapSet instead
    private NonEmptySet(ITree<T> treap) {
      this.treap = treap;
    }

    @Override
    public <Q extends Comparable<? super Q>> ISet<Q> makeEmptySameType() {
      return makeEmpty();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }

      if (!(o instanceof ISet)) {
        return false;
      }

      ISet<?> set = (ISet<?>) o;

      // Note: we're suppressing unchecked type-cast warnings because the places where these warnings are being
      // generated in this method occur when we're only calling methods that care about the base Object type.
      // No runtime errors are possible here.

      //noinspection unchecked
      if (set.empty()) {
        return false;
      }

      //
      // Because set equality is a different animal from structural equality, and each treap could well have
      // a very different structure (because of the randomness), we need to do something else, like converting
      // the sets to a sorted list, which means doing an in-order traversal. Because of laziness, this will
      // terminate early if they're not equal, so it should be reasonably efficient.
      //
      //noinspection unchecked
      return toSortedList().equals(set.toSortedList());

    }

    @Override
    public int hashCode() {
      return treap.hashCode();
    }

    @Override
    public ISet<T> add(T value) {
      return new NonEmptySet<>(treap.insert(value));
    }

    @Override
    public ISet<T> remove(T value) {
      return new NonEmptySet<>(treap.remove(value)); // doesn't matter what the value is, only value equality is tested
    }

    @Override
    public Option<T> oget(T value) {
      return treap.find(value);
    }

    @Override
    public boolean empty() {
      return treap.empty();
    }

    @Override
    public int size() {
      return treap.size();
    }

    @Override
    public ISet<T> greaterThan(T query, boolean inclusive) {
      // just delegate to the internal treap
      return new NonEmptySet<>(treap.greaterThan(query, inclusive));
    }

    @Override
    public ISet<T> lessThan(T query, boolean inclusive) {
      // just delegate to the internal treap
      return new NonEmptySet<>(treap.lessThan(query, inclusive));
    }

    @Override
    public IList<T> toSortedList() {
      return treap.toLazyList(); // this happens to sort its results, doing an in-order traversal
    }

    @Override
    public String toString() {
      return "{" + toSortedList().join(", ") + "}";
    }
  }

  class Empty<T extends Comparable<? super T>> implements ISet.Empty<T>, TreapSet<T> {
    private static final ISet<?> SINGLETON = new TreapSet.Empty<>();

    // external user: don't call this; instead, call makeEmpty()
    private Empty() { }

    @Override
    public <Q extends Comparable<? super Q>> ISet<Q> makeEmptySameType() {
      return makeEmpty();
    }

    @Override
    public ISet<T> add(T value) {
      return new NonEmptySet<>(Treap.<T>makeEmpty().insert(value));
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }

      if (!(o instanceof ISet)) {
        return false;
      }

      ISet<?> set = (ISet<?>) o;

      // Note: we're suppressing unchecked type-cast warnings because the places where these warnings are being
      // generated in this method occur when we're only calling methods that care about the base Object type.
      // No runtime errors are possible here.

      //noinspection unchecked
      return set.empty();
    }

    @Override
    public int hashCode() {
      return 1;
    }

    @Override
    public String toString() {
      return "{}";
    }
  }
}
