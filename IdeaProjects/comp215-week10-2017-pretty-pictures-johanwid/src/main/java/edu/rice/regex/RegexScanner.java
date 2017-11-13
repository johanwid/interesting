/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.regex;

import edu.rice.list.IList;
import edu.rice.list.KeyValue;
import edu.rice.list.LazyList;
import edu.rice.list.List;
import edu.rice.tree.IMap;
import edu.rice.tree.TreapMap;
import edu.rice.util.Log;
import edu.rice.util.Try;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static edu.rice.util.Option.*;

/**
 * Given a Java enum which defines regexes for scanning tokens, this class builds a regex with
 * "named-capturing groups" and then uses it to tokenize your input. The names of the tokens
 * will come from the Enum, and the regular expressions for the tokens will come by fetching the
 * pattern associated with each token in the enum. If you have a token type that doesn't ever occur in
 * your input, such as "FAIL" in the example below, make sure its pattern is the empty string, and it
 * will be ignored while constructing the regular expressions.
 *
 * <p>To make this work, make sure your enum implements the {@link TokenPatterns} interface, which means
 * it will have an extra method, {@link TokenPatterns#pattern()} which returns the regex pattern.
 *
 * <p>Example:
 * <pre>
 * <code>
 * enum CurlyLanguagePatterns implements TokenPatterns {
 *     OPENCURLY("\\{"),
 *     CLOSECURLY("\\}"),
 *     WHITESPACE("\\s+"),
 *     FAIL("");/\
 *
 *     public final String pattern;
 *
 *     CurlyLanguagePatterns(String pattern) {
 *         this.pattern = pattern;
 *     }
 *
 *     public String pattern() { return pattern; }
 * }
 * </code>
 * </pre>
 * <p> Typical usage: </p>
 * <pre>
 * <code>
 * IList&lt;Token&lt;CurlyLanguagePatterns&gt;&gt; results =
 *     scanPatterns(inputString, CurlyLanguagePatterns.class, new Token&lt;&gt;(CurlyLanguagePatterns.FAIL, ""));
 * </code>
 * </pre>
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public interface RegexScanner {
  /**
   * This method runs the scanner on the input, given the set of possible token patterns,
   * and returns a list of tokens.
   *
   * @param enumPatternsClazz a class reference to an enum which implements {@link TokenPatterns}
   * @param <T> a type parameter corresponding to the enum's class reference
   * @param input the string being tokenized
   * @param failToken the token to return if the scanner fails to recognize a token
   * @return a list of {@link Token}'s, each of which will have the type (from the enum) and
   *         the string value; or the <code>failToken</code> if something went wrong.
   */
  static <T extends Enum<T> & TokenPatterns> IList<Token<T>>
      scanPatterns(String input, Class<T> enumPatternsClazz, Token<T> failToken) {

    // Engineering note: the type constraints on T have seemingly magical properties. If you poke around
    // on StackOverflow for code doing similar things, you'll see lots of wildcard types (i.e., Class<?>) and
    // typecasts. By constraining the argument to the constructor, enumPatternClazz, to be Class<T>, and with all the
    // constraints on T, the Java compiler will only allow you to pass in the Class for an enum that implements
    // our desired String getPattern() method.

    // With these type constraints, there's no way to have the type parameter T and the class object
    // enumPatternsClazz be anything other than one and the same. This leads to the uncomfortable question of why
    // you have to pass both a type parameter and a class parameter. Why not pass just one? Sigh. That would be yet
    // another weakness of the Java language.

    // The "real" solution would be to use a programming language that has "reified generics", wherein the type
    // parameter T is a thing that you can directly interact with, doing all the things that Java forces you
    // to do with these "Class" objects instead. Microsoft's C# actually does this properly. Likewise, some of the
    // other languages that run on the JVM, like Kotlin, also have reified generics.

    // Further reading:
    // http://whyjavasucks.com/Blog/5/Java_By_Example/87/Type_Erasure
    // http://stackoverflow.com/questions/31876372/what-is-reification
    // https://kotlinlang.org/docs/reference/inline-functions.html#reified-type-parameters

    return new CrunchedPatterns<>(enumPatternsClazz).tokenize(input, failToken);
  }

  /**
   * This class isn't meant to be visible to the outside world. It's purpose is to parse the
   * enum and build up everything we're going to need to run a lexical scanner based on it.
   * Technically, we could squeeze this constructor and its {@link #tokenize(String, Token)}
   * method all into the static method above, yielding one big monster of a method.
   *
   * <p>That would be a bit ugly. Instead, it's helpful to break things into pieces
   * like this, both in terms of testability and in terms of keeping the complexity of our
   * code under control. Also, at some point we might want to reuse the state here for more
   * than one run of the token scanner; this structure gives us some future extensibility.
   */
  class CrunchedPatterns<T extends Enum<T> & TokenPatterns> {
    private static final String TAG = "RegexScanner";

    private final Pattern pattern;
    private final IMap<String, T> nameToTokenMap;
    private final IList<String> groupNames;

    /**
     * Given an enum type that includes String values (and implements the {@link TokenPatterns} interface), this builds
     * a regular expression using "named-capturing groups" and uses that to help tokenize input strings.
     *
     * @param enumPatternsClazz
     *     The enum's "Class"
     */
    private CrunchedPatterns(Class<T> enumPatternsClazz) {
      if (!enumPatternsClazz.isEnum()) {
        // This particular failure should never actually happen, because of the type constraint T extends Enum<T>.
        // Nonetheless, a bit of paranoia seems reasonable.
        throw new RuntimeException("RegexScanner requires an enum class");
      }

      // this gets us an array of all the enum values in the type.
      final IList<T> enumConstants = List.fromArray(enumPatternsClazz.getEnumConstants());

      final IList<KeyValue<String, String>> nameToRegexMap =
          enumConstants
              .map(e -> e.match(KeyValue::make))
              .filter(kv -> !kv.getValue().equals(""));  // get rid of non-parsing tokens, error/metadata tokens, etc.

      final int numPatterns = nameToRegexMap.length();
      nameToTokenMap = TreapMap.fromList(enumConstants.map(e -> KeyValue.make(e.name(), e)));

      groupNames = nameToRegexMap.map(KeyValue::getKey);

      // Before we build the "real" regular expression that combines all the individual ones,
      // we're first going to try compiling the individual expressions to make sure that they're
      // individually well-formed. This will result in better error-feedback to developers.

      final int numSuccess = nameToRegexMap.map(kv -> kv.match((name, pattern) ->
          KeyValue.make(name,
              Try.of(() -> Pattern.compile(pattern))
                  .logIfFailure(TAG, throwable -> String.format("regular expression (%s) for (%s) is not valid: %s",
                      pattern, name, throwable.getMessage())))))
          .filter(kv -> kv.getValue().isSuccess())
          .length();

      if (numSuccess != numPatterns) {
        Log.e(TAG, () -> String.format("found only %d of %d valid regular expressions", numSuccess, numPatterns));
        throw new IllegalArgumentException("invalid regular expression");
      }

      // This is the final "group matching" regex pattern that we'll use in the tokenizer. Here's a short tutorial
      // that shows what's going on here.
      // http://www.logicbig.com/tutorials/core-java-tutorial/java-regular-expressions/named-captruing-groups/

      pattern = Pattern.compile(
          nameToRegexMap
              // build the named-capturing groups regular expression
              .map(kv -> String.format("(?<%s>%s)", kv.getKey(), kv.getValue()))
              .join("|"));
    }

    /**
     * This returns a list of pairs corresponding to the output of the tokenizer, where first element
     * of the pair is the enum value (you can later cast this to the type you used when making the
     * RegexScanner) and the second element is the string that the regex matched.
     *
     * <p>If the tokenizer hits something for which there isn't a matching regex, the next element of the
     * resulting list of tokens will be the failToken.
     */
    private IList<Token<T>> tokenize(String input, Token<T> failToken) {
      final java.util.regex.Matcher jmatcher = pattern.matcher(input);

      // Engineering note: We need a counter for how far into the string we are; we use this to detect
      // skipped characters. We also need to remember if we had a scanner error. We can't just have an
      // int and a boolean hanging around normally because that violates Java8's lambda lexical scope
      // capture rules. So instead, we wrap it into a class instance, which will then be within the
      // lexical scope of the lambdas and where we can freely do all the mutation we want.

      // Yes, this is seemingly inexplicable. What matters is that keeping state on the side, which mutates,
      // is not easily compatible with lambdas, yet we must do precisely this to work with the (awful) regex
      // APIs that we get from java.util.regex. All of this awful business is hidden from the user of our
      // RegexScanner package, at least.

      // Basically, lambdas aren't capable of reaching back into their lexical scope and mutating values.
      // However, they can mutate values inside of other values, so fine, that's what we're doing.

      final NamedGroupState state = new NamedGroupState();
      state.matchOffset = 0;
      state.failure = false;

      return LazyList.ogenerate(() -> {
        if (state.failure) {
          return none();
        }

        if (jmatcher.find(state.matchOffset)) {
          MatchResult mresult = jmatcher.toMatchResult();
          int matchStart = mresult.start();

          IList<String> namesFound = groupNames.filter(name -> jmatcher.group(name) != null);

          if (namesFound.length() == 0) {
            // this case (hopefully) won't happen because, if there are no matches, then
            // jmatcher.find() should return false. But in the interests of paranoia...
            Log.e(TAG, () -> String.format("no matching token found, scanner failed (context: %s)",
                safeSubstring(input, state.matchOffset, 10)));
            state.failure = true;
            return some(failToken);
          }

          if (namesFound.length() > 1) {
            Log.e(TAG, () ->
                String.format(
                    "multiple matches (token types: [%s]), input patterns are ambiguous (error!), scanner failed (context: %s)",
                    namesFound.join(","), safeSubstring(input, state.matchOffset, 10)));
            state.failure = true;
            return some(failToken);
          }

          if (matchStart > state.matchOffset) {
            Log.e(TAG, () -> String.format("matcher skipped some characters, scanner failed (context: %s)",
                safeSubstring(input, state.matchOffset, 10)));
            state.failure = true;
            return some(failToken);
          }

          String matchName = namesFound.head(); // the token, we found it, hurrah!
          String matchString = mresult.group();
          state.matchOffset += matchString.length(); // advance the state for next time: mutation!

          if (matchString.isEmpty()) {
            Log.e(TAG, () -> String.format("matcher found a zero-length string! bug in regex for token rule (%s)", matchName));
            state.failure = true;
            return some(failToken);
          }

          return nameToTokenMap
              .oget(matchName) // go from the token string to the actual TokenPatterns enum
              .map(type -> new Token<>(type, matchString)) // then build a token around it
              .orElse(some(failToken));

        } else {
          // two possibilities: either we hit the end of the input, or we failed to match any of the patterns
          if (state.matchOffset >= input.length()) {
            return none(); // empty-list; we're done!
          }

          // otherwise, there are some characters remaining that we don't know what to do with
          Log.e(TAG, () -> String.format("no matching token found, scanner failed (context: %s)",
              safeSubstring(input, state.matchOffset, 10)));
          state.failure = true;
          return some(failToken);
        }
      });
    }

    /**
     * The real {@link String#substring(int, int)} will throw an exception if you ask for anything beyond the end
     * of the string. This method will truncate at the end. No exceptions.
     */
    private static String safeSubstring(String input, int offset, int length) {
      if (offset + length > input.length()) {
        return input.substring(offset, input.length());
      } else {
        return input.substring(offset, offset + length);
      }
    }

    // used as state by the LazyList generator, working around Java's "effectively final" constraint on closures
    private static class NamedGroupState {
      int matchOffset;
      boolean failure;
    }
  }

  // Engineering note: while this scanner uses the regular expression system built into the Java standard libraries,
  // it's not necessary the most efficient way of accomplishing the job. Instead, there are tools that are tailor-made
  // for precisely this purpose. See, for example, JFlex (http://www.jflex.de/). You'll see a lot more about this
  // if you take Comp412.
}
