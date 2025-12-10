package ru.yandex.practicum;

import ru.yandex.practicum.exception.InvalidWordException;
import ru.yandex.practicum.exception.WordNotFoundInDictionaryException;

import java.io.PrintWriter;
import java.util.*;

public class WordleGame {
    private final String answer;
    private int attemptsRemaining;
    private final WordleDictionary dictionary;
    private final PrintWriter log;

    private final List<String> guessedWords = new ArrayList<>();
    private final List<String> hints = new ArrayList<>();

    private final Set<Character> correctLetters = new HashSet<>();
    private final Set<Character> wrongLetters = new HashSet<>();
    private final Map<Integer, Character> correctPositions = new HashMap<>();
    private final Map<Integer, Set<Character>> wrongPositions = new HashMap<>();

    public static final int MAX_ATTEMPTS = 6;
    private static final int WORD_LENGTH = 5;

    public WordleGame(WordleDictionary dictionary, PrintWriter log) {
        if (dictionary == null) throw new IllegalArgumentException("Словарь не может быть null");
        if (log == null) throw new IllegalArgumentException("Логгер не может быть null");

        this.dictionary = dictionary;
        this.log = log;
        this.answer = dictionary.getRandomWord();
        this.attemptsRemaining = MAX_ATTEMPTS;

        for (int i = 0; i < WORD_LENGTH; i++) {
            wrongPositions.put(i, new HashSet<>());
        }

        log.println("=".repeat(50));
        log.println("Игра началась");
        log.println("Загаданное слово: " + answer);
        log.println("Попыток: " + MAX_ATTEMPTS);
        log.println("Размер словаря: " + dictionary.size());
        log.println("=".repeat(50));
    }

    public String checkWord(String word) throws WordNotFoundInDictionaryException, InvalidWordException {
        String normalizedWord = WordleDictionary.normalizeWord(word);

        if (guessedWords.contains(normalizedWord)) {
            throw new InvalidWordException("Это слово уже было использовано: " + normalizedWord);
        }

        dictionary.validateWord(normalizedWord);

        if (!dictionary.contains(normalizedWord)) {
            throw new WordNotFoundInDictionaryException(normalizedWord);
        }

        String hint = WordleDictionary.analyzeWord(answer, normalizedWord);
        guessedWords.add(normalizedWord);
        hints.add(hint);

        updateAnalysis(normalizedWord, hint);
        attemptsRemaining--;

        log.println("Попытка " + (MAX_ATTEMPTS - attemptsRemaining) + "/" + MAX_ATTEMPTS);
        log.println("Введено: " + normalizedWord);
        log.println("Результат: " + hint);
        log.println("Осталось: " + attemptsRemaining);

        return hint;
    }

    private void updateAnalysis(String word, String hint) {
        for (int i = 0; i < WORD_LENGTH; i++) {
            char letter = word.charAt(i);
            char hintChar = hint.charAt(i);

            switch (hintChar) {
                case '+':
                    correctLetters.add(letter);
                    correctPositions.put(i, letter);
                    wrongPositions.get(i).remove(letter);
                    break;
                case '^':
                    correctLetters.add(letter);
                    wrongPositions.get(i).add(letter);
                    break;
                case '-':
                    if (!correctLetters.contains(letter)) {
                        wrongLetters.add(letter);
                    }
                    break;
            }
        }
    }

    public String getHint() {
        List<String> allWords = dictionary.getAllWords();
        if (allWords.isEmpty()) {
            return null;
        }

        List<String> availableWords = new ArrayList<>();
        for (String word : allWords) {
            if (!guessedWords.contains(word) && !word.equals(answer)) {
                availableWords.add(word);
            }
        }

        if (availableWords.isEmpty()) {
            return null;
        }

        return availableWords.get(new Random().nextInt(availableWords.size()));
    }

    public boolean isWordGuessed() {
        return !guessedWords.isEmpty() && "+++++".equals(hints.get(hints.size() - 1));
    }

    public boolean isGameOver() {
        return attemptsRemaining <= 0 || isWordGuessed();
    }

    public String getAnswer() {
        return answer;
    }

    public int getAttemptsRemaining() {
        return attemptsRemaining;
    }

    public List<String> getGuessedWords() {
        return new ArrayList<>(guessedWords);
    }

    public List<String> getHints() {
        return new ArrayList<>(hints);
    }

    public int getMaxAttempts() {
        return MAX_ATTEMPTS;
    }

    public Set<Character> getCorrectLetters() {
        return new HashSet<>(correctLetters);
    }

    public Set<Character> getWrongLetters() {
        return new HashSet<>(wrongLetters);
    }

    public Map<Integer, Character> getCorrectPositions() {
        return new HashMap<>(correctPositions);
    }

    public Map<Integer, Set<Character>> getWrongPositions() {
        Map<Integer, Set<Character>> copy = new HashMap<>();
        for (Map.Entry<Integer, Set<Character>> entry : wrongPositions.entrySet()) {
            copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return copy;
    }
}