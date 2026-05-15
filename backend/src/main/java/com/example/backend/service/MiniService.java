package com.example.backend.service;

import com.example.backend.model.CodeResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MiniService {

    public CodeResponse refactorCode(String code) {
        try {
            CodeResponse cr = new CodeResponse();
            cr.setOriginalCode(code == null ? "" : code);

            if (code == null || code.isBlank()) {
                cr.setRefactoredCode("");
                cr.setExplanation("No code provided to refactor.");
                cr.setWarnings(new ArrayList<>());
                return cr;
            }

            List<String> warnings = new ArrayList<>();
            String refactored = code;
            
            try {
                // Fix common syntax errors and code quality issues
                
                // 1. Fix double semicolons
                if (refactored.contains(";;")) {
                    refactored = refactored.replaceAll(";{2,}", ";");
                    warnings.add("Fixed double semicolons");
                }
                
                // 2. Fix missing newline in printf statements (for C)
                if (refactored.contains("printf(") && !refactored.contains("\\n")) {
                    refactored = refactored.replaceAll("printf\\(\"([^\"]*?)\"\\)", "printf(\"$1\\\\n\")");
                    warnings.add("Added newline to printf statement");
                }
                
                // 3. Fix return statement in main (should return 0, not void)
                if (refactored.contains("int main()") || refactored.contains("int main(void)")) {
                    refactored = refactored.replaceAll("return\\s*;", "return 0;");
                    warnings.add("Fixed return statement in main function");
                }
                
                // 4. Clean up whitespace
                String[] lines = refactored.split("\r?\n");
                StringBuilder sb = new StringBuilder();
                boolean lastBlank = false;
                
                for (String line : lines) {
                    try {
                        // Trim trailing spaces but preserve indentation
                        String trimmed = line.replaceAll("\\s+$", "");
                        boolean isBlank = trimmed.isBlank();
                        
                        // Skip duplicate blank lines
                        if (isBlank && lastBlank) continue;
                        
                        sb.append(trimmed).append("\n");
                        lastBlank = isBlank;
                    } catch (Exception lineError) {
                        // If error processing a line, skip it and add warning
                        System.err.println("Error processing line: " + lineError.getMessage());
                        sb.append(line).append("\n");
                    }
                }

                refactored = sb.toString().trim();
            } catch (Exception processingError) {
                // If regex or string processing fails, return original code with warning
                System.err.println("Error during refactoring: " + processingError.getMessage());
                processingError.printStackTrace();
                warnings.add("Error during processing: " + processingError.getClass().getSimpleName());
                refactored = code;
            }

            cr.setRefactoredCode(refactored);
            cr.setWarnings(warnings);

            String explanation;
            if (!warnings.isEmpty()) {
                explanation = "Applied automatic fixes: " + String.join(", ", warnings) + ".";
            } else {
                explanation = "Code formatting cleaned up: trimmed trailing whitespace and removed duplicate blank lines.";
            }
            
            cr.setExplanation(explanation);

            return cr;
        } catch (Exception e) {
            // Catch any unexpected exceptions at the method level
            System.err.println("Unexpected error in MiniService: " + e.getMessage());
            e.printStackTrace();
            
            CodeResponse errorCr = new CodeResponse();
            errorCr.setOriginalCode(code != null ? code : "");
            errorCr.setRefactoredCode("");
            errorCr.setExplanation("Error processing code: " + e.getMessage());
            errorCr.setWarnings(List.of("Exception", e.getClass().getSimpleName()));
            return errorCr;
        }
    }
}


