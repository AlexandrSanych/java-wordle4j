package ru.yandex.practicum.exception;

public class InvalidWordException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidWordException(String message) {
        super(message);
    }

    public InvalidWordException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidWordException(Throwable cause) {
        super(cause);
    }
}