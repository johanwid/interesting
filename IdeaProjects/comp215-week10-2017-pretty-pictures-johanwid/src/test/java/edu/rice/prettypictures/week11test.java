
package edu.rice.prettypictures;

//tests

import org.junit.Test;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import static org.junit.Assert.*;

@ParametersAreNonnullByDefault
@CheckReturnValue
public class week11test {

  @Test
  public void testConstant() throws Exception {
    argFuncs x = new argFuncs.Constant(1, 1, 1); //white
    argFuncs y = new argFuncs.Constant(-1, -1, -1); //black
    assertEquals(
        new argFuncs.Constant(1, 1, 1).evaluate(1, 1).getRGB(),
        x.evaluate(1, 1).getRGB());
    assertEquals(
        new argFuncs.Constant(-1, -1, -1).evaluate(1, 1).getRGB(),
        y.evaluate(1, 1).getRGB());
  }

  @Test
  public void testNegate() throws Exception {
    argFuncs x = new argFuncs.Negate(new argFuncs.VarX());
    argFuncs y = new argFuncs.Negate(new argFuncs.VarY());
    assertEquals(
        new argFuncs.Negate(new argFuncs.VarX()).evaluate(1, 1).getRGB(),
        x.evaluate(1, 1).getRGB());
    assertEquals(
        new argFuncs.Negate(new argFuncs.VarY()).evaluate(1, 1).getRGB(),
        y.evaluate(1, 1).getRGB());
  }

  @Test
  public void testSine() throws Exception {
    argFuncs x = new argFuncs.Sine(new argFuncs.VarX());
    argFuncs y = new argFuncs.Sine(new argFuncs.VarY());
    assertEquals(
        new argFuncs.Sine(new argFuncs.VarX()).evaluate(1, 1).getRGB(),
        x.evaluate(1, 1).getRGB());
    assertEquals(
        new argFuncs.Sine(new argFuncs.VarY()).evaluate(1, 1).getRGB(),
        y.evaluate(1, 1).getRGB());
  }

  @Test
  public void testCosine() throws Exception {
    argFuncs x = new argFuncs.Cosine(new argFuncs.VarX());
    argFuncs y = new argFuncs.Cosine(new argFuncs.VarY());
    assertEquals(
        new argFuncs.Cosine(new argFuncs.VarX()).evaluate(1, 1).getRGB(),
        x.evaluate(1, 1).getRGB());
    assertEquals(
        new argFuncs.Cosine(new argFuncs.VarY()).evaluate(1, 1).getRGB(),
        y.evaluate(1, 1).getRGB());
  }

  @Test
  public void testArctan() throws Exception {
    argFuncs x = new argFuncs.Arctan(new argFuncs.VarX());
    argFuncs y = new argFuncs.Arctan(new argFuncs.VarY());
    assertEquals(
        new argFuncs.Arctan(new argFuncs.VarX()).evaluate(1, 1).getRGB(),
        x.evaluate(1, 1).getRGB());
    assertEquals(
        new argFuncs.Arctan(new argFuncs.VarY()).evaluate(1, 1).getRGB(),
        y.evaluate(1, 1).getRGB());
  }

  @Test
  public void testRoundDown() throws Exception {
    argFuncs x = new argFuncs.RoundDown(new argFuncs.VarX());
    argFuncs y = new argFuncs.RoundDown(new argFuncs.VarY());
    assertEquals(
        new argFuncs.RoundDown(new argFuncs.VarX()).evaluate(1, 1).getRGB(),
        x.evaluate(1, 1).getRGB());
    assertEquals(
        new argFuncs.RoundDown(new argFuncs.VarY()).evaluate(1, 1).getRGB(),
        y.evaluate(1, 1).getRGB());
  }

  @Test
  public void testRoundUp() throws Exception {
    argFuncs x = new argFuncs.RoundUp(new argFuncs.VarX());
    argFuncs y = new argFuncs.RoundUp(new argFuncs.VarY());
    assertEquals(
        new argFuncs.RoundUp(new argFuncs.VarX()).evaluate(1, 1).getRGB(),
        x.evaluate(1, 1).getRGB());
    assertEquals(
        new argFuncs.RoundUp(new argFuncs.VarY()).evaluate(1, 1).getRGB(),
        y.evaluate(1, 1).getRGB());
  }

  @Test
  public void testExpon() throws Exception {
    argFuncs x = new argFuncs.Expon(new argFuncs.VarX());
    argFuncs y = new argFuncs.Expon(new argFuncs.VarY());
    assertEquals(
        new argFuncs.Expon(new argFuncs.VarX()).evaluate(1, 1).getRGB(),
        x.evaluate(1, 1).getRGB());
    assertEquals(
        new argFuncs.Expon(new argFuncs.VarY()).evaluate(1, 1).getRGB(),
        y.evaluate(1, 1).getRGB());
  }

