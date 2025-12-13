package ru.yandex.practicum;

import ru.yandex.practicum.exception.InvalidWordException;

import java.io.PrintWriter;
import java.util.*;

public class WordleDictionary {
    private final List<String> words;
    private final Random random;
    private Map<Character, Integer> cachedFrequency;
    private final Set<String> wordSet;
    private final PrintWriter log;

    private static final int MOST_COMMON_LETTERS_COUNT = 10;

    public WordleDictionary(List<String> words, PrintWriter log) {
        if (words == null) {
            throw new IllegalArgumentException("Список слов не может быть null");
        }

        this.log = log;
        logMessage("Создание словаря. Исходный список: " + words.size() + " слов");

        this.wordSet = new LinkedHashSet<>();
        for (String word : words) {
            String normalized = normalizeWord(word);
            if (normalized.length() == WordleGame.WORD_LENGTH && normalized.matches("[а-я]+")) {
                if (this.wordSet.add(normalized)) {
                    logMessage("  Добавлено слово: " + normalized);
                }
            } else {
                logMessage("  Пропущено слово: " + word + " → " + normalized +
                        " (длина: " + normalized.length() + ", требуется: " + WordleGame.WORD_LENGTH + ")");
            }
        }

        this.words = new ArrayList<>(wordSet);
        this.random = new Random();

        logMessage("Словарь создан. Уникальных слов: " + this.words.size());
    }

    public boolean contains(String word) {
        String normalized = normalizeWord(word);
        boolean result = wordSet.contains(normalized);
        logMessage("Проверка слова '" + word + "' → '" + normalized + "': " + result);
        return result;
    }

    public String getRandomWord() {
        if (words.isEmpty()) {
            logMessage("Попытка получить случайное слово из пустого словаря");
            throw new IllegalStateException("Словарь пуст.");
        }
        String word = words.get(random.nextInt(words.size()));
        logMessage("Выбрано случайное слово: " + word);
        return word;
    }

    public List<String> getAllWords() {
        logMessage("Запрос всех слов (возвращено: " + words.size() + ")");
        return Collections.unmodifiableList(words);
    }

    public void validateWord(String word) throws InvalidWordException {
        String normalized = normalizeWord(word);
        logMessage("Валидация слова: '" + word + "' → '" + normalized + "'");

        if (normalized.isBlank()) {
            logMessage("  Ошибка: слово пустое");
            throw new InvalidWordException("Слово не может быть пустым");
        }
        if (normalized.length() != WordleGame.WORD_LENGTH) {
            logMessage("  Ошибка: длина " + normalized.length() + " вместо " + WordleGame.WORD_LENGTH);
            throw new InvalidWordException("Слово должно быть " + WordleGame.WORD_LENGTH +
                    " букв. Введено: " + normalized.length());
        }
        if (!normalized.matches("[а-я]+")) {
            logMessage("  Ошибка: содержит не только русские буквы");
            throw new InvalidWordException("Слово должно содержать только русские буквы: " + word);
        }

        logMessage("  Слово валидно");
    }

    public static String analyzeWord(String secret, String guess) {
        if (secret == null || guess == null) {
            throw new IllegalArgumentException("Слова не могут быть null");
        }

        secret = normalizeWord(secret);
        guess = normalizeWord(guess);

        if (secret.length() != WordleGame.WORD_LENGTH || guess.length() != WordleGame.WORD_LENGTH) {
            throw new IllegalArgumentException("Слова должны быть по " + WordleGame.WORD_LENGTH + " букв");
        }

        char[] result = new char[WordleGame.WORD_LENGTH];
        Arrays.fill(result, '-');

        boolean[] secretUsed = new boolean[WordleGame.WORD_LENGTH];
        boolean[] guessUsed = new boolean[WordleGame.WORD_LENGTH];

        // Шаг 1: Находим точные совпадения
        for (int i = 0; i < WordleGame.WORD_LENGTH; i++) {
            if (secret.charAt(i) == guess.charAt(i)) {
                result[i] = '+';
                secretUsed[i] = true;
                guessUsed[i] = true;
            }
        }

        // Шаг 2: Находим буквы на других местах
        for (int i = 0; i < WordleGame.WORD_LENGTH; i++) {
            if (guessUsed[i]) continue;

            char guessChar = guess.charAt(i);

            for (int j = 0; j < WordleGame.WORD_LENGTH; j++) {
                if (!secretUsed[j] && secret.charAt(j) == guessChar) {
                    result[i] = '^';
                    secretUsed[j] = true;
                    guessUsed[i] = true;
                    break;
                }
            }
        }

        String hint = new String(result);
        return hint;
    }

