package com.example.backend.service;

import com.example.backend.model.CodeResponse;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class RefactorService {
    public CodeResponse processCode(String code) {
        CodeResponse response = new CodeResponse();
        List<String> warnings = new ArrayList<>();
        String[] lines = code.split("\n");
        // Long function check
        if (lines.length > 20) {
            warnings.add("Long function detected.");
        }
        // Duplicate line detection
        Set<String> uniqueLines = new HashSet<>();
        for (String line : lines) {
            if (!uniqueLines.add(line.trim())) {
                warnings.add("Duplicate line found: " + line);
            }
        }
        // Mock AI response
        String refactoredCode = code
                .replace("int a", "int number")
                .replace("temp", "temporaryValue");
        String explanation =
                "Variable names improved and duplicate lines detected.";
        response.setOriginalCode(code);
        response.setRefactoredCode(refactoredCode);
        response.setExplanation(explanation);
        response.setWarnings(warnings);
        return response;
    }
}
