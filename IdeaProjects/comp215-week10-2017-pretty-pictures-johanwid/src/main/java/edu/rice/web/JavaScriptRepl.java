/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.web;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import edu.rice.util.Log;
import edu.rice.util.Option;
import edu.rice.util.Try;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import static edu.rice.json.Builders.jobject;
import static edu.rice.json.Builders.jpair;
import static edu.rice.web.Utils.*;
import static j2html.TagCreator.*;
import static spark.Spark.get;
import static spark.Spark.staticFileLocation;

/**
 * A read-eval-print loop for Nashorn (the JavaScript environment built into Java8) which
 * runs in a web page.
 *
 * <p>This is cool because you can see all of the Java classes that you've created and
 * use them interactively. Java9 will support a REPL for the Java language, itself, but
 * for our purposes JavaScript is still quite useful.
 *
 * <p>Note that there are important security issues here, since JavaScript
 * exposes commands that would be particularly undesirable if an intruder was able to
 * send commands, over the Internet, directly into this running web server. As such,
 * we use a random number as part of our initialization and launch the browser with that
 * number as part of the URL. This keeps anybody else from being able to get in.
 */
@ParametersAreNonnullByDefault
@CanIgnoreReturnValue
public class JavaScriptRepl {
  private static final String TAG = "JavaScriptRepl";

  /**
   * The accessKey represents a session key. This key is stored in the browser in a hidden form field
   * and returned to the server as part of the JSON structure. That's how we reject requests that might
   * arrive from elsewhere than the browser that we want.
   */
  private static final String accessKey = bigRandom(80);

  /**
   * The launchCode represents a single-use key, which we feed to the browser as part of the initial
   * URL when we launch the REPL. If the launchCode matches up, then the browser is sent an HTML
   * page that includes the accessKey, which will then be used for subsequent transactions.
   *
   * <p>One of the weird limitations of how Java launches a browser is that all we get is a URL.
   * There's no way to set any other browser state. No cookies. No nothing. So that's why we're
   * doing this whole launchCode thing and making it single-use. Unfortunately, it means that
   * bookmarking the URL will totally fail.
   */
  private static final String launchCode = bigRandom(20);

  /**
   * This boolean tracks whether we've consumed the launchCode.
   */
  private static boolean launchCodeUsed = false;

  /**
   * Launch a JavaScript REPL, living in the same Java virtual machine as whatever else you're doing,
   * so it can call any public static method, perhaps useful for inspecting state.
   */
  public static void launch() {
    Log.i(TAG, "Starting!");

    // Create a Nashorn JavaScript instance
    ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

    // Initialize the SparkJava web server
    staticFileLocation("/WebPublic");
    jsonSparkExceptionHandler(TAG);
    launchBrowser("http://localhost:4567/jsrepl/" + launchCode);

    // This handles the requests that we field from the web page, evaluating JavaScript strings and returning
    // the results to be displayed.
    get("/jseval/", (request, response) -> {
      logSparkRequest(TAG, request);

      // Engineering notes: SparkJava returns null rather than using Options to indicate missing parameters.
      // We could have used a series of if (whatever == null) statements, but maybe we'd end up forgetting
      // a case and we'd have a weird bug, and weird bugs here could translate to security vulnerabilities.
      // By converting these maybe-null values into Option-values, we can use our pattern matching machinery
      // to ensure that we don't forget a case.

      Option<String> oCommandLine = Option.ofNullable(request.queryParams("input"));
      Option<String> oFoundKey = Option.ofNullable(request.queryParams("key"));
      Log.i(TAG, "commandLine: " + oCommandLine);

      response.header("cache-control", "no-cache"); // because we're regenerating it every time

      return oCommandLine.match(
          () -> {
            Log.e(TAG, "empty command line");
            return jobject(jpair("response", "empty command line")).toString();
          },

          commandLine -> oFoundKey.match(
              () -> {
                Log.e(TAG, "absent access key, permission denied");
                return jobject(jpair("response", "absent access key, permission denied")).toString();
              },
              foundKey -> {
                // We're only going to evaluate the statement if we got the access key, because security matters.

                if (accessKey.equals(foundKey)) {
                  // Engineering notes: we're constructing the JSON response, which includes evaluating the commandLine
                  // and dealing with any errors that might have occurred, which will manifest as a ScriptException
                  // (on syntax errors and the like). Annoyingly, if a JavaScript expression evaluates to null,
                  // then the eval() statement below returns Java's null, which our Try class will reject as an
                  // invalid input, leading us to the failure case. So fine, we deal with that explicitly.

                  final String jsEvalTxt = Try.of(() -> engine.eval(commandLine))
                      .match(exception -> (exception instanceof NullPointerException)
                              ? "null"
                              : "Error: " + exception.getMessage(),
                          result -> result).toString();

                  // Notice how we're building the response with j2html rather than just concatenating a bunch
                  // of raw HTML text together? This guarantees that weird output won't accidentally end up
                  // running in the client-side JavaScript interpreter. This is a good security practice.

                  final String responseTxt = p()
                      .with(b("> " + commandLine))
                      .with(br())
                      .withText(jsEvalTxt)
                      .render();

                  // We could have avoided these intermediate strings and just mashed everything together into
                  // a single expression, but it's actually nice to deal with each stage separately. First we
                  // deal with calling the JavaScript interpreter. Next we deal with constructing the HTML
                  // response. Lastly, we package that into a JSON message. Three stages, three values.

                  return jobject(jpair("response", responseTxt)).toString();
                } else {
                  Log.e(TAG, "incorrect access key, permission denied");
                  return jobject(jpair("response", "incorrect access key, permission denied")).toString();
                }
              }));
    });

    // This generates the web page front-end for our JavaScript REPL.
    get("/jsrepl/:launchcode", (request, response) -> {
      Option<String> oLaunchCode = Option.ofNullable(request.params("launchcode"));

      return oLaunchCode.match(
          () -> {
            Log.e(TAG, "absent launch code, permission denied");
            return html().withText("absent launch code, permission denied");
          },
          foundLaunchCode -> {
            if (launchCode.equals(foundLaunchCode) && !launchCodeUsed) {
              launchCodeUsed = true;
              return html().with(
                  muicssHeader("Nashorn JavaScript REPL", "/jsrepl.js", "/commandline.css"),
                  // alt: "/commandline-dark.css"
                  muicssCommandLineBody("Nashorn JavaScript REPL!", "accessKey", accessKey));
            } else {
              Log.e(TAG, "incorrect launch code, permission denied");
              return html().withText("incorrect launch code, permission denied");
            }
          });
    });
  }

  /**
   * Main routine that just launches the JavaScript REPL.
   */
  public static void main(String[] args) {
    launch();
  }
}

