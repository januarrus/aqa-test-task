package aqa.data;

import java.security.SecureRandom;

/**
 * Генерация токенов. Каждый тест берёт свой новый токен,
 * чтобы тесты не влияли друг на друга через хранилище приложения.
 */
public final class Tokens {

    public static final String DIGITS = "0123456789";
    /** Все заглавные латинские буквы — допустимы по документации. */
    public static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /** Буквы A–F — единственные, которые фактически принимает приложение (см. дефект в README). */
    public static final String LETTERS_A_TO_F = "ABCDEF";
    /** Буквы G–Z — допустимы по документации, но отклоняются приложением. */
    public static final String LETTERS_G_TO_Z = "GHIJKLMNOPQRSTUVWXYZ";
    public static final int VALID_LENGTH = 32;

    private static final String SAFE_ALPHABET = DIGITS + LETTERS_A_TO_F;
    private static final SecureRandom RANDOM = new SecureRandom();

    private Tokens() {
    }

    /**
     * Новый валидный токен длиной 32 символа.
     * Состоит из цифр и букв A–F: такой токен корректен и по документации, и для приложения,
     * поэтому сценарии LOGIN/ACTION/LOGOUT проверяются независимо от дефекта валидации.
     */
    public static String valid() {
        return random(VALID_LENGTH);
    }

    /** Случайная строка заданной длины из цифр и букв A–F. */
    public static String random(int length) {
        return random(SAFE_ALPHABET, length);
    }

    /** Случайная строка заданной длины из указанного набора символов. */
    public static String random(String alphabet, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
        }
        return sb.toString();
    }
}
