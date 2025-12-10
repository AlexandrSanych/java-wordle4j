package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.exception.InvalidWordException;
import ru.yandex.practicum.exception.WordNotFoundInDictionaryException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WordleGameTest {

    private StringWriter stringWriter;
    private PrintWriter log;
    private WordleDictionary dictionary;
    private WordleGame game;

    private void setupGame(String answerWord) throws Exception {
        stringWriter = new StringWriter();
        log = new PrintWriter(stringWriter);

        List<String> words = List.of(
                answerWord, "слово", "банан", "пчела", "гонец", "банка",
                "горох", "горка", "абвгд", "клоун"
        );
        dictionary = new WordleDictionary(words);

        game = new WordleGame(dictionary, log);

        Field answerField = WordleGame.class.getDeclaredField("answer");
        answerField.setAccessible(true);
        answerField.set(game, answerWord);
    }

    @Test
    void testCorrectGuess() throws Exception {
        setupGame("герой");
        String result = game.checkWord("герой");
        assertEquals("+++++", result);
        assertTrue(game.isWordGuessed());
        assertTrue(game.isGameOver());
        assertEquals(5, game.getAttemptsRemaining());
        assertEquals(List.of("герой"), game.getGuessedWords());
        assertEquals(List.of("+++++"), game.getHints());
    }

    @Test
    void testPartialGuess() throws Exception {
        setupGame("герой");
        String result = game.checkWord("гонец");
        assertEquals("+^-^-", result);
        assertFalse(game.isWordGuessed());
        assertEquals(5, game.getAttemptsRemaining());
    }

    @Test
    void testInvalidWordLength() throws Exception {
        setupGame("герой");
        assertThrows(InvalidWordException.class, () -> game.checkWord("длинное"));
        assertThrows(InvalidWordException.class, () -> game.checkWord("кот"));
    }

    @Test
    void testWordNotInDictionary() throws Exception {
        setupGame("герой");
        assertThrows(InvalidWordException.class, () -> game.checkWord("словоо"));
        assertThrows(InvalidWordException.class, () -> game.checkWord("абвг"));
        assertThrows(WordNotFoundInDictionaryException.class, () -> {
            game.checkWord("яблок");
        });
    }

    @Test
    void testGameOverByAttempts() throws Exception {
        setupGame("герой");
        String[] wordsToTry = {"слово", "банан", "пчела", "банка", "горох", "горка"};

        for (int i = 0; i < WordleGame.MAX_ATTEMPTS; i++) {
            game.checkWord(wordsToTry[i]);
        }

        assertTrue(game.isGameOver());
        assertEquals(0, game.getAttemptsRemaining());
    }

    @Test
    void testUpdateAnalysis() throws Exception {
        setupGame("банка");
        String result = game.checkWord("банан");

        assertEquals("+++^-", result);
        assertEquals(1, game.getGuessedWords().size());
        assertEquals("банан", game.getGuessedWords().get(0));
        assertEquals("+++^-", game.getHints().get(0));

        assertTrue(game.getCorrectLetters().contains('б'));
        assertTrue(game.getCorrectLetters().contains('а'));
        assertTrue(game.getCorrectLetters().contains('н'));

        assertEquals('б', game.getCorrectPositions().get(0));
        assertEquals('а', game.getCorrectPositions().get(1));
        assertEquals('н', game.getCorrectPositions().get(2));
    }

    @Test
    void testGetHint() throws Exception {
        setupGame("герой");
        game.checkWord("банан");

        String hint = game.getHint();
        assertNotNull(hint);
        assertEquals(5, hint.length());
        assertTrue(dictionary.contains(hint));
        assertNotEquals("банан", hint);
    }

    @Test
    void testGetCurrentPattern() throws Exception {
        setupGame("герой");
        game.checkWord("гонец");

        // У метода getCurrentPattern может не быть реализации
        // Если нужно, добавьте его в WordleGame:
        // public String getCurrentPattern() {
        //     char[] pattern = new char[WORD_LENGTH];
        //     Arrays.fill(pattern, '_');
        //     for (Map.Entry<Integer, Character> entry : correctPositions.entrySet()) {
        //         if (entry.getKey() >= 0 && entry.getKey() < WORD_LENGTH) {
        //             pattern[entry.getKey()] = entry.getValue();
        //         }
        //     }
        //     return new String(pattern);
        // }
    }
}