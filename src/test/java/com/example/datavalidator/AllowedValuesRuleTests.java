package com.example.datavalidator;

import com.example.datavalidator.DataValidatorController.Household;
import com.example.datavalidator.DataValidatorController.Income;
import com.example.datavalidator.DataValidatorController.StudentInfo;
import com.example.datavalidator.DataValidatorController.ValidationEntry;
import com.example.datavalidator.DataValidatorController.ValidationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AllowedValuesRuleTests {

    private static final LocalDate SCHOOL_START = LocalDate.of(2027, 9, 1);

    private final AllowedValuesRule rule = new AllowedValuesRule();

    @ParameterizedTest
    @CsvSource({"dependent,single", "dependent,married", "independent,single", "independent,married"})
    void acceptsAllowedValues(String dependencyStatus, String maritalStatus) {
        List<ValidationEntry> entries = new ArrayList<>();

        assertTrue(rule.validate(request(dependencyStatus, maritalStatus), entries, SCHOOL_START));
        assertEquals(List.of(
                new ValidationEntry("Dependency Status", "Valid"),
                new ValidationEntry("Marital Status", "Valid")), entries);
    }

    @Test
    void rejectsUnknownDependencyStatus() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertFalse(rule.validate(request("Dependent", "single"), entries, SCHOOL_START));
        assertEquals(List.of(
                new ValidationEntry("Dependency Status",
                        "Invalid value (Dependent), expected dependent or independent"),
                new ValidationEntry("Marital Status", "Valid")), entries);
    }

    @Test
    void rejectsUnknownMaritalStatus() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertFalse(rule.validate(request("dependent", "divorced"), entries, SCHOOL_START));
        assertEquals(List.of(
                new ValidationEntry("Dependency Status", "Valid"),
                new ValidationEntry("Marital Status",
                        "Invalid value (divorced), expected single or married")), entries);
    }

    @Test
    void reportsBothWhenBothInvalid() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertFalse(rule.validate(request("", "unknown"), entries, SCHOOL_START));
        assertEquals(List.of(
                new ValidationEntry("Dependency Status",
                        "Invalid value (), expected dependent or independent"),
                new ValidationEntry("Marital Status",
                        "Invalid value (unknown), expected single or married")), entries);
    }

    private static ValidationRequest request(String dependencyStatus, String maritalStatus) {
        return new ValidationRequest(
                new StudentInfo("Jane", "Smith", "123456789", LocalDate.of(2003, 5, 15)),
                dependencyStatus,
                maritalStatus,
                new Household(4, 1),
                new Income(5000L, 65000L),
                "CA",
                null);
    }
}
