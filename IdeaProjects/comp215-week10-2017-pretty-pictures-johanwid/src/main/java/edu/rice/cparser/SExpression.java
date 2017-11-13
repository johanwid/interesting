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
import edu.rice.regex.Token;
import edu.rice.sexpr.Scanner.SexprPatterns;
import edu.rice.sexpr.Value;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;

import static edu.rice.cparser.ParserFunction.parseTerminal;
import static edu.rice.cparser.ParserFunction.pfWithString;
import static edu.rice.sexpr.Scanner.SexprPatterns.*;
import static edu.rice.sexpr.Scanner.scanSexpr;

/**
 * SExpression implements the {@link Expression} interface to handle s-expression parsing.
 * When using {@link SExpression#parseSexpr(String)}, the results will be compatible with
 * and composable against other {@link Expression} parsers. If you wish to convert from
 * an SExpression to a {@link Value}, use the {@link #toValue()} method.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public class SExpression implements Expression<SexprPatterns> {
  // Engineering notes: Compare this to edu.rice.sexpr.Parser, and the striking difference
  // is this parser is maybe 15 lines of code! The only complexity is that we want a new data class
  // for SExpression, distinct from the general-purpose Expressions used by our parser combinator.

  // Note as well how SExpression.toValue() shows how easy it can be to convert from one of
  // these things to a value type that wasn't specifically engineered to deal with the Expression
  // type of our parser-combinator.

  /**
   * Given a raw Java string, scan it into tokens and then parse it as an s-expression.
   * See also, {@link #PARSER} for a {@link ParserFunction}-composable parser.
   */
  public static Result<SexprPatterns> parseSexpr(String input) {
    // If the input has anything after the s-expression, this composition will treat it as an error.
    return PARSER.thenEnd().parse(scanSexpr(input));
  }

  /**
   * This s-expression parser is fully composable with any other
   * {@link ParserFunction}. If successful, PARSER yields an {@link SExpression}
   * inside its {@link Result.Ok}, providing easy access to the list of
   * {@link Expression} within.
   *
   * <p>If the input to PARSER has any tokens after the end of a valid s-expression,
   * then the {@link Result.Ok} will contain those remaining tokens. If you wish
   * to require the input to be exactly one s-expression with no remaining tokens,
   * then you should use <code>PARSER.thenEnd()</code>. This behavior is also
   * available from {@link #parseSexpr(String)}.
   */
  public static final ParserFunction<SexprPatterns> PARSER =
      // Recall that an s-expression is an open-paren a list of word-or-sexps and then a close-paren.
      // Notice how easily this turns into code?
      parseTerminal(OPEN)
          .then(parseTerminal(WORD)
              .or(pfWithString(SExpression::parseInternal, () -> "SExpression"))
              .list()
              .then(parseTerminal(CLOSE)))
      .mapExpression(SExpression::make);

  private static Result<SexprPatterns> parseInternal(IList<Token<SexprPatterns>> tokens) {
    // This function fits anywhere that wants ParserFunction<SexprPatterns>, which
    // lets us use it recursively in the definition of PARSER, above.
    return PARSER.parse(tokens);
  }

  // Data definition: an s-expression is defined as a list of expressions. An expression
  // can be a terminal (WORD) or an s-expression. The only difference between this definition
  // and the grammar above is that the grammar worries about OPEN and CLOSE parens and will
  // reject any tokens after the CLOSE.
  private final IList<Expression<SexprPatterns>> contents;

  private SExpression(IList<Expression<SexprPatterns>> contents) {
    this.contents = contents;
  }

  /**
   * Builder method for making an s-expression given a list of expressions.
   */
  public static SExpression make(IList<Expression<SexprPatterns>> contents) {
    return new SExpression(contents);
  }

  private static SExpression make(Expression<SexprPatterns> contents) {
    // this code *assumes* that the input is a properly formed Expression, so we're
    // skipping all the error checking we might otherwise do. We're assuming that
    // the parser dumps out a list with three entries, with #0 and #2 being the
    // open and close parentheses, respectively, and with #1 being the list of
    // expressions inside the s-expression, which is what we really care about.
    return new SExpression(contents.asList().nth(1).get().asList());
  }

  /**
   * Given an SExpression from our parser, recursively converts it to a {@link edu.rice.sexpr.Value}.
   */
  public Value toValue() {
    return Value.sexpr(contents.map(expr -> {
      if (expr instanceof Terminal<?>) {
        return Value.word(((Terminal<?>) expr).token.data);
      } else if (expr instanceof SExpression) {
        return ((SExpression) expr).toValue();
      } else {
        throw new RuntimeException("unexpected expression type: " + expr.getClass().getName());
      }
    }));
  }

  @Override
  public IList<Expression<SexprPatterns>> asList() {
    return contents;
  }

  @Override
  public String toString() {
    return "(" + contents.join(" ") + ")";
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SExpression)) {
      return false;
    }

    SExpression other = (SExpression) o;
    return other.contents.equals(contents);
  }

  @Override
  public int hashCode() {
    return contents.hashCode();
  }
}