  @Test
  public void testLogrm() throws Exception {
    argFuncs x = new argFuncs.Logrm(new argFuncs.VarX());
    argFuncs y = new argFuncs.Logrm(new argFuncs.VarY());
    assertEquals(
        new argFuncs.Logrm(new argFuncs.VarX()).evaluate(1, 1).getRGB(),
        x.evaluate(1, 1).getRGB());
    assertEquals(
        new argFuncs.Logrm(new argFuncs.VarY()).evaluate(1, 1).getRGB(),
        y.evaluate(1, 1).getRGB());
  }

  @Test
  public void testAbsVal() throws Exception {
    argFuncs x = new argFuncs.AbsVal(new argFuncs.VarX());
    argFuncs y = new argFuncs.AbsVal(new argFuncs.VarY());
    assertEquals(
        new argFuncs.AbsVal(new argFuncs.VarX()).evaluate(1, 1).getRGB(),
        x.evaluate(1, 1).getRGB());
    assertEquals(
        new argFuncs.AbsVal(new argFuncs.VarY()).evaluate(1, 1).getRGB(),
        y.evaluate(1, 1).getRGB());
  }

  @Test
  public void testClip() throws Exception {
    argFuncs x = new argFuncs.Clip(new argFuncs.VarX());
    argFuncs y = new argFuncs.Clip(new argFuncs.VarY());
    assertEquals(
        new argFuncs.Clip(new argFuncs.VarX()).evaluate(1, 1).getRGB(),
        x.evaluate(1, 1).getRGB());
    assertEquals(
        new argFuncs.Clip(new argFuncs.VarY()).evaluate(1, 1).getRGB(),
        y.evaluate(1, 1).getRGB());
  }

  @Test
  public void testWrap() throws Exception {
    argFuncs x = new argFuncs.Wrap(new argFuncs.VarX());
    argFuncs y = new argFuncs.Wrap(new argFuncs.VarY());
    assertEquals(
        new argFuncs.Wrap(new argFuncs.VarX()).evaluate(1, 1).getRGB(),
        x.evaluate(1, 1).getRGB());
    assertEquals(
        new argFuncs.Wrap(new argFuncs.VarY()).evaluate(1, 1).getRGB(),
        y.evaluate(1, 1).getRGB());
  }

  @Test
  public void testRGBtoYCrCb() throws Exception {
    argFuncs x = new argFuncs.RGBtoYCrCb(new argFuncs.VarX());
    argFuncs y = new argFuncs.RGBtoYCrCb(new argFuncs.VarY());
    assertEquals(
        new argFuncs.RGBtoYCrCb(new argFuncs.VarX()).evaluate(1, 1).getRGB(),
        x.evaluate(1, 1).getRGB());
    assertEquals(
        new argFuncs.RGBtoYCrCb(new argFuncs.VarY()).evaluate(1, 1).getRGB(),
        y.evaluate(1, 1).getRGB());
  }

  @Test
  public void testAdd() throws Exception {
    argFuncs a = new argFuncs.VarX();
    argFuncs b = new argFuncs.VarY();
    argFuncs m = new argFuncs.Add(a, a);
    argFuncs n = new argFuncs.Add(b, b);
    argFuncs x = new argFuncs.Add(new argFuncs.VarX(), new argFuncs.VarX());
    argFuncs y = new argFuncs.Add(new argFuncs.VarY(), new argFuncs.VarY());
    assertEquals(
        m.evaluate(1, 1).getRGB(),
        x.evaluate(1, 1).getRGB());
    assertEquals(
        n.evaluate(1, 1).getRGB(),
        y.evaluate(1, 1).getRGB());
  }

  @Test
  public void testSubtract() throws Exception {
    argFuncs a = new argFuncs.VarX();
    argFuncs b = new argFuncs.VarY();
    argFuncs m = new argFuncs.Subtract(a, a);
    argFuncs n = new argFuncs.Subtract(b, b);
    argFuncs x = new argFuncs.Subtract(new argFuncs.VarX(), new argFuncs.VarX());
    argFuncs y = new argFuncs.Subtract(new argFuncs.VarY(), new argFuncs.VarY());
    assertEquals(
        m.evaluate(1, 1).getRGB(),
        x.evaluate(1, 1).getRGB());
    assertEquals(
        n.evaluate(1, 1).getRGB(),
        y.evaluate(1, 1).getRGB());
  }

