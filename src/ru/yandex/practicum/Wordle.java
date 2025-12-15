package ru.yandex.practicum;

import ru.yandex.practicum.exception.InvalidWordException;
import ru.yandex.practicum.exception.WordNotFoundInDictionaryException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Wordle {
    private static final String DICTIONARY_FILE = "words_ru.txt";
    private static final String LOG_FILE = "wordle_game.log";
    private static final String CRASH_LOG_FILE = "wordle_crash.log";

    public static void main(String[] args) {
        boolean playAgain = true;
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

        while (playAgain) {
            try (PrintWriter log = createLogger()) {
                runGame(log, scanner);
                playAgain = askForRestart(scanner, log);
            } catch (Exception e) {
                handleCriticalError(e, null);
                playAgain = false;
            }
        }

        System.out.println("\nСпасибо за игру! До свидания!");
        scanner.close();
    }

     private static PrintWriter createLogger() throws IOException {
        Path logPath = Paths.get(LOG_FILE);
        boolean append = Files.exists(logPath);
        return new PrintWriter(
                Files.newBufferedWriter(logPath,
                        StandardCharsets.UTF_8,
                        append ? java.nio.file.StandardOpenOption.APPEND : java.nio.file.StandardOpenOption.CREATE)
        );
    }

  private static void runGame(PrintWriter log, Scanner scanner) throws IOException {
        log.println("\n" + "=".repeat(50));
        log.println("Запуск Wordle " + java.time.LocalDateTime.now());
        log.println("Словарь: " + DICTIONARY_FILE);
        log.println("Лог-файл: " + LOG_FILE);
        log.println("=".repeat(50));

        WordleDictionaryLoader loader = new WordleDictionaryLoader(log);
        WordleDictionary dictionary = loader.loadDictionary(DICTIONARY_FILE);
        WordleGame game = new WordleGame(dictionary, log);

        showWelcomeMessage();
        gameLoop(game, scanner, log);
        showResults(game, log);
    }

     private static void showWelcomeMessage() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("Добро пожаловать в Wordle на русском языке!");
        System.out.println("У вас есть " + WordleGame.MAX_ATTEMPTS + " попыток, чтобы угадать " +
                WordleGame.WORD_LENGTH + "-буквенное слово.");
        System.out.println("=".repeat(50));
        System.out.println("Подсказки:");
        System.out.println("  + — буква на правильном месте");
        System.out.println("  ^ — буква есть, но не на этом месте");
        System.out.println("  - — буквы нет в слове");
        System.out.println("\nКоманды:");
        System.out.println("  Нажмите Enter — получить подсказку");
        System.out.println("  'стоп' — выйти из игры");
        System.out.println("=".repeat(50) + "\n");
    }

     private static void gameLoop(WordleGame game, Scanner scanner, PrintWriter log) {
        while (!game.isGameOver()) {
            System.out.print("Введите слово (или нажмите Enter для подсказки): ");
            if (!scanner.hasNextLine()) return;

            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                String hint = game.getHint();
                if (hint != null) {
                    System.out.println("Подсказка: " + hint + "\n");
                    log.println("Игрок запросил подсказку: " + hint);
                } else {
                    System.out.println("Подсказки временно недоступны.\n");
                    log.println("Игрок запросил подсказку, но подсказки недоступны");
                }
                continue;
            }

            if (input.equalsIgnoreCase("стоп")) {
                System.out.println("\nИгра остановлена.");
                log.println("Игрок остановил игру.");
                return;
            }

            try {
                String result = game.checkWord(input);
                System.out.println("Результат: " + result + "\n");

                if (game.isWordGuessed()) {
                    System.out.println("🎉 ПОЗДРАВЛЯЕМ! Вы угадали слово!");
                    log.println("Игрок угадал слово!");
                    break;
                }

                System.out.println("Осталось попыток: " + game.getAttemptsRemaining());

            } catch (InvalidWordException | WordNotFoundInDictionaryException e) {
                System.out.println("❌ Ошибка: " + e.getMessage() + "\n");
                log.println("Ошибка ввода: " + e.getMessage());
            }
        }
    }

     private static void showResults(WordleGame game, PrintWriter log) {
        System.out.println("\n" + "=".repeat(50));

        if (!game.isWordGuessed()) {
            System.out.println("😔 К сожалению, вы не угадали слово.");
            System.out.println("Загаданное слово было: " + game.getAnswer());
        } else {
            // Используем MAX_ATTEMPTS вместо getMaxAttempts()
            System.out.println("🎉 ПОБЕДА! Слово угадано за " +
                    (WordleGame.MAX_ATTEMPTS - game.getAttemptsRemaining()) + " попыток!");
        }

        System.out.println("\nИстория попыток:");
        var words = game.getGuessedWords();
        var hints = game.getHints();

        for (int i = 0; i < words.size(); i++) {
            System.out.printf("%2d. %s → %s%n", i + 1, words.get(i), hints.get(i));
        }

        System.out.println("=".repeat(50));

        log.println("Игра завершена. Угадано: " + game.isWordGuessed());
        log.println("Загаданное слово: " + game.getAnswer());
        log.println("Попыток использовано: " + (WordleGame.MAX_ATTEMPTS - game.getAttemptsRemaining()));
        log.println("Лог сохранен в: " + LOG_FILE);
    }

     private static boolean askForRestart(Scanner scanner, PrintWriter log) {
        System.out.print("\nХотите сыграть ещё раз? (да/нет): ");
        String response = scanner.nextLine().trim().toLowerCase();

        boolean restart = response.equals("да") || response.equals("yes") || response.equals("y");

        if (log != null) {
            log.println("Игрок выбрал: " + (restart ? "играть снова" : "выйти"));
        }

        if (restart) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("НОВАЯ ИГРА");
            System.out.println("=".repeat(50));
        }

        return restart;
    }

     private static void handleCriticalError(Exception e, PrintWriter log) {
        try (PrintWriter errorLog = new PrintWriter(
                new FileWriter(CRASH_LOG_FILE, StandardCharsets.UTF_8, true))) {
            errorLog.println("\n" + "=".repeat(80));
            errorLog.println("CRASH " + java.time.LocalDateTime.now());
            errorLog.println("Словарь: " + DICTIONARY_FILE);
            errorLog.println("Лог-файл: " + LOG_FILE);
            errorLog.println("Краш-лог: " + CRASH_LOG_FILE);
            errorLog.println("Message: " + e.getMessage());
            errorLog.println("Class: " + e.getClass().getName());

            for (StackTraceElement ste : e.getStackTrace()) {
                errorLog.println("  at " + ste);
            }
            errorLog.println("=".repeat(80));
        } catch (IOException ioException) {
            System.err.println("Не удалось записать лог ошибки: " + ioException.getMessage());
        }

        System.err.println("\n" + "=".repeat(50));
        System.err.println("КРИТИЧЕСКАЯ ОШИБКА");
        System.err.println("=".repeat(50));
        System.err.println("Сообщение: " + e.getMessage());
        System.err.println("\nИгра не может быть запущена.");
        System.err.println("Пожалуйста, проверьте:");
        System.err.println("1. Существует ли файл словаря: " + DICTIONARY_FILE);
        System.err.println("2. Содержит ли он " + WordleGame.WORD_LENGTH + "-буквенные слова");
        System.err.println("3. Доступны ли права на чтение файла");
        System.err.println("4. Проблема записана в файл: " + CRASH_LOG_FILE);
        System.err.println("=".repeat(50));

        if (log != null) {
            log.println("КРИТИЧЕСКАЯ ОШИБКА: " + e.getMessage());
        }
    }
}