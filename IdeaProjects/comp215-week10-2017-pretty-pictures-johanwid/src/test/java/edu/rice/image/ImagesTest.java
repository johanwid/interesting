/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.image;

import edu.rice.util.Try;
import org.junit.Test;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.image.BufferedImage;

import static org.junit.Assert.*;

@ParametersAreNonnullByDefault
@CheckReturnValue
public class ImagesTest {

  @Test
  public void testReadFile() throws Exception {
    Try<BufferedImage> tImage = Images.readResource("rice-logo.png");
    assertTrue(tImage.isSuccess());
    BufferedImage image = tImage.get();
    assertEquals(449, image.getWidth());
    assertEquals(276, image.getHeight());
    assertEquals(0xffffff, image.getRGB(0, 0) & 0xffffff); // masking out alpha; we just want to compare R,G,B
  }

  @Test
  public void testImageToPng() throws Exception {
    Try<BufferedImage> tImage = Images.readResource("orange-square.png");
    assertTrue(tImage.isSuccess());
    BufferedImage referenceOrangeSquare = tImage.get();
    assertEquals(200, referenceOrangeSquare.getWidth());
    assertEquals(200, referenceOrangeSquare.getHeight());

    // red=255, green=80,blue=0, also important to mask out the alpha
    assertEquals(0xff8000, referenceOrangeSquare.getRGB(0, 0) & 0xffffff);

    BufferedImage orangeSquare = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
    for (int y = 0; y < 200; y++) {
      for (int x = 0; x < 200; x++) {
        orangeSquare.setRGB(x, y, 0xff8000);
      }
    }

    Try<byte[]> pngOrangeTest = Images.imageToPng(orangeSquare);
    assertTrue(pngOrangeTest.isSuccess());
    Try<byte[]> pngReferenceOrangeTest = Images.imageToPng(referenceOrangeSquare);
    assertTrue(pngReferenceOrangeTest.isSuccess());

    assertArrayEquals(pngReferenceOrangeTest.get(), pngOrangeTest.get());
  }
}