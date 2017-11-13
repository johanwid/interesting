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

import org.junit.Test;

import static edu.rice.week8covariance.CarsAndLenses.*;
import static org.junit.Assert.assertEquals;

public class CarsAndLensesTest {
  AstonMartin rapide = new AstonMartin("Rapide", 2017);
  BondDB5 bondCar = new BondDB5(5);

  AstonMartin newerRapide = AstonMartin.Year.set(rapide, 2018);
  AstonMartin rapideS = AstonMartin.Model.set(rapide, "Rapide S");
  AstonMartin updatedBondCar = AstonMartinDB5.Year.set(bondCar, 2018);
  AstonMartinDB5 updatedBondCar2 = AstonMartinDB5.Year.set(bondCar, 2018);
//  BondDB5 updatedBondCar3 = AstonMartinDB5.Year.set(bondCar, 2018); // type error, doesn't compile

  @Test
  public void bondDB5Test() throws Exception {
    // note that when we use a lens for AstonMartinDB5 and run it on a BondDB5, it doesn't
    // know anything about the specialness of the Bond model, so what we get back doesn't
    // know how to shoot anything.
    assertEquals("Click.", updatedBondCar.fireMachineGuns());
    assertEquals("Click.", updatedBondCar2.fireMachineGuns());

    // But the original Bond car has bullets
    assertEquals("Bang!", bondCar.fireMachineGuns());
    assertEquals("Bang!", bondCar.fireMachineGuns());
    assertEquals("Bang!", bondCar.fireMachineGuns());
    assertEquals("Bang!", bondCar.fireMachineGuns());
    assertEquals("Bang!", bondCar.fireMachineGuns());
    assertEquals("Click.", bondCar.fireMachineGuns());

    assertEquals((Integer) 0, BondDB5.Bullets.get(bondCar));

    // And, while we're at it, notice how the broader lenses work just fine even on a very specific car
    assertEquals("Aston Martin", Car.Make.get(bondCar));
  }
}