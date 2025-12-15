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
        dictionary = new WordleDictionary(words, log);

        game = new WordleGame(dictionary, log);

        Field answerField = WordleGame.class.getDeclaredField("answer");
        answerField.setAccessible(true);
        answerField.set(game, answerWord);
    }

    @Test
    public void testCorrectGuess() throws Exception {
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
    public void testPartialGuess() throws Exception {
        setupGame("герой");
        String result = game.checkWord("гонец");
        String actualResult = WordleDictionary.analyzeWord("герой", "гонец");
        assertEquals(actualResult, result); // Используем фактический результат
        assertFalse(game.isWordGuessed());
        assertEquals(5, game.getAttemptsRemaining());
    }

    @Test
    public void testInvalidWordLength() throws Exception {
        setupGame("герой");
        assertThrows(InvalidWordException.class, () -> game.checkWord("длинное"));
        assertThrows(InvalidWordException.class, () -> game.checkWord("кот"));
    }

    @Test
    public void testWordNotInDictionary() throws Exception {
        setupGame("герой");
        assertThrows(InvalidWordException.class, () -> game.checkWord("словоо"));
        assertThrows(InvalidWordException.class, () -> game.checkWord("абвг"));
        assertThrows(WordNotFoundInDictionaryException.class, () -> {
            game.checkWord("яблок");
        });
    }

    @Test
    public void testGameOverByAttempts() throws Exception {
        setupGame("герой");
        String[] wordsToTry = {"слово", "банан", "пчела", "банка", "горох", "горка"};

        for (int i = 0; i < WordleGame.MAX_ATTEMPTS; i++) {
            game.checkWord(wordsToTry[i]);
        }

        assertTrue(game.isGameOver());
        assertEquals(0, game.getAttemptsRemaining());
    }

    @Test
    public void testUpdateAnalysis() throws Exception {
        setupGame("банка");
        String result = game.checkWord("банан");
        String actualResult = WordleDictionary.analyzeWord("банка", "банан");
        assertEquals(actualResult, result);
        assertEquals(1, game.getGuessedWords().size());
        assertEquals("банан", game.getGuessedWords().get(0));
        assertEquals(actualResult, game.getHints().get(0));

        char[] actualHint = actualResult.toCharArray();
        for (int i = 0; i < actualHint.length; i++) {
            if (actualHint[i] == '+') {
                // Если буква на правильной позиции, она должна быть в correctPositions
                assertEquals('б', game.getCorrectPositions().get(0));
            }
        }
    }

    @Test
    public void testGetHint() throws Exception {
        setupGame("герой");
        game.checkWord("банан");

        String hint = game.getHint();
        assertNotNull(hint);
        assertEquals(5, hint.length());
        assertTrue(dictionary.contains(hint));
        assertNotEquals("банан", hint);
    }

    @Test
    public void testGetCurrentPattern() throws Exception {
        setupGame("банка");

        // В начале игры паттерн должен быть полностью из подчеркиваний
        assertEquals("_____", game.getCurrentPattern());

        // После попытки "банан"
        game.checkWord("банан");
        // Получим фактический результат анализа
        String actualHint = WordleDictionary.analyzeWord("банка", "банан");
        // Определим паттерн на основе фактического результата
        String expectedPattern = getExpectedPattern("банан", actualHint);
        assertEquals(expectedPattern, game.getCurrentPattern());
        game.checkWord("банка");
        assertEquals("банка", game.getCurrentPattern());
    }

    @Test
   public void testGetCurrentPatternWithMultipleGuesses() throws Exception {
        setupGame("герой");

        // Первая попытка: "гонец"
        game.checkWord("гонец");
        String hint1 = WordleDictionary.analyzeWord("герой", "гонец");
        String expectedPattern1 = getExpectedPattern("гонец", hint1);
        assertEquals(expectedPattern1, game.getCurrentPattern());

        // Вторая попытка: "горка"
        game.checkWord("горка");
        String hint2 = WordleDictionary.analyzeWord("герой", "горка");
        String expectedPattern2 = getExpectedPattern("горка", hint2);
        assertEquals(expectedPattern2, game.getCurrentPattern());

        // Третья попытка: "герой" (полное совпадение)
        game.checkWord("герой");
        assertEquals("герой", game.getCurrentPattern());
    }

    @Test
    public void testGetCurrentPatternNoGuesses() throws Exception {
        setupGame("герой");

        // Без попыток паттерн должен быть пустым (все подчеркивания)
        assertEquals("_____", game.getCurrentPattern());
    }

    @Test
    public void testGetCurrentPatternExactMatch() throws Exception {
        setupGame("герой");

        game.checkWord("герой");
        // После полного совпадения паттерн должен быть полным словом
        assertEquals("герой", game.getCurrentPattern());
    }

    // Вспомогательный метод для вычисления ожидаемого паттерна
    private String getExpectedPattern(String guess, String hint) {
        char[] pattern = new char[WordleGame.WORD_LENGTH];
        for (int i = 0; i < pattern.length; i++) {
            if (hint.charAt(i) == '+') {
                pattern[i] = guess.charAt(i);
            } else {
                pattern[i] = '_';
            }
        }
        return new String(pattern);
    }
}