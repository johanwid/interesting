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

import edu.rice.io.Files;
import edu.rice.util.Try;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.Imaging;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.image.BufferedImage;


/**
 * A front-end to make it easy to read and write image files.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public class Images {
  private static final String TAG = "Images";

  private Images() {
  } // this class should never be instantiated

  /**
   * Given the name of a resource, typically a file in the "resources" directory, containing some sort of image in any
   * format that Apache Commons Imaging knows how to load (JPEG, PNG, whatever), this function returns a standard
   * BufferedImage object, suitable for further processing.
   *
   * @param resourceName
   *     path to the file
   * @return a Try.success of the image, or a Try.failure indicating what went wrong
   */
  public static Try<BufferedImage> readResource(String resourceName) {
    return Files.readResourceBytes(resourceName).flatmap(Images::readBytes);
  }


  /**
   * Given the location of a file on disk, containing some sort of image in any format that Apache Commons Imaging knows
   * how to load (JPEG, PNG, whatever), this function returns a standard BufferedImage object, suitable for further
   * processing.
   *
   * @param filePath
   *     path to the file
   * @return a Try.success of the image, or a Try.failure indicating what went wrong
   */
  public static Try<BufferedImage> readFile(String filePath) {
    return Files.readBytes(filePath).flatmap(Images::readBytes);
  }

  /**
   * Given an array of raw byte data, containing any sort of image that Apache Commons Imaging knows how to load (JPEG,
   * PNG, whatever), this function returns a standard BufferedImage object, suitable for further processing.
   *
   * @param data
   *     raw byte array of input
   * @return a Try.success of the image, or a Try.failure indicating what went wrong
   */
  public static Try<BufferedImage> readBytes(byte[] data) {
    return Try.of(() -> Imaging.getBufferedImage(data))
        .logIfFailure(TAG, err -> String.format("failed to read image from data (%d bytes)", data.length));
  }

  /**
   * Given the path to write a file and a BufferedImage, generate a PNG representation of that image and write it to the
   * given file.
   *
   * @return an empty Try.success if everything goes well, or a Try.failure indicating what went wrong
   */
  public static Try<Void> writePngFile(String filePath, BufferedImage image) {
    return imageToPng(image).flatmap(imageBytes -> Files.writeBytes(filePath, imageBytes));
  }

  /**
   * Given a BufferedImage, convert it to PNG format.
   *
   * @return a Try.success of the raw PNG bytes, or a Try.failure indicating what went wrong
   */
  public static Try<byte[]> imageToPng(BufferedImage image) {
    return Try.of(() -> Imaging.writeImageToBytes(image, ImageFormats.PNG, null))
        .logIfFailure(TAG, err -> "failed to convert image to bytes");
  }
}
