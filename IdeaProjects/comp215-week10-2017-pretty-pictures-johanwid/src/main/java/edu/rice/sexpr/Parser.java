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
import edu.rice.list.List;
import edu.rice.regex.Token;
import edu.rice.util.Option;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

import static edu.rice.sexpr.Scanner.SexprPatterns;
import static edu.rice.sexpr.Scanner.SexprPatterns.*;
import static edu.rice.sexpr.Scanner.scanSexpr;
import static edu.rice.sexpr.Value.*;
import static edu.rice.util.Option.none;
import static edu.rice.util.Option.some;

/**
 * S-Expression Recursive-Descent Parser. The way we're doing it here is a bit like:
 * <br>Value ::= Word | SExpr
 * <br>SExpr ::= ( ValueList )
 * <br>ValueList ::= Value ValueList || *Nothing*
 *
 * <p>If you look carefully, you'll notice that the definition of a ValueList is basically identical
 * to the definition of {@link List}, and we'll internally store our ValueLists as exactly that: IList&lt;Value&gt;.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public class Parser {
  private static final String TAG = "SexprParser";

  private Parser() { }

  /**
   * Given a String input, this will attempt to parse it and give you back an
   * S-Expression which can then be interrogated for its internal contents.
   *
   * @see Value.Sexpr#nth(int)
   * @return Option.some of the S-Expression, if the parse operation succeeded, or option.none if it failed
   */
  public static Option<Value> parseSexpr(String input) {
    // Engineering note: we're not exposing Result outside of this file. From the outside, you say parseSexpr
    // and you get back an optional Value. The details of what kind of Value can be found within, and the
    // remaining tokens are dealt with here. If there *are* remaining tokens, then the input might well have
    // *started* with a valid s-expression, but the string, as a whole, is *not* an s-expression, so we'll
    // return Option.none().

    // Also, you might be wondering about the need to explicitly state a type for the Option.none(). You
    // could actually replace Option.<Value>none() with none(result.production), which just ignores its argument.
    // What's going on here? Welcome to the limits of Java's type inference. Option.some() can look at the type
    // of its input and infer the type of its output. Option.none() has no input, and Java sadly isn't clever
    // enough to notice that both lambdas here need to return the exact same type.

    // We saw this issue earlier with List.makeEmpty() when we first introduced lists with parameterized types.

    return makeValue(scanSexpr(input))
        .flatmap(result -> result.tokens.match(
            // this is what we want: no remaining tokens after we're done parsing the Value
            emptyList -> some(result.production),

            (head, tail) -> Option.<Value>none()
                  // adding explicit logging because otherwise the programmer may get really confused wondering why
                  .logIfNone(TAG, () -> "tokens remaining in the stream after end of the s-expression; parser failure")));
  }

  /**
   * This internal class is the result of calling each production. It's got a type parameter, because each
   * production returns something different, but they always return the resulting production, and a list
   * of remaining tokens. That pairing is handled here. Yes, we could have used Pair instead, but then the
   * type parameters would start getting really ugly. Better to be specific for our needs.
   */
  static class Result<T> {
    public final T production;
    public final IList<Token<SexprPatterns>> tokens;

    Result(T production, IList<Token<SexprPatterns>> tokens) {
      this.production = production;
      this.tokens = tokens;
    }

    @Override
    public String toString() {
      return String.format("Result(production: %s, tokens: %s)", production.toString(), tokens.toString());
    }
  }

  private static final IList<Function<IList<Token<SexprPatterns>>, Option<Result<Value>>>> MAKERS = List.of(
      Parser::makeSexpr,
      Parser::makeWord);

  /**
   * General-purpose maker for all value types; will internally try all the concrete JSON builders
   * and return the result of whichever one succeeds.
   *
   * @return Option.some of the Result, which includes the Value and a list of the remaining tokens; option.none if it failed
   */
  static Option<Result<Value>> makeValue(IList<Token<SexprPatterns>> tokenList) {
    // Returns the first non-empty result, if it exists. If we wanted to be paranoid,
    // we could try them all and yell if more than one succeeds, since that would indicate
    // an ambiguous grammar.
    return MAKERS.oflatmap(x -> x.apply(tokenList)).ohead();
  }

  /**
   * Attempts to construct a S-Expression from a list of tokens.
   *
   * @return Option.some of the Result, which includes the S-Expression Value and a list of the remaining tokens;
   *     option.none if it failed.
   */
  static Option<Result<Value>> makeSexpr(IList<Token<SexprPatterns>> tokenList) {
    return tokenList.match(
        emptyList -> none(),

        (token, remainingTokens) -> (token.type == OPEN)
            // next, we recursively add together the list; this will consume the close square and return to us a
            // list of tokens, which we'll then convert into an Sexpr.
            ? makeValueList(remainingTokens)
                .map(result -> new Result<>(sexpr(result.production), result.tokens))

            : none());
  }

  /**
   * This helper function deals with everything after the open-paren, recursively gobbling tokens until it hits
   * the close-paren, and then building an IList of values on the way back out.
   */
  private static Option<Result<IList<Value>>> makeValueList(IList<Token<SexprPatterns>> tokenList) {
    return tokenList.match(
        emptyList -> none(),

        (token, remainingTokens) -> (token.type == CLOSE)

            ? some(new Result<>(List.makeEmpty(), remainingTokens))

            // recursively continue consuming the rest of the input and then prepend the current value to the front of the
            // list that's returned from the recursive call and pass along the remaining unconsumed tokens
            : makeValue(tokenList)
                .flatmap(headResult -> makeValueList(headResult.tokens)
                    .map(tailResults -> new Result<>(tailResults.production.add(headResult.production), tailResults.tokens))));
  }

  /**
   * Attempts to construct a Word from a list of tokens.
   *
   * @return Option.some of the Result, which includes the Value and a list of the remaining tokens; option.none if it failed
   */
  static Option<Result<Value>> makeWord(IList<Token<SexprPatterns>> tokenList) {
    return tokenList.match(
        emptyList -> none(),

        (token, remainingTokens) -> (token.type == WORD)
            ? some(new Result<>(word(token.data), remainingTokens))
            : none());
  }
}
