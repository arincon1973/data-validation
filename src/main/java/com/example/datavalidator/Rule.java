package com.example.datavalidator;

import com.example.datavalidator.DataValidatorController.ValidationEntry;
import com.example.datavalidator.DataValidatorController.ValidationRequest;

import java.time.LocalDate;
import java.util.List;

public interface Rule {

    boolean validate(ValidationRequest request, List<ValidationEntry> entries, LocalDate schoolStart);

    float ruleOrder();

    // Rules that run before order 1 check fundamental input problems, so their findings are rejections.
    default String severity() {
        return ruleOrder() < 1 ? "reject" : "needs review";
    }

    // For failures; passing checks use valid() so the rule's severity only shows when it fails.
    default ValidationEntry entry(String description, String status) {
        return new ValidationEntry(description, status, severity());
    }

    default ValidationEntry valid(String description) {
        return new ValidationEntry(description, "Valid", "N/A");
    }
}
