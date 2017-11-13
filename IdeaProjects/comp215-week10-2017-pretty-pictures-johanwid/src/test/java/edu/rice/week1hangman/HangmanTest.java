/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.week1hangman;

import org.junit.Test;

import static org.junit.Assert.*;

public class HangmanTest {
  @Test
  public void testGuessLetter() throws Exception {
    Hangman hangman = new Hangman("mutation");
    assertEquals("mutation", hangman.getWordToGuess());
    assertEquals(1, hangman.guessLetter('a'));
    assertEquals(2, hangman.guessLetter('t'));
    assertEquals(10, hangman.guessesLeft());
    assertEquals(0, hangman.guessLetter('s'));
    assertEquals(9, hangman.guessesLeft());
  }

  @Test
  public void testAddLetter() throws Exception {
    Hangman hangman = new Hangman("mutation");
    assertEquals("########", hangman.getCurrentGuess());
    hangman.addLetter('a');
    assertEquals("###a####", hangman.getCurrentGuess());
    hangman.addLetter('t');
    assertEquals("##tat###", hangman.getCurrentGuess());
  }

  @Test
  public void testLetterPresent() throws Exception {
    Hangman hangman = new Hangman("mutation");
    assertEquals(1, hangman.guessLetter('a'));
    assertEquals(2, hangman.guessLetter('t'));

    // Correctly guessing the letters should not by itself change the current guess
    assertEquals(1, hangman.guessLetter('a'));
    assertEquals(2, hangman.guessLetter('t'));

    // Adding the letters to the solution should change the current guess
    hangman.addLetter('a');
    assertEquals("###a####", hangman.getCurrentGuess());
    hangman.addLetter('t');
    assertEquals("##tat###", hangman.getCurrentGuess());

    // Testing the 'already there' logic
    assertEquals(-1, hangman.guessLetter('a'));
    assertEquals(-1, hangman.guessLetter('t'));
  }

}

