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
import edu.rice.regex.RegexScanner;
import edu.rice.regex.Token;
import edu.rice.regex.TokenPatterns;
import org.intellij.lang.annotations.Language;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;

import static edu.rice.json.Scanner.JsonPatterns.*;
import static edu.rice.regex.RegexScanner.scanPatterns;

/**
 * This class takes a string and tokenizes it for JSON. {@link RegexScanner} does all the
 * heavy lifting. Note that String tokens coming from the tokenizer will <b>not</b> have
 * quotation marks around them. We remove those. They <b>may have escape characters</b> within,
 * which you'll want to deal with elsewhere.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public interface Scanner {
  /**
   * Given a string, return a list of JSON tokens. If there's a failure in the tokenizer,
   * there will be a FAIL token at the point of the failure. Also note that whitespace tokens
   * are filtered out. You don't have to worry about them.
   * @see JsonPatterns#FAIL
   */
  static IList<Token<JsonPatterns>> scanJson(@Language("JSON") String input) {
    return scanPatterns(input, JsonPatterns.class, new Token<>(FAIL, ""))
        .filter(x -> x.type != WHITESPACE) // remove whitespace tokens; we don't care about them
        .map(x -> x.type == STRING         // remove leading and trailing quotation marks from strings
            ? new Token<>(STRING, x.data.substring(1, x.data.length() - 1))
            : x);
  }

  // Engineering note: We're going to want to reuse these regular expressions in many different places,
  // not just in the scanner, so we're defining them as separate Strings. Also notable, when you define
  // a constant like this, as part of an interface, it's *always* final. The compiler would reject code
  // that tried to assign something to one of these strings.

  // We're taking advantage of IntelliJ's "regular expression language" annotation, which allows IntelliJ
  // to verify that these complex regular expressions are well-formed. IntelliJ will even detect if you
  // repeat a pattern or otherwise have redundancies in your regular expression. IntelliJ calls this
  // "language injection" and they support a number of other languages like HTML and SQL.

  String jsonStringPatternNoQuotes =
      "(" +                                  // grouping of acceptable characters:
          "[^\"\\\\\\p{Cntrl}]" +            //   any unicode char except " or \ or control char
          "|\\\\" +                          //   or a backslash followed by one of:
          "([\"\\\\/bfnrt]" +                //   - a series of acceptable single characters
          "|u[0123456789abcdefABCDEF]{4})" + //   - or the code for a unicode quad-hex thing
          ")*";                              // zero or more of the group

  String jsonStringPattern = "\"" + jsonStringPatternNoQuotes + "\"";

  String jsonNumberPattern =
      "(-)?" +                               // optional minus sign
          "(0|" +                            // either a zero, or
          "[1-9][0-9]*)" +                   // a series of digits starting with a non-zero
          "(\\.[0-9]+)?" +                   // optional decimal point followed by one or more digits
          "([eE][+-]?[0-9]+)?" +             // optional exponent with optional +/- sign
          "\\b";                             // word boundary checker at the end, ensures we don't terminate early

  enum JsonPatterns implements TokenPatterns {
    STRING(jsonStringPattern),
    NUMBER(jsonNumberPattern),
    TRUE("true\\b"),
    FALSE("false\\b"),
    NULL("null\\b"),
    OPENCURLY("\\{"),
    CLOSECURLY("}"),
    COLON(":"),
    COMMA(","),
    OPENSQUARE("\\["),
    CLOSESQUARE("]"),
    WHITESPACE("\\s+"),
    FAIL("");                    // if the matcher fails, you get one of these

    public final String pattern;

    // We'd prefer to say @RegEx (which comes from the somewhat-more-standard JSR305 annotations supported
    // by FindBugs and others, but IntelliJ doesn't do the highlighting for it in this context, but it *does* do
    // highlighting when you say @Language("RegExp"). See this bug report.
    // https://youtrack.jetbrains.com/issue/IDEA-172271

    JsonPatterns(@Language("RegExp") String pattern) {
      this.pattern = pattern;
    }

    @Override
    public String pattern() {
      return pattern;
    }
  }
}
