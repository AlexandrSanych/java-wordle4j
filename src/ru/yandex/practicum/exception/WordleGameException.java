package ru.yandex.practicum.exception;

public class WordleGameException extends Exception {
    private static final long serialVersionUID = 1L;

    public WordleGameException(String message) {
        super(message);
    }

    public WordleGameException(String message, Throwable cause) {
        super(message, cause);
    }

    public WordleGameException(Throwable cause) {
        super(cause);
    }
}