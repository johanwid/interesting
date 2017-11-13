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

import java.util.Arrays;

/**
 * A simple Hangman game. Computer picks a random word and the player has to guess
 * the word by guessing individual letters. Player has 10 attempts to guess the letters in the word.
 * Correct guesses do not count against the number of attempts.
 */
public class Hangman {

  /**
   * The mystery word that the player is attempting to guess.
   */
  private String wordToGuess;

  /**
   * The player's current guess. The correctly guessed letters are in their positions as in the mystery word.
   * The still unknown letters are represented by '#'.
   */
  private String currentGuess;

  int guessesLeft;

  public String getWordToGuess() {
    return wordToGuess;
  }

  public String getCurrentGuess() {
    return currentGuess;
  }

  public int guessesLeft() {
    return guessesLeft;
  }

  /**
   * Constructor for Hangman.
  */
  public Hangman() {
    wordToGuess = HangmanIO.getRandomWord();
    char[] charArray = new char[wordToGuess.length()];
    Arrays.fill(charArray, '#');
    currentGuess = new String(charArray);
    guessesLeft = 10;
  }

  /**
   * Constructor for Hangman with a specific word.
   */
  public Hangman(String word) {
    wordToGuess = word;
    char[] charArray = new char[wordToGuess.length()];
    Arrays.fill(charArray, '#');
    currentGuess = new String(charArray);
    guessesLeft = 10;
  }

  /**
   *  Find whether wordToGuess contains letter.
   *
   *  @param letter is the character that the player has guessed
   *
   *  @return the number of times letter appears in the wordToGuess. Return -1 if the letter already appears
   *
   */
  public int guessLetter(char letter) {
    int i;

    for (i = 0; i < currentGuess.length(); i++) {
      if (currentGuess.charAt(i) == letter) {
        return -1;
      }
    }

    int correctGuesses = 0;
    char[] current = currentGuess.toCharArray();

    for (i = 0; i < wordToGuess.length(); i++) {
      if (wordToGuess.charAt(i) == letter) {
        correctGuesses++;
        current[i] = letter;
      }
    }

    if (correctGuesses == 0) {
      guessesLeft--;
    }

    return correctGuesses;
  }

  /**
   *  Replace the '#'s in currentGuess with the letter on the positions it appears in wordToGuess.
   *
   *  @param letter is the character that the player has guessed
   *
   *
   */
  public void addLetter(char letter) {
    int i;

    char[] current = currentGuess.toCharArray();

    for (i = 0; i < wordToGuess.length(); i++) {
      if (wordToGuess.charAt(i) == letter) {
        current[i] = letter;
      }
    }

    currentGuess = new String(current);

  }

  public boolean gameWon() {
    return wordToGuess.equals(currentGuess);
  }

  /**
   * Display the message to the player that has won the game.
   */
  public void displayWin() {
    HangmanIO.println("Congratulations, you have guessed the word correctly!!!");
    HangmanIO.println("The word we were looking for was \'" + wordToGuess + "\'");
    HangmanIO.println("");
  }

  /**
   * Display the message to the player that has lost the game.
   */
  public void displayLoss() {
    HangmanIO.println("Sorry, you ran out of guesses. You lose the game!");
    HangmanIO.println("The word we were looking for was \'" + wordToGuess + "\'");
    HangmanIO.println("");
  }

  /**
   * The main program for the Hangman game.
   */
  public static void main(String[] args) {

    HangmanIO.println("Welcome to the Hangman game!");
    String answer;
    HangmanIO.print("Would you like to play? (Y/N) ");
    while (true) {
      answer = HangmanIO.nextLine();
      while (!answer.equals("Y") && !answer.equals("N") && !answer.equals("y") && !answer.equals("n") ) {
        HangmanIO.println("Please answer with Y or N");
        HangmanIO.print("Would you like to play? (Y/N) ");
        answer = HangmanIO.nextLine();
      }
      if (answer.equals("N") || answer.equals("n")) {
        return;
      }

      Hangman hangman = new Hangman();

      boolean gameWon = false;

      while (hangman.guessesLeft > 0 && !gameWon) {
        hangman.displayStatus();
        char guessedLetter = HangmanIO.getLetterFromPlayer();
        int numCorrectPositions = hangman.guessLetter(guessedLetter);
        if (numCorrectPositions == -1) {
          HangmanIO.println("You have already guessed that letter!!");
          HangmanIO.println("");
        } else if (numCorrectPositions == 0) {
          HangmanIO.println("Sorry, the word does not contain the letter \'" + guessedLetter + "\'");
          HangmanIO.println("");
        } else {
          HangmanIO.println("Correct, the word contains the letter \'" + guessedLetter + "\' in " +
                  numCorrectPositions + " positions!");
          hangman.addLetter(guessedLetter);
          gameWon = hangman.gameWon();
          if (gameWon) {
            hangman.displayWin();
          }
        }
      }

      if (!gameWon) {
        hangman.displayLoss();
      }

      HangmanIO.print("Would you like to play again? (Y/N) ");

    }
  }


  /**
   * Display the current status of the game.
   */
  private void displayStatus() {
    HangmanIO.println("Your current guess is \'" + currentGuess + "\'");
    HangmanIO.println("You have " + guessesLeft + " guesses left");
    HangmanIO.println("");
  }

}
