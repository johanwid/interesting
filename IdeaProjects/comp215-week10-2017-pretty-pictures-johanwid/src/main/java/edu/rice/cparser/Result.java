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

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Every {@link ParserFunction} returns a Result. Those results have two subtypes: <i>Ok</i>, which corresponds to a successful
 * parsing of the input and then contains the resulting Expression and list of tokens. The other subtype
 * is <i>ParserError</i>, which corresponds to an inability to parser the input.
 *
 * <p>This Result class is a bit different from the {@link edu.rice.json.Parser.Result} or
 * {@link edu.rice.sexpr.Parser.Result}. Those classes are typically wrapped in an {@link edu.rice.util.Option},
 * while we instead roll that some/none functionality directly into this Result class. This yields cleaner code.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
interface Result<T extends Enum<T> & TokenPatterns> {
  /**
   * Make an "ok" parser result.
   * @param production The expression resulting from the parser.
   * @param tokens The remaining unread tokens in the input stream
   */
  static <T extends Enum<T> & TokenPatterns> Result<T> resultOk(Expression<T> production, IList<Token<T>> tokens) {
    return new Ok<>(production, tokens);
  }

  /**
   * Make an "error" parser result.
   */
  static <T extends Enum<T> & TokenPatterns> Result<T> resultError() {
    @SuppressWarnings("unchecked")
    ParserError<T> typedError = (ParserError<T>) ParserError.SINGLETON;
    return typedError;
  }

  /**
   * Make an "error" parser result, ignoring the input token, but passing through its type.
   * (This helps with inadequate type inference when using {@link #resultError()}.
   */
  @SuppressWarnings("unused")
  static <T extends Enum<T> & TokenPatterns> Result<T> resultError(Expression<T> ignored) {
    return resultError();
  }

  /**
   * Make an "error" parser result, ignoring the input token, but passing through its type.
   * (This helps with inadequate type inference when using {@link #resultError()}.
   */
  @SuppressWarnings("unused")
  static <T extends Enum<T> & TokenPatterns> Result<T> resultError(Token<T> ignored) {
    return resultError();
  }

  /**
   * General-purpose structural pattern matching, taking two lambdas: one for if the result is "ok" and the other if
   * it's an "error".
   */
  default <R> R match(Function<Ok<T>, R> okFunc, Function<ParserError<T>, R> errorFunc) {
    if (isOk()) {
      return okFunc.apply(asOk());
    } else if (this.equals(ParserError.SINGLETON)) {
      return errorFunc.apply(asError());
    } else {
      throw new IllegalArgumentException("match on an unexpected object type; shouldn't ever happen!");
    }
  }

  /**
   * Getter for {@link Result.Ok}, throws an exception if it's not that type. Useful for tests and such when
   * you're absolutely sure you don't have a parsing failure.
   */
  default Ok<T> asOk() {
    if (this instanceof Ok<?>) {
      return (Ok<T>) this;
    } else {
      throw new IllegalArgumentException("type isn't Result.Ok: " + this.getClass().getName());
    }
  }

  /**
   * Getter for {@link ParserError}, throws an exception if it's not that type. Useful for tests and such when
   * you're absolutely sure you had a parsing failure.
   */
  default ParserError<T> asError() {
    if (this instanceof Result.ParserError<?>) {
      return (ParserError<T>) this;
    } else {
      throw new IllegalArgumentException("type isn't Result.ParserError: " + this.getClass().getName());
    }
  }

  /**
   * Returns whether the result is {@link Result.Ok}.
   */
  default boolean isOk() {
    return this instanceof Ok<?>;
  }

  /**
   * Returns whether the result is {@link ParserError}.
   */
  default boolean isError() {
    return this instanceof Result.ParserError<?>;
  }

  /**
   * Operates just like {@link edu.rice.util.Option#flatmap(Function)} and other such things. The lambda is
   * applied to the result if it's "Ok" to generate another result.
   */
  default Result<T> flatmap(Function<Ok<T>, Result<T>> flatmapFunc) {
    return match(flatmapFunc, error -> error);
  }

  /**
   * Makes it easy to change the production expression to a different type of expression.
   */
  default Result<T> mapProduction(UnaryOperator<Expression<T>> mapFunc) {
    return match(
        ok -> resultOk(mapFunc.apply(ok.production), ok.tokens),
        error -> error);
  }


  class Ok<T extends Enum<T> & TokenPatterns> implements Result<T> {
    public final Expression<T> production;
    public final IList<Token<T>> tokens;

    private Ok(Expression<T> production, IList<Token<T>> tokens) {
      this.production = production;
      this.tokens = tokens;
    }

    @Override
    public String toString() {
      return String.format("Result.Ok(production: %s, tokens: %s)", production.toString(), tokens.toString());
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Ok<?>)) {
        return false;
      }

      Ok<?> otherResult = (Ok<?>) o;

      return production.equals(otherResult.production) && tokens.equals(otherResult.tokens);
    }

    @Override
    public int hashCode() {
      return production.hashCode() * 7 + tokens.hashCode() * 31;
    }
  }

  class ParserError<T extends Enum<T> & TokenPatterns> implements Result<T> {
    private static final ParserError<?> SINGLETON = new ParserError<>();

    private ParserError() { }

    @Override
    public String toString() {
      return "Result.ParserError()";
    }

    @Override
    public boolean equals(Object o) {
      return (o instanceof Result.ParserError);
    }

    @Override
    public int hashCode() {
      return 1;
    }
  }
}

