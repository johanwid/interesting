/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.json;

import edu.rice.list.IList;
import edu.rice.list.List;
import edu.rice.regex.Token;
import edu.rice.util.Log;
import edu.rice.util.Option;
import org.intellij.lang.annotations.Language;
import org.intellij.lang.annotations.Subst;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

import static edu.rice.json.Scanner.JsonPatterns;
import static edu.rice.json.Scanner.scanJson;
import static edu.rice.json.Value.*;
import static edu.rice.util.Option.none;
import static edu.rice.util.Option.some;
import static edu.rice.util.Strings.stringToOptionDouble;

/**
 * Parser for various JSON types. Everything public is a static method; this class is never instantiated.
 * If you're trying to convert a String to an arbitrary JSON value, then you probably want to use {@link #parseJsonValue(String)}.
 * If your String is something that you require to be a JSON Object or Array, then you probably want to use
 * {@link #parseJsonObject(String)} or {@link #parseJsonArray(String)}, respectively.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public class Parser {
  private static final String TAG = "JsonParsers";

  // Engineering note: There's no particular reason for this to be a class vs. an interface. Either way, we
  // just want to export a bunch of static methods. If it were an interface, we wouldn't need to declare
  // the private constructor, as below, but then there would be no way to have the private helper methods
  // or the package-scope methods that we have here. (Java9 adds private but not package-scope methods to
  // interfaces.)  The approach here is pretty much the standard way of doing things before Java8, so
  // we'll use it here as well.

  // So why are some method public and others package-scope? The public static methods are meant to be the *external*
  // interface to our JSON code. The package-scope static methods are internal, but we want them visible to our
  // unit tests, which are all in the same edu.rice.json package.

  // On the other hand, the private methods (and the MAKERS field) aren't meant to be used by anybody
  // outside of this file. That's what "private" is meant to convey.

  private Parser() { } // never instantiate this class!

  /**
   * Given a String input, this will attempt to parse it and give you back a
   * JSON object, which can then be interrogated for its internal contents.
   *
   * @see JObject#getMap()
   * @return Option.some of the JSON value, if the parse operation succeeded, or option.none if it failed
   */
  public static Option<JObject> parseJsonObject(@Language("JSON") String input) {
    IList<Token<JsonPatterns>> tokens = scanJson(input);

    return makeObject(tokens)
        .flatmap(result -> result.tokens.match(
            // this is what we want: no remaining tokens after we're done parsing the Object
            emptyList -> result.production.asOJObject(),

            (head, tail) -> Option.<JObject>none()
                  // adding explicit logging because otherwise the programmer may get really confused wondering why
                  .logIfNone(TAG, () -> "tokens remaining in the stream after end of the JSON object; parser failure")));
  }

  /**
   * Given a String input, this will attempt to parse it and give you back a
   * JSON value (of any type: object, array, string, etc.). You may then interrogate the result
   * for its concrete type and/or contents.
   *
   * @return Option.some of the JSON value, if the parse operation succeeded, or option.none if it failed
   */
  public static Option<Value> parseJsonValue(@Language("JSON") String input) {
    IList<Token<JsonPatterns>> tokens = scanJson(input);

    return makeValue(tokens)
        .flatmap(result -> result.tokens.match(
            // this is what we want: no remaining tokens after we're done parsing the Value
            emptyList -> some(result.production),

            (head, tail) -> Option.<Value>none()
                // adding explicit logging because otherwise the programmer may get really confused wondering why
                .logIfNone(TAG, () -> "tokens remaining in the stream after end of the JSON value; parser failure")));
  }

  /**
   * Given a String input, this will attempt to parse it and give you back a
   * JSON array which can then be interrogated for its internal contents.
   *
   * @see JArray#nth(int)
   * @return Option.some of the JSON array, if the parse operation succeeded, or option.none if it failed
   */
  public static Option<JArray> parseJsonArray(@Language("JSON") String input) {
    IList<Token<JsonPatterns>> tokens = scanJson(input);

    return makeArray(tokens)
       .flatmap(result -> result.tokens.match(
         // this is what we want: no remaining tokens after we're done parsing the Value
         emptyList -> result.production.asOJArray(),

         (head, tail) -> Option.<JArray>none()
           // adding explicit logging because otherwise the programmer may get really confused wondering why
           .logIfNone(TAG, () -> "tokens remaining in the stream after end of the JSON array; parser failure")));
  }

  /**
   * Every internal make-method returns an Option&lt;Result&gt;, which inside contains
   * the Value produced as well as an IList of the remaining tokens. That Result
   * is parameterized. Commonly it's Result&lt;Value&gt; but some helper functions
   * and such return other things besides Value, while still returning a production
   * of some kind and a list of remaining tokens.
   */
  static class Result<T> {
    public final T production;
    public final IList<Token<JsonPatterns>> tokens;

    Result(T production, IList<Token<JsonPatterns>> tokens) {
      this.production = production;
      this.tokens = tokens;
    }

    @Override
    public String toString() {
      return String.format("Result(production: %s, tokens: %s)", production.toString(), tokens.toString());
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Result<?>)) {
        return false;
      }

      Result<?> otherResult = (Result<?>) o;

      return production.equals(otherResult.production) && tokens.equals(otherResult.tokens);
    }

    @Override
    public int hashCode() {
      return production.hashCode() * 7 + tokens.hashCode() * 31;
    }
  }

  //
  // ENGINEEERING NOTES / Data definition:
  //
  // Every different method here represents a different JSON production type that we might
  // be able to parse. If it successfully parses the thing it's looking for, it will return
  // an Option<Result<Value>> containing the production (JObject, JString, etc.) as well as a list
  // of the remaining unparsed tokens. If it fails, it returns Option.None.
  //
  // Each of the make-methods returns the wider type Option<Result<Value>> rather than something
  // more specific, like makeString returning Option<Result<JString>>, in order for all of the
  // make-methods to have the *same* type signature, which lets us make a list of them, as below,
  // and not worry about type compatibility.
  //
  // The only exception to this is makeKeyValue, which is only necessary as an internal helper
  // function to makeObject. You can get away with being more specific there.
  //

  //
  // Here is a list of the builders as lambdas; note the impressive type signature.
  //
  // <p>A value can be:
  //   string
  //   number
  //   object
  //   array
  //   true
  //   false
  //   null
  //
  private static final IList<Function<IList<Token<JsonPatterns>>, Option<Result<Value>>>> MAKERS = List.of(
      Parser::makeString,
      Parser::makeNumber,
      Parser::makeObject,
      Parser::makeArray,
      Parser::makeBoolean,
      Parser::makeNull);

  /**
   * General-purpose maker for all value types; will internally try all the concrete JSON builders
   * and return the result of whichever one succeeds.
   *
   * @return Option.some of the Result, which includes the Value and a list of the remaining tokens; option.none if it failed
   */
  static Option<Result<Value>> makeValue(IList<Token<JsonPatterns>> tokenList) {
    // oflatmap() gives us a list of the Option.some() results. If there's nothing, then the parser failed.
    // If there's exactly one, then that's our answer. If there are more than one, then we have an ambiguous
    // parser (i.e., it has more than one way of parsing the same message), which indicates that the parser
    // is broken.
    return MAKERS.oflatmap(x -> x.apply(tokenList)).match(

        // none of the builders succeeded, so we'll pass that along
        emptyList -> none(),

        // we got exactly one success, which is exactly what we want
        (head, emptyTail) -> some(head),

        // oops, multiple successful builders!
        (first, second, remainder) -> {
          Log.e(TAG, "Ambiguous parser! Only one production should be successful.");
          throw new RuntimeException("Ambiguous parser! Only one production should be successful.");
        });
  }

  /**
   * Maker for JSON Objects.
   *
   * @return Option.some of the Result, which includes the Value and a list of the remaining tokens; option.none if it failed
   */
  static Option<Result<Value>> makeObject(IList<Token<JsonPatterns>> tokenList) {
    return tokenList.match(
        emptyList -> none(),

        (token, remainingTokens) -> token.type == JsonPatterns.OPENCURLY
            // next, we recursively add together the list of key-value pairs; this will consume the close curly
            ? makeObjectList(remainingTokens, true)
              .map(result -> new Result<>(new JObject(result.production), result.tokens))
            : none());
  }

  /**
   * Recursive helper function: get the list of KeyValue tuples within a JSON object.
   */
  private static Option<Result<IList<JKeyValue>>>
      makeObjectList(IList<Token<JsonPatterns>> tokenList, boolean firstTime) {

    return tokenList.match(
        emptyList -> none(),

        (token, remainingTokens) -> {
          Option<Result<JKeyValue>> oKeyValue;

          // Engineering note: why are we using a switch here? Notice how we don't have to say JsonPatterns.CLOSECURLY
          // and JsonPatterns.COMMA everywhere? Java provides this nice trick when you switch on an enum, and then you
          // get this nice shorthand. Otherwise, we don't tend to use the switch() statement very much in Comp215.

          // (You can use import static to get this sort of behavior elsewhere, but a switch statement does it for you.)

          // In general, we tend to use "registries" (like "MAKERS", above) or match() methods, since those can have return
          // values, while you can't say "x = switch() ...", which makes switches much less useful when doing functional
          // programming. Notice how we instead declare oKeyValue above, assign to it below, and then use it? That's
          // not very beautiful. We could potentially split out this function into two functions: one that requires a
          // comma and one that doesn't. That might simplify the error handling, but not enough that it's really work having
          // two separate functions doing very similar things.

          switch (token.type) {
            // if we find a close bracket, then we're done!
            case CLOSECURLY:
              return some(new Result<>(List.makeEmpty(), remainingTokens));

            case COMMA:
              // we require a comma between key-value tuples, but not the first time
              if (firstTime) {
                return none();
              }
              oKeyValue = makeKeyValue(remainingTokens);
              break;

            default:
              // we required a comma, but didn't get it
              if (!firstTime) {
                return none();
              }
              oKeyValue = makeKeyValue(tokenList);
              break;
          }

          // recursively continue consuming the rest of the input and then prepend the current key/value pair to
          // the front of the list that's returned from the recursive call (pair.production) and pass along the
          // remaining unconsumed tokens
          return oKeyValue.flatmap(headResult ->
              makeObjectList(headResult.tokens, false)
                  .map(tailResults -> new Result<>(tailResults.production.add(headResult.production), tailResults.tokens)));
        });
  }

  /**
   * Attempts to construct a JKeyValue from a list of tokens.
   *
   * @return Option.some of the Result, which includes the JKeyValue and a list of the remaining tokens; option.none if it failed
   */
  static Option<Result<JKeyValue>> makeKeyValue(IList<Token<JsonPatterns>> tokenList) {
    return tokenList.match(
        emptyList -> none(),

        (head, emptyTail) -> none(),

        (string, colon, remainingTokens) -> {
          if (string.type != JsonPatterns.STRING || colon.type != JsonPatterns.COLON) {
            return none();
          }

          // What's with the annotation here? See the engineering note in Builders::jsonString.
          @Subst("") String rawData = string.data;

          // We could alternatively call into JString.build(), but we've already verified
          // the token type, and it's a terminal token, so we'll take a short-cut.
          JString jstring = JString.fromEscapedString(rawData);

          // and finally grab the value and turn it into a pair
          return makeValue(remainingTokens)
              .map(value -> new Result<>(JKeyValue.of(jstring, value.production), value.tokens));
        });
  }

  /**
   * Attempts to construct a JArray from a list of tokens.
   *
   * @return Option.some of the Result, which includes the Value and a list of the remaining tokens; option.none if it failed
   */
  static Option<Result<Value>> makeArray(IList<Token<JsonPatterns>> tokenList) {
    return tokenList.match(
        emptyList -> none(),

        (token, remainingTokens) -> token.type == JsonPatterns.OPENSQUARE
              // next, we recursively add together the list; this will consume the close square
            ? makeArrayList(remainingTokens, true)
              .map(result -> new Result<>(new JArray(result.production), result.tokens))
            : none());
  }

  /**
   * JSON arrays are lists of values, comma-separated. This will go until it hits a close-square-bracket, consume it,
   * and return the list of values. Note the somewhat convoluted "firstTime" logic to deal with commas, which are
   * required to be there, but only between values. No dangling commas at the front or end, which makes our life
   * a bit more complicated. Alternately, we could have arranged this as two separate functions, one for the first
   * time (no comma requirement) and one for thereafter (commas required before values).
   */
  private static Option<Result<IList<Value>>> makeArrayList(
      IList<Token<JsonPatterns>> tokenList, boolean firstTime) {

    return tokenList.match(
        emptyList -> none(),

        (token, remainingTokens) -> {

          Option<Result<Value>> nextValue; // the next value in the JSON array will go here

          // but before we try to grab that value, we first need to deal with the requirements of
          // JSON, namely that if we hit a close-square-bracket, we're done, and we have to deal with
          // commas, which are required between array elements. Note the use of the firstTime boolean
          // to distinguish the two cases while we're doing our parsing.

          switch (token.type) {
            case CLOSESQUARE:
              return some(new Result<>(List.makeEmpty(), remainingTokens));

            case COMMA:
              // Comma must be absent if it's the first time through.
              if (firstTime) {
                return none();
              }

              // But if it's not, then it's required! Try to get the next JValue.
              nextValue = makeValue(remainingTokens);
              break;

            default:
              // if it's not the first time, we required a comma.
              if (!firstTime) {
                return none();
              }

              // Otherwise, the current token needs to be reused, so we're using tokenList rather than remainingTokens
              nextValue = makeValue(tokenList);
              break;
          }

          // recursively continue consuming the rest of the input and then prepend the current value to the front of the
          // list that's returned from the recursive call and pass along the remaining unconsumed tokens
          return nextValue
              .flatmap(headResult ->
                  makeArrayList(headResult.tokens, false)
                      .map(tailResults ->
                          new Result<>(tailResults.production.add(headResult.production), tailResults.tokens)));
        });
  }

  /**
   * Attempts to construct a JString from a list of tokens.
   *
   * @return Option.some of the Result, which includes the Value and a list of the remaining tokens; option.none if it failed
   */
  static Option<Result<Value>> makeString(IList<Token<JsonPatterns>> tokenList) {
    return tokenList.match(
        emptyList -> none(),

        (token, remainingTokens) -> {
          switch (token.type) {
            case STRING:
              // What's with the annotation here? See the engineering note in Builders::jsonString.
              @Subst("") String rawToken = token.data;
              return some(new Result<>(JString.fromEscapedString(rawToken), remainingTokens));

            default:
              return none();
          }
        });
  }

  /**
   * Attempts to construct a JNumber from a list of tokens.
   *
   * @return Option.some of the Result, which includes the Value and a list of the remaining tokens; option.none if it failed
   */
  static Option<Result<Value>> makeNumber(IList<Token<JsonPatterns>> tokenList) {
    return tokenList.match(
        emptyList -> none(),

        (token, remainingTokens) -> token.type == JsonPatterns.NUMBER
            ? stringToOptionDouble(token.data).map(number -> new Result<>(new JNumber(number), remainingTokens))
            : none());
  }

  /**
   * Attempts to construct a JBoolean from a list of tokens.
   *
   * @return Option.some of the Result, which includes the Value and a list of the remaining tokens; option.none if it failed
   */
  static Option<Result<Value>> makeBoolean(IList<Token<JsonPatterns>> tokenList) {
    return tokenList.match(
        emptyList -> none(),

        (token, remainingTokens) -> {
          switch (token.type) {
            case TRUE:
              return some(new Result<>(JBoolean.of(true), remainingTokens));

            case FALSE:
              return some(new Result<>(JBoolean.of(false), remainingTokens));

            default:
              return none();
          }
        });
  }

  /**
   * Attempts to construct a JNull from a list of tokens.
   *
   * @return Option.some of the Result, which includes the Value and a list of the remaining tokens; option.none if it failed
   */
  static Option<Result<Value>> makeNull(IList<Token<JsonPatterns>> tokenList) {
    return tokenList.match(
        emptyList -> none(),

        (token, remainingTokens) -> token.type == JsonPatterns.NULL
            ? some(new Result<>(JNull.make(), remainingTokens))
            : none());
  }
}

// Engineering note: in the real world, you hardly ever would code up a hand-built recursive descent parser like
// this. Instead, you tend to use "parser-generators" where you write down the BNF in a file and the parser-generator
// does all the rest for you. You'll see these things in Comp412. Java programmers might tend to use
// ANTLR (http://www.antlr.org/), and there are many comparable tools that target other programming languages.
// Here, for example, is an ANTLR grammar for JSON: https://github.com/antlr/grammars-v4/blob/master/json/JSON.g4

// If you want to see something really crazy, check out this JSON parser, implemented in Java, using a library
// ported from Haskell, that does basically everything here in 1/4 the lines of code.
// https://github.com/jon-hanson/parsecj/blob/master/src/test/java/org/javafp/parsecj/json/Grammar.java

// This is an example of a "parser combinator", which is a structure that lets you "compose" together little parsers
// and then make bigger parsers out of them, such that your composition exactly follows the rules of the grammar
// you're trying to parse. We'll briefly introduce this in more detail later in the semester, but don't panic.
// This sort of thing counts as "optional" for Comp215, but you'll probably see something like it in Comp311.
