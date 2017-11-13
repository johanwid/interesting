"use strict";


// this doesn't happen until the DOM is instantiated
$(document).ready(function() {
    printParagraph("<i><u>RPN calculator initialized, ready to rock</u></i>")

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
        url: "/rpnserver/",
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