  @Test
  public void testMultiply() throws Exception {
    argFuncs a = new argFuncs.VarX();
    argFuncs b = new argFuncs.VarY();
    argFuncs m = new argFuncs.Multiply(a, a);
    argFuncs n = new argFuncs.Multiply(b, b);
    argFuncs x = new argFuncs.Multiply(new argFuncs.VarX(), new argFuncs.VarX());
    argFuncs y = new argFuncs.Multiply(new argFuncs.VarY(), new argFuncs.VarY());
    assertEquals(
        m.evaluate(1, 1).getRGB(),
        x.evaluate(1, 1).getRGB());
    assertEquals(
        n.evaluate(1, 1).getRGB(),
        y.evaluate(1, 1).getRGB());
  }

  @Test
  public void testDivide() throws Exception {
    argFuncs a = new argFuncs.VarX();
    argFuncs b = new argFuncs.VarY();
    argFuncs m = new argFuncs.Divide(a, a);
    argFuncs n = new argFuncs.Divide(b, b);
    argFuncs x = new argFuncs.Divide(new argFuncs.VarX(), new argFuncs.VarX());
    argFuncs y = new argFuncs.Divide(new argFuncs.VarY(), new argFuncs.VarY());
    assertEquals(
        m.evaluate(1, 1).getRGB(),
        x.evaluate(1, 1).getRGB());
    assertEquals(
        n.evaluate(1, 1).getRGB(),
        y.evaluate(1, 1).getRGB());
  }

  @Test
  public void testDotProduct() throws Exception {
    argFuncs a = new argFuncs.VarX();
    argFuncs b = new argFuncs.VarY();
    argFuncs m = new argFuncs.DotProduct(a, a);
    argFuncs n = new argFuncs.DotProduct(b, b);
    argFuncs x = new argFuncs.DotProduct(new argFuncs.VarX(), new argFuncs.VarX());
    argFuncs y = new argFuncs.DotProduct(new argFuncs.VarY(), new argFuncs.VarY());
    assertEquals(
        m.evaluate(1, 1).getRGB(),
        x.evaluate(1, 1).getRGB());
    assertEquals(
        n.evaluate(1, 1).getRGB(),
        y.evaluate(1, 1).getRGB());
  }

  @Test
  public void testGSPerlin() throws Exception {
    argFuncs a = new argFuncs.VarX();
    argFuncs b = new argFuncs.VarY();
    argFuncs m = new argFuncs.GSPerlin(a, a);
    argFuncs n = new argFuncs.GSPerlin(b, b);
    argFuncs x = new argFuncs.GSPerlin(new argFuncs.VarX(), new argFuncs.VarX());
    argFuncs y = new argFuncs.GSPerlin(new argFuncs.VarY(), new argFuncs.VarY());
    assertEquals(
        m.evaluate(1, 1).getRGB(),
        x.evaluate(1, 1).getRGB());
    assertEquals(
        n.evaluate(1, 1).getRGB(),
        y.evaluate(1, 1).getRGB());
  }

  @Test
  public void testPerlin() throws Exception {
    argFuncs a = new argFuncs.VarX();
    argFuncs b = new argFuncs.VarY();
    argFuncs m = new argFuncs.Perlin(a, a);
    argFuncs n = new argFuncs.Perlin(b, b);
    argFuncs x = new argFuncs.Perlin(new argFuncs.VarX(), new argFuncs.VarX());
    argFuncs y = new argFuncs.Perlin(new argFuncs.VarY(), new argFuncs.VarY());
    assertEquals(
        m.evaluate(1, 1).getRGB(),
        x.evaluate(1, 1).getRGB());
    assertEquals(
        n.evaluate(1, 1).getRGB(),
        y.evaluate(1, 1).getRGB());
  }

  @Test
  public void testDissolve() throws Exception {
    argFuncs a = new argFuncs.VarX();
    argFuncs b = new argFuncs.VarY();
    argFuncs m = new argFuncs.Dissolve(a, a, a);
    argFuncs n = new argFuncs.Dissolve(b, b, b);
    argFuncs x = new argFuncs.Dissolve(new argFuncs.VarX(), new argFuncs.VarX(),
        new argFuncs.VarX());
    argFuncs y = new argFuncs.Dissolve(new argFuncs.VarY(), new argFuncs.VarY(),
        new argFuncs.VarY());
    assertEquals(
        m.evaluate(1, 1).getRGB(),
        x.evaluate(1, 1).getRGB());
    assertEquals(
        n.evaluate(1, 1).getRGB(),
        y.evaluate(1, 1).getRGB());
  }
}