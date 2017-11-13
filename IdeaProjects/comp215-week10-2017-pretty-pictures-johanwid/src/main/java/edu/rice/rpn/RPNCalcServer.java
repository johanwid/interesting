/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.rpn;

import edu.rice.util.Log;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;

import static j2html.TagCreator.html;
import static edu.rice.json.Builders.*;
import static edu.rice.web.Utils.*;
import static spark.Spark.*;

/**
 * Web server for your RPN Calculator. "Run" this, then point your browser at
 * localhost:4567/rpncalc/
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public class RPNCalcServer {
  private static final String TAG = "RPNCalcServer";

  /**
   * Main method to initialize the web server; args are ignored.
   */
  public static void main(String[] args) {
    Log.i(TAG, "Starting!");
    RPNCalculator rpnCalculator = new RPNCalculator();

    staticFileLocation("/WebPublic");
    jsonSparkExceptionHandler(TAG); // set up an exception handler
    launchBrowser("http://localhost:4567/rpncalc/"); // help users find our server

    // This route handles the requests from our JavaScript client, running inside the web browser.
    get("/rpnserver/", (request, response) -> {
      logSparkRequest(TAG, request);

      String commandLine = request.queryParams("input");

      if (commandLine != null) {
        response.status(200); // okay!
        response.header("cache-control", "no-cache"); // because we're regenerating it every time
        return jobject(jpair("response", "<b>&gt; " + commandLine + "</b><br/>" + rpnCalculator.calc(commandLine))).toString();
      }

      // if we got here, the command line we wanted was absent
      Log.i(TAG, "empty command line");
      response.status(400); // bad request
      return jobject().toString(); // empty JSON object
    });

    // This route handles the request for the web page that will run on the client, which will in turn
    // make requests to us using the /rpnserver route, above.
    get("/rpncalc/", (request, response) ->
        html().with(
            muicssHeader("Comp215 RPN Calculator!", "/rpncalc.js", "/commandline.css"),
            muicssCommandLineBody("Comp215 RPN Calculator!")));
  }
}
