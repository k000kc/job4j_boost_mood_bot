package ru.job4j.bmb.aspects;

public class SentContentExeption extends RuntimeException {
    public SentContentExeption(String message, Throwable throwable) {
        super(message, throwable);
    }
}
