package com.example.datavalidator;

import com.example.datavalidator.DataValidatorController.Household;
import com.example.datavalidator.DataValidatorController.Income;
import com.example.datavalidator.DataValidatorController.SpouseInfo;
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

class SpouseInfoRuleTests {

    private static final LocalDate SCHOOL_START = LocalDate.of(2027, 9, 1);

    private final SpouseInfoRule rule = new SpouseInfoRule();

    @Test
    void acceptsMarriedWithCompleteSpouseInfo() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertTrue(rule.validate(request("married", new SpouseInfo("John", "Smith", "987-65-4321")),
                entries, SCHOOL_START));
        assertEquals(List.of(new ValidationEntry("Spouse", "Valid")), entries);
    }

    @Test
    void acceptsSingleWithoutSpouseInfo() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertTrue(rule.validate(request("single", null), entries, SCHOOL_START));
        assertEquals(List.of(new ValidationEntry("Spouse", "Valid")), entries);
    }

    @Test
    void ignoresSpouseInfoForSingle() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertTrue(rule.validate(request("single", new SpouseInfo(null, null, "bad")), entries, SCHOOL_START));
        assertEquals(List.of(new ValidationEntry("Spouse", "Valid")), entries);
    }

    @Test
    void rejectsMarriedWithoutSpouseInfo() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertFalse(rule.validate(request("married", null), entries, SCHOOL_START));
        assertEquals(List.of(new ValidationEntry("Spouse",
                "Spouse info is required for married students")), entries);
    }

    @Test
    void reportsEachMissingField() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertFalse(rule.validate(request("married", new SpouseInfo(null, " ", null)), entries, SCHOOL_START));
        assertEquals(List.of(
                new ValidationEntry("Spouse", "Spouse first name is required for married students"),
                new ValidationEntry("Spouse", "Spouse last name is required for married students"),
                new ValidationEntry("Spouse", "Spouse SSN is required for married students")), entries);
    }

    @Test
    void rejectsInvalidSpouseSsn() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertFalse(rule.validate(request("married", new SpouseInfo("John", "Smith", "98765")),
                entries, SCHOOL_START));
        assertEquals(List.of(new ValidationEntry("Spouse",
                "Invalid spouse SSN format (expected 9 digits or xxx-xx-xxxx)")), entries);
    }

    private static ValidationRequest request(String maritalStatus, SpouseInfo spouse) {
        return new ValidationRequest(
                new StudentInfo("Jane", "Smith", "123456789", LocalDate.of(2003, 5, 15)),
                "independent",
                maritalStatus,
                new Household(2, 1),
                new Income(5000L, null),
                "CA",
                spouse);
    }
}
