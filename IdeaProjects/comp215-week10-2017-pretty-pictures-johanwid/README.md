# <img src="http://www.rice.edu/_images/rice-logo.jpg" width=180> Comp215, Fall 2017
## Week 10-11: Pretty Pictures

This two-week long project will take place in 
[src/main/java/edu/rice/prettypictures](/../../tree/master/src/main/java/edu/rice/prettypictures)
and the week10 lab can be found in
[src/main/java/edu/rice/week10lab/ImageServer.java](src/main/java/edu/rice/week10lab/ImageServer.java)
The project and lab specifications, as well as the course slide decks,
can be found on the
[Comp215 Piazza](https://piazza.com/class/j23hjc5og5l3sk).
(We'll publish a separate GitHub link for the week11 lab next week, but your week11 project will happen here in this repo.)


## Student Information
Please edit _README.md_ and replace your instructor's name and NetID with your own:
(In future weeks, we'll have more things for you to do here.)

_Student name_: Johan WIDMANN

_Student NetID_: jw69

Your NetID is typically your initials and a numeric digit. That's
what we need here.

_If you contacted us in advance about the late policy and we approved a late submission,
please cut-and-paste the text from that email here._

## Honor Code Reminder

We'd like to remind you that the plagiarism detection software we're using is very good.
Should you feel tempted to borrow code from a friend, we will detect it and you will
be reported to the Honor Council for violating our course policies, as detailed in the syllabus.
This extends to your *designs*.
If we observe that two students share a strikingly similar *design* for how they handle
the same features, this will be seen as evidence of plagiarism. Your code must be your own
and your design must be your own as well.

## Week 10 ToDo Items

To make your graders lives easier, please declare which of the 20 required functions you implemented:

1) Constant: black (yes/no)no
2) Constant: red (yes/no)no
3) Constant: green (yes/no)no
4) Constant: blue (yes/no)no
5) Constant: white (yes/no)no
6) Variable: *x* (yes/no)no
7) Variable: *y* (yes/no)no
8) Function: *-x* (yes/no)no
9) Function: *-y* (yes/no)no
10) Function: sin(*x*) (yes/no)no
11) Function: sin(*y*) (yes/no) no
12) Function: sin(0.4) (yes/no)nono
13) Function: sin(2345.2) (yes/no)
14) Function: *x*+*y* (yes/no)no
15) Function: *x*+*x* (yes/no)no
16) Function: *y*+*y* (yes/no)no
17) Function: *x*+0.56 (yes/no)no
18) Function: 0.3 + (-*y*) (yes/no)no
19) Function: *x*/*y* (yes/no)no
20) Function: sin(1/*x*) (yes/no)no

Just a reminder: you're not only implementing the web server (in
[PrettyPicturesServer](src/main/java/edu/rice/prettypictures/PrettyPicturesServer.java))
to generate these intrinsic functions as images. You're building separate
classes and interfaces that deal with these images, completely unrelated to
the web server. 
You're also writing unit tests with >50% test coverage for these functions.

Also, below, please describe how your code will allow these intrinsic
functions to combine together, cross-breed, etc. Please describe your
design and/or give the grader pointers to the interfaces or classes that
you've designed to help you when you do the cross-breeding next week.
(We're not actually requiring you to *implement* the cross-breeding this
week. We just want you to say how the functions that you've implemented
will let you do the rest of the assignment.)

*Your answer here: (please be concise!)*

## Week11 ToDo Items

To make your graders lives easier, please declare which of these functions you implemented:

i have implemented all except external

1) Constants (yes/no) yes
2) Variables X and Y (yes/no) yes 
3) Negate (yes/no) yes
4) Round-down / floor (yes/no) yes
5) Round-up / ceiling (yes/no) yesy
6) Sin (yes/no) yes
7) Cos (yes/no)yes
8) Arc-tangent (yes/no)yes
9) Exponentiate (yes/no)yes
10) Logarithm (yes/no)yes
11) Absolute value (yes/no)yes
12) Clip (yes/no)yes
13) Wrap (yes/no)yes
14) Add (yes/no)yes
15) Subtract (yes/no)yes
16) Multiply (yes/no)yes
17) Divide (yes/no)yes
18) Inner product (yes/no)yes
19) External image (yes/no)no
20) YCrCb-to-RGB and RGB-to-YCrCb (yes/no)yes
21) Perlin greyscale noise (yes/no)yes
22) Perlin color noise (yes/no)yes
23) Dissolve (yes/no)yes

And please answer the following questions:

- When you increased the coverage requirement to 80\% in your [build.gradle](build.gradle) file,
  how many bugs did you end up finding in the process of writing extra unit tests?
    github was not allowing me to push my code, so i couldn't see. it's doubtful even these
    readme changes will get seen

- Which Java class(es) describe how your functions can be composed and evaluated?
    that is dealt with in argFuncs.java

- Which Java class(es) deal with cross-breeding and mutation?
    as of now, this hasn't been finished.

- Which Java class(es) deal with saving and loading your genotypes, so that
restarting your server isn't visible to external users?
    none of the classes do
    
- Where is the directory with your ten favorite images? (Don't forget to commit it to GitHub!)
    this part was not completed

- Did you commit and push a *YourNetID12x18.png* file so we can print it for you?
    this part was not completed

nosir

## Cool points (optional):

Please describe things that you may have done which go beyond the
requirements of the assignment for which you wish to be considered for cool
points.