    public static String normalizeWord(String word) {
        if (word == null) return "";
        return word.trim().toLowerCase().replace('ё', 'е');
    }

    public List<String> findSuggestions(Set<Character> mustContain,
                                        Set<Character> mustNotContain,
                                        String pattern) {
        logMessage("Поиск предложений: mustContain=" + mustContain +
                ", mustNotContain=" + mustNotContain +
                ", pattern=" + pattern);

        List<String> suggestions = new ArrayList<>();

        for (String word : words) {
            boolean valid = true;

            for (char c : mustContain) {
                if (word.indexOf(c) == -1) {
                    valid = false;
                    break;
                }
            }

            if (!valid) continue;

            for (char c : mustNotContain) {
                if (word.indexOf(c) != -1) {
                    valid = false;
                    break;
                }
            }

            if (!valid) continue;

            if (pattern != null && pattern.length() == WordleGame.WORD_LENGTH) {
                for (int i = 0; i < WordleGame.WORD_LENGTH; i++) {
                    char patternChar = pattern.charAt(i);
                    if (patternChar != '_' && word.charAt(i) != patternChar) {
                        valid = false;
                        break;
                    }
                }
            }

            if (valid) {
                suggestions.add(word);
            }
        }

        logMessage("Найдено предложений: " + suggestions.size());
        return suggestions;
    }

    public List<String> findSuggestionsOptimized(Set<Character> mustContain,
                                                 Set<Character> mustNotContain,
                                                 Map<Integer, Character> correctPositions,
                                                 Map<Integer, Set<Character>> wrongPositions) {
        logMessage("Оптимизированный поиск предложений: mustContain=" + mustContain +
                ", mustNotContain=" + mustNotContain +
                ", correctPositions=" + correctPositions +
                ", wrongPositions=" + wrongPositions);

        List<String> suggestions = new ArrayList<>();

        for (String word : words) {
            boolean valid = true;

            for (char c : mustContain) {
                if (word.indexOf(c) == -1) {
                    valid = false;
                    break;
                }
            }

            if (!valid) continue;

            for (char c : mustNotContain) {
                if (word.indexOf(c) != -1) {
                    valid = false;
                    break;
                }
            }

            if (!valid) continue;

            for (Map.Entry<Integer, Character> entry : correctPositions.entrySet()) {
                int pos = entry.getKey();
                char expectedChar = entry.getValue();
                if (pos < 0 || pos >= WordleGame.WORD_LENGTH || word.charAt(pos) != expectedChar) {
                    valid = false;
                    break;
                }
            }

            if (!valid) continue;

            for (Map.Entry<Integer, Set<Character>> entry : wrongPositions.entrySet()) {
                int pos = entry.getKey();
                Set<Character> forbiddenChars = entry.getValue();
                if (pos >= 0 && pos < WordleGame.WORD_LENGTH && forbiddenChars.contains(word.charAt(pos))) {
                    valid = false;
                    break;
                }
            }

            if (valid) {
                suggestions.add(word);
            }
        }

        logMessage("Найдено предложений (оптимизировано): " + suggestions.size());
        return suggestions;
    }

    public Map<Character, Integer> getLetterFrequency() {
        logMessage("Вычисление частоты букв");

        if (cachedFrequency == null) {
            cachedFrequency = new HashMap<>();
            for (String word : words) {
                for (char c : word.toCharArray()) {
                    cachedFrequency.put(c, cachedFrequency.getOrDefault(c, 0) + 1);
                }
            }
            logMessage("Частота букв вычислена: " + cachedFrequency.size() + " уникальных букв");
        }
        return new HashMap<>(cachedFrequency);
    }

    public List<Character> getMostCommonLetters() {
        logMessage("Поиск самых частых букв");

        Map<Character, Integer> freq = getLetterFrequency();
        List<Map.Entry<Character, Integer>> entries = new ArrayList<>(freq.entrySet());
        entries.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        List<Character> result = new ArrayList<>();
        for (int i = 0; i < Math.min(MOST_COMMON_LETTERS_COUNT, entries.size()); i++) {
            result.add(entries.get(i).getKey());
        }

        logMessage("Самые частые буквы (первые " + MOST_COMMON_LETTERS_COUNT + "): " + result);
        return result;
    }

    public int size() {
        return words.size();
    }

    public boolean isEmpty() {
        return words.isEmpty();
    }

    private void logMessage(String message) {
        if (log != null) {
            log.println("[Dictionary] " + message);
            log.flush();
        }
    }
}