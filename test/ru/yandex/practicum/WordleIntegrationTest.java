package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WordleIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    void testCompleteGameFlow() throws Exception {
        // 1. Создаем словарь
        Path dictFile = tempDir.resolve("dict.txt");
        Files.writeString(dictFile, "герой\nбанан\nклоун\nгорох\nгорка\nгонец\nслово\nпчела\nбанка\nабвгд");

        // 2. Загружаем словарь
        StringWriter loaderLog = new StringWriter();
        WordleDictionaryLoader loader = new WordleDictionaryLoader(new PrintWriter(loaderLog));
        WordleDictionary dictionary = loader.loadDictionary(dictFile.toString());

        // 3. Создаем игру
        StringWriter gameLog = new StringWriter();
        WordleGame game = new WordleGame(dictionary, new PrintWriter(gameLog));

        // 4. Получаем загаданное слово
        String answer = game.getAnswer();
        assertNotNull(answer);
        assertEquals(5, answer.length());
        assertTrue(dictionary.contains(answer));

        // 5. Делаем попытку (не угадываем сразу)
        String result = game.checkWord("гонец");
        assertNotNull(result);
        assertEquals(5, result.length());
        assertEquals(5, game.getAttemptsRemaining());
        assertFalse(game.isGameOver());

        // 6. Пробуем получить подсказку
        String hint = game.getHint();
        if (hint != null) {
            assertEquals(5, hint.length());
            assertTrue(dictionary.contains(hint));
        }

        // 7. Угадываем слово
        String winResult = game.checkWord(answer);
        assertEquals("+++++", winResult);
        assertTrue(game.isWordGuessed());
        assertTrue(game.isGameOver());

        // 8. Проверяем историю
        List<String> guessedWords = game.getGuessedWords();
        assertEquals(2, guessedWords.size());
        assertEquals("гонец", guessedWords.get(0));
        assertEquals(answer, guessedWords.get(1));
    }

    @Test
    void testWordNormalizationInGame() throws Exception {
        Path dictFile = tempDir.resolve("dict2.txt");
        Files.writeString(dictFile, "ежика\nгерой\nбанан"); // "ежика" - 5 букв

        WordleDictionaryLoader loader = new WordleDictionaryLoader(new PrintWriter(new StringWriter()));
        WordleDictionary dictionary = loader.loadDictionary(dictFile.toString());
        WordleGame game = new WordleGame(dictionary, new PrintWriter(new StringWriter()));

        // Устанавливаем загаданное слово "ежика"
        Field answerField = WordleGame.class.getDeclaredField("answer");
        answerField.setAccessible(true);
        answerField.set(game, "ежика");

        // Пробуем угадать с буквой ё
        String result = game.checkWord("ЁЖИКА");
        assertEquals("+++++", result);
        assertTrue(game.isWordGuessed());
    }
}