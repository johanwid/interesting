/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.io;

import edu.rice.json.Value;
import org.junit.Test;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;

import static edu.rice.json.Operations.ogetPath;
import static edu.rice.json.Parser.parseJsonObject;
import static org.junit.Assert.assertEquals;

// Normally, we'd have to worry about these Option.get() or Try.get() calls failing, but in a testing situation, we
// *expect* them to succeed. If they fail, then JUnit makes the whole test fail, which is exactly what we want.
@ParametersAreNonnullByDefault
@CheckReturnValue
public class JsonResourceIOTest {
  @Test
  public void testJsonFile() throws Throwable {
    // this test puts many different things together: our ability to read resource files, then process them
    Value jsonValue = parseJsonObject(Files.readResource("testdata.json").getOrElseThrow()).get();

    assertEquals("SGML",
        ogetPath(jsonValue, "glossary/GlossDiv/GlossList/GlossEntry/Acronym").get().asJString().toUnescapedString());
  }
}
