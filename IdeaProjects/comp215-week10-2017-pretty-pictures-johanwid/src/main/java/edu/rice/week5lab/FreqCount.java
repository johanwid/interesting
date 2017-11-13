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

import edu.rice.list.IList;
import edu.rice.list.KeyValue;
import edu.rice.list.LazyList;
import edu.rice.tree.IMap;
import edu.rice.tree.TreapMap;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Week 5 lab assignment.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public class FreqCount {
  /**
   * Given a string of text as input, this will tokenize the string into its component words in a
   * fairly simplistic way, by splitting on whitespace or punctuation. Capitalization is ignored as
   * well. The result will be a map from those words to the integer frequency of their occurrence.
   */
  public static IMap<String, Integer> count(String input) {
    // Note: you should be splitting on whitespace and punctuation. The words that come out should
    // be free of any such things. You're writing a regular expression to match all the things that
    // can come *between* words.

    // https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html

    String regex = "[\\s\\p{Punct}]+";

    IList<String> inputList = LazyList
        .fromArray(input.split(regex))
        .map(String::toLowerCase);

    return TreapMap.fromList(inputList, string -> 1, (a, b) -> a + b);
  }

  /**
   * Given a mapping from strings to integers, such as count() might return, return a list of
   * KeyValue tuples sorted from most frequent to least frequent.
   */
  public static IList<KeyValue<String, Integer>> mostFrequent(IMap<String, Integer> freqMap) {
    return freqMap
        .toList()
        .sort((kv1, kv2) -> {
          int v1 = kv1.getValue();
          int v2 = kv2.getValue();
          return v1 == v2
              ? kv1.getKey().compareTo(kv2.getKey()) < 0
              : v1 > v2;
        });
  }
}
