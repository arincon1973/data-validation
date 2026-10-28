package com.example.datavalidator;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(DataValidatorController.class)
@Import({RulesConfig.class, SimpleMeterRegistry.class})
@ExtendWith(OutputCaptureExtension.class)
class RequestLoggingFilterTests {

    private static final String UUID_PATTERN = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void logsStartAndEndWithSameRequestId(CapturedOutput output) throws Exception {
        mockMvc.perform(get("/validate"));

        assertThat(output.getOut()).containsPattern("Request " + UUID_PATTERN + " started: GET /validate");
        assertThat(output.getOut()).containsPattern("Request " + UUID_PATTERN + " finished: status 405 in \\d+ ms");

        String id = output.getOut().replaceAll("(?s).*Request (" + UUID_PATTERN + ") started.*", "$1");
        assertThat(output.getOut()).contains("Request " + id + " finished");
    }
}
