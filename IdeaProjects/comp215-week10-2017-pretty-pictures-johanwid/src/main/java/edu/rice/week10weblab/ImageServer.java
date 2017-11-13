/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.week10weblab;

import edu.rice.image.Images;
import edu.rice.util.Log;
import edu.rice.util.Try;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.image.BufferedImage;

import static edu.rice.util.Performance.nanoBenchmarkVal;
import static edu.rice.util.Strings.stringToTryInteger;
import static edu.rice.web.Utils.jsonSparkExceptionHandler;
import static edu.rice.web.Utils.launchBrowser;
import static spark.Spark.get;
import static spark.Spark.staticFileLocation;

/**
 * Week 12 lab, to make you learn a bit about image processing and Spark's version of structured
 * pattern matching.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public class ImageServer {
  private static final String TAG = "ImageServer";

  /**
   * Main entry function; arguments are ignored.
   */
  public static void main(String[] args) {
    staticFileLocation("/WebPublic");
    jsonSparkExceptionHandler(TAG); // set up an exception handler
    launchBrowser("http://localhost:4567/week10images.html");

    get("/", (request, response) -> {
      response.redirect("/week10images.html", 301); // you can find this file in /WebPublic
      return "Next page!";
    });

    get("/color-rectangle/imgsize/:width/:height/color/:rgb", (request, response) -> {
//      logSparkRequest(TAG, request);

      // You may optionally want to turn on the logSparkRequest call, commented out above, to see
      // the full input we're getting from the client web page.

      // Engineering note: the Spark web server guarantees us that we'll have width, height, and color
      // parameters in the URL, otherwise we would never get here. However, it makes no guarantees
      // that those numbers are well-formed integers, greater than zero, etc. If a negative number
      // shows up, Spark won't care, but we need to properly handle the error. We're using stringToTryInteger
      // to do make the attempt, but it might have a success() or failure() within. We don't especially
      // care *why* it might have failed, so in those cases we'll just reach for tolerable defaults.

      final int widthParam = stringToTryInteger(request.params().get(":width")).getOrElse(1);
      final int heightParam = stringToTryInteger(request.params().get(":height")).getOrElse(1);
      final int rgb = stringToTryInteger(request.params().get(":rgb"), 16).getOrElse(0);

      final int width = (widthParam < 1) ? 1 : widthParam;
      final int height = (heightParam < 1) ? 1 : heightParam;

      return nanoBenchmarkVal(
          () -> {
            // Your task: create a BufferedImage of the correct size, set the pixels to the correct values,
            // then use the imageToPng() function to generate the necessary bytes and return them from
            // this lambda. (Those bytes will show up as the "result" in the lambda below, which are
            // ultimately returned to the web browser.)

            //throw new RuntimeException("color-rectangle not implemented yet");
            BufferedImage image =
                new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            for (int y = 0; y < height; y++) {

              for (int x = 0; x < width; x++) {
                image.setRGB(x, y, rgb);
              }
            }
            return Images.imageToPng(image).get();
          })
          .match((time, result) -> {
            Log.i(TAG, String.format("color-rectangle: made %dx%d image in %.3fms", width, height, 1e-6 * time));
            response.type("image/png");
            return result;
          });
    });

    get("/checkers/imgsize/:width/:height/boxsize/:boxsize/color1/:rgb1/color2/:rgb2", (request, response) -> {
//      logSparkRequest(TAG, request);

      // You may optionally want to turn on the logSparkRequest call, commented out above, to see
      // the full input we're getting from the client web page.

      final int widthParam = stringToTryInteger(request.params().get(":width")).getOrElse(1);
      final int heightParam = stringToTryInteger(request.params().get(":height")).getOrElse(1);
      final int boxsizeParam = stringToTryInteger(request.params().get(":boxsize")).getOrElse(1);
      final int rgb1 = stringToTryInteger(request.params().get(":rgb1"), 16).getOrElse(0);
      final int rgb2 = stringToTryInteger(request.params().get(":rgb2"), 16).getOrElse(0);

      final int width = (widthParam < 1) ? 1 : widthParam;
      final int height = (heightParam < 1) ? 1 : heightParam;
      final int boxsize = (boxsizeParam < 1) ? 1 : boxsizeParam;

      return nanoBenchmarkVal(
          () -> {
            // Your task: as before, create a BufferedImage of the correct size, etc.

            // throw new RuntimeException("checkers not implemented yet");
            BufferedImage image =
                new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            for (int y = 0; y < height; y++) {
              for (int x = 0; x < width; x++) {
                if (x / boxsize % 2 == y / boxsize % 2) {
                  image.setRGB(x, y, rgb1);
                } else {
                  image.setRGB(x, y, rgb2);
                }
              }
            }
            return Images.imageToPng(image).get();
          })
          .match((time, result) -> {
            Log.i(TAG, String.format("checkers: made %dx%d image in %.3fms", width, height, 1e-6 * time));
            response.type("image/png");
            return result;
          });
    });

    get("/rice-logo/imgsize/:width/:height/", (request, response) -> {
//      logSparkRequest(TAG, request);

      // You may optionally want to turn on the logSparkRequest call, commented out above, to see
      // the full input we're getting from the client web page.

      final int widthParam = stringToTryInteger(request.params().get(":width")).getOrElse(1);
      final int heightParam = stringToTryInteger(request.params().get(":height")).getOrElse(1);

      final int width = (widthParam < 1) ? 1 : widthParam;
      final int height = (heightParam < 1) ? 1 : heightParam;

      Try<BufferedImage> tryRiceLogo = Images.readResource("rice-logo.png");
      if (tryRiceLogo.isFailure()) {
        response.status(404);
        return "rice-logo.png not found!";
      }

      BufferedImage riceLogo = tryRiceLogo.get();
      int riceLogoWidth = riceLogo.getWidth();
      int riceLogoHeight = riceLogo.getHeight();

      return nanoBenchmarkVal(
          () -> {
            // Your task: create a BufferedImage of the correct size, etc. Use modular arithmetic such that your
            // queries to riceLogo "wrap around" when you're asking for something beyond the original pixel size
            // of the logo.

            throw new RuntimeException("rice-logo not implemented yet");
          })
          .match((time, result) -> {
            Log.i(TAG, String.format("rice-logo: made %dx%d image in %.3fms", width, height, 1e-6 * time));
            response.type("image/png");
            return result;
          });
    });
  }
}
