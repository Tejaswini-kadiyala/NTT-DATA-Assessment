package com.example.backend.service;

import com.example.backend.model.CodeResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CodeResponse refactorCode(String code) {
        String url =
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                        + apiKey;

        String prompt = """
            Refactor the following code and provide:
            1. Refactored Code
            2. Naming Suggestions
            3. Explanation

            Code:
            """ + code;

        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(textPart));
        Map<String, Object> request = Map.of("contents", List.of(content));

        ResponseEntity<String> response =
                restTemplate.postForEntity(url, request, String.class);

        String body = response.getBody();

        String generated = extractGeneratedText(body);

        // Attempt to split the model output into sections. The model was prompted
        // to return: 1. Refactored Code, 2. Naming Suggestions, 3. Explanation.
        String refactored = "";
        String naming = "";
        String explanation = "";

        // If the HTTP response was not successful, record that in explanation and
        // include the raw body for debugging.
        if (!response.getStatusCode().is2xxSuccessful()) {
            explanation = "API returned status " + response.getStatusCode().value() + ".\n\nRaw response:\n" + (body != null ? body : "");
            CodeResponse errorCr = new CodeResponse();
            errorCr.setOriginalCode(code);
            errorCr.setRefactoredCode("");
            errorCr.setExplanation(explanation);
            errorCr.setWarnings(List.of("API returned non-success status: " + response.getStatusCode()));
            return errorCr;
        }

        if (generated != null && !generated.isBlank()) {
            // Try headings first
            int idxRef = indexOfIgnoreCase(generated, "refactored code");
            int idxNaming = indexOfIgnoreCase(generated, "naming suggestion");
            if (idxNaming == -1) idxNaming = indexOfIgnoreCase(generated, "naming suggestions");
            int idxExpl = indexOfIgnoreCase(generated, "explanation");

            if (idxRef != -1 && (idxNaming != -1 || idxExpl != -1)) {
                int endRef = generated.length();
                if (idxNaming != -1) endRef = Math.min(endRef, idxNaming);
                if (idxExpl != -1) endRef = Math.min(endRef, idxExpl);

                refactored = generated.substring(idxRef, endRef).trim();

                if (idxNaming != -1) {
                    int endNaming = idxExpl != -1 ? idxExpl : generated.length();
                    naming = generated.substring(idxNaming, endNaming).trim();
                }

                if (idxExpl != -1) {
                    explanation = generated.substring(idxExpl).trim();
                }
            } else {
                // Try numeric markers like "1.", "2.", "3." as a fallback
                int one = indexOfIgnoreCase(generated, "1.");
                int two = indexOfIgnoreCase(generated, "2.");
                int three = indexOfIgnoreCase(generated, "3.");
                if (one != -1 && two != -1) {
                    refactored = generated.substring(one, two).trim();
                    if (three != -1) {
                        naming = generated.substring(two, three).trim();
                        explanation = generated.substring(three).trim();
                    } else {
                        naming = generated.substring(two).trim();
                    }
                } else {
                    // As a last resort, put the entire generated text into refactored
                    refactored = generated.trim();
                }
            }
        }

        CodeResponse cr = new CodeResponse();
        cr.setOriginalCode(code);

        // If we couldn't parse anything meaningful, place the raw body into the
        // refactored code field so the frontend displays something useful.
        boolean hasMeaningful = (!refactored.isBlank()) || (!naming.isBlank()) || (!explanation.isBlank());
        if (!hasMeaningful) {
            if (body != null && !body.isBlank()) {
                cr.setRefactoredCode(body);
                cr.setExplanation("No structured output could be parsed from the model response. Raw response placed in Refactored Code.");
            } else {
                cr.setRefactoredCode("No response returned from model.");
                cr.setExplanation("No response returned from model.");
            }
            cr.setWarnings(new ArrayList<>());
            return cr;
        }

        cr.setRefactoredCode(refactored);

        String combinedExplanation = "";
        if (!naming.isBlank()) {
            combinedExplanation += naming.trim();
        }
        if (!explanation.isBlank()) {
            if (!combinedExplanation.isBlank()) combinedExplanation += "\n\n";
            combinedExplanation += explanation.trim();
        }
        cr.setExplanation(combinedExplanation);
        cr.setWarnings(new ArrayList<>());

        return cr;
    }

    // Extract a likely human-readable generated string from the API JSON body.
    private String extractGeneratedText(String body) {
        if (body == null) return null;
        // Try to parse JSON and look for common fields returned by generative APIs.
        try {
            Object parsed = objectMapper.readValue(body, Object.class);
            Object found = findFirstStringValue(parsed);
            if (found != null) return found.toString();
        } catch (JsonProcessingException e) {
            // Not JSON, treat body as raw text
            return body;
        }
        // Fallback
        return body;
    }

    private Object findFirstStringValue(Object node) {
        if (node == null) return null;
        if (node instanceof String) {
            String s = ((String) node).trim();
            if (!s.isEmpty()) return s;
            return null;
        }
        if (node instanceof Map) {
            Map<?, ?> m = (Map<?, ?>) node;
            // Check some common keys first
            String[] keys = new String[]{"candidates", "outputs", "output", "result", "response", "message", "content", "text"};
            for (String k : keys) {
                if (m.containsKey(k)) {
                    Object child = m.get(k);
                    Object found = findFirstStringValue(child);
                    if (found != null) return found;
                }
            }
            // Otherwise iterate
            for (Object val : m.values()) {
                Object found = findFirstStringValue(val);
                if (found != null) return found;
            }
        }
        if (node instanceof List) {
            List<?> lst = (List<?>) node;
            for (Object item : lst) {
                Object found = findFirstStringValue(item);
                if (found != null) return found;
            }
        }
        return null;
    }

    private int indexOfIgnoreCase(String haystack, String needle) {
        if (haystack == null || needle == null) return -1;
        return haystack.toLowerCase().indexOf(needle.toLowerCase());
    }
}

