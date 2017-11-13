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

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import edu.rice.list.IList;
import edu.rice.list.LazyList;
import edu.rice.list.List;
import edu.rice.util.Log;
import edu.rice.util.Option;
import edu.rice.util.Pair;
import edu.rice.util.Try;
import spark.utils.IOUtils;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import static edu.rice.util.Strings.regexToPredicate;
import static edu.rice.util.Strings.stringToUTF8;

/**
 * This class contains simple alternatives to all the crazy ways of reading/writing files and resources in Java.
 * Unlike the official Java ways of doing file IO, these static methods never throw an exception. Instead, they
 * use Try, which either has the successful value inside (much like an {@link edu.rice.util.Option#some(Object)}
 * or has an exception within (akin to {@link Option#none()}, but with the exception). This structure keeps your
 * code cleaner than with classical Java try/catch blocks.
 *
 * <p>Note that Comp215 has a few rules about files to make sure that your code runs correctly when your grader
 * is checking your work. In particular:
 * <ul>
 *   <li>If you're naming files in a subdirectory, you shall use a forward-slash as the separator. No backslashes!</li>
 *   <li>No absolute paths, i.e., nothing that starts with a slash on Unix/Mac and nothing that starts with C: on Windows.</li>
 *   <li>No URL-style paths, i.e., nothing that starts with file:</li>
 * </ul>
 *
 * <p>Instead, all paths will be <i>relative</i>. When you run your program, the starting directory will be the
 * top of your IntelliJ project, which is a perfectly reasonable place to read or write state files for
 * projects where that's appropriate like the PrettyPictures project. On the other hand, if you want a
 * read-only file, that should go into the <i>resources</i> directory in <i>src/main/resources</i> or
 * <i>src/test/resources</i>, and you can then read those files using {@link #readResource(String)},
 * {@link #readResourceBytes(String)}, or {@link #readResourceDir(String)}.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public class Files {
  private static final String TAG = "Files";

  private Files() { } // this class should never be instantiated

  private static Try<InputStream> resourceToStream(String resourceName) {
    validatePath(resourceName);
    // If ClassLoader.getSystemResourceAsStream finds nothing, it returns null, which Try.ofNullable
    // will translate to a Try.failure with a NullPointerException inside
    return Try.ofNullable(ClassLoader.getSystemResourceAsStream(resourceName))
      .logIfFailure(TAG, err -> "getSystemResources failed for resource(" + resourceName + ")");
  }

  /**
   * Given a resource name, which typically maps to a file in the "resources" directory, read it in and return a
   * String. This method assumes that the resource file is encoded as a UTF-8 string.
   * If you want to get raw bytes rather than a string, use {@link #readResourceBytes(String)} instead.
   *
   * @return a Try.success of the file contents as a String, or a Try.failure indicating what went wrong
   */
  public static Try<String> readResource(String resourceName) {
    return readResourceBytes(resourceName).map(bytes -> new String(bytes, StandardCharsets.UTF_8));
  }

  /**
   * Given a resource name, which typically maps to a file in the "resources" directory, read it in and return an
   * array of bytes. If you want the result as a String rather than an array of raw bytes, use
   * {@link #readResource(String)} instead.
   *
   * @return a Try.success of the file contents as a byte array, or a Try.failure indicating what went wrong
   */
  public static Try<byte[]> readResourceBytes(String resourceName) {
    return resourceToStream(resourceName)
        .flatmap(stream ->
            Try.of(() -> IOUtils.toByteArray(stream)) // handy helper function from Apache Commons
                .andThen(stream::close)); // no matter what, we need to close the stream
  }

  /**
   * Given a directory path into the resources, returns a list of resource names suitable for then passing
   * to {@link #readResource(String)}, {@link #resourceToStream(String)}, etc.
   *
   * @return a Try.success of the list of resource names, or a Try.failure indicating what went wrong
   */
  public static Try<IList<String>> readResourceDir(String dirPath) {
    validatePath(dirPath);

    return Try.of(() -> LazyList.fromEnumeration(ClassLoader.getSystemResources(dirPath)))
        .logIfFailure(TAG, err -> "getSystemResources failed for path(" + dirPath + ")")

        // Engineering note: Map vs. flatmap? ClassLoader.getSystemResources gives us a *list* of URLs
        // (for example, one directory from the main resources and another from the test resources).
        // The first map() just unpacks the IList<URL> from within the Try. The flatmap() after
        // that operates on the IList, where we map each URL to a list of files available at that URL.
        // For that reason, you'll notice that we eat errors below here, since we might succeed for
        // one URL and we might fail for another. We just want to return a list of the successful
        // files. Errors will be logged and we'll just continue moving onward.

        .map(dirUrls -> dirUrls.flatmap(dirUrl -> {
          final String rawUrlPath = dirUrl.getPath();
//          Log.i(TAG, () -> "rawUrlPath " + rawUrlPath);

          switch (dirUrl.getProtocol()) {
            case "file":

              // On Windows, we get URL paths like file:/C:/Users/dwallach/....
              // On Macs, we get URL paths like file:/Users/dwallach/...

              // With those Windows URLs, getPath() will give us /C:/Users/... which doesn't
              // work when we try to actually open the files. The solution? Match a regular
              // expression and then remove the leading slash.

              final String urlPath = (rawUrlPath.matches("^/\\p{Upper}:/.*$"))
                      ? rawUrlPath.substring(1)
                      : rawUrlPath;

              // if the URLDecoder fails, for whatever reason, we'll just go with the original undecoded path
              final Path decodedPath = Paths.get(Try.of(() ->
                  URLDecoder.decode(urlPath, "UTF-8")).getOrElse(urlPath));

              // Engineering note: Path and Paths are classes that come to us from the java.nio.files
              // package. We're using these, rather than directly messing with the paths by hand because
              // some brave Oracle engineer spent countless hours debugging and testing those classes
              // on each platform. We don't want to deal with all the weird rules of file paths, slashes
              // vs. backslashes, etc. Notably, the various functions that we might use to read a
              // directory have the habit of giving us absolute paths from the root; Path.relativize()
              // is exactly the way to get back to a nice relative path again, which is desirable when
              // printing / debugging your code. It's also desirable because we're enforcing a bunch of
              // rules about relative paths vs. absolute paths to ensure that student code runs correctly
              // on grader machines. (See below.)

              // In the 2015 and 2016 versions of Comp215, we did all this work by manipulating the
              // path strings directly, and of course it mostly worked but not always. For the 2017 version,
              // we're now doing it "properly".

              return readdirPath(decodedPath.toString())
                  .getOrElse(List.makeEmpty())
                  .map(decodedPath::relativize) // gets us to just the names of the files in the given directory
                  .map(Path::toString) // which we'll convert back to strings
                  .map(path -> dirPath + "/" + path); // and add the directory path back on again

            case "jar":
              // Solution adapted from here:
              // http://www.uofr.net/~greg/java/get-resource-listing.html

              String jarPath = rawUrlPath.substring(5, rawUrlPath.indexOf("!")); //strip out only the JAR file

              try {
                // This code is somewhat likely to work, but could be slow for huge JAR files.
                // Testing & optimization would be necessary, but Comp215 isn't going to
                // use Jar files for its resources, so we'll leave this as "good enough" for now.
                return LazyList.fromEnumeration(
                    new JarFile(URLDecoder.decode(jarPath, "UTF-8")).entries())
                    .map(ZipEntry::getName)
                    .filter(name -> name.startsWith(dirPath));

              } catch (IOException exception) {
                Log.e(TAG, "trouble reading " + dirUrl + ", ignoring and marching onward", exception);
                return LazyList.makeEmpty();
              }

            default:
              Log.e(TAG, "unknown protocol in " + dirUrl);
              return LazyList.makeEmpty();
          }
        }));
  }

  /**
   * Given a relative filename, read it in and return a String. This method
   * assumes that the file is encoded as a UTF-8 string. If you want the result as an array of raw
   * bytes rather than a String, use {@link #readBytes(String)} instead.
   *
   * <p>The starting directory used for this is the root of your IntelliJ project.
   * For Comp215, this is all that you'll ever need. If you try to use an absolute file path,
   * or a path using backslashes (which only works on Windows, while forward slashes work everywhere)
   * you'll get an exception. We do this because we want to make sure that your code will run
   * just as well when it's being graded.
   *
   * @return a Try.success of the file contents as a String, or a Try.failure indicating what went wrong
   * @throws IllegalArgumentException if the file path is an absolute path or uses backslashes
   */
  public static Try<String> read(String filePath) {
    return readBytes(filePath).map(bytes -> new String(bytes, StandardCharsets.UTF_8));
  }

  /**
   * Given a filename, read it in and return the contents as an array of bytes. If you want the result
   * as a String, use {@link #read(String)} instead.
   *
   * <p>The starting directory used for this is the root of your IntelliJ project.
   * For Comp215, this is all that you'll ever need. If you try to use an absolute file path,
   * or a path using backslashes (which only works on Windows, while forward slashes work everywhere)
   * you'll get an exception. We do this because we want to make sure that your code will run
   * just as well when it's being graded.
   *
   * @return a Try.success of the file contents as an array of bytes, or a Try.failure indicating what went wrong
   * @throws IllegalArgumentException if the file path is an absolute path or uses backslashes
   */
  public static Try<byte[]> readBytes(String filePath) {
    validatePath(filePath);
    return Try.of(() -> java.nio.file.Files.readAllBytes(Paths.get(filePath)))
        .logIfFailure(TAG, ex -> "failed to read file(" + filePath + ")");
  }

  /**
   * The given string of data is written to a file of the requested name, all at once.
   *
   * <p>The starting directory used for this is the root of your IntelliJ project.
   * For Comp215, this is all that you'll ever need. If you try to use an absolute file path,
   * or a path using backslashes (which only works on Windows, while forward slashes work everywhere)
   * you'll get an exception. We do this because we want to make sure that your code will run
   * just as well when it's being graded.
   *
   * @return an empty Try.success if everything goes well, or a Try.failure indicating what went wrong
   * @throws IllegalArgumentException if the file path is an absolute path or uses backslashes
   */
  @CanIgnoreReturnValue
  public static Try<Void> write(String filePath, String data) {
    // We're going to use Strings.stringToUTF8 to convert 'data' to an array of bytes. This is *highly*
    // unlikely to fail, but if it does, we'd get back an empty-string. Oh well.

    return writeBytes(filePath, stringToUTF8(data));
  }

  /**
   * The given byte-array of data is written to a file of the requested name, all at once.
   *
   * <p>The starting directory used for this is the root of your IntelliJ project.
   * For Comp215, this is all that you'll ever need. If you try to use an absolute file path,
   * or a path using backslashes (which only works on Windows, while forward slashes work everywhere)
   * you'll get an exception. We do this because we want to make sure that your code will run
   * just as well when it's being graded.
   *
   * @return an empty Try.success if everything goes well, or a Try.failure indicating what went wrong
   * @throws IllegalArgumentException if the file path is an absolute path or uses backslashes
   */
  @CanIgnoreReturnValue
  public static Try<Void> writeBytes(String filePath, byte[] rawData) {
    validatePath(filePath);
    return Try.of(() ->
        java.nio.file.Files.write(Paths.get(filePath),
            rawData,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING))
        .logIfFailure(TAG, ex -> "failed to write file(" + filePath + ")")
        .toTryVoid(); // the write call returns a Path, but we don't care about it
  }

  /**
   * If the file is present, it's removed.
   *
   * <p>The starting directory used for this is the root of your IntelliJ project.
   * For Comp215, this is all that you'll ever need. If you try to use an absolute file path,
   * or a path using backslashes (which only works on Windows, while forward slashes work everywhere)
   * you'll get an exception. We do this because we want to make sure that your code will run
   * just as well when it's being graded.
   *
   * @return an empty Try.success if everything goes well, or a Try.failure indicating what went wrong
   * @throws IllegalArgumentException if the file path is an absolute path or uses backslashes
   */
  @CanIgnoreReturnValue
  public static Try<Void> remove(String filePath) {
    validatePath(filePath);
    return Try.ofRunnable(() -> java.nio.file.Files.delete(Paths.get(filePath)))
        .logIfFailure(TAG, ex -> "failed to remove file(" + filePath + ")");
  }

  private static Try<IList<Path>> readdirPath(String filePath) {
    return Try.of(() -> java.nio.file.Files.newDirectoryStream(Paths.get(filePath)))
        .logIfFailure(TAG, ex -> "failed to read directory(" + filePath + ")")
        .map(dirs -> List.fromIterator(dirs.iterator()));
  }

  /**
   * Given a relative directory path, this returns a list of all files (or subdirectories) in that directory,
   * excluding "." and "..". If nothing is actually there, then an empty list will be returned.
   *
   * <p>The starting directory used for this is the root of your IntelliJ project.
   * For Comp215, this is all that you'll ever need. If you try to use an absolute file path,
   * or a path using backslashes (which only works on Windows, while forward slashes work everywhere)
   * you'll get an exception. We do this because we want to make sure that your code will run
   * just as well when it's being graded.
   *
   * @return a Try.success of the list of resource names, or a Try.failure indicating what went wrong
   * @throws IllegalArgumentException if the file path is an absolute path or uses backslashes
   */
  public static Try<IList<String>> readdir(String filePath) {
    validatePath(filePath);
    Path rootPath = Paths.get(filePath);

    return readdirPath(filePath)
        .map(list -> list
            .map(rootPath::relativize) // gets us to just the names of the files in the given directory
            .map(Path::toString) // which we'll convert back to strings
            .map(path -> filePath + "/" + path)); // and add the directory path back on again
  }

  // Engineering note: We stated a bunch of rules for what sorts of filenames / paths are appropriate
  // for Comp215 (see the Javadoc at the top of this file). Here's how we *enforce* those rules. We
  // state several regular expressions on filenames / paths and have a series of error messages that
  // go along with them. We convert those error messages to exceptions, and we'll throw the appropriate
  // exception if the path matches, causing the student's code to fail.

  // All of this effort is here to catch the one or two students who might otherwise have code that
  // "works for me" but doesn't work for the graders.

  // In the real world, these specific constraints wouldn't necessarily be what you want, but then any
  // real world program is still going to have a limited set of places that it wants to read and
  // write to the filesystem, and those places are indeed going to be slightly different on Windows
  // vs. Mac vs. whatever else, so it's entirely appropriate to define rules and enforce them.

  private static final IList<Pair<Predicate<String>, RuntimeException>> pathPatterns =
      List.of(
          new Pair<>("\\\\", "backslashes are not allowed in file paths"),
          new Pair<>("^\\p{Alpha}:", "Windows-style absolute paths are not allowed"),
          new Pair<>("^/", "Unix-style absolute paths are not allowed"),
          new Pair<>("^\\p{Alpha}+:", "URI-style paths are not allowed"))
          .map(pair -> new Pair<>(
              regexToPredicate(pair.a),
              new IllegalArgumentException(pair.b + " (in Comp215, anyway)")));

  private static void validatePath(String path) {
    pathPatterns.foreach(pair -> {
      if (pair.a.test(path)) {
        throw pair.b;
      }
    });
  }
}
