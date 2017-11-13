/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.util;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import edu.rice.list.IList;
import edu.rice.list.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * This interface handles the Comp215 variant of {@link Optional}. Our version is more flexible than Oracle's.
 * For an extended rant on this problem, see 
 * <a href="https://developer.atlassian.com/blog/2015/08/optional-broken/">Atlassian's discussion of Optional</a>.
 * This code we're using here is heavily influenced by 
 * <a href="https://github.com/vavr-io/vavr/blob/master/vavr/src/main/java/io/vavr/control/Option.java">Vavr's Option class</a>.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public interface Option<T> {
  //
  // Data definition:
  //
  // There are two kinds of options: Some and None, which have their corresponding classes below.
  // Some classes wrap a value, which can be fetched later. None has no corresponding value.
  //

  /**
   * Creates an option holding the value within.
   */
  static <T> Option<T> some(T value) {
    return new Some<>(value);
  }

  /**
   * If the input value is non-null, this returns {@link #some(Object)}, otherwise
   * if the input is null, then {@link #none()} is returned.
   */
  static <T> Option<T> ofNullable(@Nullable T value) {
    return (value == null)
        ? none()
        : some(value);
  }

  /**
   * Creates an empty option.
   */
  static <T> Option<T> none() {
    @SuppressWarnings("unchecked")
    Option<T> typedNone = (Option<T>) None.SINGLETON;
    return typedNone;
  }

  /**
   * Creates an empty optional, ignores its argument. (Useful in places where you want to mention
   * <code>Option::none</code> as a function taking one input and ignoring it. Also allows type inference to
   * flow from the input type to the output type, so fewer places where you need to decorate your
   * code with explicit type parameters.)
   */
  @SuppressWarnings("unused")
  static <T> Option<T> none(T ignored) {
    return none();
  }

  /**
   * Converts an {@link Optional} from java.util to our own Option.
   */
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  static <T> Option<T> fromOptional(Optional<T> optional) {
    return optional.map(Option::some).orElse(none());
  }

  /**
   * Converts an Option to an {@link IList} of zero or one elements.
   */
  default IList<T> toList() {
    return match(List::makeEmpty, List::of);
  }

  /**
   * Returns whether there's something present here (i.e., it's an Option.Some) or whether there's
   * nothing here (i.e., it's an Option.None).
   */
  boolean isSome();

  /**
   * The opposite of {@link #isSome()}: returns true if there's Option.None.
   */
  default boolean isNone() {
    return !isSome();
  }

  /**
   * For an Option.Some, this returns the internal value. For an Option.None, this throws an exception.
   */
  T get();

  /**
   * Takes two lambdas: one to call if it's an Option.None, and the the other to call if
   * it's an Option.Some. The latter is given the internal value as a parameter.
   */
  default <Q> Q match(Supplier<? extends Q> noneFunc, Function<? super T, ? extends Q> someFunc) {
    if (isSome()) {
      return someFunc.apply(get());
    } else {
      return noneFunc.get();
    }
  }

  /**
   * Returns the internal value, if it's present, otherwise it returns the alternative.
   */
  default T getOrElse(T alternative) {
    return match(() -> alternative, val -> val);
  }

  /**
   * Returns the internal value, if it's present, otherwise it calls alternativeFunc and returns its result.
   */
  default T getOrElse(Supplier<? extends T> alternativeFunc) {
    return match(alternativeFunc::get, val -> val);
  }

  /**
   * This is a variant on {@link #getOrElse(Object)}. If we're starting with an Option.Some, then this
   * returns the original Option value. If we're starting with an Option.None, then the
   * <code>alternative</code> is returned. Very useful for chaining together options.
   */
  default Option<T> orElse(Option<? extends T> alternative) {
    if (isSome()) {
      return this;
    } else {
      return narrow(alternative);
    }
  }

  /**
   * This is a variant on {@link #getOrElse(Object)}. If we're starting with an Option.Some, then this
   * returns the original Option value. If we're starting with an Option.None, then the
   * <code>alternativeFunc</code> is invoked and its result is returned.
   */
  default Option<T> orElse(Supplier<? extends Option<? extends T>> alternativeFunc) {
    if (isSome()) {
      return this;
    } else {
      return narrow(alternativeFunc.get());
    }
  }

  /**
   * Returns the internal value, if it's present, otherwise throws the exception
   * supplied by the lambda.
   */
  default T getOrElseThrow(Supplier<? extends RuntimeException> exceptionSupplier) {
    return match(
        () -> {
          throw exceptionSupplier.get();
        },
        val -> val);
  }

  /**
   * For an Option.Some, run the predicate on its contents. If it's true, then you
   * get back the original Option.Some. If the predicate returns false, or if it's
   * an Option.None, then you get back Option.None.
   *
   * <p>Analogous to filtering a list with zero or one elements, as in {@link IList#filter(Predicate)}.
   */
  default Option<T> filter(Predicate<? super T> predicate) {
    return flatmap(
        val -> (predicate.test(val))
            ? this
            : none());
  }

  /**
   * Returns a new Option, with the function applied to the internal value, if it's present.
   *
   * <p>Analogous to mapping over a list with zero or one elements as in {@link IList#map(Function)}.
   */
  default <R> Option<R> map(Function<? super T, ? extends R> func) {
    return flatmap(val -> some(func.apply(val)));
  }

  /**
   * Returns a new Option, with the function applied to the internal value, if it's present.
   * This function is expected to return an Option.
   *
   * <p>Analogous to flatmapping over a list with zero or one elements, as in {@link IList#flatmap(Function)}.
   */
  default <R> Option<R> flatmap(Function<? super T, ? extends Option<? extends R>> func) {
    return match(Option::none, val -> narrow(func.apply(val)));
  }

  /**
   * Allows you to narrow a type parameter.
   *
   * <p>Analogous to narrowing a list's type as in {@link IList#narrow(IList)}.
   */
  static <T> Option<T> narrow(Option<? extends T> option) {
    @SuppressWarnings("unchecked")
    Option<T> castOption = (Option<T>) option;
    return castOption;
  }

  /**
   * Sometimes, you want to generate a log when you have an Option.none, to indicate
   * an error of whatever sort, and you want to do nothing otherwise. This log method
   * calls {@link Log#e(String, Object)} with the given tag, and then evaluates the lambda for the logging
   * string. If it's an Option.some, this is a no-op.
   *
   * <p>The original Option is returned, no matter what, making it easy to pipeline
   * calls to the log method with whatever else you're doing.
   */
  @CanIgnoreReturnValue
  default Option<T> logIfNone(String tag, Supplier<String> func) {
    if (isNone()) {
      Log.e(tag, func.get());
    }

    return this;
  }

  class Some<T> implements Option<T> {
    private final T contents;

    // don't call this externally
    private Some(T contents) {
      this.contents = contents;
    }

    @Override
    public boolean isSome() {
      return true;
    }

    @Override
    public T get() {
      return contents;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Some<?>)) {
        return false;
      }

      Some<?> otherSome = (Some<?>) o;

      return contents.equals(otherSome.contents);
    }

    @Override
    public String toString() {
      return "Option.Some(" + Strings.objectToEscapedString(contents) + ")";
    }

    @Override
    public int hashCode() {
      return 0;
    }
  }

  class None<T> implements Option<T> {
    // Just like we did with the empty list singleton, we'll have an Option.none() singleton. We can
    // get away with this for the same reasons, namely that we never actually care what the type parameter
    // is, so Option.none() doesn't need more than one instance
    private static final Option<?> SINGLETON = new None<>();

    // don't call this externally
    private None() { }

    @Override
    public boolean isSome() {
      return false;
    }

    @Override
    public T get() {
      throw new NoSuchElementException("can't get() from Option.None");
    }

    @Override
    public boolean equals(Object o) {
      // simplification: all Option.none()'s are the same, regardless of type parameter
      return o instanceof Option.None<?>;
    }

    @Override
    public String toString() {
      return "Option.None()";
    }

    @Override
    public int hashCode() {
      return 0;
    }
  }
}
