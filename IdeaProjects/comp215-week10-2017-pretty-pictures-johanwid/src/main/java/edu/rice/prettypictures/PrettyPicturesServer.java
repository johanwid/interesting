/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.prettypictures;

import edu.rice.image.Images;
import edu.rice.util.Log;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;

import java.awt.image.BufferedImage;
import java.nio.Buffer;

import static edu.rice.util.Performance.nanoBenchmarkVal;
import static edu.rice.util.Strings.stringToTryInteger;
import static edu.rice.web.Utils.*;
import static spark.Spark.*;

/**
 * Web server for Pretty Pictures. "Run" this, and it will launch your browser with the full JavaScript
 * breeding client.
 */

@ParametersAreNonnullByDefault
@CheckReturnValue
public class PrettyPicturesServer {
  private static final String TAG = "PrettyPicturesServer";

  /**
   * Main entry point for the PrettyPictures web server. Args are ignored.
   */
  public static void main(String[] args) {
    staticFileLocation("/WebPublic/");
    jsonSparkExceptionHandler(TAG);
    setupDefaultHandlers();

    // Perform setup here, if necessary.

    /* ********************************************
     * * Write these handlers for the first half. *
     * ********************************************/

    /*
     * POST /test/
     * This handler is used to load the standard test generation.
     * Return a JSON response where "response" is keyed to a JObject with three key/values:
     * * numGenerations, the total number of generations
     * * currentGeneration, the number of the generation to display
     * * numImages, the number of images per generation
     */
    post("/test/", (request, response) -> {
      logSparkRequest(TAG, request);

      // HINT: If we got here, that means the user/tester pressed the [ ? ] button.

      // This is when you should set up some state that you'll be using to handle the
      // subsequent image requests. Build whatever data structure you're going to have to
      // represent your generations, then return the specific JSON structure defined above,
      // which you'll be returning in many instances.

      Log.e(TAG, "/test/ route not implemented yet");
      return "";
    });

    /*
     * GET /image/gen/:gen/img/:img/height/:height/width/:width/
     * This handler is used to request a specific image from a specific generation.
     * Return image number :img from generation number :gen as a :width by :height png.
     *
     * Hint: Remember that Images.imageToPng returns a Try<byte[]>, and so you will
     *   need to get() the byte[] out of the Try<>. Remember also to set the response
     *   type to "image/png".
     *
     * For part 1, you only need to support "generation 0". For part 2, you will be supporting
     * crossbreeding and thus multiple generations.
     */

//    get("/image/gen/:gen/img/:img/height/:height/width/:width/", (request, response) -> {
//
//      final int widthParam = stringToTryInteger(request.params().get(":width")).getOrElse(1);
//      final int heightParam = stringToTryInteger(request.params().get(":height")).getOrElse(1);
//
//      final int width = (widthParam < 1) ? 1 : widthParam;
//      final int height = (heightParam < 1) ? 1 : heightParam;
//
//      // making the squares
//
//
//
//      return nanoBenchmarkVal(
//          () -> {
//
//          })
//          .match((time, result) -> {
//            Log.i(TAG, String.format("checkers: made %dx%d image in %.3fms", width, height, 1e-6 * time));
//            response.type("image/png");
//            return result;
//          });
//    });

    /* *********************************************
     * * Write these handlers for the second half. *
     * *********************************************/

    /*
     * GET /string/gen/:gen/img/:img/
     * This handler is used to print the internal structure of your image functions.
     * Return a string representation of image number :image from generation number :gen.
     */

    /*
     * GET /client-init/
     * This handler is used to initialize the breeder client with information about the server.
     * Return a JSON response as in POST /test/.
     */


    /*
     * POST /reset/:count/
     * This handler is used to reset the server to a random first generation.
     * Reset the stored generations to a new, randomly generated first generation with :count images.
     * Return a JSON response as in POST /test/.
     */

    /*
     * POST /breed/oldgen/:oldgen/img/*
     * This handler is used to breed a new generation from a previous one.
     * Create a new generation bred from generation :olggen using the images in *.
     * Return a JSON response as in POST /test/.
     */

    launchBrowser("http://localhost:4567/prettyPictures/");
  }

  /**
   * You shouldn't need to worry about these handlers. When our JavaScript, running in the browser,
   * wants to log something, it will call these handlers, so the log data shows up in your Java log.
   */
  private static void setupDefaultHandlers() {
    post("/log/i/", (request, response) -> {
      String msg = request.queryParams("msg");
      Log.i("Client", msg);
      return "Information logged.";
    });

    post("/log/e/", (request, response) -> {
      String msg = request.queryParams("msg");
      Log.e("Client", msg);
      return "Error logged.";
    });

    get("/", (request, response) -> {
      response.redirect("/prettyPictures/", 301); // passes the request to the handlers below
      return "Redirecting to PrettyPictures...";
    });

    get("/prettyPictures/", (request, response) -> {
      response.redirect("/prettyPicturesBreeder.html", 301); // you can find this in /WebPublic
      return "Redirecting to breeder client...";
    });
  }
}
