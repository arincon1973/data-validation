package com.example.datavalidator;

import com.example.datavalidator.DataValidatorController.ValidationEntry;
import com.example.datavalidator.DataValidatorController.ValidationRequest;

import java.time.LocalDate;
import java.util.List;

public interface Rule {

    boolean validate(ValidationRequest request, List<ValidationEntry> entries, LocalDate schoolStart);

    float ruleOrder();
}
