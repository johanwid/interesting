/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.tree;

import edu.rice.list.LazyList;
import edu.rice.util.Option;
import edu.rice.util.Pair;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static edu.rice.tree.Tree.Helpers.equalsHelper;
import static edu.rice.tree.Tree.Helpers.hashCodeHelper;
import static edu.rice.tree.Tree.Helpers.toStringHelper;
import static edu.rice.util.Option.some;
import static edu.rice.util.Strings.objectToEscapedString;

/**
 * General-purpose unbalanced binary tree, parameterized over any comparable type.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public interface Tree<T extends Comparable<? super T>> extends ITree<T> {

  /**
   * Returns an empty tree of the given type parameter.
   * @param <T> any comparable type
   */
  static <T extends Comparable<? super T>> ITree<T> makeEmpty() {
    @SuppressWarnings("unchecked")
    ITree<T> typedEmpty = (ITree<T>) Empty.SINGLETON;
    return typedEmpty;

  }

  /**
   * Given a bunch of values passed as varargs to this function, returns a tree with those values.
   */
  @SuppressWarnings("varargs")
  @SafeVarargs
  static <T extends Comparable<T>> ITree<T> of(@Nullable T... values) {
    return Tree.<T>makeEmpty().insertList(LazyList.fromArray(values));
  }

  /**
   * Helper functions that we don't want to make public. These are only visible within the edu.rice.tree package.
   */
  class Helpers {
    private Helpers() { } // do not instantiate!

    /**
     * This is the internal function that we'll use to convert all trees to strings. This will work over all ITree
     * implementations.
     */
    static <T extends Comparable<? super T>> String toStringHelper(ITree<T> tree) {
      return tree.match(
          emptyTree -> "Tree()",

          (elem, leftTree, rightTree) -> {
            // if there's a priority (i.e., if we're dealing with a treap) then we want to include it
            int priority = tree.getPriority();
            String priorityString = (priority != Integer.MAX_VALUE) ? String.format("priority=%d, ", priority) : "";

            // special handling for leaf nodes, so they're easier to read
            if (leftTree.empty() && rightTree.empty()) {
              return String.format("Tree(%s%s)", priorityString, objectToEscapedString(elem));
            } else {
              return String.format("Tree(%s%s, %s, %s)",
                  priorityString,
                  toStringHelper(leftTree),
                  objectToEscapedString(elem), toStringHelper(rightTree));
            }
          });
    }

    /**
     * Deep structural equality of the trees. If you want set-equality, then convert to a list first.
     */
    @SuppressWarnings("unchecked")
    static <T extends Comparable<? super T>> boolean equalsHelper(ITree<T> tree, ITree<?> otherTree) {
      // Note: we're suppressing unchecked type-cast warnings because the places where these warnings are being
      // generated in this method occur when we're only calling methods that care about the base Object type.
      // No runtime errors are possible here.
      return tree.match(
          treeEmpty -> otherTree.empty(),
          (treeVal, treeLeft, treeRight) ->
              otherTree.match(
                  otherEmpty -> false,
                  (otherVal, otherLeft, otherRight) ->
                      treeVal.equals(otherVal) && treeLeft.equals(otherLeft) && treeRight.equals(otherRight)));
    }

    /**
     * Computing hashes over a tree.
     */
    static <T extends Comparable<? super T>> int hashCodeHelper(ITree<T> tree) {
      return tree.match(
          emptyTree -> 1,
          (elem, leftTree, rightTree) ->
              elem.hashCode() * 71 + leftTree.hashCode() * 31 + rightTree.hashCode()); // a kludge, but it's something
    }
  }

  class Node<T extends Comparable<? super T>> implements ITree<T> {
    private final ITree<T> left;
    private final ITree<T> right;
    private final T value;

    // external tree users: don't use this; instead, insert to an empty tree
    private Node(T value, ITree<T> left, ITree<T> right) {
      this.left = left;
      this.right = right;
      this.value = value;
    }

    @Override
    public T getValue() {
      return value;
    }

    /**
     * Gets the left subtree.
     */
    @Override
    public ITree<T> getLeft() {
      return left;
    }

    @Override
    public ITree<T> getRight() {
      return right;
    }

    @Override
    public boolean empty() {
      return false;
    }

    @Override
    public ITree<T> insert(T newbie) {
      int comparison = newbie.compareTo(value);
      if (comparison < 0) {
        return new Node<>(value, left.insert(newbie), right);
      }
      if (comparison > 0) {
        return new Node<>(value, left, right.insert(newbie));
      }

      // If the newbie is exactly the same as what's there, then we don't need to update anything.
      if (this.value == newbie) {
        return this;
      }

      // This is a curious case. If we're equal (this.value.equals(newbie)), but not the same (this.value != newbie),
      // then we're going to update the value in place. This will be useful for key/value stores where the
      // equals method operates on the keys.
      return new Node<>(newbie, left, right);
    }

    @Override
    public int hashCode() {
      return hashCodeHelper(this);
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof ITree<?>)) {
        return false;
      }
      ITree<?> otherTree = (ITree<?>) o;

      return equalsHelper(this, otherTree);
    }

    ITree<T> rotateLeft() {
      return right.match(
          emptyTree -> this,
          (rValue, rLeft, rRight) -> new Node<>(rValue, new Node<>(value, left, rLeft), rRight));
    }

    ITree<T> rotateRight() {
      return left.match(
          emptyTree -> this,
          (lValue, lLeft, lRight) -> new Node<>(lValue,
              lLeft,
              new Node<>(value, lRight, right)));
    }

    @Override
    public String toString() {
      return toStringHelper(this);
    }

    @Override
    public ITree<T> greaterThan(T floor, boolean inclusive) {
      int comparison = floor.compareTo(value);
      if (comparison == 0 && !inclusive) {
        return right;
      }
      if (comparison == 0) {
        return new Node<>(value, makeEmpty(), right);
      }

      // if the floor is entirely to the right
      if (comparison > 0) {
        return right.greaterThan(floor, inclusive);
      }

      // the floor is somewhere to the left
      return new Node<>(value, left.greaterThan(floor, inclusive), right);
    }

    @Override
    public ITree<T> lessThan(T ceiling, boolean inclusive) {
      int comparison = ceiling.compareTo(value);
      if (comparison == 0 && !inclusive) {
        return left;
      }
      if (comparison == 0) {
        return new Node<>(value, left, makeEmpty());
      }

      // if the ceiling is entirely to the left
      if (comparison < 0) {
        return left.lessThan(ceiling, inclusive);
      }

      // the ceiling is somewhere to the right
      return new Node<>(value, left, right.lessThan(ceiling, inclusive));
    }

    @Override
    public ITree<T> remove(T deadValue) {
      int comparison = deadValue.compareTo(this.value);
      if (comparison == 0) {
        // we need to remove the tree head; first see if we have an easy out
        if (left.empty()) {
          return right;
        }
        if (right.empty()) {
          return left;
        }

        // we could arbitrarily decide about rotating right or left at this point
        // (for treaps, it matters; here it doesn't)
        return rotateRight().remove(deadValue);
      } else if (comparison < 0) {
        // it's to the left
        return new Node<>(value, left.remove(deadValue), right);
      } else {
        // it's to the right
        return new Node<>(value, left, right.remove(deadValue));
      }
    }

    @Override
    public boolean valid() {
      // it's a start; doesn't validate that *every* left-val is less than the current-val, but
      // at least recursively checks basic tree properties
      return (left.empty() || left.getValue().compareTo(value) < 0) &&
          (right.empty() || value.compareTo(right.getValue()) < 0) &&
          left.valid() &&
          right.valid();
    }

    @Override
    public Option<Pair<T, ITree<T>>> removeMin() {
      // Recursively, Node.removeMin() will never return an Option.none(),
      // since, by definition, it's in a non-empty tree. The empty-tree case
      // is handled in ITreeEmpty.

      return left.match(
          // if there are no left-subchildren, then we've found the minimum value,
          // and the tree without the minimum value is just the right subtree
          emptyTree -> some(new Pair<>(value, right)),

          // the recursive removeMin() cannot be empty; we're just going to fetch the value from the Option
          (lValue, lLeft, lRight) ->
              left.removeMin().get().match((minValue, remainingTree) ->
                  some(new Pair<>(minValue, new Node<>(value, remainingTree, right)))));
    }
  }

  /**
   * This class implements the case where a Tree might be empty. External users will never
   * use Tree.Empty directly but will instead use the public interface (ITree).
   */
  class Empty<T extends Comparable<? super T>> implements Tree<T>, ITree.Empty<T> {
    private static final ITree<?> SINGLETON = new Tree.Empty<>();

    // external user: don't call this; instead use makeEmpty()
    private Empty() { }

    @Override
    public ITree<T> insert(T value) {
      return new Node<>(value, this, this);
    }

    @Override
    public String toString() {
      return toStringHelper(this);
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof ITree<?>)) {
        return false;
      }
      ITree<?> otherTree = (ITree<?>) o;

      // Note: we're suppressing unchecked type-cast warnings because the places where these warnings are being
      // generated in this method occur when we're only calling methods that care about the base Object type.
      // No runtime errors are possible here.

      //noinspection unchecked
      return otherTree.empty();
    }

    @Override
    public int hashCode() {
      return hashCodeHelper(this);
    }
  }
}
