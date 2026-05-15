package com.example.backend.controller;

import com.example.backend.model.CodeRequest;
import com.example.backend.model.CodeResponse;
import com.example.backend.service.GeminiService;
import com.example.backend.service.MiniService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class RefactorController {
    @Autowired
    private GeminiService geminiService;

    @Autowired
    private MiniService miniService;

    @PostMapping("/refactor")
    public CodeResponse refactor(@RequestBody CodeRequest request) {
        try {
            // Validate request
            if (request == null || request.getCode() == null) {
                return createErrorResponse("", "Invalid request: code is required",
                        List.of("Validation error"));
            }

            String code = request.getCode().trim();
            
            // Check if code is empty
            if (code.isEmpty()) {
                return createErrorResponse("", "Code cannot be empty",
                        List.of("Validation error"));
            }

            CodeResponse res = geminiService.refactorCode(code);
            
            // If Gemini didn't produce useful structured output, fall back to MiniService
            if ((res.getRefactoredCode() == null || res.getRefactoredCode().isBlank())
                    || res.getRefactoredCode().contains("No structured output could be parsed")
                    || res.getRefactoredCode().contains("No response returned from model")) {
                return miniService.refactorCode(code);
            }
            return res;
        } catch (Exception e) {
            // Log the exception
            System.err.println("Error in refactor endpoint: " + e.getMessage());
            e.printStackTrace();
            
            // Return error response
            return createErrorResponse("", "Server error: " + e.getMessage(),
                    List.of("Exception", e.getClass().getSimpleName()));
        }
    }

    // Helper method to create error response
    private CodeResponse createErrorResponse(String originalCode, String explanation, List<String> warnings) {
        CodeResponse errorResponse = new CodeResponse();
        errorResponse.setOriginalCode(originalCode);
        errorResponse.setRefactoredCode("");
        errorResponse.setExplanation(explanation);
        errorResponse.setWarnings(warnings);
        return errorResponse;
    }
}
