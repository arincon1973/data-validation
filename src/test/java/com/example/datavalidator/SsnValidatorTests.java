package com.example.datavalidator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SsnValidatorTests {

    @ParameterizedTest
    @ValueSource(strings = {"123456789", "123-45-6789"})
    void acceptsValidFormats(String ssn) {
        assertTrue(SsnValidator.isValid(ssn));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "12345678",       // 8 digits
            "1234567890",     // 10 digits
            "12-345-6789",    // dashes in the wrong places
            "123-456789",     // only one dash
            "12345-6789",     // only one dash
            "123 45 6789",    // spaces instead of dashes
            "12a456789",      // letter
            " 123456789",     // leading space
            "123456789 ",     // trailing space
            "١٢٣٤٥٦٧٨٩"       // non-ASCII digits
    })
    void rejectsInvalidFormats(String ssn) {
        assertFalse(SsnValidator.isValid(ssn));
    }

    @Test
    void rejectsNull() {
        assertFalse(SsnValidator.isValid(null));
    }
}
