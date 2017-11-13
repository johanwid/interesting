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

import edu.rice.list.IList;
import edu.rice.list.LazyList;
import edu.rice.list.List;
import edu.rice.util.Option;
import edu.rice.util.Pair;
import edu.rice.util.TriConsumer;
import edu.rice.util.TriFunction;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;

import static edu.rice.util.Option.none;
import static edu.rice.util.Option.some;

@ParametersAreNonnullByDefault
@CheckReturnValue
public interface ITree<T extends Comparable<? super T>> {
  /**
   * Returns the number of elements in the tree.
   */
  default int size() {
    return match(
      emptyTree -> 0,
      (elem, leftTree, rightTree) -> leftTree.size()  + rightTree.size() + 1);
  }

  /**
   * Returns the value at the root of the tree.
   */
  T getValue();

  /**
   * Returns the left subtree.
   */
  ITree<T> getLeft();

  /**
   * Returns the right subtree.
   */
  ITree<T> getRight();

  /**
   * Returns a new tree equal to the current tree with the new element inserted into it. If the new
   * value is already present, it replaces the old value.
   */
  ITree<T> insert(T newbie);

  /**
   * General-purpose structural pattern matching with deconstruction on a tree.
   *
   * @param emptyFunc
   *     called if the node is empty
   * @param nonEmptyFunc
   *     called if the node has a value within
   * @param <Q>
   *     the return type of either emptyFunc or nonEmptyFunc
   * @return returns the value of whichever function matches
   */
  default <Q> Q match(Function<? super ITree<T>, ? extends Q> emptyFunc,
                      TriFunction<? super T, ? super ITree<T>, ? super ITree<T>, ? extends Q> nonEmptyFunc) {
    if (empty()) {
      return emptyFunc.apply(this);
    } else {
      return nonEmptyFunc.apply(this.getValue(), this.getLeft(), this.getRight());
    }
  }

  /**
   * General-purpose structural pattern matching with deconstruction on a tree, except
   * with no return values.
   *
   * @param emptyFunc
   *     called if the node is empty
   * @param nonEmptyFunc
   *     called if the node has a value within
   */
  default void consume(Consumer<? super ITree<T>> emptyFunc,
                       TriConsumer<? super T, ? super ITree<T>, ? super ITree<T>> nonEmptyFunc) {
    if (empty()) {
      emptyFunc.accept(this);
    } else {
      nonEmptyFunc.accept(this.getValue(), this.getLeft(), this.getRight());
    }
  }

  /**
   * Visits the tree, in-order, running the consumer on each element.
   */
  default void inorder(Consumer<T> consumer) {
    consume(
        emptyTree -> { },
        (val, left, right) -> {
          left.inorder(consumer);
          consumer.accept(val);
          right.inorder(consumer);
        });
  }

  /**
   * Returns whether or not the current tree is empty.
   */
  boolean empty();

  /**
   * Finds something that's equal to the query and returns it, if present.
   */
  default Option<T> find(T query) {
    return match(
        emptyTree -> none(),

        (elem, leftTree, rightTree) -> {
          int comparison = query.compareTo(elem);
          if (comparison < 0) {
            return leftTree.find(query);
          }
          if (comparison > 0) {
            return rightTree.find(query);
          }
          return some(elem);
        });
  }

  /**
   * Returns a new tree with all elements greater than the floor value, either inclusive or
   * exclusive.
   */
  ITree<T> greaterThan(T floor, boolean inclusive);

  /**
   * Returns a new tree with all elements lesser than the ceiling value, either inclusive or
   * exclusive.
   */
  ITree<T> lessThan(T ceiling, boolean inclusive);

  /**
   * Returns a new tree equivalent to the original, but absent the value if it's present.
   */
  ITree<T> remove(T value);

  /**
   * Returns a new tree equivalent to the original, without its minimum value; also returns the
   * minimum value. The result is optional because the tree might be empty.
   */
  Option<Pair<T, ITree<T>>> removeMin();

  /**
   * Returns the priority, if it's a treap, otherwise max-int (grumble: this seems like an ugly
   * thing to have in the interface, but the alternative is to do a bunch of typecasting inside
   * Treap, along with the remote possibility of bug-induced runtime crashes).
   */
  default int getPriority() {
    return Integer.MAX_VALUE;
  }

  /**
   * Validates that the tree is well-formed, returns true if it's all good (generally for testing
   * purposes).
   */
  boolean valid();

  /**
   * Returns the maximum depth of the tree.
   */
  default int maxDepth() {
    return match(
        emptyTree -> 0,
        (elem, leftTree, rightTree) -> Integer.max(leftTree.maxDepth(), rightTree.maxDepth()) + 1);
  }

  /**
   * Inserts all the elements in the list into the tree, returning a new tree.
   */
  default ITree<T> insertList(IList<? extends T> values) {
    // Note the clever use of foldl here, which maps (tree,val)->tree, accumulating all of the
    // values in the input list, one by one, starting from the current state of the list (this).
    // Also, since foldl() has been optimized to avoid tail calls, this method will operate
    // efficiently, even if the list is really large.

    // IntelliJ thinks you can remove the type argument, but if you try, it will then start
    // complaining about it. Sigh.
    //noinspection RedundantTypeArguments
    return values.foldl(this, ITree<T>::insert);
  }

  /**
   * Removes all the elements in the list from the tree, returning a new tree.
   */
  default ITree<T> removeList(IList<? extends T> values) {
    // IntelliJ thinks you can remove the type argument, but if you try, it will then start
    // complaining about it. Sigh.
    //noinspection RedundantTypeArguments
    return values.foldl(this, ITree<T>::remove);
  }

  /**
   * Lazily constructs a list, in-order, from the underlying tree. If you only want a few elements
   * from the tree, this will be significantly faster than the eager version.
   * @see ITree#toList()
   */
  default IList<T> toLazyList() {
    return match(
        emptyTree -> LazyList.makeEmpty(),
        (elem, leftTree, rightTree) ->
            LazyList.lazyConcat(leftTree.toLazyList(), () -> LazyList.make(elem, rightTree::toLazyList)));
  }

  /**
   * Eagerly constructs a list, in-order, from the underlying tree.
   */
  default IList<T> toList() {
    return match(
        emptyTree -> List.makeEmpty(),
        (elem, leftTree, rightTree) -> leftTree.toList().concat(rightTree.toList().add(elem)));
  }

  /**
   * Empty trees have a lot of code in common, so we can put that all here.
   */
  interface Empty<T extends Comparable<? super T>> extends ITree<T> {
    @Override
    default void inorder(Consumer<T> consumer) { }

    @Override
    default int size() {
      return 0;
    }

    @Override
    default T getValue() {
      throw new NoSuchElementException("getValue() not defined on an empty tree");
    }

    @Override
    default ITree<T> getLeft() {
      return this;
    }

    @Override
    default ITree<T> getRight() {
      return this;
    }

    @Override
    default boolean empty() {
      return true;
    }

    @Override
    default ITree<T> greaterThan(T floor, boolean inclusive) {
      return this;
    }

    @Override
    default ITree<T> lessThan(T ceiling, boolean inclusive) {
      return this;
    }

    @Override
    default ITree<T> remove(T value) {
      return this;
    }

    @Override
    default Option<Pair<T, ITree<T>>> removeMin() {
      return none();
    }

    @Override
    default boolean valid() {
      return true;
    }
  }
}
