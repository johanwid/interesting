/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.cparser;

import edu.rice.list.IList;
import edu.rice.list.LazyList;
import edu.rice.list.List;
import edu.rice.regex.TokenPatterns;
import edu.rice.regex.Token;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * All {@link ParserFunction} parsers return an Expression, which could be one of the implementations
 * within this interface (terminals, pairs, nothings), or could be any other class that implements
 * this same interface.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
interface Expression<T extends Enum<T> & TokenPatterns> {
  // Data definition: an expression can be:

  //   a terminal (i.e., a single token)
  //   a pair of expressions
  //   a "nothing" expression
  //   --or--
  //   anything else, anywhere else, that implements this Expression interface

  // A simple parser made by combining ParserFunction's together will ultimately yield
  // pairs of expressions bottoming out at terminals and nothing's.

  // A fancier parser will use Result.mapExpression() to replace these expressions with other
  // expressions (still implementing the same interface) that correspond to specific
  // value types for that parser's particular language.

  // We don't support a match() method on Expressions, since there could be an unbounded
  // number of implementations of Expression.

  /**
   * Gets the results as a list, no matter what it is. A terminal expression is converted to a list with one element.
   * An {@link ExprPair} is treated much like our original definition of a {@link List}, where we defined a list as a head
   * and a tail-list or empty-list, interpreted here as the "nothing" expression.
   *
   * <p>Build your grammars in the same fashion, and this method will be useful for processing the result of
   * the parser.
   */
  IList<Expression<T>> asList();

  /**
   * Convenience function. When you know, from its position in the grammar, that an expression is really
   * an ExprPair, this is somewhat more pleasant to use than a typecast.
   */
  default ExprPair<T> asExprPair() {
    if (this instanceof ExprPair<?>) {
      return (ExprPair<T>) this;
    } else {
      throw new IllegalArgumentException("Expression isn't an ExprPair: " + toString());
    }
  }

  /**
   * Convenience function. When you know, from its position in the grammar, that an expression is really
   * a Terminal, this is somewhat more pleasant to use than a typecast.
   */
  default Terminal<T> asTerminal() {
    if (this instanceof Terminal<?>) {
      return (Terminal<T>) this;
    } else {
      throw new IllegalArgumentException("Expression isn't a Terminal: " + toString());
    }
  }

  /**
   * Makes a "nothing" expression, containing no tokens within.
   */
  static <T extends Enum<T> & TokenPatterns> Expression<T> exprNothing() {
    @SuppressWarnings("unchecked")
    Expression<T> typedNothing = (Expression<T>) Nothing.SINGLETON;
    return typedNothing;
  }

  /**
   * Makes a "terminal" expression, containing exactly one token within.
   */
  static <T extends Enum<T> & TokenPatterns> Expression<T> exprTerminal(Token<T> token) {
    return new Terminal<>(token);
  }

  /**
   * Makes a "terminal" expression, containing exactly one token within.
   */
  static <T extends Enum<T> & TokenPatterns> Expression<T> exprTerminal(T tokenType, String value) {
    return new Terminal<>(new Token<>(tokenType, value));
  }

  /**
   * Makes a pair of expressions, used by various parser combinators. If expressions are combined in the same
   * fashion as lists (having a head and tail-list or nothing), then they can be easily converted back to
   * an {@link IList} using {@link #asList()}.
   */
  static <T extends Enum<T> & TokenPatterns> Expression<T> exprPair(Expression<T> exprA, Expression<T> exprB) {
    return new ExprPair<>(exprA, exprB);
  }

  class Terminal<T extends Enum<T> & TokenPatterns> implements Expression<T> {
    public final Token<T> token;

    private Terminal(Token<T> token) {
      this.token = token;
    }

    @Override
    public IList<Expression<T>> asList() {
      return List.of(this);
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Terminal<?>)) {
        return false;
      }

      Terminal<?> other = (Terminal<?>) o;
      return token.equals(other.token);
    }

    @Override
    public String toString() {
      return "Terminal" + token; // printing the token brings its own parentheses
    }

    @Override
    public int hashCode() {
      return token.hashCode();
    }
  }

  class ExprPair<T extends Enum<T> & TokenPatterns> implements Expression<T> {
    public final Expression<T> exprA;
    public final Expression<T> exprB;

    private ExprPair(Expression<T> exprA, Expression<T> exprB) {
      this.exprA = exprA;
      this.exprB = exprB;
    }

    @Override
    public IList<Expression<T>> asList() {
      return LazyList.make(exprA, exprB::asList);

      // Engineering note: If we wanted to have a higher-performance zero-copy solution, we could try to
      // have ExprPair<T> implement IList<T>, but that would add some extra complexity here that we don't
      // need. Also, it's probably useful, in terms of bug-free use of Expressions, to avoid making it easy
      // to mistake an expression for a list. And, on top of all that, if you really care about performance,
      // then you're not going to use a parser-combinator library like this. Take Comp412 for more details!
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof ExprPair<?>)) {
        return false;
      }

      ExprPair<?> other = (ExprPair<?>) o;
      return exprA.equals(other.exprA) && exprB.equals(other.exprB);
    }

    @Override
    public String toString() {
      return "Pair(" + exprA + ", " + exprB + ")";
    }

    @Override
    public int hashCode() {
      return exprA.hashCode() * 31 + exprB.hashCode();
    }
  }

  class Nothing<T extends Enum<T> & TokenPatterns> implements Expression<T> {
    public static final Expression<?> SINGLETON = new Nothing<>();

    private Nothing() { }

    @Override
    public IList<Expression<T>> asList() {
      return List.makeEmpty();
    }

    @Override
    public boolean equals(Object o) {
      return o == this;
    }

    @Override
    public String toString() {
      return "∅";
    }

    @Override
    public int hashCode() {
      return 0;
    }
  }
}
