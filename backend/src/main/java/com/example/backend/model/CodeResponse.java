package com.example.backend.model;

import java.util.List;

public class CodeResponse {
    private String originalCode;
    private String refactoredCode;
    private String explanation;
    private List<String> warnings;

    public String getOriginalCode() { return originalCode; }
    public void setOriginalCode(String originalCode) { this.originalCode = originalCode; }
    public String getRefactoredCode() { return refactoredCode; }
    public void setRefactoredCode(String refactoredCode) { this.refactoredCode = refactoredCode; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
}
