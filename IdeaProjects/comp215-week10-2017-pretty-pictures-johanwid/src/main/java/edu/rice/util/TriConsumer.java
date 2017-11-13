/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.util;

/**
 * This should have been included in java.util.function. It's basically the same
 * as BiConsumer, except for functions of three arguments rather than just two.
 * @see java.util.function.BiConsumer
 */
@FunctionalInterface
public interface TriConsumer<T1,T2,T3> {
  /**
   * Applies this function to the given arguments.
   *
   * @param t1 the first function argument
   * @param t2 the second function argument
   * @param t3 the third function argument
   */
  void accept(T1 t1, T2 t2, T3 t3);
}
