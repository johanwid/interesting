/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.sexpr;

import edu.rice.list.IList;
import edu.rice.list.LazyList;
import edu.rice.util.Option;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

/**
 * S-Expression Value. The way we're doing it here is a bit like:
 * <br>Value ::= Word | SExpr
 * <br>SExpr ::= ( ValueList )
 * <br>ValueList ::= Value ValueList || *Nothing*
 *
 * <p>If you look carefully, you'll notice that the definition of a ValueList is basically identical
 * to the definition of {@link IList}, and we'll internally store our ValueLists as exactly that: IList&lt;Value&gt;.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public interface Value {
  /**
   * General-purpose structural pattern matching on an s-expression value, with one lambda per concrete
   * type of the Value. Typical usage:
   * <pre><code>
   *     Value val = ... ;
   *     Option&lt;Whatever&gt; oresult = val.match(
   *         word -&gt; Option.some(word.something()),
   *         sexpr -&gt; Option.none());
   * </code></pre>
   */
  default <Q> Q match(Function<Word, Q> wordF, Function<Sexpr, Q> sexprF) {

    if (this instanceof Word) {
      return wordF.apply(asWord());
    } else if (this instanceof Sexpr) {
      return sexprF.apply(asSexpr());
    } else {
      // This will never actually happen, but we're being suitably paranoid
      throw new RuntimeException("this should never happen! unexpected value type: " + getClass().getName());
    }
  }

  /**
   * If you know that this Value is <b>definitely</b> a Word, this method does the casting for you
   * in a nice, pipelined fashion.
   */
  default Word asWord() {
    return (Word) this;
  }

  /**
   * If you know that this Value is <b>definitely</b> a Sexpr, this method does the casting for you
   * in a nice, pipelined fashion.
   */
  default Sexpr asSexpr() {
    return (Sexpr) this;
  }

  /**
   * Handy builder method for making an s-expression.
   */
  static Value sexpr(IList<Value> valueList) {
    return new Sexpr(valueList);
  }

  /**
   * Handy builder method for making an s-expression, here allowing varargs.
   */
  static Value sexpr(Value ...valueList) {
    return sexpr(LazyList.fromArray(valueList));
  }

  /**
   * Handy builder method for making words inside an s-expression.
   */
  static Value word(String str) {
    return new Word(str);
  }

  class Sexpr implements Value {
    private final IList<Value> valueList;

    /**
     * This constructor is something we use from edu.rice.sexpr.Parser, but it's not intended for public use.
     */
    private Sexpr(IList<Value> valueList) {
      this.valueList = valueList;
    }

    /**
     * Returns a list of the Values in the Sexpr.
     */
    public IList<Value> getList() {
      return valueList;
    }

    /**
     * Returns the nth value in the array, if present.
     */
    public Option<Value> nth(int i) {
      return valueList.nth(i);
    }

    @Override
    public String toString() {
      return "( " + valueList.join(" ") + " )";
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Sexpr)) {
        return false;
      }

      Sexpr sexpr = (Sexpr) o;

      return valueList.equals(sexpr.valueList);
    }

    @Override
    public int hashCode() {
      return valueList.hashCode();
    }
  }

  class Word implements Value {
    private final String word;

    /**
     * This constructor is something we use from edu.rice.sexpr.Parser, but it's not intended for public use.
     */
    private Word(String word) {
      this.word = word;
    }

    /**
     * Gives you back the number inside the JNumber as a double.
     */
    public String get() {
      return word;
    }

    @Override
    public String toString() {
      return word;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Word)) {
        return false;
      }

      Word ow = (Word) o;

      return ow.get().equals(word);
    }

    @Override
    public int hashCode() {
      return word.hashCode();
    }
  }
}
