package com.example.datavalidator;

import com.example.datavalidator.DataValidatorController.Household;
import com.example.datavalidator.DataValidatorController.Income;
import com.example.datavalidator.DataValidatorController.StudentInfo;
import com.example.datavalidator.DataValidatorController.ValidationEntry;
import com.example.datavalidator.DataValidatorController.ValidationRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgeRuleTests {

    private static final LocalDate SCHOOL_START = LocalDate.of(2027, 9, 1);

    private final AgeRule rule = new AgeRule();

    @Test
    void acceptsStudentTurning14OnSchoolStart() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertTrue(rule.validate(requestBornOn(LocalDate.of(2013, 9, 1)), entries, SCHOOL_START));
        assertEquals(List.of(new ValidationEntry("Age", "Valid", "N/A")), entries);
    }

    @Test
    void rejectsStudentTurning14DayAfterSchoolStart() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertFalse(rule.validate(requestBornOn(LocalDate.of(2013, 9, 2)), entries, SCHOOL_START));
        assertEquals(List.of(new ValidationEntry("Age", "Student too young (13 years old, < 14)", "needs review")), entries);
    }

    @Test
    void acceptsStudentAged120() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertTrue(rule.validate(requestBornOn(LocalDate.of(1907, 6, 1)), entries, SCHOOL_START));
        assertEquals(List.of(new ValidationEntry("Age", "Valid", "N/A")), entries);
    }

    @Test
    void rejectsStudentAged121() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertFalse(rule.validate(requestBornOn(LocalDate.of(1906, 6, 1)), entries, SCHOOL_START));
        assertEquals(List.of(new ValidationEntry("Age", "Student too old (121 years old, > 120)", "needs review")), entries);
    }

    private static ValidationRequest requestBornOn(LocalDate dateOfBirth) {
        return new ValidationRequest(
                new StudentInfo("Jane", "Smith", "123456789", dateOfBirth),
                "dependent",
                "single",
                new Household(4, 1),
                new Income(5000L, 65000L),
                "CA",
                null);
    }
}
