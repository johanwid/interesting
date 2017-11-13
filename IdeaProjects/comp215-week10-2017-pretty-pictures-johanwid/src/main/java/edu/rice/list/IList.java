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

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import edu.rice.lens.Lens;
import edu.rice.tree.BinaryHeap;
import edu.rice.tree.IPriorityQueue;
import edu.rice.util.Log;
import edu.rice.util.Option;
import edu.rice.util.TriConsumer;
import edu.rice.util.TriFunction;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.NoSuchElementException;
import java.util.function.*;

import static edu.rice.lens.Lens.lens;

/**
 * All of our lists, whether lazy or eager, will implement this interface. A bunch of "default"
 * methods are provided which are quite useful and can, of course, be overriden by the concrete list
 * class if it has a more efficient strategy.
 *
 * <p>Unlike Java's lists, this list interface is engineered around being functional. No mutation! That
 * means there are no setter methods. Every list operation returns a new list, and the old list
 * doesn't change.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public interface IList<T> {
  /**
   * Similar to {@link #head()}, but returns an {@link Option} variant; if there's something in the list, then you
   * get the {@link Option#some(Object)} of the value. If the list is empty, you get {@link Option#none()}.
   */
  default Option<T> ohead() {
    return match(
        emptyList -> Option.none(),
        (head, tail) -> Option.some(head));
  }

  /**
   * Return the first element of the list.
   * Use {@link #match(Function, BiFunction)} or {@link #ohead()} if
   * you don't know whether your list is empty and you want to avoid runtime exceptions.
   *
   * @throws NoSuchElementException if you try to take the head() of an empty list
   */
  T head();

  /**
   * Return a new list with this element in front of the current list.
   */
  IList<T> add(T t);
  // Engineering note: we've got multiple IList implementations, so we can't offer a default add method.
  // See inside List.java and LazyList.java for how we still manage to avoid repeating ourselves.

  /**
   * Return the tail of the list (i.e., a list with everything but the first element).
   * Use {@link #match(Function, BiFunction)} or {@link #ohead()} if you don't know
   * whether your list is empty or not and you want to handle both cases at once.
   *
   * @throws NoSuchElementException if you call tail() on an empty list
   */
  IList<T> tail();

  /**
   * Return whether the list is empty or not.
   */
  boolean empty();

  /**
   * Return the length of the list.
   */
  default int length() {
    // Foldl here ignores the elements in the list, and just uses the folding function to increment the counter each time.
    return foldl(0, (count, elem) -> count + 1);
  }

  /**
   * Returns whether the requested element exists in the list.
   */
  default boolean contains(T value) {
    // this default implementation will be efficient for lazy lists, and will end up doing extra work on eager lists
    // but we don't really care (for now)
    return !filter(x -> x.equals(value)).empty();
  }


  /**
   * Returns the same list, but backwards.
   */
  default IList<T> reverse() {
    // Foldl prepends elements, one by one from the original list onto a results list, which starts from empty.
    return foldl(makeEmptySameType(), IList::add);
  }

  /**
   * Sometimes you have an IList, maybe it's eager or maybe it's lazy, but you want to get a new
   * empty list of the <b>same</b> concrete type (i.e., LazyList vs. List). This method is a nice shorthand that does it for you.
   * Alternatively, you may of course use the static methods {@link List#makeEmpty()} or {@link LazyList#makeEmpty()}.
   *
   * <p>For added flexibility, you can use a different type parameter than the type parameter of the
   * original list.
   */
  <Q> IList<Q> makeEmptySameType();

  /**
   * Return a list of elements matching the predicate.
   */
  IList<T> filter(Predicate<? super T> predicate);

  /**
   * Returns a list of all elements matching the predicate, while the predicate is true.
   * Once the predicate is false, no subsequent elements are returned.
   */
  IList<T> takeWhile(Predicate<? super T> predicate);

  /**
   * General-purpose structural pattern matching on a list with deconstruction as well.
   *
   * @param emptyFunc
   *     called if the list is empty
   * @param nonEmptyFunc
   *     called if the list has at least one value within; the head of the list is the first argument,
   *     then a list with the remainder
   * @param <Q>
   *     the return type of either emptyFunc or nonEmptyFunc
   * @return returns the value of invoking whichever function matches
   */
  default <Q> Q match(Function<? super IList<T>, ? extends Q> emptyFunc,
                      BiFunction<? super T, ? super IList<T>, ? extends Q> nonEmptyFunc) {
    if (empty()) {
      return emptyFunc.apply(this);
    } else {
      return nonEmptyFunc.apply(head(), tail());
    }
  }

  /**
   * General-purpose structural pattern matching on a list with deconstruction as well.
   *
   * @param emptyFunc
   *     called if the list is empty
   * @param oneElemFunc
   *     called if the list has exactly one value within; the head of the list is the first argument,
   *     then a list with the remainder
   * @param twoOrMoreFunc
   *     called if the list has two or more values within; the head of the list is the first argument,
   *     then the 2nd element, then a list with the remainder
   * @param <Q>
   *     the return type of either emptyFunc or nonEmptyFunc
   * @return returns the value of invoking whichever function matches
   */
  default <Q> Q match(Function<IList<T>, ? extends Q> emptyFunc,
                      BiFunction<? super T, ? super IList<T>, ? extends Q> oneElemFunc,
                      TriFunction<? super T, ? super T, ? super IList<T>, ? extends Q> twoOrMoreFunc) {

    if (empty()) {
      return emptyFunc.apply(this);
    } else if (tail().empty()) {
      return oneElemFunc.apply(head(), tail());
    } else {
      return twoOrMoreFunc.apply(head(), tail().head(), tail().tail());
    }
  }

  /**
   * General-purpose structural pattern matching on a list with deconstruction as well. This
   * returns nothing and expects lambdas that return nothing. Analogous to
   * {@link #match(Function, BiFunction, TriFunction)} but with no return value.
   *
   * @param emptyFunc
   *     called if the list is empty
   * @param nonEmptyFunc
   *     called if the list has at least one value within; the head of the list is the first argument,
   *     then a list with the remainder
   */
  default void consume(Consumer<? super IList<T>> emptyFunc,
                       BiConsumer<? super T, ? super IList<T>> nonEmptyFunc) {
    if (empty()) {
      emptyFunc.accept(this);
    } else {
      nonEmptyFunc.accept(head(), tail());
    }
  }

  /**
   * General-purpose structural pattern matching on a list with deconstruction as well. This
   * returns nothing and expects lambdas that return nothing. Analogous to
   * {@link #match(Function, BiFunction)} but with no return value.
   *
   * @param emptyFunc
   *     called if the list is empty
   * @param oneElemFunc
   *     called if the list has exactly one value within; the head of the list is the first argument,
   *     then a list with the remainder
   * @param twoOrMoreFunc
   *     called if the list has two or more values within; the head of the list is the first argument,
   *     then the 2nd element, then a list with the remainder
   */
  default void consume(Consumer<? super IList<T>> emptyFunc,
                       BiConsumer<? super T, ? super IList<T>> oneElemFunc,
                       TriConsumer<? super T, ? super T, ? super IList<T>> twoOrMoreFunc) {

    if (empty()) {
      emptyFunc.accept(this);
    } else if (tail().empty()) {
      oneElemFunc.accept(head(), tail());
    } else {
      twoOrMoreFunc.accept(head(), tail().head(), tail().tail());
    }
  }

  /**
   * Return a list with the given function applied to each of the elements of the current list.
   */
  <Q> IList<Q> map(Function<? super T, ? extends Q> f);

  /**
   * Given a function that returns a <i>list</i> of things, flatmap applies that function
   * to each element in the given list and concatenates the resulting lists. Zero-length
   * lists are handled correctly.
   */
  <Q> IList<Q> flatmap(Function<? super T, ? extends IList<? extends Q>> f);

  /**
   * Given a function that returns an optional result, applied to a list of values, oflatmap unpacks the
   * options and returns a list of the {@link Option#some(Object)} values.
   *
   * <p>You can think of an Option as if it was a list that had zero or one things in it, so when we
   * have a lambda that returns an Option value, that's akin to having a lambda that returns a
   * list, which might be empty, and we want to concatenate those lists as with {@link #flatmap(Function)}.
   *
   * <p>(In some functional programming languages, Options are literally lists with zero or one
   * element inside, so flatmap works without modification. In our edu.rice library, Option is a
   * distinct type, so we have oflatmap() as a variant on flatmap() to deal with it.)
   */
  default <Q> IList<Q> oflatmap(Function<? super T, Option<? extends Q>> f) {
    // Detailed engineering notes on this appear in ListTest.testOFlatMap.

    // Fun stuff: here are two different ways of expressing exactly the same function.
    // Can you convince yourself that they're identical?
    return flatmap(f.andThen(Option::toList));
//    return map(f).filter(Option::isSome).map(Option::get);
  }

  /**
   * Return a list with afterTail concatenate after it, (e.g., if a = [1,2,3] and b=[4,5,6],
   * a.concat(b) -&gt; [1,2,3,4,5,6]). If you need lazy concatenation, there are static methods
   * in LazyList to help you.
   * @see LazyList#lazyConcat(IList)
   * @see LazyList.Helpers#lazyConcat(IList, Supplier)
   */
  IList<T> concat(IList<? extends T> afterTail);

  /**
   * Return at most the first n elements of the list. For shorter lists, you'll get back the whole list.
   */
  IList<T> limit(int n);

  /**
   * Return {@link Option#some(Object)} the nth element in the list, if present, otherwise {@link Option#none()}.
   * The head of the list would correspond to n=0.
   *
   * <p><b>Warning: this function runs in <i>O(n)</i> time</b>. If you try to
   * implement a classic for-loop over the list using this, it will run in <i>O(n^2)</i> time. Don't do that!
   * That's what other list methods like {@link #map(Function)}, {@link #filter(Predicate)}, or even
   * {@link #foreach(Consumer)} are all about.
   */
  default Option<T> nth(int n) {
    // We could write this recursively, and it would be much cleaner, but also it would be slower and would
    // run out of memory. We could alternatively do something complicated with map and find, but this function
    // is something that's going to be called often, so we care about keeping it fast.

    if (n < 0) {
      return Option.none();
    }

    IList<T> list = this;
    for (;;) {
      if (list.empty()) {
        return Option.none();
      }
      if (n == 0) {
        return Option.some(list.head());
      }
      n--;
      list = list.tail();
    }
  }

  /**
   * Skips N elements of the list, returning whatever remains of the list after that.
   * If the list has fewer than N elements, an empty list will be returned.
   */
  default IList<T> skipN(int n) {
    // written here in a mutating style because otherwise we
    // could have a stack overflow, because Java can't do tail-call optimization...

    IList<T> currentList = this;

    for (int i = 0; i < n; i++) {
      currentList = currentList.tail();
      if (currentList.empty()) {
        return currentList;
      }
    }

    return currentList;
  }

  /**
   * Returns a new list equivalent to the old list, but with the nth item replaced by the (option)
   * value. If the option is none, then we're replacing that list element with nothing --
   * deleting it. Of course, the original list is never mutated.
   */
  default IList<T> updateNth(int n, Option<? extends T> newVal) {
    return updateNth(n, val -> newVal);  // lambda ignores the old value
  }

  /**
   * Returns a new list equivalent to the old list, but with the nth item replaced by the given function
   * applied to the old value. If the option is none, then we're replacing that list element with
   * nothing -- deleting it. Of course, the original list is never mutated.
   */
  IList<T> updateNth(int n, Function<? super T, Option<? extends T>> updateF);

  /**
   * Returns a {@link Lens} onto the nth item of any given list. Note that this lens returns Option&lt;T&gt;
   * rather than T, meaning that if try to read the nth item in the list and it's absent, you get
   * back {@link Option#none()}. Similarly, if you use the setter to set the nth item to {@link Option#none()}, then
   * that element is removed from the resulting list. Of course, the "setter" operation is functional,
   * so the original list is unmodified.
   * @see #nth(int)
   * @see #updateNth(int, Option)
   */
  static <T> Lens<IList<T>, Option<T>> lensNth(int n) {
    return lens(
        list -> list.nth(n),
        (list, oVal) -> list.updateNth(n, oVal));
  }

  /**
   * Return a subsequence of the list, starting at first and ending at last, inclusive, where first
   * and last correspond to the same elements returned by {@link #nth(int)}. If the requested range goes beyond the
   * end of the list, as many values as are in range will be returned. If the entire range is outside
   * of the list, then an empty list will be returned.
   */
  default IList<T> sublist(int first, int last) {
    if (first > last) {
      return makeEmptySameType();
    }
    if (first < 0) {
      first = 0;
    }
    if (last < 0) {
      return makeEmptySameType();
    }

    return skipN(first).limit(last - first + 1);
  }

  /**
   * Returns a new list equal to the old list in sorted order (lowest to highest) based on the
   * <i>lessThanFunction</i> that's passed. For a list of integers, this might be
   * <pre>
   * <code>(a,b) -&gt; a&lt;b</code>
   * </pre>
   * while for a general-purpose comparable it would be
   * <pre>
   * <code>(a,b) -&gt; a.compareTo(b)&lt;0</code>.
   * </pre>
   * Or, for a list of comparables, you could just use {@link IList#sort()} to get it sorted
   * in "natural" order.
   */
  default IList<T> sort(BiPredicate<? super T, ? super T> lessThanFunction) {
    // Engineering note: we're creating a new BinaryHeap, inserting copies of each thing
    // from the current list into the heap, and then "draining" the heap out to another
    // list. That means we're using mutation, on the inside, but from the outside nobody
    // can tell the difference.
    IPriorityQueue<T> heap = BinaryHeap.make(lessThanFunction);

    foreach(heap::insert);

    // Engineering note: by using lazy lists, the heap structure doesn't become garbage until
    // the entire contents of the list have been read. We're still paying the O(n log n) cost
    // to insert everything into the heap, but we're delaying the cost of extracting things
    // from the heap until needed. If, for example, the caller only reads a few entries from
    // the sorted result, then only those entries will be extracted from the heap.
    return heap.drainToLazyList();
  }

  /**
   * Returns a new list equal to the old list in "natural" sorted order (lowest to highest) for types
   * that are {@link Comparable}. Calling this on a list of non-comparable type will result in an runtime error.
   * Use {@link #sort(BiPredicate)} for a general-purpose sorting method on all types or for something other
   * than "natural" ordering.
   * @throws IllegalArgumentException if the list isn't of a comparable type
   */
  default <ComparableT extends Comparable<? super ComparableT>> IList<T> sort() {
    // Engineering note: We need to introduce a new type parameter for when we discover that the
    // elements of our list are comparable. The definition of ComparableT happens above as part of the method
    // definition, yet ComparableT is only used internally. For better or for worse, Java only lets you introduce
    // type parameters as part of a method definition, so this looks a bit unusual.

    if (empty()) {
      return this;
    } else if (head() instanceof Comparable<?>) {
      // Normally, Java would warn us about changing from the original type IList<T> to the new
      // type IList<ComparableT>, as well it should, but the instanceof test above, which happens
      // at runtime, will make sure that these typecasts below won't fail, so we can safely suppress
      // Java's warnings for "unchecked" casts. (Because we did check.) If the head() is comparable, we can
      // then safely assume that the rest of the list is also comparable since the whole list is of
      // the same type T. There are potential cases where that assumption might fail
      // (think: a heterogeneous list of Object, where the first object just happens to be comparable
      // but subsequent elements aren't comparable) which will then result in a runtime error when
      // sorting the list and trying to compare something that isn't actually comparable.

      // Wouldn't it be cool if we could avoid having these runtime errors at all, and make it so
      // IList.sort() is only defined if the type parameter is Comparable? In Java, we could define
      // a static method (rather than an instance method like this one) which makes a suitable
      // constraint on the type of the list. That might look something like so:

      // static <T extends Comparable<T>> IList<T> sort(IList<T> list) { ... }

      // The Java compiler will refuse to let you call this static method with any list that it
      // cannot prove, statically, to be a list of comparables, thus guaranteeing that there will
      // be no runtime failures. The downside is that you can't write list.sort() but instead
      // have to write IList.sort(list) or something along those lines which just looks uglier.

      // One of the fun things about Kotlin, a language that's closely related to Java, is that you
      // can create "extension methods" which feel like a method on an object, yet with all the type
      // constraints and whatnot that we might want to do in a static method. Think of an "extension method"
      // as something like the above static method, but with syntactic sugar that lets it feel like
      // you're calling any other instance method on the object.

      @SuppressWarnings("unchecked")
      IList<ComparableT> tList = (IList<ComparableT>) this;
      IList<ComparableT> sortedList = tList.sort((a, b) -> a.compareTo(b) < 0);

      @SuppressWarnings("unchecked")
      IList<T> resultList = (IList<T>) sortedList;

      return resultList;
    } else {
      String errorMsg = "tried to call sort() on a list of non-Comparable type; actual list type might be IList<" +
          head().getClass().getName() + ">";
      Log.e("IList.sort", errorMsg);
      throw new IllegalArgumentException(errorMsg);
    }
  }

  /**
   * If we have a list and a comparison function on elements of the list, then this will say whether or not
   * they're sorted according to the lessThanFunc that's passed as a parameter.
   *
   * <p>If your list is defined over a comparable type, and you want to test for being sorted in "natural order",
   * then you may prefer {@link #isSorted()}
   */
  default boolean isSorted(BiPredicate<? super T,? super T> lessThanFunc) {
    // Engineering note: we could have done this with match() and recursion, but we don't want to run out of memory,
    // during these computations. Similarly, we could have done it with foldl, but we want to terminate
    // early if the list isn't sorted. So, we're stuck with this hand-written loop instead.

    if (empty()) {
      return true;
    }
    if (tail().empty()) {
      return true;
    }

    T prev = head();
    IList<T> list = tail();
    while (!list.empty()) {
      if (!lessThanFunc.test(prev, list.head())) {
        return false;
      }
      prev = list.head();
      list = list.tail();
    }
    return true;
  }

  /**
   * If we have a list of comparables, then this will say whether or not they're sorted in natural order.
   * Calling this on lists of other types will result in an IllegalArgumentException. For a general-purpose
   * test, see {@link #isSorted(BiPredicate)}.
   */
  default <ComparableT extends Comparable<? super ComparableT>> boolean isSorted() {
    // Engineering note: We need to introduce a new type parameter for when we discover that the
    // elements of our list are comparable. The definition of ComparableT happens above as part of the method
    // definition, above, and it's only used internally. For better or for worse, Java only lets you introduce
    // type parameters as part of a method definition, so this looks a bit unusual.

    if (empty()) {
      return true;
    } else if (head() instanceof Comparable<?>) {
      // Normally, Java would warn us about changing from the original type IList<T> to the new
      // type IList<ComparableT>, as well it should, but the instanceof test above, which happens
      // at runtime, will make sure that these typecasts below won't fail, so we can safely disable
      // Java's warnings with the "noinspection" directives.

      @SuppressWarnings("unchecked")
      IList<ComparableT> tList = (IList<ComparableT>) this;
      return tList.isSorted((a, b) -> a.compareTo(b) <= 0);
    } else {
      String errorMsg = "tried to call isSorted() on a list of non-Comparable type; actual list type is IList<" +
          head().getClass().getName() + ">";
      Log.e("IList.isSorted", errorMsg);
      throw new IllegalArgumentException(errorMsg);
    }
  }


  /**
   * Foreach operator: runs the consumer once on each item, start to finish; returns the
   * original list again, so you can chain other operations. Feel free to ignore the result
   * if you don't need it.
   */
  @CanIgnoreReturnValue
  default IList<T> foreach(Consumer<? super T> consumer) {
    // Engineering notes: two things going on here. First, as with foldl, there's a serious boost
    // to be had by converting this to a loop. Second, we're overriding the @CheckReturnValue that
    // otherwise applies to every method in this class because it's okay for the caller to ignore
    // the return value, which is really nothing more than the same input again. You call this
    // method specifically because you're looking for it to have some side-effects, like say
    // printing the contents of a list.

    IList<T> current = this;
    for (;;) {
      if (current.empty()) {
        return this;
      }
      consumer.accept(current.head());
      current = current.tail();
    }
  }

  /**
   * Given another list of the same length as this list, "zip" it together with the current list,
   * using the zipFunc to combine the elements of the two lists together, and yielding a list of the
   * results. Whichever list is shorter will determine the ultimate length of the zipped result. Any
   * extra entries in one list without a corresponding entry in the other will be ignored.
   */
  <U, V> IList<V> zip(IList<? extends U> list, BiFunction<? super T, ? super U, ? extends V> zipFunc);

  /**
   * Fold left: folds from head to tail. Example: the list is {a,b,c,d,e} and the folding function is
   * plus, then this returns (((((zero + a) + b) + c) + d) + e). If either foldl or foldr would give you the
   * same answer (e.g., if the folding function f is associative), then foldl is preferable. The
   * runtime speed is about the same, but foldr will have a stack-overflow on very large lists and foldl
   * won't.
   *
   * <p>Note: when the result type is different from the list element type (e.g., if you're folding a
   * list of strings into a tree of strings) then the zero should be of the result type,
   * and the corresponding function f will take two arguments: the result type (first argument) and
   * the list element type (second argument).
   *
   * @param zero
   *     value when there's nothing else to fold (e.g., for strings, this might be the empty
   *     string)
   * @param f
   *     folding function (used for accumulating things, where the left-arg to
   *     the function is the previous sum and the right-arg are the elements of the list in order)
   */
  default <U> U foldl(U zero, BiFunction<? super U, ? super T, ? extends U> f) {
    // recursive version
//    if(empty()) return zero;
//    return tail().foldl(f.apply(zero, head()), f);

    // iterative version
    IList<T> current = this;
    for (;;) {
      if (current.empty()) {
        return zero;
      }
      zero = f.apply(zero, current.head());
      current = current.tail();
    }
  }


  /**
   * Fold right: combines the head with the folded accumulation of the tail. Example: the list is
   * {a,b,c,d,e} and the folding function is plus, then this returns (a + (b + (c + (d + (e + zero))))).
   *
   * <p>Note: when the result type is different from the list element type (e.g., if you're folding a
   * list of strings into a tree of strings) then the zero should be of the result type,
   * and the corresponding function f will take two arguments: the list element type (first argument)
   * and the result type (second argument).
   *
   * @param zero
   *     value when there's nothing else to fold (e.g., for strings, this might be the empty
   *     string)
   * @param f
   *     folding function (used for accumulating things, where the right-arg to
   *     the function is the previous sum and the left-arg are the elements of the list in reverse order)
   */
  default <U> U foldr(U zero, BiFunction<? super T, ? super U, ? extends U> f) {
    if (empty()) {
      return zero;
    }
    return f.apply(head(), tail().foldr(zero, f));
  }

  /**
   * Converts each element of the list to a string, then concatenates them with the mergeStr between
   * each one.
   */
  default String join(String mergeStr) {
    //
    // original, uses lots of string concatenation
    //
//        return map(Object::toString)  // first convert everything to a string
//                .foldl((a, b) -> ((a == "") ? b : a + mergeStr + b), ""); // then concatenate

    //
    // better, uses StringBuilder
    //

    // the logic here is a bit messy: if we're an empty list, then we trivially return ""
    // otherwise, we'll treat the head specially then load in the tail, using the mergeStr
    // to glue it all together.

    if (empty()) {
      return "";
    }

    // if we get here, the list has a head, at the very least
    StringBuilder result = new StringBuilder();
    result.append(head().toString());
    tail().map(Object::toString)
        .foreach(str -> {
          result.append(mergeStr);
          result.append(str);
        });

    return result.toString();
  }

  /**
   * Sometimes, you want to make a lazylist "force" all of its elements to exist, rather than
   * waiting until they're lazily demanded. This matters for a number of algorithms defined over
   * lazy lists. For that, we have the force() method. Warning: calling force() on an infinitely-long
   * lazy list will never return.
   *
   * <p>For convenience, force() returns the same list as it was called on, making it easier
   * to use in method-calling pipelines.
   */
  @CanIgnoreReturnValue
  default IList<T> force() {
    // Engineering note: We have to be careful because we might have a list that has a mix of
    // LazyList cells and normal List cells. They all implement IList, so we have to make sure
    // we touch every cell of the list, no matter what, which unfortunately means that force()
    // on an eager list cannot be a trivial no-op.

    // Also, note that we have to override @CheckReturnValue, because sometimes you'll "force" a
    // value you already have, perhaps as part of a benchmark, so you're counting on the side-effect
    // rather than needing to capture the return value.

    foreach(x -> { }); // seems cheesy, but will do the job

    return this;
  }

  /**
   * Works like mapping a normal function on a list, but returns a list of key/value tuples, where
   * the values are the result of applying the mapping function f to the key. Note that this is a
   * <i>static method</i>, not an instance method, because it needs to restrict the list to be a list of
   * Comparable types. This restriction is necessary because IMap and TreapMap require their keys to
   * be comparable, and mapkv() is designed to play nicely with IMap and TreapMap.
   *
   * <p>If you want to convert the resulting list of key-value tuples into a map, you may prefer
   * the helper function built into TreapMap.
   *
   * @see edu.rice.tree.TreapMap#fromList(IList, Function)
   */
  static <K extends Comparable<? super K>, V> IList<KeyValue<K, V>>
      mapkv(IList<? extends K> list, Function<? super K, ? extends V> f) {

    // Engineering note: You might look at how IList.sort() or IList.isSorted() do runtime
    // checks on the list elements to see if they're Comparable, and then wonder why we don't
    // do exactly the same thing here. Well, the difference is that KeyValue<K, V> requires K
    // to be Comparable, and it's part of the return type of the mapkv function. That constraint
    // makes all the difference. It's also why this function has to be static.

    return list.map(key -> KeyValue.make(key, f.apply(key)));
  }

  /**
   * Useful when converting from wildcard types to concrete types (e.g., from IList&lt;? extends
   * T&gt; to IList&lt;T&gt;). Also, note that this is a static method, not an instance method.
   *
   * <p>This is only allowable because our lists are immutable. If you tried to play this sort
   * of game with {@link java.util.List} and other such classes, you could violate static soundness and end
   * up with a runtime type error. (No mutation!)
   */
  static <T> IList<T> narrow(IList<? extends T> input) {
    @SuppressWarnings("unchecked")
    IList<T> result = (IList<T>) input;

    // Cool trick: when the following line is type-safe, then you know it's safe to do the
    // above unchecked typecast, which runs in constant time versus the below line which is O(n).
    // Why does the line below type check? Liskov Substitution Principle! Then why can't we
    // just have this happen automatically? Why do we need narrow() at all? Mutation!

//    IList<T> result2 = input.map(x->x);

    return result;
  }


  /**
   * Empty lists, whether eager or lazy, have a lot of behaviors in common. Since we're big believers in
   * not repeating ourselves, both eager and empty list implementations will implement the IList.Empty
   * interface, and thus pick up all these default methods which override many IList methods above.
   *
   * @see IList
   */
  interface Empty<T> extends IList<T> {
    @Override
    default boolean empty() {
      return true;
    }

    @Override
    default T head() {
      Log.e("IListEmpty", "can't take head() of an empty list");
      throw new NoSuchElementException("can't take head() of an empty list");
    }

    @Override
    default IList<T> tail() {
      Log.e("IListEmpty", "can't take tail() of an empty list");
      throw new NoSuchElementException("can't take tail() of an empty list");
    }

    @Override
    default IList<T> reverse() {
      return this;
    }

    @Override
    default int length() {
      return 0;
    }

    @Override
    default <Q> IList<Q> map(Function<? super T, ? extends Q> f) {
      return makeEmptySameType();
    }

    @Override
    default <Q> IList<Q> flatmap(Function<? super T, ? extends IList<? extends Q>> f) {
      return makeEmptySameType();
    }

    @Override
    default IList<T> concat(IList<? extends T> afterTail) {
      return narrow(afterTail);
    }

    @Override
    default IList<T> filter(Predicate<? super T> predicate) {
      return this;
    }

    @Override
    default IList<T> takeWhile(Predicate<? super T> predicate) {
      return this;
    }

    @Override
    default IList<T> limit(int n) {
      return this;
    }

    @Override
    default Option<T> nth(int n) {
      return Option.none();
    }

    @Override
    default IList<T> skipN(int n) {
      return makeEmptySameType();
    }

    @Override
    default IList<T> updateNth(int n, Function<? super T, Option<? extends T>> updateFunc) {
      return this;
    }

    @Override
    default IList<T> sublist(int start, int end) {
      return this;
    }

    @Override
    default <U, V> IList<V> zip(IList<? extends U> list, BiFunction<? super T, ? super U, ? extends V> zipFunc) {
      return makeEmptySameType();
    }
  }
}
