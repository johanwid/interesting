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
import edu.rice.list.KeyValue;
import edu.rice.list.LazyList;
import edu.rice.util.Option;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

/**
 * This is the interface for general-purpose functional mappings from comparable types (keys) to anything else (values).
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public interface IMap<K extends Comparable<? super K>, V> {
  /**
   * Returns a key/value set representation of the Map.
   */
  ISet<KeyValue<K, V>> getSet();

  /**
   * Updates the value, applying the given function from the old value to the new value; if the old
   * value for the given key is absent, the input to the function will be Option.none(). If the
   * function returns Option.none(), then the result will be equivalent to removing the key from
   * the map (as in map.remove(key)).
   */
  default IMap<K, V> update(K key, UnaryOperator<Option<V>> updateFunc) {
    return updateFunc.apply(oget(key)).match(() -> remove(key), newVal -> add(key, newVal));
  }

  /**
   * Gets the value corresponding to a key, if it exists.
   */
  Option<V> oget(K key);

  /**
   * Returns a new map, without the key, if present.
   */
  IMap<K, V> remove(K key);

  /**
   * Returns a new map, adding the additional key/value pair. If the key is already present, the
   * prior value for that key is replaced.
   */
  default IMap<K, V> add(K key, V value) {
    return add(KeyValue.make(key, value));
  }

  /**
   * Returns a new map, adding the additional key/value pair. If the key is already present, the
   * prior value for that key is replaced.
   */
  IMap<K, V> add(KeyValue<K, V> kv);

  /**
   * Returns whether there are any key/value pairs stored in the map or not.
   */
  boolean empty();

  /**
   * Returns the number of key/value pairs present in the map.
   */
  int size();

  /**
   * Returns a new map corresponding to the set-union of the two maps. If the same key exists in
   * both maps, the union map's value will have the result of calling the mergeOp operation on the
   * two original values.
   */
  IMap<K, V> union(IMap<K, V> otherMap, BinaryOperator<V> mergeOp);

  /**
   * Returns a new map corresponding to the set-intersection of the two maps. If the same key exists
   * in both maps, the intersection map's value will have the result of calling the mergeOp
   * operation on the two original values.
   */
  IMap<K, V> intersect(IMap<K, V> otherMap, BinaryOperator<V> mergeOp);

  /**
   * Returns a new map corresponding to the current map with the set-intersection removed. This
   * is also called the set "complement" and is sometimes written "A \ B" or "A - B". This removal
   * only follows the keys, i.e., any key in otherMap will cause its corresponding key in this map
   * to be removed, regardless of the values associated with those keys.
   */
  IMap<K, V> except(IMap<K, V> otherMap);

  /**
   * Returns a new map with all keys greater-than (or equal to) the query key.
   */
  IMap<K, V> greaterThan(K queryKey, boolean inclusive);

  /**
   * Returns a new map with all keys lesser-than (or equal to) the query key.
   */
  IMap<K, V> lessThan(K queryKey, boolean inclusive);

  /**
   * Returns a list that iterates over the key-value pairs, in *sorted* order on the keys.
   */
  IList<KeyValue<K, V>> toSortedList();

  /**
   * Returns a list that iterates over the key-value pairs, in no guaranteed order; may or may not
   * be sorted. Call this one if you might care about performance. Call the other one if you require
   * ordering.
   */
  IList<KeyValue<K, V>> toList();

  /**
   * Returns a list of all keys in the map.
   */
  default IList<K> keys() {
    return toList().map(KeyValue::getKey);
  }

  /**
   * Returns a list of all the values in the map.
   */
  default IList<V> values() {
    return toList().map(KeyValue::getValue);
  }

  /**
   * Adds all the key/value pairs in the list into the map, returning a new map.
   */
  default IMap<K, V> addList(IList<KeyValue<K, V>> pairs) {
    return pairs.foldl(this, (map, elem) -> map.add(elem.getKey(), elem.getValue()));
  }

  /**
   * Adds all the key/value pairs in the list into the map, returning a new map. If the same key
   * occurs more than once, they're merged with mergeOp.
   */
  default IMap<K, V> addListMerge(IList<KeyValue<K, V>> pairs, BinaryOperator<V> mergeOp) {
    return pairs.foldl(this, (map, elem) -> map.merge(elem.getKey(), elem.getValue(), mergeOp));
  }

  /**
   * Returns a new map, adding the additional key/value pair. If the key is already present, the
   * prior value for that key is merged with mergeOp and the new value.
   */
  IMap<K, V> merge(K key, V value, BinaryOperator<V> mergeOp);

  /**
   * Removes all the elements with the given keys in the list from the map, returning a new map.
   */
  default IMap<K, V> removeList(IList<? extends K> keys) {
    return keys.foldl(this, IMap<K, V>::remove);
  }

  /**
   * Sometimes you have an IMap and you want an empty IMap of the *same* concrete type. This method is a nice
   * shorthand that does it for you. Alternatively, you may of course use the static method TreapMap.makeEmpty().
   * @see TreapMap#makeEmpty()
   */
  <K2 extends Comparable<? super K2>, V2> IMap<K2,V2> makeEmptySameType();

  /**
   * This specialied interface provides default methods for empty IMap implementations. External users don't
   * need this. Just use IMap.
   * @see IMap
   */
  interface Empty<K extends Comparable<? super K>, V> extends IMap<K,V> {
    @Override
    default IMap<K, V> remove(K key) {
      return this;
    }

    @Override
    default Option<V> oget(K key) {
      return Option.none();
    }

    @Override
    default boolean empty() {
      return true;
    }

    @Override
    default int size() {
      return 0;
    }

    @Override
    default IMap<K, V> union(IMap<K, V> otherMap, BinaryOperator<V> mergeOp) {
      if (otherMap.empty()) {
        return this; // union of empty & empty -> empty
      } else {
        return otherMap;
      }
    }

    @Override
    default IMap<K, V> intersect(IMap<K, V> otherMap, BinaryOperator<V> mergeOp) {
      return this; // intersect of empty & anything -> empty
    }

    @Override
    default IMap<K, V> except(IMap<K, V> otherMap) {
      return this; // empty except anything -> empty
    }

    @Override
    default IMap<K, V> greaterThan(K query, boolean inclusive) {
      return this;
    }

    @Override
    default IMap<K, V> lessThan(K query, boolean inclusive) {
      return this;
    }

    @Override
    default IList<KeyValue<K, V>> toSortedList() {
      return LazyList.makeEmpty();
    }

    @Override
    default IList<KeyValue<K, V>> toList() {
      return LazyList.makeEmpty();
    }

    @Override
    default IMap<K, V> merge(K key, V value, BinaryOperator<V> mergeOp) {
      return add(key, value);
    }
  }
}
