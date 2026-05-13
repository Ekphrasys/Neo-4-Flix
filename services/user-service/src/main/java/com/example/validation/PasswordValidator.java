package com.example.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PasswordValidator {

    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[^a-zA-Z0-9]");

    private PasswordValidator() {
    }

    /**
     * Validates a password against the strong password policy.
     *
     * @param password the raw password to validate
     * @return a list of violation messages (empty if valid)
     */
    public static List<String> validate(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.length() < MIN_LENGTH) {
            errors.add("Password must be at least " + MIN_LENGTH + " characters long");
        }
        if (password == null || !UPPERCASE.matcher(password).find()) {
            errors.add("Password must contain at least one uppercase letter");
        }
        if (password == null || !LOWERCASE.matcher(password).find()) {
            errors.add("Password must contain at least one lowercase letter");
        }
        if (password == null || !DIGIT.matcher(password).find()) {
            errors.add("Password must contain at least one digit");
        }
        if (password == null || !SPECIAL.matcher(password).find()) {
            errors.add("Password must contain at least one special character");
        }

        return errors;
    }
}
