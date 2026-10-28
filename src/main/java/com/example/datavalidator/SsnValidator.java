package com.example.datavalidator;

import java.util.regex.Pattern;

public final class SsnValidator {

    // Either 9 digits ("123456789") or dashed ("123-45-6789").
    private static final Pattern SSN = Pattern.compile("\\d{9}|\\d{3}-\\d{2}-\\d{4}");

    private SsnValidator() {}

    public static boolean isValid(String ssn) {
        return ssn != null && SSN.matcher(ssn).matches();
    }
}
