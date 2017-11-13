/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.week8covariance;

import edu.rice.week8covariance.CarExamples.*;
import org.junit.Test;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;

@ParametersAreNonnullByDefault
@CheckReturnValue
public class CarExamplesTest {
  private final GList<Porsche> carList1 = GList.of(new Porsche911(1971), new Porsche("944",1985));
  private final GList<Corvette> carList2 = GList.of(new Corvette(1985));
  // Exercise: if you declare GList<Chevy> carList3, then you'll get an error in the expression for carList4. Why?
  private final GList<Car> carList3 = GList.of(new Corvette(1977), new Chevy("Citation", 1980));
  private final GList<Car> carList4 = carList3.concat(carList1).concat(carList2);
  private final GList<Car> carList5 = carList4.concat(carList4);

  @Test
  public void testCountChevys() throws Exception {
    //count the number of Chevrolets
    Predicate<Car> isChevy = elem -> elem.make.equals("Chevy");
    assertEquals(0, carList1.filter(isChevy).length());
    assertEquals(1, carList2.filter(isChevy).length());
    assertEquals(2, carList3.filter(isChevy).length());
    assertEquals(3, carList4.filter(isChevy).length());
    assertEquals(6, carList5.filter(isChevy).length());
  }

  @Test
  public void testConvertToPorsche911MechA() {
    //convert any car to a Porsche 911 using Mechanic A
    final GList<Porsche911> porsche911GList1 = carList1.map(CarExamples::mechanicA);
    final GList<Porsche911> porsche911GList2 = carList2.map(CarExamples::mechanicA);
    final GList<Porsche911> porsche911GList3 = carList3.map(CarExamples::mechanicA);
    final GList<Porsche911> porsche911GList4 = carList4.map(CarExamples::mechanicA);
    final GList<Porsche911> porsche911GList5 = carList5.map(CarExamples::mechanicA);
    assertEquals(2, porsche911GList1.length());
    assertEquals(1, porsche911GList2.length());
    assertEquals(2, porsche911GList3.length());
    assertEquals(5, porsche911GList4.length());
    assertEquals(10, porsche911GList5.length());
  }

  @Test
  public void testConvertToPorsche911MechB() {
    //convert any car to a Porsche 911 using Mechanic B (Mechanic A is unavailable)
    final GList<Porsche911> porsche911GList1 = carList1.map(CarExamples::mechanicB);
    final GList<Porsche911> porsche911GList2 =
        carList2.map(elem -> CarExamples.mechanicB(new Porsche(elem.model, elem.year)));
    final GList<Porsche911> porsche911GList3 =
        carList3.map(elem -> CarExamples.mechanicB(new Porsche(elem.model, elem.year)));
    final GList<Porsche911> porsche911GList4 =
        carList4.map(elem -> CarExamples.mechanicB(new Porsche(elem.model, elem.year)));
    final GList<Porsche911> porsche911GList5 =
        carList5.map(elem -> CarExamples.mechanicB(new Porsche(elem.model, elem.year)));
    assertEquals(2, porsche911GList1.length());
    assertEquals(1, porsche911GList2.length());
    assertEquals(2, porsche911GList3.length());
    assertEquals(5, porsche911GList4.length());
    assertEquals(10, porsche911GList5.length());
  }
}
