package com.example.datavalidator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DataValidatorController.class)
@Import(RulesConfig.class)
class DataValidatorControllerTests {

    @Autowired
    private MockMvc mockMvc;

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
                .andExpect(jsonPath("$.results.length()").value(2))
                .andExpect(jsonPath("$.results[0].description").value("Status"))
                .andExpect(jsonPath("$.results[0].status").value("Validation passed"))
                .andExpect(jsonPath("$.results[1].description").value("Age"))
                .andExpect(jsonPath("$.results[1].status").value("Valid"));
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
                .andExpect(jsonPath("$.results[1].description").value("Age"))
                .andExpect(jsonPath("$.results[1].status").value("Student too young (13 years old, < 14)"));
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
    void acceptsPartialSpouseInfo() throws Exception {
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
                .andExpect(jsonPath("$.results[0].status").value("Validation passed"));
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
