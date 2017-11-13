/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.week9lab;

import edu.rice.util.Option;
import edu.rice.week8covariance.GList;

import org.junit.Test;

import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.Assert.*;
import static edu.rice.week8covariance.CarExamples.*;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@CheckReturnValue
public class Week9LabTest {
  private final GList<Porsche> carList1 = GList.of(new Porsche911(1971), new Porsche("944",1985));
  private final GList<Corvette> carList2 = GList.of(new Corvette(1985));
  private final GList<Car> carList3 = GList.of(new Corvette(1977), new Chevy("Citation", 1980));
  private final GList<Car> carList4 = carList3.concat(carList1).concat(carList2);
  private final GList<Car> carList5 = carList4.concat(carList4);


  /**
   * Find all cars from a list sharing the build year with a given car.
   *
   * @param list a list of cars
   * @param car the car whose model year we are looking for
   * @return the model of the first car found in the list with the same year as car, "" if nothing is found.
   */
  public static String findSameYear(GList<? extends Car> list, Car car) {
    return list.empty() ?
            ""
            : list.head().year == car.year ?
            list.head().model
            : findSameYear(list.tail(), car);
  }

  /**
   * Find the first element in a list satisfying a predicate test and then apply the given function to it, returning
   * an optional result, depending on whether the list entry was found.
   *
   * @param list : a generic list of things
   * @param f    : a function to apply on the found element
   * @param test : a predicate that returns true if the element is found
   * @return an Option.some() of the result of applying function f to the found element, if an element is found,
   *     Option.none() otherwise
   */
  public static <T,R> Option<R> findAndApply(GList<T> list, Function<? super T, R> f, Predicate<? super T> test) {
    return list.empty() ?
            Option.none()
            : test.test(list.head()) ?
            Option.some(f.apply(list.head()))
            : findAndApply(list.tail(), f, test);
  }

  @Test
  public void testFindSameYear() throws Exception {
    Car car = new Car("Chevy", "Caprice", 1971);

    assertEquals("911", findSameYear(carList1, car));
    assertEquals("", findSameYear(carList2, car));

    Porsche911 porsche = new Porsche911(1980);
    assertEquals("Citation", findSameYear(carList5, porsche));
  }

  @Test
  public void testFindAndApply() throws Exception {
    Predicate<Car> isModernChevy = elem -> elem.make.equals("Chevy") && elem.year >= 1980;
    Function<Car, String> model = elem -> elem.model;
    Car car = new Car("Chevy", "Caprice", 1980);

    assertEquals(Option.none(), findAndApply(carList1, model, isModernChevy));
    assertEquals("Corvette", findAndApply(carList2, model, isModernChevy).get());
    assertEquals("Citation", findAndApply(carList3, model, isModernChevy).get());
    assertEquals("Citation", findAndApply(carList4, model, isModernChevy).get());
    assertEquals("Citation", findAndApply(carList5, model, isModernChevy).get());

    Porsche porsche = new Porsche("928", 1988);
    assertEquals("911", findAndApply(carList5, x -> x.model, x -> x.make.equals(porsche.make)).get());

    assertEquals(Integer.valueOf(1985), findAndApply(carList1, x -> x.year, x -> x.model.equals("944")).get());
    assertEquals(Integer.valueOf(1985), findAndApply(carList5, x -> x.year, x -> x.model.equals("944")).get());

    Function<Porsche, Porsche911> to911 = elem -> new Porsche911(elem.year);
    assertEquals("Porsche", findAndApply(carList1, to911, x -> x.model.equals("944")).get().make);
  }
}