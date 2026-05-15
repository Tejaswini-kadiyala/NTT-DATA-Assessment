package com.example.backend.service;

import com.example.backend.model.CodeResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MiniService {

    public CodeResponse refactorCode(String code) {
        CodeResponse cr = new CodeResponse();
        cr.setOriginalCode(code == null ? "" : code);

        // Very simple deterministic "refactor": trim trailing spaces and remove
        // duplicate blank lines. This is a fallback when the AI model fails.
        if (code == null || code.isBlank()) {
            cr.setRefactoredCode("");
            cr.setExplanation("No code provided to refactor.");
            cr.setWarnings(new ArrayList<>());
            return cr;
        }

        String[] lines = code.split("\r?\n");
        StringBuilder sb = new StringBuilder();
        boolean lastBlank = false;
        for (String line : lines) {
            String trimmed = line.replaceAll("\\s+$", "");
            boolean isBlank = trimmed.isBlank();
            if (isBlank && lastBlank) continue;
            sb.append(trimmed).append("\n");
            lastBlank = isBlank;
        }

        String refactored = sb.toString().trim();
        cr.setRefactoredCode(refactored);

        List<String> warnings = new ArrayList<>();
        if (refactored.length() > code.length()) {
            warnings.add("Refactored code is longer than original.");
        }

        cr.setWarnings(warnings);

        String explanation = "Fallback mini refactor applied: trimmed trailing whitespace and removed duplicate blank lines.";
        explanation += "\n\nNaming suggestions: consider using more descriptive variable and function names.";
        cr.setExplanation(explanation);

        return cr;
    }
}


