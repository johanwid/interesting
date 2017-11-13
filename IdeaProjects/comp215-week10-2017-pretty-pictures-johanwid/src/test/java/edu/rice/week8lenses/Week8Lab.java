/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.week8lenses;

import edu.rice.lens.Lens;

import org.junit.Test;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;

import static edu.rice.lens.Lens.lens;

import static org.junit.Assert.assertEquals;

@ParametersAreNonnullByDefault
@CheckReturnValue
public class Week8Lab {

  public static class Street {
    private final String name;

    public Street(String name) {
      this.name = name;
    }

    public static final Lens<Street, String> Name = lens(
        street -> street.name,
        (street, name) -> new Street(name));
  }

  public static class Address {
    private final Street street;
    private final Integer number;

    public Address(Street street, Integer number) {
      this.street = street;
      this.number = number;
    }

    public static final Lens<Address,Street> Street = lens(
        address -> address.street,
        (address, street) -> new Address(street, address.number));

    public static final Lens<Address,Integer> Number = lens(
        address -> address.number,
        (address, number) -> new Address(address.street, number));

  }

  public static class Company {
    private final String name;
    private final Address address;

    public Company(String name, Address address) {
      this.name = name;
      this.address = address;
    }

    public static final Lens<Company, String> Name = lens(
        company -> company.name,
        (company, name) -> new Company(name, company.address));

    public static final Lens<Company, Address>  Address = lens(
        company -> company.address,
        (company,address) -> new Company(company.name, address ));

  }

  /**
   * A simple class to hold employee info.
   */
  public static class Employee {
    private final String name;
    private final Integer age;
    private final Company company;

    /**
     * Constructor.
     */
    public Employee(String name, Integer age, Company company) {
      this.name = name;
      this.age = age;
      this.company = company;
    }

    public static final Lens<Employee, String> Name = lens(
        employee -> employee.name,
        (employee, name) -> new Employee(name, employee.age, employee.company));

    public static final Lens<Employee, Integer>  Age = lens(
        employee -> employee.age,
        (employee,age) -> new Employee(employee.name, age, employee.company));

    public static final Lens<Employee, Company>  Company = lens(
        employee -> employee.company,
        (employee,company) -> new Employee(employee.name, employee.age, company));

  }

  /**
   * Unit tests to exercise {@link Employee}.
   */
  @Test
  public void testLenses() {

    // Make some companies and employees
    final Company rice = new Company("Rice", new Address(new Street("Main"), 6100));
    final Company uh = new Company("University of Houston", new Address(new Street("Calhoun"), 4800));
    final Employee zoran = new Employee("Zoran", 47, rice);
    final Employee dan = new Employee("Dan", 46, rice);

    // Lenses composition
    final Lens<Company, String> companyStreetName  = Company.Address
        .andThen(Address.Street)
        .andThen(Street.Name);

    final Lens<Employee, String> streetName       = Employee.Company
        .andThen(companyStreetName);

    final Lens<Employee, Integer> streetNumber       = Employee.Company
        .andThen(Company.Address)
        .andThen(Address.Number);

    final Lens<Employee, String> companyName       = Employee.Company
        .andThen(Company.Name);


    // First, check that the getter methods work on lenses
    assertEquals("Rice", Company.Name.get(rice));
    assertEquals("Main", streetName.get(dan));
    assertEquals(Integer.valueOf(47), Employee.Age.get(zoran));
    assertEquals(Integer.valueOf(6100), streetNumber.get(zoran));

    // Make some new employees by making changes to the existing ones
    final Employee danClone =  companyName.set(streetName.set(dan, "Fannin"), "BCM");
    final Employee zoranClone = Employee.Company.set(zoran, uh);
    final Company riceClone = Company.Address.set(rice,Company.Address.get(uh));

    // Check that the new employees and companies have the correct data
    assertEquals("Dan", Employee.Name.get(danClone));
    assertEquals("Fannin", streetName.get(danClone));
    assertEquals(Integer.valueOf(6100), streetNumber.get(danClone));
    assertEquals("BCM", companyName.get(danClone));
    assertEquals(Integer.valueOf(47), Employee.Age.get(zoranClone));
    assertEquals(Integer.valueOf(4800), streetNumber.get(zoranClone));
    assertEquals("Calhoun", streetName.get(zoranClone));
    assertEquals("University of Houston", companyName.get(zoranClone));
    assertEquals("Calhoun", companyStreetName.get(riceClone));
  }
}
