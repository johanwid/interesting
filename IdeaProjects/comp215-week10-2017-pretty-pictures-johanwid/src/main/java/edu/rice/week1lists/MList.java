/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.week1lists;

/**
 * Mutating list variant that internally uses an ObjectList.
 * @see ObjectList
 */
public class MList {
  private ObjectList list = ObjectList.makeEmpty(); // initially empty

  public void add(Object o) {
    list = list.add(o);
  }

  public boolean contains(Object o) {
    return list.contains(o);
  }

  public boolean empty() {
    return list.empty();
  }

  /**
   * Removes the head element from the list and returns it.
   */
  public Object getHead() {
    Object headVal = list.head(); // will throw an exception if the list is empty
    list = list.tail();
    return headVal;
  }
}
