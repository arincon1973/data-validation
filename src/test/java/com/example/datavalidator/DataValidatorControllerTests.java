package com.example.datavalidator;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DataValidatorController.class)
@Import({RulesConfig.class, SimpleMeterRegistry.class})
class DataValidatorControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void recordsDurationOfEachRule() throws Exception {
        // The registry is shared with the other tests in this class.
        meterRegistry.clear();
        mockMvc.perform(post("/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentInfo": {
                                    "firstName": "Jane",
                                    "lastName": "Smith",
                                    "ssn": "123456789",
                                    "dateOfBirth": "2013-09-02"
                                  },
                                  "dependencyStatus": "dependent",
                                  "maritalStatus": "single",
                                  "household": {
                                    "numberInHousehold": 4,
                                    "numberInCollege": 1
                                  },
                                  "income": {
                                    "studentIncome": 5000,
                                    "parentIncome": 65000
                                  },
                                  "stateOfResidence": "CA"
                                }
                                """))
                .andExpect(status().isOk());

        Timer ageTimer = meterRegistry.get("validation.rule")
                .tag("rule", "AgeRule")
                .tag("result", "failed")
                .timer();
        assertThat(ageTimer.count()).isEqualTo(1);
        assertThat(meterRegistry.get("validation.rule").timers()).hasSize(9);
    }

    @Test
    void reportsValidationPassed() throws Exception {
        mockMvc.perform(post("/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentInfo": {
                                    "firstName": "Ada",
                                    "lastName": "Lovelace",
                                    "ssn": "123456789",
                                    "dateOfBirth": "2003-05-15"
                                  },
                                  "dependencyStatus": "dependent",
                                  "maritalStatus": "single",
                                  "household": {
                                    "numberInHousehold": 4,
                                    "numberInCollege": 1
                                  },
                                  "income": {
                                    "studentIncome": 5000,
                                    "parentIncome": 65000
                                  },
                                  "stateOfResidence": "CA"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.length()").value(11))
                .andExpect(jsonPath("$.results[0].description").value("Status"))
                .andExpect(jsonPath("$.results[0].status").value("Validation passed"))
                .andExpect(jsonPath("$.results[1].description").value("Dependency Status"))
                .andExpect(jsonPath("$.results[1].status").value("Valid"))
                .andExpect(jsonPath("$.results[2].description").value("Marital Status"))
                .andExpect(jsonPath("$.results[2].status").value("Valid"))
                .andExpect(jsonPath("$.results[3].description").value("Conflicting Values"))
                .andExpect(jsonPath("$.results[3].status").value("Valid"))
                .andExpect(jsonPath("$.results[4].description").value("Age"))
                .andExpect(jsonPath("$.results[4].status").value("Valid"))
                .andExpect(jsonPath("$.results[5].description").value("SSN"))
                .andExpect(jsonPath("$.results[5].status").value("Valid"))
                .andExpect(jsonPath("$.results[6].description").value("Student Income"))
                .andExpect(jsonPath("$.results[6].status").value("Valid"))
                .andExpect(jsonPath("$.results[7].description").value("State of Residence"))
                .andExpect(jsonPath("$.results[7].status").value("Valid"))
                .andExpect(jsonPath("$.results[8].description").value("Household"))
                .andExpect(jsonPath("$.results[8].status").value("Valid"))
                .andExpect(jsonPath("$.results[9].description").value("Parent Income"))
                .andExpect(jsonPath("$.results[9].status").value("Valid"))
                .andExpect(jsonPath("$.results[10].description").value("Spouse"))
                .andExpect(jsonPath("$.results[10].status").value("Valid"));
    }

    @Test
    void reportsDependentWithoutParentIncome() throws Exception {
        mockMvc.perform(post("/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentInfo": {
                                    "firstName": "Jane",
                                    "lastName": "Smith",
                                    "ssn": "123456789",
                                    "dateOfBirth": "2003-05-15"
                                  },
                                  "dependencyStatus": "dependent",
                                  "maritalStatus": "single",
                                  "household": {
                                    "numberInHousehold": 4,
                                    "numberInCollege": 1
                                  },
                                  "income": {
                                    "studentIncome": 5000
                                  },
                                  "stateOfResidence": "CA"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].status").value("Validation failed"))
                .andExpect(jsonPath("$.results[9].description").value("Parent Income"))
                .andExpect(jsonPath("$.results[9].status").value("Parent income is required for dependent students"));
    }

    @Test
    void reportsDependentAndMarriedConflict() throws Exception {
        mockMvc.perform(post("/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentInfo": {
                                    "firstName": "Jane",
                                    "lastName": "Smith",
                                    "ssn": "123456789",
                                    "dateOfBirth": "2003-05-15"
                                  },
                                  "dependencyStatus": "dependent",
                                  "maritalStatus": "married",
                                  "household": {
                                    "numberInHousehold": 4,
                                    "numberInCollege": 1
                                  },
                                  "income": {
                                    "studentIncome": 5000,
                                    "parentIncome": 65000
                                  },
                                  "stateOfResidence": "CA"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].status").value("Validation failed"))
                .andExpect(jsonPath("$.results[3].description").value("Conflicting Values"))
                .andExpect(jsonPath("$.results[3].status").value("Student cannot be both dependent and married"));
    }

    @Test
    void reportsInvalidMaritalStatus() throws Exception {
        mockMvc.perform(post("/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentInfo": {
                                    "firstName": "Jane",
                                    "lastName": "Smith",
                                    "ssn": "123456789",
                                    "dateOfBirth": "2003-05-15"
                                  },
                                  "dependencyStatus": "dependent",
                                  "maritalStatus": "divorced",
                                  "household": {
                                    "numberInHousehold": 4,
                                    "numberInCollege": 1
                                  },
                                  "income": {
                                    "studentIncome": 5000,
                                    "parentIncome": 65000
                                  },
                                  "stateOfResidence": "CA"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].status").value("Validation failed"))
                .andExpect(jsonPath("$.results[2].description").value("Marital Status"))
                .andExpect(jsonPath("$.results[2].status")
                        .value("Invalid value (divorced), expected single or married"));
    }

    @Test
    void reportsInvalidHousehold() throws Exception {
        mockMvc.perform(post("/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentInfo": {
                                    "firstName": "Jane",
                                    "lastName": "Smith",
                                    "ssn": "123456789",
                                    "dateOfBirth": "2003-05-15"
                                  },
                                  "dependencyStatus": "dependent",
                                  "maritalStatus": "single",
                                  "household": {
                                    "numberInHousehold": 1,
                                    "numberInCollege": 1
                                  },
                                  "income": {
                                    "studentIncome": 5000,
                                    "parentIncome": 65000
                                  },
                                  "stateOfResidence": "CA"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].status").value("Validation failed"))
                .andExpect(jsonPath("$.results[8].description").value("Household"))
                .andExpect(jsonPath("$.results[8].status")
                        .value("Dependent student household must have at least 2 people (1, < 2)"));
    }

    @Test
    void reportsInvalidStateOfResidence() throws Exception {
        mockMvc.perform(post("/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentInfo": {
                                    "firstName": "Jane",
                                    "lastName": "Smith",
                                    "ssn": "123456789",
                                    "dateOfBirth": "2003-05-15"
                                  },
                                  "dependencyStatus": "dependent",
                                  "maritalStatus": "single",
                                  "household": {
                                    "numberInHousehold": 4,
                                    "numberInCollege": 1
                                  },
                                  "income": {
                                    "studentIncome": 5000,
                                    "parentIncome": 65000
                                  },
                                  "stateOfResidence": "XX"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].status").value("Validation failed"))
                .andExpect(jsonPath("$.results[7].description").value("State of Residence"))
                .andExpect(jsonPath("$.results[7].status").value("Invalid US state abbreviation (XX)"));
    }

    @Test
    void reportsNegativeStudentIncome() throws Exception {
        mockMvc.perform(post("/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentInfo": {
                                    "firstName": "Jane",
                                    "lastName": "Smith",
                                    "ssn": "123456789",
                                    "dateOfBirth": "2003-05-15"
                                  },
                                  "dependencyStatus": "dependent",
                                  "maritalStatus": "single",
                                  "household": {
                                    "numberInHousehold": 4,
                                    "numberInCollege": 1
                                  },
                                  "income": {
                                    "studentIncome": -500,
                                    "parentIncome": 65000
                                  },
                                  "stateOfResidence": "CA"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].status").value("Validation failed"))
                .andExpect(jsonPath("$.results[6].description").value("Student Income"))
                .andExpect(jsonPath("$.results[6].status")
                        .value("Student income cannot be negative (-500, < 0)"));
    }

    @Test
    void reportsInvalidSsn() throws Exception {
        mockMvc.perform(post("/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentInfo": {
                                    "firstName": "Jane",
                                    "lastName": "Smith",
                                    "ssn": "123-456-789",
                                    "dateOfBirth": "2003-05-15"
                                  },
                                  "dependencyStatus": "dependent",
                                  "maritalStatus": "single",
                                  "household": {
                                    "numberInHousehold": 4,
                                    "numberInCollege": 1
                                  },
                                  "income": {
                                    "studentIncome": 5000,
                                    "parentIncome": 65000
                                  },
                                  "stateOfResidence": "CA"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].status").value("Validation failed"))
                .andExpect(jsonPath("$.results[5].description").value("SSN"))
                .andExpect(jsonPath("$.results[5].status")
                        .value("Invalid SSN format (expected 9 digits or xxx-xx-xxxx)"));
    }

    @Test
    void reportsStudentTooYoungOnSchoolStart() throws Exception {
        mockMvc.perform(post("/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentInfo": {
                                    "firstName": "Jane",
                                    "lastName": "Smith",
                                    "ssn": "123456789",
                                    "dateOfBirth": "2013-09-02"
                                  },
                                  "dependencyStatus": "dependent",
                                  "maritalStatus": "single",
                                  "household": {
                                    "numberInHousehold": 4,
                                    "numberInCollege": 1
                                  },
                                  "income": {
                                    "studentIncome": 5000,
                                    "parentIncome": 65000
                                  },
                                  "stateOfResidence": "CA"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].description").value("Status"))
                .andExpect(jsonPath("$.results[0].status").value("Validation failed"))
                .andExpect(jsonPath("$.results[4].description").value("Age"))
                .andExpect(jsonPath("$.results[4].status").value("Student too young (13 years old, < 14)"));
    }

    @Test
    void acceptsSpouseInfo() throws Exception {
        mockMvc.perform(post("/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentInfo": {
                                    "firstName": "Jane",
                                    "lastName": "Smith",
                                    "ssn": "123456789",
                                    "dateOfBirth": "2003-05-15"
                                  },
                                  "dependencyStatus": "independent",
                                  "maritalStatus": "married",
                                  "household": {
                                    "numberInHousehold": 2,
                                    "numberInCollege": 1
                                  },
                                  "income": {
                                    "studentIncome": 5000
                                  },
                                  "stateOfResidence": "CA",
                                  "spouseInfo": {
                                    "firstName": "John",
                                    "lastName": "Smith",
                                    "ssn": "987654321"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].status").value("Validation passed"));
    }

    @Test
    void reportsIncompleteSpouseInfoForMarried() throws Exception {
        mockMvc.perform(post("/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentInfo": {
                                    "firstName": "Jane",
                                    "lastName": "Smith",
                                    "ssn": "123456789",
                                    "dateOfBirth": "2003-05-15"
                                  },
                                  "dependencyStatus": "independent",
                                  "maritalStatus": "married",
                                  "household": {
                                    "numberInHousehold": 2,
                                    "numberInCollege": 1
                                  },
                                  "income": {
                                    "studentIncome": 5000
                                  },
                                  "stateOfResidence": "CA",
                                  "spouseInfo": {
                                    "firstName": "John"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].status").value("Validation failed"))
                .andExpect(jsonPath("$.results[10].description").value("Spouse"))
                .andExpect(jsonPath("$.results[10].status").value("Spouse last name is required for married students"))
                .andExpect(jsonPath("$.results[11].description").value("Spouse"))
                .andExpect(jsonPath("$.results[11].status").value("Spouse SSN is required for married students"));
    }

    @Test
    void rejectsMissingName() throws Exception {
        mockMvc.perform(post("/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"foo\": 1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsMissingNestedField() throws Exception {
        mockMvc.perform(post("/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentInfo": {
                                    "firstName": "Jane",
                                    "lastName": "Smith",
                                    "ssn": "123456789",
                                    "dateOfBirth": "2003-05-15"
                                  },
                                  "dependencyStatus": "dependent",
                                  "maritalStatus": "single",
                                  "household": {
                                    "numberInHousehold": 4
                                  },
                                  "income": {
                                    "studentIncome": 5000,
                                    "parentIncome": 65000
                                  },
                                  "stateOfResidence": "CA"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void acceptsMissingParentIncome() throws Exception {
        mockMvc.perform(post("/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentInfo": {
                                    "firstName": "Jane",
                                    "lastName": "Smith",
                                    "ssn": "123456789",
                                    "dateOfBirth": "2003-05-15"
                                  },
                                  "dependencyStatus": "independent",
                                  "maritalStatus": "single",
                                  "household": {
                                    "numberInHousehold": 1,
                                    "numberInCollege": 1
                                  },
                                  "income": {
                                    "studentIncome": 5000
                                  },
                                  "stateOfResidence": "CA"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].status").value("Validation passed"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"05/15/2003", "2003-5-15", "2003-13-01", "2003-02-30", "not-a-date"})
    void rejectsInvalidDateOfBirth(String dateOfBirth) throws Exception {
        mockMvc.perform(post("/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentInfo": {
                                    "firstName": "Jane",
                                    "lastName": "Smith",
                                    "ssn": "123456789",
                                    "dateOfBirth": "%s"
                                  },
                                  "dependencyStatus": "dependent",
                                  "maritalStatus": "single",
                                  "household": {
                                    "numberInHousehold": 4,
                                    "numberInCollege": 1
                                  },
                                  "income": {
                                    "studentIncome": 5000,
                                    "parentIncome": 65000
                                  },
                                  "stateOfResidence": "CA"
                                }
                                """.formatted(dateOfBirth)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsGet() throws Exception {
        mockMvc.perform(get("/validate"))
                .andExpect(status().isMethodNotAllowed());
    }
}
