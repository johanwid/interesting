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

import edu.rice.json.Value;
import edu.rice.list.LazyList;
import edu.rice.stream.Adapters;
import edu.rice.tree.TreapMap;
import edu.rice.util.Log;
import edu.rice.util.Try;
import j2html.tags.ContainerTag;
import spark.Request;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import java.security.SecureRandom;

import static edu.rice.json.Builders.*;
import static j2html.TagCreator.*;
import static spark.Spark.exception;

/**
 * Useful helper functions for dealing with the SparkJava web server.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public interface Utils {
  /**
   * Given a URL, tries to launch the real web browser to load that URL.
   * If it fails, this fact is logged, but it's best to treat this command
   * as flakey. It might work, it might fail. It might log that failure,
   * it might not.
   */
  static void launchBrowser(String url) {
    Log.i("SERVER URL", url); // if the remote launch fails, this will at least leave a clickable url in the log

    Try.ofRunnable(() -> java.awt.Desktop.getDesktop().browse(java.net.URI.create(url)))
        .logIfFailureVerbose("Utils.launchBrowser", exception -> "failed to launch: " + url);
  }

  /**
   * j2html helper used for making web pages that use the MUI javascript library.
   */
  static ContainerTag muicssHeader(String pageTitle, String jsLibrary, String cssFile) {
    return head().with(
        title(pageTitle),
        meta().attr("charset", "utf-8"),
        meta().attr("http-equiv", "X-UA-Compatible").withContent("IE=edge"),
        meta().withName("viewport").withContent("width=device-width, initial-scale=1"),
        link().withHref("/mui-0.6.0/css/mui.css").withRel("stylesheet").withType("text/css"),
        link().withHref(cssFile).withRel("stylesheet").withType("text/css"),
        script().withType("text/javascript").withSrc("/mui-0.6.0/js/mui.min.js"),
        script().withType("text/javascript").withSrc("/jquery-1.12.4.min.js"),
        script().withType("text/javascript").withSrc(jsLibrary));
  }

  /**
   * j2html helper used for making web page bodies that implement a command-line interface.
   */
  static ContainerTag muicssCommandLineBody(String pageTitle) {
    return muicssCommandLineBody(pageTitle, "hello", "hello");
  }

  /**
   * j2html helper used for making web page bodies that implement a command-line interface, including
   * a hidden key/value pair, using the specified key for the HTML div id and the specified value in
   * that form field.
   */
  static ContainerTag muicssCommandLineBody(String pageTitle, String hiddenKey, String hiddenValue) {
    return body().with(
        header()
            .withId("header").with(
                nav().withId("appbar").withClass("mui-container-fluid").with(h1(pageTitle))),
        div().withId("textOutput"),
        div().withId("goButton").with(
            button(">").withClass("mui-btn mui-btn--fab mui-btn--primary")),
        div().withId("footer").with(textEntryForm("commandLine",
            "type your commands here", hiddenKey, hiddenValue)));
  }

  /**
   * j2html helper used for making text entry forms plus a hidden form field using the specified
   * key for the HTML div id, and the specified value in that form field.
   */
  static ContainerTag textEntryForm(String id, String placeHolder, String hiddenKey, String hiddenValue) {
    return form().with(
        div().withClass("mui-textfield").with(
            input()
                .withId(id)
                .withType("text")
                .attr("autocomplete", "off")
                .withPlaceholder(placeHolder)),
            input()
                .withId(hiddenKey)
                .withType("text")
                .withValue(hiddenValue)
                .attr("readonly", null) // no value, just "readonly" by itself
                .isHidden()); // not user visible, but still part of the form
  }

  /**
   * Given a Spark web server request, this converts it to a JSON format, suitable for
   * subsequent processing, or just making it easier to print the whole request in one go.
   */
  static Value requestToJson(Request request) {
    return jobject(
        jpair("method", request.requestMethod()),
        jpair("url", request.url()),
        jpair("body", request.body()),

        // The SparkJava library gives us "parameters" and "query parameters". The former are elements from the URL
        // before the ? as specified with colons in the get() command pattern. The latter are the set of
        // ?foo=x,bar=y things that go at the end of the URL. If you're wondering where to look for your input,
        // printing / logging of this will be helpful to understanding what's going on.
        jpair("params", jobject(
            TreapMap.fromMap(request.params())
                .toList()
                .map(kv -> jpair(kv.getKey(), kv.getValue())))),

        // This line is a bit convoluted. SparkJava gives us a java.util.Set of queryParam strings,
        // so we convert to an iterator which is easy to convert to a list, and then we can finally make
        // a map out of that list.
        jpair("queryParams", jobject(
            TreapMap.fromList(
                LazyList.fromIterator(request.queryParams().iterator()),
                request::queryParams)
                .toList()
                .map(kv -> jpair(kv.getKey(), kv.getValue())))));
  }

  /**
   * Every hit on a Spark web server gives the lambda two arguments: a "request" and a "response".
   * The Request has lots of stuff in it. This will use the standard edu.rice.util.Log infrastructure to
   * print out the request, which is helpful when you're trying to decipher what's in a request so
   * you can process it properly.
   *
   * @param tag
   *     The string to use for tagging the log
   * @param request
   *     The request from a Spark web server lambda
   * @see Log#i(String, Object)
   */
  static void logSparkRequest(String tag, Request request) {
    // dump the full request into the log as a JSON object, because why not?
    Log.i(tag, () -> "Spark request:\n" + requestToJson(request).toIndentedString());
  }

  /**
   * Sets up a Spark web server exception handler. If some web request handler blows up with an
   * exception, this will cause the Spark server to (1) log the exception using the standard
   * edu.rice.util.Log service and (2) return a JSON structure with the exception, which may be useful to
   * see from the web client.
   *
   * @param tag
   *     The string to use for tagging the log
   * @see Log#e(String, Object)
   */
  static void jsonSparkExceptionHandler(String tag) {
    exception(Exception.class, (e, request, response) -> {
      response.status(404);
      Log.e(tag, "Spark web lambda failed!", e);

      // we're going to return a JSON array where the elements correspond to the stack backtrace (i.e., files
      // and line numbers from Java). Not that anybody on the browser side is necessarily going to have any
      // use for this, but it might be helpful if you're looking in a browser debugger.

      response.body(
          jobject(
              jpair("exception-stack-trace",
                  jarray(
                      LazyList.fromArray(e.getStackTrace())
                          .map(elem -> javaString(elem.toString())))),
              jpair("exception-description", e.toString())).toString());
    });
  }

  /**
   * Generates a large random number, delivered as a string of base-10 characters (0-9), of the desired number
   * of digits. This number will be "secure" inasmuch as the underlying {@link SecureRandom} class returns
   * numbers that are "secure" against guessing and whatnot.
   */
  static String bigRandom(int digits) {
    // SecureRandom.ints() returns an IntStream, which we convert to Stream<Integer> and then to IList<Integer>
    // for subsequent processing.

    return Adapters.streamToList(
        new SecureRandom().ints(0, 10).limit(digits).boxed())
        .join("");
  }
}
