/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.cparser;

import edu.rice.json.Scanner;
import edu.rice.json.Scanner.JsonPatterns;
import edu.rice.list.IList;
import edu.rice.list.List;
import edu.rice.regex.Token;
import edu.rice.sexpr.Scanner.SexprPatterns;
import org.junit.Test;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;

import static edu.rice.cparser.Expression.*;
import static edu.rice.cparser.ParserFunction.parseTerminal;
import static edu.rice.cparser.Result.*;
import static edu.rice.sexpr.Scanner.SexprPatterns.*;
import static edu.rice.sexpr.Scanner.scanSexpr;
import static org.junit.Assert.assertEquals;

/**
 * Basic unit tests to exercise the parser combinator with a balanced-parens sort of language.
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
public class ParserFunctionTest {
  final IList<Token<SexprPatterns>> parens = scanSexpr("((()))");
  final IList<Token<SexprPatterns>> wordAlice = scanSexpr("alice");
  final IList<Token<SexprPatterns>> wordBob = scanSexpr("bob");
  final IList<Token<SexprPatterns>> wordCharlie = scanSexpr("charlie");
  final IList<Token<SexprPatterns>> wordsBobCharlie = scanSexpr("bob charlie");
  final IList<Token<SexprPatterns>> words = scanSexpr("alice bob charlie");
  final ParserFunction<SexprPatterns> aliceParser = parseTerminal(WORD, "alice");
  final ParserFunction<SexprPatterns> bobParser = parseTerminal(WORD, "bob");
  final ParserFunction<SexprPatterns> wordParser = parseTerminal(WORD); // any WORD
  final Expression<SexprPatterns> aliceTerminal = exprTerminal(WORD, "alice");
  final Expression<SexprPatterns> bobTerminal = exprTerminal(WORD, "bob");
  final Expression<SexprPatterns> charlieTerminal = exprTerminal(WORD, "charlie");

  @Test
  public void then() throws Exception {
    final ParserFunction<SexprPatterns> aliceThenBobParser = aliceParser.then(bobParser);

    assertEquals(resultError(), aliceThenBobParser.parse(wordAlice));
    assertEquals(resultError(), aliceThenBobParser.parse(wordBob));
    assertEquals(
        resultOk(exprPair(aliceTerminal, bobTerminal), wordCharlie),
        aliceThenBobParser.parse(words));
  }

  @Test
  public void or() throws Exception {
    final ParserFunction<SexprPatterns> aliceOrBobParser = aliceParser.or(bobParser);

    assertEquals(resultError(), aliceOrBobParser.parse(wordCharlie));
    assertEquals(
        resultOk(aliceTerminal, List.makeEmpty()),
        aliceOrBobParser.parse(wordAlice));
    assertEquals(
        resultOk(bobTerminal, List.makeEmpty()),
        aliceOrBobParser.parse(wordBob));
    assertEquals(
        resultOk(aliceTerminal, wordsBobCharlie),
        aliceOrBobParser.parse(words));
  }

  @Test
  public void list() throws Exception {
    final IList<Token<SexprPatterns>> words = scanSexpr("alice alice alice bob");
    final ParserFunction<SexprPatterns> manyAlice = aliceParser.list();

    assertEquals(
        resultOk(exprNothing(), wordsBobCharlie),
        manyAlice.parse(wordsBobCharlie));

    assertEquals(
        resultOk(exprPair(aliceTerminal, exprNothing()), List.makeEmpty()),
        manyAlice.parse(wordAlice));

    assertEquals(
        resultOk(
            exprPair(aliceTerminal,
                exprPair(aliceTerminal,
                    exprPair(aliceTerminal,
                        exprNothing()))),
            wordBob),
        manyAlice.parse(words));

    assertEquals(
        List.of(aliceTerminal, aliceTerminal, aliceTerminal),
        manyAlice.parse(words).match(ok -> ok.production.asList(), error -> List.makeEmpty()));

    assertEquals(
        resultOk(
            exprPair(
                exprPair(aliceTerminal,
                    exprPair(aliceTerminal,
                        exprPair(aliceTerminal,
                            exprNothing()))),
                bobTerminal),
            List.makeEmpty()),
        manyAlice.then(bobParser).parse(words));
  }

  @Test
  public void listSeparators() throws Exception {
    final IList<Token<JsonPatterns>> jsonTokens0 = Scanner.scanJson("[]");
    final IList<Token<JsonPatterns>> jsonTokens1 = Scanner.scanJson("[1, 2, 3, 4]");

    final ParserFunction<JsonPatterns> simpleParser =
        parseTerminal(JsonPatterns.OPENSQUARE)
            .then(
                parseTerminal(JsonPatterns.NUMBER)
                    .separatedList(parseTerminal(JsonPatterns.COMMA)))
            .then(parseTerminal(JsonPatterns.CLOSESQUARE));

    final Expression<JsonPatterns> open = exprTerminal(JsonPatterns.OPENSQUARE, "[");
    final Expression<JsonPatterns> close = exprTerminal(JsonPatterns.CLOSESQUARE, "]");
    final Expression<JsonPatterns> one = exprTerminal(JsonPatterns.NUMBER, "1");
    final Expression<JsonPatterns> two = exprTerminal(JsonPatterns.NUMBER, "2");
    final Expression<JsonPatterns> three = exprTerminal(JsonPatterns.NUMBER, "3");
    final Expression<JsonPatterns> four = exprTerminal(JsonPatterns.NUMBER, "4");

    assertEquals(
        resultOk(
            exprPair(
                exprPair(open,
                    exprNothing()),
                close), List.makeEmpty()),
        simpleParser.parse(jsonTokens0));

    // notice how the commas disappear? that's a helpful feature of the separatedList method
    assertEquals(
        resultOk(
            exprPair(
                exprPair(open,
                    exprPair(one,
                        exprPair(two,
                            exprPair(three,
                                exprPair(four, exprNothing()))))),
                close), List.makeEmpty()),
        simpleParser.parse(jsonTokens1));
  }

  @Test
  public void testTerminal() throws Exception {
    assertEquals(resultOk(aliceTerminal, List.makeEmpty()), aliceParser.parse(wordAlice));
    assertEquals(resultError(), aliceParser.parse(wordBob));
    assertEquals(resultOk(aliceTerminal, wordsBobCharlie), aliceParser.parse(words));
    assertEquals(resultError(), aliceParser.parse(parens));
  }

  @Test
  public void end() throws Exception {
    assertEquals(resultOk(aliceTerminal, List.makeEmpty()), aliceParser.thenEnd().parse(wordAlice));
    assertEquals(resultError(), aliceParser.thenEnd().parse(words));
  }

  @Test
  public void terminalType() throws Exception {
    assertEquals(resultOk(aliceTerminal, List.makeEmpty()), wordParser.parse(wordAlice));
    assertEquals(resultOk(bobTerminal, List.makeEmpty()), wordParser.parse(wordBob));
    assertEquals(resultError(), wordParser.parse(parens));
  }

  final IList<Token<SexprPatterns>> deeperExpression = scanSexpr("(alice bob charlie)");
  final Expression<SexprPatterns> openParenTerminal = exprTerminal(OPEN, "(");
  final Expression<SexprPatterns> closeParenTerminal = exprTerminal(CLOSE, ")");
  final ParserFunction<SexprPatterns> openParenParser = parseTerminal(OPEN, "(");
  final ParserFunction<SexprPatterns> closeParenParser = parseTerminal(CLOSE, ")");

  @Test
  public void testParensAndList() throws Exception {
    // Note how two slightly different ways of writing the grammar can yield two very different parse trees.
    // The first form is pretty much what you want to be using, such that you end up with your productions
    // following the same structure as our lists, and then the asList() method will work as desired. See below.
    final ParserFunction<SexprPatterns> openWordsCloseParser =
        openParenParser.then(wordParser.list().then(closeParenParser));
    final ParserFunction<SexprPatterns> openWordsCloseParser2 =
        openParenParser.then(wordParser.list()).then(closeParenParser);

    final Result<SexprPatterns> result1 = openWordsCloseParser.parse(deeperExpression);

    assertEquals(
        resultOk(
            exprPair(
                openParenTerminal,
                exprPair(
                    exprPair(aliceTerminal,
                        exprPair(bobTerminal,
                            exprPair(charlieTerminal,
                                exprNothing()))),
                    closeParenTerminal)),
            List.makeEmpty()),
        result1);

    // we're going to coerce this into list format, which should be a list with three elements
    final IList<Expression<SexprPatterns>> asList = result1.match(ok -> ok.production.asList(), error -> List.makeEmpty());
    assertEquals(3, asList.length());
    assertEquals(openParenTerminal, asList.head());
    assertEquals(closeParenTerminal, asList.nth(2).get());
    assertEquals(List.of(aliceTerminal, bobTerminal, charlieTerminal), asList.nth(1).get().asList());

    // Note how this structure differs from how the first parser does it.
    assertEquals(
        resultOk(
            exprPair(
                exprPair(
                    openParenTerminal,
                    exprPair(aliceTerminal,
                        exprPair(bobTerminal,
                            exprPair(charlieTerminal,
                                exprNothing())))),
                closeParenTerminal),
            List.makeEmpty()),
        openWordsCloseParser2.parse(deeperExpression));
  }
}