package com.example.backend.controller;

import com.example.backend.model.CodeRequest;
import com.example.backend.model.CodeResponse;
import com.example.backend.service.GeminiService;
import com.example.backend.service.MiniService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
        CodeResponse res = geminiService.refactorCode(request.getCode());
        // If Gemini didn't produce useful structured output, fall back to MiniService
        if ((res.getRefactoredCode() == null || res.getRefactoredCode().isBlank())
                || res.getRefactoredCode().contains("No structured output could be parsed")
                || res.getRefactoredCode().contains("No response returned from model")) {
            return miniService.refactorCode(request.getCode());
        }
        return res;
    }
}
