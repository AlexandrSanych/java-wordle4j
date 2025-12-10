package ru.yandex.practicum;

import ru.yandex.practicum.exception.InvalidWordException;

import java.util.*;

public class WordleDictionary {
    private final List<String> words;
    private final Random random;
    private Map<Character, Integer> cachedFrequency;
    private final Set<String> wordSet;

    public WordleDictionary(List<String> words) {
        if (words == null) {
            throw new IllegalArgumentException("Список слов не может быть null");
        }

        this.wordSet = new LinkedHashSet<>();
        for (String word : words) {
            String normalized = normalizeWord(word);
            if (normalized.length() == 5 && normalized.matches("[а-я]+")) {
                this.wordSet.add(normalized);
            }
        }

        this.words = new ArrayList<>(wordSet);
        this.random = new Random();
    }

    public boolean contains(String word) {
        return wordSet.contains(normalizeWord(word));
    }

    public String getRandomWord() {
        if (words.isEmpty()) {
            throw new IllegalStateException("Словарь пуст.");
        }
        return words.get(random.nextInt(words.size()));
    }

    public List<String> getAllWords() {
        return Collections.unmodifiableList(words);
    }

    public void validateWord(String word) throws InvalidWordException {
        String normalized = normalizeWord(word);

        if (normalized.isBlank()) {
            throw new InvalidWordException("Слово не может быть пустым");
        }
        if (normalized.length() != 5) {
            throw new InvalidWordException("Слово должно быть 5 букв. Введено: " + normalized.length());
        }
        if (!normalized.matches("[а-я]+")) {
            throw new InvalidWordException("Слово должно содержать только русские буквы: " + word);
        }
    }

    public static String analyzeWord(String secret, String guess) {
        if (secret == null || guess == null) {
            throw new IllegalArgumentException("Слова не могут быть null");
        }

        secret = normalizeWord(secret);
        guess = normalizeWord(guess);

        if (secret.length() != 5 || guess.length() != 5) {
            throw new IllegalArgumentException("Слова должны быть по 5 букв");
        }

        char[] result = new char[5];
        Arrays.fill(result, '-');

        boolean[] secretUsed = new boolean[5];
        boolean[] guessUsed = new boolean[5];

        // Шаг 1: Находим точные совпадения
        for (int i = 0; i < 5; i++) {
            if (secret.charAt(i) == guess.charAt(i)) {
                result[i] = '+';
                secretUsed[i] = true;
                guessUsed[i] = true;
            }
        }

        // Шаг 2: Находим буквы на других местах
        for (int i = 0; i < 5; i++) {
            if (guessUsed[i]) continue; // Уже обработано

            char guessChar = guess.charAt(i);

            for (int j = 0; j < 5; j++) {
                if (!secretUsed[j] && secret.charAt(j) == guessChar) {
                    result[i] = '^';
                    secretUsed[j] = true;
                    guessUsed[i] = true;
                    break;
                }
            }
        }

        return new String(result);
    }

    public static String normalizeWord(String word) {
        if (word == null) return "";
        return word.trim().toLowerCase().replace('ё', 'е');
    }

    public List<String> findSuggestions(Set<Character> mustContain,
                                        Set<Character> mustNotContain,
                                        String pattern) {
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

            if (pattern != null && pattern.length() == 5) {
                for (int i = 0; i < 5; i++) {
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

        return suggestions;
    }

    public List<String> findSuggestionsOptimized(Set<Character> mustContain,
                                                 Set<Character> mustNotContain,
                                                 Map<Integer, Character> correctPositions,
                                                 Map<Integer, Set<Character>> wrongPositions) {
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
                if (pos < 0 || pos >= 5 || word.charAt(pos) != expectedChar) {
                    valid = false;
                    break;
                }
            }

            if (!valid) continue;

            for (Map.Entry<Integer, Set<Character>> entry : wrongPositions.entrySet()) {
                int pos = entry.getKey();
                Set<Character> forbiddenChars = entry.getValue();
                if (pos >= 0 && pos < 5 && forbiddenChars.contains(word.charAt(pos))) {
                    valid = false;
                    break;
                }
            }

            if (valid) {
                suggestions.add(word);
            }
        }

        return suggestions;
    }

    public Map<Character, Integer> getLetterFrequency() {
        if (cachedFrequency == null) {
            cachedFrequency = new HashMap<>();
            for (String word : words) {
                for (char c : word.toCharArray()) {
                    cachedFrequency.put(c, cachedFrequency.getOrDefault(c, 0) + 1);
                }
            }
        }
        return new HashMap<>(cachedFrequency);
    }

    public List<Character> getMostCommonLetters() {
        Map<Character, Integer> freq = getLetterFrequency();
        List<Map.Entry<Character, Integer>> entries = new ArrayList<>(freq.entrySet());
        entries.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        List<Character> result = new ArrayList<>();
        for (int i = 0; i < Math.min(10, entries.size()); i++) {
            result.add(entries.get(i).getKey());
        }
        return result;
    }

    public int size() {
        return words.size();
    }

    public boolean isEmpty() {
        return words.isEmpty();
    }
}