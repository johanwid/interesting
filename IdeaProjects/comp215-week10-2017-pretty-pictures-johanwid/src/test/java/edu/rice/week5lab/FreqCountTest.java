/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.week5lab;

import edu.rice.io.Files;
import edu.rice.list.IList;
import edu.rice.list.KeyValue;
import edu.rice.list.List;
import org.junit.Test;

import edu.rice.tree.IMap;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@ParametersAreNonnullByDefault
@CheckReturnValue
public class FreqCountTest {
  // Courtesy of: https://baconipsum.com/
  private final String baconIpsum = Files.readResource("baconIpsum.txt").getOrElse("missing file!");

  // Courtesy of: http://www.archives.gov/exhibits/charters/constitution_transcript.html
  private final String usConstitution = Files.readResource("USConstitution.txt").getOrElse("missing file!");

  @Test
  public void testFrequencyCount() throws Exception {
    IMap<String, Integer> baconCount = FreqCount.count(baconIpsum);
    IMap<String, Integer> constitutionCount = FreqCount.count(usConstitution);

    IList<KeyValue<String, Integer>> topBacon = FreqCount.mostFrequent(baconCount);
    IList<KeyValue<String, Integer>> topConstitution = FreqCount.mostFrequent(constitutionCount);

    // A common issue with broken regular expressions is that they'll match the empty string, so
    // first we'll have an assertion that flags this common error.

    assertFalse("Empty strings should not appear in frequency counts!", topBacon.map(KeyValue::getKey).contains(""));
    assertFalse("Empty strings should not appear in frequency counts!", topConstitution.map(KeyValue::getKey).contains(""));

    assertEquals(
        List.of(
            KeyValue.make("pork", 18),
            KeyValue.make("ribs", 12),
            KeyValue.make("beef", 11),
            KeyValue.make("short", 9),
            KeyValue.make("loin", 8),
            KeyValue.make("tip", 8),
            KeyValue.make("alcatra", 7),
            KeyValue.make("corned", 7),
            KeyValue.make("bacon", 6),
            KeyValue.make("belly", 6)),
        topBacon.limit(10));

    assertEquals(
        List.of(
            KeyValue.make("the", 423),
            KeyValue.make("of", 289),
            KeyValue.make("and", 192),
            KeyValue.make("shall", 191),
            KeyValue.make("be", 125),
            KeyValue.make("to", 110),
            KeyValue.make("in", 89),
            KeyValue.make("states", 80),
            KeyValue.make("or", 79),
            KeyValue.make("united", 54)),
        topConstitution.limit(10));

    System.out.println("Top ten bacon words: " + topBacon.limit(10));
    System.out.println("Top ten Constitution words: " + topConstitution.limit(10));
  }
}

