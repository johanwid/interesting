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

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;

import static edu.rice.util.Strings.objectToEscapedString;

/**
 * General-purpose parsing tokens, produced by {@link RegexScanner}, among other places.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public class Token<T extends Enum<T> & TokenPatterns> {
  public final T type;
  public final String data;

  public Token(T type, String data) {
    this.type = type;
    this.data = data;
  }

  @Override
  public int hashCode() {
    return toString().hashCode(); // a kludge, but hopefully useful
  }

  @Override
  public boolean equals(Object t) {
    if (!(t instanceof Token<?>)) {
      return false;
    }

    // we're doing an unchecked type cast here, but it's okay because if the TokenPatterns differ,
    // the equals() test will sort it out
    Token<? extends Enum<T>> tt = (Token<? extends Enum<T>>) t;
    return this.data.equals(tt.data) && this.type.equals(tt.type);
  }

  @Override
  public String toString() {
    return String.format("(%s: %s)", type.name(), objectToEscapedString(data));
  }
}
