package ru.yandex.practicum.exception;

public class WordNotFoundInDictionaryException extends WordleGameException {
    private static final long serialVersionUID = 1L;

    public WordNotFoundInDictionaryException(String word) {
        super("Слово не найдено в словаре: " + word);
    }

    public WordNotFoundInDictionaryException(String word, Throwable cause) {
        super("Слово не найдено в словаре: " + word, cause);
    }
}