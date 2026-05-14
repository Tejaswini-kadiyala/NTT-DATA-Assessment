package com.example.backend.controller;

import com.example.backend.model.CodeRequest;
import com.example.backend.model.CodeResponse;
import com.example.backend.service.RefactorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class RefactorController {
    @Autowired
    private RefactorService refactorService;

    @PostMapping("/refactor")
    public CodeResponse refactor(@RequestBody CodeRequest request) {
        return refactorService.processCode(request.getCode());
    }
}
