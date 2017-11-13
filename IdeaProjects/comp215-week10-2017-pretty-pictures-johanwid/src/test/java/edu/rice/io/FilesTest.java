/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.io;

import edu.rice.list.IList;
import edu.rice.list.List;
import edu.rice.util.Log;
import edu.rice.util.Try;
import org.junit.Test;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.file.NoSuchFileException;

import static edu.rice.io.Files.*;
import static org.junit.Assert.*;

@ParametersAreNonnullByDefault
@CheckReturnValue
public class FilesTest {
  @Test
  public void testFiles() throws Exception {
    Try<Void> result1 = write("testData.txt", "Hello, world!");
    Try<Void> result2 = write("testData2.txt", "Hello, Rice!");
    assertTrue(result1.isSuccess());
    assertTrue(result2.isSuccess());

    Try<String> read1 = read("testData.txt");
    Try<String> read2 = read("testData2.txt");

    assertEquals(Try.success("Hello, world!"), read1);
    assertEquals(Try.success("Hello, Rice!"), read2);

    // overwrite the old file: should succeed
    Try<Void> result3 = write("testData.txt", "Hello, hello, hello!");
    Try<String> read3 = read("testData.txt");
    assertTrue(result3.isSuccess());
    assertEquals(Try.success("Hello, hello, hello!"), read3);

    // now, delete the first file
    Try<Void> result4 = remove("testData.txt");
    assertTrue(result4.isSuccess());

    // and try reading it again, which should fail
    Try<String> result5 = read("testData.txt");
    assertTrue(result5.isFailure());

    //noinspection ThrowableResultOfMethodCallIgnored
    assertTrue(result5.getException() instanceof NoSuchFileException);

    // and lastly, try removing both files; the first one should generate an exception since it's not there
    Try<Void> result6 = remove("testData.txt");
    Try<Void> result7 = remove("testData2.txt");

    assertTrue(result6.isFailure());

    //noinspection ThrowableResultOfMethodCallIgnored
    assertTrue(result6.getException() instanceof NoSuchFileException);
    assertTrue(result7.isSuccess());
  }

  @Test
  public void testReadDir() throws Exception {
    // On a Mac or Windows box, we seem to be running at the root of the project.
    // We're going to try creating and destroying a file of the same name.
    // but first we'll create some hello world files and make sure they're there
    Try<Void> result1 = write("testData.txt", "Hello, world!");
    assertTrue(result1.isSuccess());

    IList<String> fileNames = Files.readdir(".").getOrElse(List.makeEmpty());
    fileNames.foreach(x -> System.out.println("Found file: " + x));

    assertTrue(fileNames.contains("./testData.txt"));

    Try<Void> result2 = remove("./testData.txt");
    assertTrue(result2.isSuccess());
  }

  @Test
  public void testReadResourceDir() throws Exception {
    Try<IList<String>> shouldBeEmpty = Files.readResourceDir("emptyDirectory");
    shouldBeEmpty.get().foreach(name -> Log.i("testReadResourceDir", "(empty) found resource: (" + name + ")"));
    assertTrue(shouldBeEmpty.isSuccess());
    assertEquals(0, shouldBeEmpty.get().length());

    Try<IList<String>> testDirectory = Files.readResourceDir("testDirectory");
    assertTrue(testDirectory.isSuccess());
    assertEquals(2, testDirectory.get().length());

    testDirectory.get().foreach(name -> Log.i("testReadResourceDir", "(test) found resource: (" + name + ")"));
  }

  @Test
  public void testReadResourceImagesDir() throws Exception {
    Try<IList<String>> imagesDirectory = Files.readResourceDir("cool-images");
    assertTrue(imagesDirectory.isSuccess());

    IList<String> txtFiles = imagesDirectory.map(list -> list.filter(name -> name.endsWith(".txt"))).getOrElse(List.makeEmpty());
    Log.i("testReadResourceImagesDir", () -> String.format("found %d .txt files: (%s)", txtFiles.length(), txtFiles.join(",")));

    // we'll get backslashes on a Windows box and forward slashes everywhere else
    assertTrue(imagesDirectory.get().contains("cool-images/README.txt"));
  }

  @Test
  public void testResourcesWithSpaces() throws Exception {
    Try<IList<String>> spacesDirectory = Files.readResourceDir("test directory spaces");
    assertTrue(spacesDirectory.isSuccess());

    IList<String> txtFiles = spacesDirectory.map(list -> list.filter(name -> name.endsWith(".txt"))).getOrElse(List.makeEmpty());
    Log.i("testResourcesWithSpaces", () -> String.format("found %d .txt files: (%s)", txtFiles.length(), txtFiles.join(",")));

    // we normalize paths to have forward slashes, even on Windows
    assertTrue(spacesDirectory.get().contains("test directory spaces/spaces in a file.txt"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFilePathValidationUnixAbsolute() throws Exception {
    Try<String> str = Files.read("/etc/passwd");
    assertEquals(null, str); // control flow should never get here
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFilePathValidationWindowsBackslashRelative() throws Exception {
    Try<String> str = Files.read("etc\\passwd");
    assertEquals(null, str); // control flow should never get here
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFilePathValidationWindowsBackslashAbsolute() throws Exception {
    Try<String> str = Files.read("\\etc\\passwd");
    assertEquals(null, str); // control flow should never get here
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFilePathValidationWindowsBackslashDriveAbsolute() throws Exception {
    Try<String> str = Files.read("C:\\Windows\\Whatever");
    assertEquals(null, str); // control flow should never get here
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFilePathValidationURLAbsolute() throws Exception {
    Try<String> str = Files.read("file://etc/passwd");
    assertEquals(null, str); // control flow should never get here
  }
}
