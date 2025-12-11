package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class WordleDictionaryLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void testLoadValidDictionary() throws IOException {
        Path file = tempDir.resolve("words_test.txt");
        Files.write(file, """
                герой
                банан
                пчела
                слово
                гонец
                """.getBytes());

        PrintWriter log = new PrintWriter(System.out);
        WordleDictionaryLoader loader = new WordleDictionaryLoader(log);
        WordleDictionary dictionary = loader.loadDictionary(file.toString());

        assertTrue(dictionary.contains("герой"));
        assertTrue(dictionary.contains("банан"));
        assertEquals(5, dictionary.getAllWords().size());
    }

    @Test
    void testLoadWithInvalidWords() throws IOException {
        Path file = tempDir.resolve("words_mixed.txt");
        Files.write(file, """
                Герой
                длинное
                hello
                банан
                пчела
                слон123
                                
                """.getBytes());

        PrintWriter log = new PrintWriter(System.out);
        WordleDictionaryLoader loader = new WordleDictionaryLoader(log);
        WordleDictionary dictionary = loader.loadDictionary(file.toString());

        assertTrue(dictionary.contains("герой"));
        assertTrue(dictionary.contains("банан"));
        assertTrue(dictionary.contains("пчела"));
        assertEquals(3, dictionary.getAllWords().size());
    }

    @Test
    void testLoadNonExistentFile() {
        PrintWriter log = new PrintWriter(System.out);
        WordleDictionaryLoader loader = new WordleDictionaryLoader(log);

        assertThrows(IOException.class, () -> loader.loadDictionary("not_found.txt"));
    }

    @Test
    void testLoadEmptyFile() throws IOException {
        Path file = tempDir.resolve("empty.txt");
        Files.createFile(file);

        PrintWriter log = new PrintWriter(System.out);
        WordleDictionaryLoader loader = new WordleDictionaryLoader(log);

        assertThrows(IOException.class, () -> loader.loadDictionary(file.toString()));
    }

    @Test
    void testLoadFileWithDuplicates() throws IOException {
        Path file = tempDir.resolve("duplicates.txt");
        Files.write(file, """
                герой
                Герой
                банан
                БАНАН
                """.getBytes());

        PrintWriter log = new PrintWriter(System.out);
        WordleDictionaryLoader loader = new WordleDictionaryLoader(log);
        WordleDictionary dictionary = loader.loadDictionary(file.toString());

        assertEquals(2, dictionary.getAllWords().size()); // герой, банан
    }
}