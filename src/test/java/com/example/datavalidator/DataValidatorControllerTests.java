package com.example.datavalidator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DataValidatorController.class)
class DataValidatorControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsEveryFieldWithItsValue() throws Exception {
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
                .andExpect(jsonPath("$.results[0].description").value("studentInfo:firstName"))
                .andExpect(jsonPath("$.results[0].status").value("Ada"))
                .andExpect(jsonPath("$.results[1].description").value("studentInfo:lastName"))
                .andExpect(jsonPath("$.results[1].status").value("Lovelace"))
                .andExpect(jsonPath("$.results[2].description").value("studentInfo:ssn"))
                .andExpect(jsonPath("$.results[2].status").value("123456789"))
                .andExpect(jsonPath("$.results[3].description").value("studentInfo:dateOfBirth"))
                .andExpect(jsonPath("$.results[3].status").value("2003-05-15"))
                .andExpect(jsonPath("$.results[4].description").value("dependencyStatus"))
                .andExpect(jsonPath("$.results[4].status").value("dependent"))
                .andExpect(jsonPath("$.results[5].description").value("maritalStatus"))
                .andExpect(jsonPath("$.results[5].status").value("single"))
                .andExpect(jsonPath("$.results[6].description").value("household:numberInHousehold"))
                .andExpect(jsonPath("$.results[6].status").value("4"))
                .andExpect(jsonPath("$.results[7].description").value("household:numberInCollege"))
                .andExpect(jsonPath("$.results[7].status").value("1"))
                .andExpect(jsonPath("$.results[8].description").value("income:studentIncome"))
                .andExpect(jsonPath("$.results[8].status").value("5000"))
                .andExpect(jsonPath("$.results[9].description").value("income:parentIncome"))
                .andExpect(jsonPath("$.results[9].status").value("65000"))
                .andExpect(jsonPath("$.results[10].description").value("stateOfResidence"))
                .andExpect(jsonPath("$.results[10].status").value("CA"));
    }

    @Test
    void decodesUnicodeEscapes() throws Exception {
        mockMvc.perform(post("/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentInfo": {
                                    "firstName": "Jos\\u00e9",
                                    "lastName": "O'Brien",
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
                .andExpect(jsonPath("$.results[0].status").value("José"))
                .andExpect(jsonPath("$.results[1].status").value("O'Brien"));
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
    void rejectsGet() throws Exception {
        mockMvc.perform(get("/validate"))
                .andExpect(status().isMethodNotAllowed());
    }
}
