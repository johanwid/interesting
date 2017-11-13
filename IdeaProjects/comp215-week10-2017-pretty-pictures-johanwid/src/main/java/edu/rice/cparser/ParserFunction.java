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
import edu.rice.regex.TokenPatterns;
import edu.rice.util.Log;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static edu.rice.cparser.Expression.*;
import static edu.rice.cparser.Result.*;

/**
 * ParserFunctions are building blocks for parsers. These are also called
 * <i>parser combinators</i> or <i>monadic parsers</i>. The core idea is that parsers can be
 * <i>combined</i> with other parsers, and that parsers are just functions so you can define
 * them normally and make them recursive if necessary.
 *
 * <p>For an example that shows how to fit all these pieces together, check out the code for
 * {@link SExpression}, which builds a parser for s-expressions and then maps the resulting
 * expression to its own implementation that keeps the internal list and ignores the rest.
 *
 * <p><b>Don't panic!</b> If you do a web search for "parser combinators" you'll find examples
 * of this for a variety of different programming languages, some simple and others much
 * more complicated. Of note, here's a paper that explains the general theory behind this.
 * Requires some comfort reading the Haskell programming language.
 * <a href="http://unpetitaccident.com/pub/compeng/languages/Haskell/monparsing.pdf">
 * http://unpetitaccident.com/pub/compeng/languages/Haskell/monparsing.pdf</a>
 *
 * <p>There are several "industrial strength" parser-combinator libraries for Java itself.
 * See, for example, these two parsers based on the sophisticated Haskell Parsec library:
 * <ul>
 * <li><a href="https://github.com/jparsec/jparsec">JParsec, https://github.com/jparsec/jparsec</a></li>
 * <li><a href="https://github.com/jon-hanson/parsecj">ParsecJ, https://github.com/jon-hanson/parsecj</a></li>
 * </ul>
 *
 * <p>So is this the best possible way to implement a parser? Well that depends on what your
 * goals are. This parser doesn't have particularly good error handling. All you get back is
 * {@link ParserError}, without any indication of why the parser failed. Similarly, this parser
 * doesn't support <i>streaming</i>, wherein you might have an infinite volume of input and want
 * to get output immediately. Also, if you're parsing a <b>lot</b> of input, then performance is
 * probably an issue. Here, we have none of those helpful properties, but at least you can write
 * the code for a parser in a very concise way!
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
@FunctionalInterface
public interface ParserFunction<T extends Enum<T> & TokenPatterns> {
  /**
   * Every parser takes in a list of tokens and returns a {@link Result}, which internally
   * has behavior analogous to {@link edu.rice.util.Option}, in that it can indicate success
   * or failure as well as the resulting {@link Expression} in the successful case.
   */
  Result<T> parse(IList<Token<T>> tokens);

  /**
   * When combining parsers, we want to make sure that the resulting parsers not only combine
   * the parsing functionality but also combine the internal {@link Object#toString()} methods,
   * because we want parsers to be usefully convertible to strings. (Helpful for debugging, etc.)
   *
   * <p>This utility method takes two lambdas -- a new parser function and a supplier of a string, and wraps them
   * together into another ParserFunction that behaves just like the first lambda, only behaving nicely when printed.
   */
  static <T extends Enum<T> & TokenPatterns> ParserFunction<T> pfWithString(ParserFunction<T> pf, Supplier<String> ss) {
    // Engineering note: this is the only place in the entire Comp215 codebase that we've needed to use
    // anonymous inner classes. Normally, the compiler figures out, from context, the interface type of the
    // lambda and then creates a new instance of that interface type. If that was all that we wanted, then
    // we wouldn't need this code. However, lambda expressions don't do anything special for implementing
    // the other methods on Object, and we particularly want the toString() method to do something useful.
    // This requires us to dig back to the ugly days before Java8 of using anonymous inner classes, wherein
    // we declare that we're implementing an interface and fill in the missing methods.

    return new ParserFunction<T>() {
      @Override
      public Result<T> parse(IList<Token<T>> tokens) {
        return pf.parse(tokens);
      }

      @Override
      public String toString() {
        return ss.get();
      }

      // While we're at it, we might as well implement equals() and hashCode() methods, so these parsers
      // can be stored in lists and maps and otherwise be manipulated. There's no useful way to define these
      // methods on the ParserFunction lambdas (pf), so we'll just delegate to the toString() methods.

      @Override
      public boolean equals(Object o) {
        return o instanceof ParserFunction<?> && toString().equals(o.toString());
      }

      @Override
      public int hashCode() {
        return toString().hashCode();
      }
    };
  }

  /**
   * Given the current parser (A) and a second parser (B), returns a parser for (A B): A followed by B.
   */
  default ParserFunction<T> then(ParserFunction<T> b) {
    return pfWithString(
        input -> this.parse(input).flatmap(
            resultA -> b.parse(resultA.tokens).flatmap(
                resultB -> resultOk(exprPair(resultA.production, resultB.production), resultB.tokens))),

        () -> "(" + this.toString() + " then " + b.toString() + ")");
  }

  /**
   * Given the current parser, returns another parser that accepts the same input but requires there to be no more
   * remaining tokens. If tokens remain, then {@link ParserError} is returned.
   */
  default ParserFunction<T> thenEnd() {
    return pfWithString(
        input -> this.parse(input).flatmap(result -> result.tokens.empty() ? result : resultError()),
        () -> this.toString() + ", EOF");
  }

  /**
   * Given the current parser (A) and a second parser (B), returns a parser for (A | B): A or B. If both parsers
   * succeed, that means we found an ambiguity in the parser. An error will be logged and {@link ParserError} will be
   * returned.
   */
  default ParserFunction<T> or(ParserFunction<T> b) {
    return pfWithString(
        input -> this.parse(input).match(
            resultA -> b.parse(input).match(
                resultB -> {
                  Log.e("ParserFunction.or", () -> "Ambiguous parsing results: two parsers both accept this input!");
                  Log.e("ParserFunction.or", () -> "-- ParserA: " + this.toString());
                  Log.e("ParserFunction.or", () -> "-- ParserB: " + b.toString());
                  Log.e("ParserFunction.or", () -> "-- Input tokens: " + input.limit(10).join(", "));
                  return resultError();
                },
                errorB -> resultA), // if B fails but A succeeded, then we'll take whatever A's got
            errorA -> b.parse(input)), // if A fails, then we'll take whatever B's got

        () -> "(" + this.toString() + " or " + b.toString() + ")");
  }

  /**
   * Given the current parser (A), returns a parser that will accept a list of A. In the event that there are no
   * matches, the results will be an expression with an empty-list inside. This composed production never returns {@link
   * ParserError}. <b>Warning: this production has the potential to consume zero tokens, and thus
   * infinite loop if used carelessly.</b>
   */
  default ParserFunction<T> list() {
    return pfWithString(
        input -> this.parse(input).match(
            // if we fail right away, we return an expression with an empty-list and the same tokens
            resultA -> this.list().parse(resultA.tokens).match(
                resultB -> resultOk(exprPair(resultA.production, resultB.production), resultB.tokens),
                errorB -> resultA),
            errorA -> resultOk(exprNothing(), input)),

        () -> "(list-of " + this.toString() + ")");
  }

  /**
   * Given the current parser (A), returns a parser that will accept a list of A's, separated with the given separator,
   * perhaps a <code>parseTerminal(COMMA)</code> or equivalent. In the event that there are no
   * matches, the results will be an expression with an empty-list inside. This composed production never returns {@link
   * ParserError}. <b>Warning: this production has the potential to consume zero tokens, and thus
   * infinite loop if used carelessly.</b>
   *
   * <p>A useful feature of this production is that it will remove the separators from the resulting expression tree. If
   * you want to keep the separators in the resulting expression tree, then use {@link #separatedList(ParserFunction, boolean)}.
   */
  default ParserFunction<T> separatedList(ParserFunction<T> separator) {
    return separatedList(separator, true);
  }

  /**
   * Given the current parser (A), returns a parser that will accept a list of A's, separated with the given separator,
   * perhaps a <code>parseTerminal(COMMA)</code> or equivalent. In the event that there are no
   * matches, the results will be an expression with an empty-list inside. This composed production never returns {@link
   * ParserError}. <b>Warning: this production has the potential to consume zero tokens, and thus
   * infinite loop if used carelessly.</b>
   *
   * <p>A useful feature of this production is that it will optionally remove the separators from the resulting expression tree.
   */
  default ParserFunction<T> separatedList(ParserFunction<T> separator, boolean removeSeparators) {
    // This sepPlusList parser handles the sequence of separator-expr-separator-expr-..., which is to say, the
    // case after the leading expression has already been parsed.

    ParserFunction<T> sepPlusList = removeSeparators
        ? separator.then(this) // the separator first, then the thing we're getting a list of
            .mapExpression(expr -> expr.asExprPair().exprB) // replace the pair of (separator, expr) with just expr
            .list() // a repeating list of all that
        : separator.then(this).list();

    return pfWithString(
        // we'll initially parse "this" (which is the thing we're trying to get a list of), and then
        // after that go with the separated list
        input -> this.parse(input).match(
            resultA -> sepPlusList.parse(resultA.tokens).match(
                resultB -> resultOk(exprPair(resultA.production, resultB.production), resultB.tokens),
                errorB -> resultA),
            errorA -> resultOk(exprNothing(), input)),

        () -> "(separated-list-of " + this.toString() + ")");
  }

  /**
   * Allows you to add some post-processing into the parser, combining with a function that changes the expression. This
   * might be useful if you want to clean up what you've got and use a different implementation of {@link Expression}.
   */
  default ParserFunction<T> mapExpression(UnaryOperator<Expression<T>> mapFunc) {
    return input -> this.parse(input).flatmap(result -> result.mapProduction(mapFunc));
  }

  /**
   * Constructs a "terminal" parser that accepts a single token and rejects all other tokens. This variant matches
   * both the token's type (i.e., the name of the token from the enum) <b>and</b> the token's value. If you want
   * a parser that will accept any value for a given type, use {@link #parseTerminal(Enum)}.
   */
  static <T extends Enum<T> & TokenPatterns> ParserFunction<T> parseTerminal(T tokenType, String value) {
    return parseTerminal(new Token<>(tokenType, value));
  }

  /**
   * Constructs a "terminal" parser that accepts a single token and rejects all other tokens. This variant matches
   * both the token's type (i.e., the name of the token from the enum) <b>and</b> the token's value. If you want
   * a parser that will accept any value for a given type, use {@link #parseTerminal(Enum)}.
   */
  static <T extends Enum<T> & TokenPatterns> ParserFunction<T> parseTerminal(Token<T> token) {
    return pfWithString(
        input -> input.match(
            emptyList -> resultError(),
            (head, tail) -> head.equals(token)
                ? resultOk(exprTerminal(token), tail)
                : resultError()),

        () -> "Terminal" + token);

    // Engineering notes: See that second call to resultError()? If you're running anything other than the latest IntelliJ
    // build, it will have a red squiggle below it that you can fix by instead writing resultError(token). If you
    // then look at that function, you'll notice that it ignores its input. We've done the same thing elsewhere with
    // Option.none() and List.makeEmptySameType().

    // What's going on here? IntelliJ's type inference engine is sometimes better than javac, sometimes worse.
    // In this case, for older IntelliJ versions, it was unable to infer the type parameter for *one* of the calls
    // to resultError() but not the other one. But when there's a concrete argument to the function, then IntelliJ
    // suddenly figures it out again. Likewise, we could write Result.<T>resultError(), naming the type parameter
    // explicitly. That's ugly, but it works.

    // As an exercise, you might also want to have a look at how we reported this bug to JetBrains. We can't
    // just give them the entire Comp215 codebase, so we had to make a standalone example that triggered the
    // same bug, which was tricky because, perhaps unsurprisingly, this bug only crops up when lambda expressions
    // were deeply nested. They fixed it within two days of our bug report in June, and the builds of IntelliJ
    // appearing a month later started containing the fix.

    // https://youtrack.jetbrains.com/issue/IDEA-174301
  }

  /**
   * Constructs a "terminal" parser that accepts a single token and rejects all other tokens. This variant will
   * accept <i>any</i> token of the given token type, ignoring the token's value. If you want to match a token
   * with a specific type <b>and</b> value, then use {@link #parseTerminal(Token)} or {@link #parseTerminal(Enum, String)}.
   */
  static <T extends Enum<T> & TokenPatterns> ParserFunction<T> parseTerminal(T tokenType) {
    return pfWithString(
        input -> input.match(
            emptyList -> resultError(),
            (head, tail) -> head.type.equals(tokenType)
                ? resultOk(exprTerminal(head), tail)
                : resultError()),

        () -> "Terminal(" + tokenType + ")");
  }
}
