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

import edu.rice.util.Option;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.function.*;

import static edu.rice.list.IList.narrow;

/**
 * A lazy list can be infinitely long, yet implements the same
 * exact IList interface as used by traditional, eager lists.
 * @see IList
 * @see List
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public interface LazyList<T> extends IList<T> {
  //
  // Data definition:
  //
  // A LazyList is either: an empty-list, or a "cons cell" containing a "head" value and a reference to a Memo that
  // will return another lazy list (the "tail function"). These are represented by the LazyList.Empty and LazyList.Cons classes.
  //
  // External users will never see the implementation classes. Instead, they'll use the static methods of LazyList
  // to get things started, then will use the exported public interface (IList) which both Empty and Cons implement.
  //

  /**
   * Create an empty lazy list of the given type parameter.
   */
  static <T> IList<T> makeEmpty() {
    // The same instance can be used for any T since it will always be empty.
    // Details:
    // http://stackoverflow.com/questions/14313528/returning-a-generic-empty-list

    @SuppressWarnings("unchecked")
    IList<T> typedEmptyList = (IList<T>) Empty.SINGLETON;
    return typedEmptyList;
  }

  /**
   * Construct a lazy list with the specified head element and a function to iterate the rest, on
   * demand. Note that this maker is public and is intended for use by any client. This is
   * quite different from edu.rice.list.List, which wants you to start from an empty list and
   * build up from there.
   *
   * <p>The intended usage here is that you pass the head value of the list and a lambda that generates
   * the tail. That lambda will typically return yet another lazy list, with potentially yet another
   * lambda to generate the subsequent tail. This can create infinitely long lists.
   *
   * <p>Commonly, you may wish to start from some base value and iterate a function over it, or you may
   * wish to repeatedly call some external function, such as reading from a file, whose output you
   * want represented as a list. Or maybe you've got a Java Iterator. For all these cases, there are
   * static methods to help out.
   *
   * @see LazyList#generate(Supplier)
   * @see LazyList#ogenerate(Supplier)
   * @see LazyList#iterate(Object, UnaryOperator)
   * @see LazyList#fromIterator(Iterator)
   */
  static <T> IList<T> make(T headVal, Supplier<? extends IList<? extends T>> tailValFunc) {
    //
    // Engineering note: why are we using a static function rather than just making the LazyList
    // constructor public? Primarily because we want to avoid the "LazyList.Cons" type showing up anywhere
    // in user code. We're returning what seems like a perfectly normal IList, just like edu.rice.list.List
    // does, and we expect them to be used interchangeably. By hiding the constructor behind this
    // static method, we can hide the implementation details of a LazyList.
    //
    return new Cons<>(headVal, tailValFunc);
  }

  /**
   * Construct a lazy list with only one element.
   */
  static <T> IList<T> make(T headVal) {
    return make(headVal, LazyList::makeEmpty);
  }

  /**
   * Given a list of lists, lazily concatenate them together into a singular list. This static method is
   * distinct from {@link IList#concat(IList)}, in that it guarantees lazy concatenation, regardless
   * of whether the lists being concatenated are eager or lazy.
   */
  static <T> IList<T> lazyConcat(IList<? extends IList<? extends T>> listOfLists) {
    //
    // Engineering note: why is lazyConcat a static method within LazyList rather than part of the
    // interface in IList, perhaps with a default method in IList that's overridden here? Or perhaps
    // static methods in IList?
    //
    // Mostly because when you use this, you really want very specific semantics: lazy concatenation.
    // You're free to pass any sort of IList into these methods, but a "lazy" utility function clearly
    // belongs somewhere in LazyList. It's not like an eager list will have some other way of doing
    // lazy concatenation.
    //
    // Also, note that the very first thing we do is filter out any empty lists, then we do the
    // concatenation. Why? Imagine if there were thousands of empty-lists in a row. The filter
    // function is already tail-call optimized, so it won't have a stack-overflow error. Afterward,
    // we can be guaranteed to have a non-empty list in every slot, so we never have to do a
    // deep search and risk a stack overflow.
    //
    return Helpers.lazyConcatNoEmpties(listOfLists.filter(list -> !list.empty()));
  }

  /**
   * Given a prefix list and a supplier of a suffix list, lazily concatenate them together.
   */
  static <T> IList<T> lazyConcat(IList<? extends T> prefixList, Supplier<? extends IList<? extends T>> suffixListFunc) {
    if (prefixList.empty()) {
      return narrow(suffixListFunc.get());
    } else {
      return make(prefixList.head(), () -> lazyConcat(prefixList.tail(), suffixListFunc));
    }
  }


  /**
   * Given a traditional Java iterator, which might well be infinite, return a lazy list
   * that captures the output of the iterator.
   */
  static <T> IList<T> fromIterator(Iterator<? extends T> source) {
    if (source.hasNext()) {
      T nextVal = source.next();
      return make(nextVal, () -> fromIterator(source));
    } else {
      return makeEmpty();
    }
  }

  /**
   * Given a traditional Java enumeration (very similar to an iterator), which might well be infinite,
   * return a lazy list that captures the output of the iterator.
   */
  static <T> IList<T> fromEnumeration(Enumeration<? extends T> source) {
    if (source.hasMoreElements()) {
      T nextVal = source.nextElement();
      return make(nextVal, () -> fromEnumeration(source));
    } else {
      return makeEmpty();
    }
  }

  /**
   * Given a traditional Java array, return a lazy list in constant time; note that values are read
   * from the array only once per entry, and only on demand. If you're mutating the array, expect
   * unpredictable behavior from the lazy list. If you want an eager copy, then use {@link List#fromArray(Object[])}
   * instead.
   *
   * <p>Note that nulls are not allowed in lists, and any null found in the given array will cause a
   * {@link NullPointerException} to be thrown.
   */
  static <T> IList<T> fromArray(@Nullable T[] source) {
    if (source == null) {
      return makeEmpty();
    }
    return Helpers.fromArray(source, 0);
  }

  /**
   * Varargs constructor, runs in constant time.
   */
  @SuppressWarnings("varargs")
  @SafeVarargs
  static <T> IList<T> of(@Nullable T... source) {
    return fromArray(source);
  }

  /**
   * Returns a list of integers from min to max, inclusive.
   */
  static IList<Integer> rangeInt(int min, int max) {
    if (min > max) {
      return makeEmpty();
    } else {
      return rangeInt(min, max, 1);
    }
  }

  /**
   * Returns a list of integers from min to max, inclusive, skipping every increment until the
   * result would be outside the range. Note: won't work if increment is negative. You may prefer
   * one of the iterate() methods for a more general-purpose way of generating such things. (e.g.,
   * rangeInt(0, 10, 2) -&gt; [0, 2, 4, 6, 8, 10])
   */
  static IList<Integer> rangeInt(int start, int finish, int increment) {
    if (start < finish) {
      return Helpers.rangeInt(start, start, finish, increment);
    } else {
      return Helpers.rangeInt(start, finish, start, increment);
    }
  }

  /**
   * Returns an infinitely long list, starting with the "start" value, and thereafter the result of
   * repeatedly applying the iterator function to the starting value.
   */
  static <T> IList<T> iterate(T start, UnaryOperator<T> iterator) {
    return make(start, () -> iterate(iterator.apply(start), iterator));
  }

  /**
   * Returns an infinitely long list by repeatedly calling the supplier. The supplier is presumed to
   * keep internal state and return a different value each time.
   *
   * <p>There are two conditions that will cause this method to infer that there are no more values to
   * be had from the supplier: if it throws an exception or if it returns null. At that point, the
   * resulting list will be empty and the supplier will no longer be called.
   */
  static <T> IList<T> generate(Supplier<? extends T> supplier) {
    try {
      T result = supplier.get();
      if (result == null) {
        return makeEmpty();
      } else {
        return make(result, () -> generate(supplier));
      }

    } catch (Throwable throwable) {
      return makeEmpty();
    }
  }

  /**
   * Returns an infinitely long list by repeatedly calling the supplier. The supplier is presumed to
   * keep internal state and return a different value each time.
   *
   * <p>Unlike generate(), here the supplier returns an Option value. So long as the supplier returns
   * something other than Option.None, then the lazy list will continue growing. However,
   * once the supplier returns an Option.None, then the lazy list will end.
   *
   * <p>Also unlike generate(), ogenerate will not silently treat null results or exceptions as an excuse
   * to end the lazy list. Those are treated as errors.
   */
  static <T> IList<T> ogenerate(Supplier<Option<? extends T>> supplier) {
    return supplier.get().match(
        LazyList::makeEmpty,
        result -> make(result, () -> ogenerate(supplier)));
  }

  /**
   * Functions that aren't meant for public use. We could mark them private, but we want them visible within
   * the edu.rice.list package so they're amenable to unit testing.
   */
  class Helpers {
    private Helpers() { } // do not instantiate!

    static <T> IList<T> lazyConcatNoEmpties(IList<? extends IList<? extends T>> listOfLists) {
      if (listOfLists.empty()) {
        return makeEmpty();
      } else {
        // Engineering note: imagine if listOfLists didn't guarantee the "no empties" property.
        // Then, we'd have to do some kind of filtering in here, including a bunch of extra
        // effort to avoid deep recursion if we had thousands of "empties" in a row. But, since
        // this is an internal helper function, we can make the extra constraint and we don't
        // have to worry about this blowing up. Dan initially got this wrong and spent a whole
        // day debugging it.
        return lazyConcat(listOfLists.head(), () -> lazyConcatNoEmpties(listOfLists.tail()));
      }
    }

    /**
     * Given a traditional Java array, return a lazy list in constant time starting at the given offset;
     * note that values are read from the array only once per entry, and only on demand. If you're mutating
     * the array, expect unpredictable behavior from the lazy list. If you want an eager copy, then use
     * {@link List#fromArray(Object[], int)} instead.
     *
     * <p>Note that nulls are not allowed in lists, and any null found in the given array will cause a
     * {@link NullPointerException} to be thrown.
     */
    static <T> IList<T> fromArray(T[] source, int offset) {
      if (offset >= source.length) {
        return makeEmpty();
      }

      if (source[offset] == null) {
        throw new NullPointerException("nulls are not allowed in lists");
      }

      return make(source[offset], () -> fromArray(source, offset + 1));
    }

    /**
     * Returns a list of integers, starting at "current" and incrementing by "increment" until outside of the
     * range [min,max] (inclusive).
     */
    static IList<Integer> rangeInt(int current, int min, int max, int increment) {
      // note: we're not going to blow up if increment is zero; we'll just return an infinite list

      if (min > max) {
        throw new IllegalArgumentException("min must be <= max");
      }

      if (current < min || current > max) {
        return makeEmpty();
      }
      return make(current, () -> rangeInt(current + increment, min, max, increment));
    }
  }

  /**
   * This class represents the non-empty case for a LazyList. External users will never use this.
   * See instead the static methods as part of LazyList.
   * @see LazyList#make(Object, Supplier)
   */
  class Cons<T> implements LazyList<T> {
    @SuppressWarnings("unused")
    private static final String TAG = "LazyList";

    private final T headVal;
    private final Supplier<? extends IList<? extends T>> tailVal;

    private Cons(T headVal, Supplier<? extends IList<? extends T>> tailValFunc) {
      this.headVal = headVal;
      this.tailVal = Memo.make(tailValFunc);
    }

    @Override
    public <Q> IList<Q> makeEmptySameType() {
      return makeEmpty();
    }

    @Override
    public boolean empty() {
      return false;
    }

    @Override
    public T head() {
      return headVal;
    }

    @Override
    public IList<T> tail() {
      return narrow(tailVal.get());
    }

    @Override
    public IList<T> add(T t) {
      return make(t, () -> this);
    }

    @Override
    public int hashCode() {
      return List.Helpers.hashHelper(this);
    }

    @Override
    public <Q> IList<Q> map(Function<? super T, ? extends Q> f) {
      return make(f.apply(headVal), () -> tail().map(f));
    }

    @Override
    public <Q> IList<Q> flatmap(Function<? super T, ? extends IList<? extends Q>> f) {
      return lazyConcat(map(f));
    }

    @Override
    public IList<T> filter(Predicate<? super T> predicate) {
      // the usual implementation of this would recursively call tail().filter(predicate)
      // if the predicate fails (see below), but that can lead to a Java stack overflow
      // if you have a large number of false values reported by the predicate.
      // Consequently, we have to do the tail optimization by hand.

      // standard recursive version:
//      if (predicate.test(headVal)) {
//        return make(headVal, () -> tail().filter(predicate));
//      } else {
//        return tail().filter(predicate);
//      }

      // After tail-call optimization:

      IList<T> list = this; // this variable mutates; it points to each element of the list in succession

      for (;;) {
        if (list.empty()) {
          return list;
        }

        final T headVal = list.head();
        final IList<T> tailList = list.tail();

        if (predicate.test(headVal)) {
          return make(headVal, () -> tailList.filter(predicate));
        } else {
          list = tailList;
        }
      }
    }

    @Override
    public IList<T> takeWhile(Predicate<? super T> predicate) {
      if (predicate.test(headVal)) {
        return make(headVal, () -> tail().takeWhile(predicate));
      } else {
        return makeEmpty();
      }
    }

    @Override
    public IList<T> concat(IList<? extends T> afterList) {
      return make(headVal, () -> tail().concat(afterList));
    }

    @Override
    public IList<T> limit(int n) {
      if (n < 1) {
        return makeEmpty();
      } else if (n == 1) {
        // If we didn't do this, then operations like comparing list equality would end up
        // asking for tail().limit(0), which would correctly be the empty list, but would
        // also have the side effect of memoizing one step further down the list, which isn't
        // something we always want.
        return make(headVal);
      } else {
        return make(headVal, () -> tail().limit(n - 1));
      }
    }

    @Override
    public IList<T> updateNth(int n, Function<? super T, Option<? extends T>> updateFunc) {
      if (n < 0) {
        return this; // not really even sure what n<0 means, so doing nothing seems reasonable
      } else if (n == 0) {
        return updateFunc.apply(headVal).match(this::tail, newVal -> make(newVal, this::tail));
      } else {
        return make(headVal, () -> tail().updateNth(n - 1, updateFunc));
      }
    }

    @Override
    public String toString() {
      return List.Helpers.toStringHelper(this);
    }

    @Override
    public boolean equals(Object x) {
      if (!(x instanceof IList)) {
        return false;
      }

      IList<?> otherList = (IList<?>) x;
      return List.Helpers.equalsHelper(this, otherList);
    }

    @Override
    public <U, V> IList<V> zip(IList<? extends U> list, BiFunction<? super T, ? super U, ? extends V> zipFunc) {
      if (list.empty()) {
        return makeEmpty();
      } else {
        return make(zipFunc.apply(headVal, list.head()), () -> tail().zip(list.tail(), zipFunc));
      }
    }
  }

  /**
   * Note that this class is not public. Empty lists will be instances of this class, and we'll have
   * precisely one of them. Clients of the List class can use List.makeEmpty().
   * @see List#makeEmpty()
   */
  class Empty<T> implements LazyList<T>, IList.Empty<T> {
    private static final IList<?> SINGLETON = new LazyList.Empty<>();

    // don't call this; use makeEmpty()
    private Empty() { }

    @Override
    public IList<T> add(T t) {
      return make(t);
    }

    @Override
    public <Q> IList<Q> makeEmptySameType() {
      return makeEmpty();
    }

    @Override
    public boolean equals(Object x) {
      if (!(x instanceof IList)) {
        return false;
      }

      IList<?> list = (IList<?>) x;
      return list.empty(); // any empty list will be equal to this one
    }

    @Override
    public int hashCode() {
      return List.Helpers.hashHelper(this);
    }

    @Override
    public String toString() {
      return List.Helpers.toStringHelper(this);
    }
  }
}
