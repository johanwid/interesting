/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

"use strict";

// this doesn't happen until the DOM is instantiated
$(document).ready(function() {
    // printParagraph("<i><u>commandline initialized, ready to rock</u></i>")

    $("#goButton").on("click", fetchQuery)
    $("#commandLine").keydown(function (event) {
        if(event.keyCode == 13) {
            event.preventDefault() // prevent carriage-return from triggering a page reload
            fetchQuery()
        }
    })
})


function fetchQuery() {
    var savedText = $("#commandLine").val()
    $("#commandLine").val("")

    dispatchQuery(savedText)
}

function printParagraph(text) {
    var textBox = $("#textOutput")
    textBox.append("<p>" + text + "</p>") // cross-site scripting opportunity!
    textBox.scrollTop(textBox.prop("scrollHeight")); // scroll to the bottom
}

function dispatchQuery(input) {
    console.log("dispatching query: " + input)
    $.ajax( {
        url: "/lowercase/",
        type: "GET",
        data: {'input': input},
        success: function(data) {
            console.log("success: " + data)
            printParagraph(JSON.parse(data).response)
        },
        error: function(data) {
            console.log("error: " + data)
            printParagraph('<b>Bah! ' + data + ' error!</b>')
        }
    })
}

