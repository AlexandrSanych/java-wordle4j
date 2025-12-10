package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class WordleTest {

    @TempDir
    Path tempDir;

    @Test
    void testRunGame() throws IOException {
        Path dict = tempDir.resolve("words_ru.txt");
        Files.write(dict, "герой\nбанан\nпчела\nслово\nгонец".getBytes());

        ByteArrayInputStream input = new ByteArrayInputStream("герой\nстоп\n".getBytes());
        System.setIn(new BufferedInputStream(input));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        // Запускаем в отдельном потоке, т.к. main вызывает System.exit
        Thread gameThread = new Thread(() -> {
            try {
                Wordle.main(new String[]{});
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        gameThread.start();

        try {
            gameThread.join(2000); // Ждем 2 секунды
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String out = output.toString();
        assertTrue(out.contains("Добро пожаловать в Wordle"));

        System.setIn(System.in);
        System.setOut(System.out);
    }

    @Test
    void testHandleCriticalError() {
        PrintWriter log = new PrintWriter(new StringWriter());
        try {
            Method method = Wordle.class.getDeclaredMethod("handleCriticalError", Exception.class, PrintWriter.class);
            method.setAccessible(true);
            method.invoke(null, new RuntimeException("Test"), log);
        } catch (Exception e) {
            fail("handleCriticalError должен быть вызван через рефлексию: " + e.getMessage());
        }
    }
}