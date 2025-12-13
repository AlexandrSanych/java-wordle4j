package ru.yandex.practicum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class WordleDictionaryLoader {
    private final PrintWriter log;

    public WordleDictionaryLoader(PrintWriter log) {
        this.log = log;
    }

    public WordleDictionary loadDictionary(String filename) throws IOException {
        Path path = Paths.get(filename);
        logMessage("Попытка загрузки словаря из: " + path.toAbsolutePath());
        logMessage("Ожидаемая длина слова: " + WordleGame.WORD_LENGTH);

        if (!Files.exists(path)) {
            throw new IOException("Файл не найден: " + path.toAbsolutePath());
        }

        if (!Files.isReadable(path)) {
            throw new IOException("Нет прав на чтение: " + path.toAbsolutePath());
        }

        Set<String> wordSet = new LinkedHashSet<>();

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            int lineCount = 0;
            int validCount = 0;

            while ((line = reader.readLine()) != null) {
                lineCount++;
                String normalized = WordleDictionary.normalizeWord(line);

                if (normalized.length() == WordleGame.WORD_LENGTH && normalized.matches("[а-я]+")) {
                    if (wordSet.add(normalized)) {
                        validCount++;
                    }
                } else if (!normalized.isBlank()) {
                    logMessage("  Пропущено: " + line + " (длина: " + normalized.length() +
                            ", требуется: " + WordleGame.WORD_LENGTH + ")");
                }
            }

            logMessage("Загружено строк: " + lineCount +
                    ", валидных слов: " + validCount +
                    ", уникальных: " + wordSet.size());
        }

        if (wordSet.isEmpty()) {
            throw new IOException("Файл не содержит ни одного корректного " +
                    WordleGame.WORD_LENGTH + "-буквенного слова.");
        }

        return new WordleDictionary(new ArrayList<>(wordSet), log);
    }

    private void logMessage(String message) {
        if (log != null) {
            log.println("[Loader] " + message);
            log.flush();
        }
    }
}