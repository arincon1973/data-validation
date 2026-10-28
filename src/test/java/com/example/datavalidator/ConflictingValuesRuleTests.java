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

class ConflictingValuesRuleTests {

    private static final LocalDate SCHOOL_START = LocalDate.of(2027, 9, 1);

    private final ConflictingValuesRule rule = new ConflictingValuesRule();

    @ParameterizedTest
    @CsvSource({"dependent,single", "independent,single", "independent,married"})
    void acceptsNonConflictingValues(String dependencyStatus, String maritalStatus) {
        List<ValidationEntry> entries = new ArrayList<>();

        assertTrue(rule.validate(request(dependencyStatus, maritalStatus), entries, SCHOOL_START));
        assertEquals(List.of(new ValidationEntry("Conflicting Values", "Valid", "N/A")), entries);
    }

    @Test
    void rejectsDependentAndMarried() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertFalse(rule.validate(request("dependent", "married"), entries, SCHOOL_START));
        assertEquals(List.of(new ValidationEntry("Conflicting Values",
                "Student cannot be both dependent and married", "reject")), entries);
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
