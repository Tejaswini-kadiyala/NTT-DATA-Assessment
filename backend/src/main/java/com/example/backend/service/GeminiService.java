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
        try {
            String url =
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                            + apiKey;

            String prompt = """
                Refactor the following code. Return ONLY:
                1. **Refactored Code** - Clean code WITHOUT any inline comments or explanations, and with improved variable names, structure, and formatting. Do not include the original code in the response.
                2. **Explanation** - A brief 2-3 sentence summary of improvements made
                
                Code:
                """ + code;

            Map<String, Object> textPart = Map.of("text", prompt);
            Map<String, Object> content = Map.of("parts", List.of(textPart));
            Map<String, Object> request = Map.of("contents", List.of(content));

            ResponseEntity<String> response = null;
            try {
                response = restTemplate.postForEntity(url, request, String.class);
            } catch (Exception e) {
                return createErrorResponse(code, "API connection failed: " + e.getMessage(),
                        List.of("Connection error", e.getClass().getSimpleName()));
            }

            String body = response.getBody();
            String generated = extractGeneratedText(body);

            String refactored = "";
            String explanation = "";

            if (!response.getStatusCode().is2xxSuccessful()) {
                explanation = "API returned status " + response.getStatusCode().value() + ".";
                CodeResponse errorCr = new CodeResponse();
                errorCr.setOriginalCode(code);
                errorCr.setRefactoredCode("");
                errorCr.setExplanation(explanation);
                errorCr.setWarnings(List.of("API returned non-success status: " + response.getStatusCode()));
                return errorCr;
            }

            try {
                if (generated != null && !generated.isBlank()) {
                    int idxRef = indexOfIgnoreCase(generated, "refactored code");
                    int idxExpl = indexOfIgnoreCase(generated, "explanation");

                    if (idxRef != -1) {
                        int endRef = idxExpl != -1 ? idxExpl : generated.length();
                        if (endRef > idxRef && endRef <= generated.length()) {
                            String refactoredSection = generated.substring(idxRef, endRef).trim();
                            refactored = extractCodeBlock(refactoredSection);

                            if (refactored.isEmpty()) {
                                refactored = refactoredSection
                                        .replaceFirst("(?i)\\*\\*?refactored\\s+code\\*\\*?[:\\n]*", "")
                                        .trim();
                            }
                        }
                    }

                    if (idxExpl != -1) {
                        if (idxExpl <= generated.length()) {
                            String explanationSection = generated.substring(idxExpl).trim();
                            explanation = explanationSection
                                    .replaceFirst("(?i)\\*\\*?explanation\\*\\*?[:\\n]*", "")
                                    .trim();

                            explanation = explanation.replaceAll("(?i)```[\\s\\S]*?```", "").trim();
                            explanation = limitToSentences(explanation, 3);
                        }
                    } else {
                        int one = indexOfIgnoreCase(generated, "1.");
                        int two = indexOfIgnoreCase(generated, "2.");

                        if (one != -1 && two != -1 && two > one && two <= generated.length()) {
                            String section1 = generated.substring(one, two);
                            refactored = extractCodeBlock(section1);
                            if (refactored.isEmpty()) {
                                refactored = section1.replaceFirst("1\\.\\s*", "").trim();
                            }

                            if (two < generated.length()) {
                                explanation = generated.substring(two).replaceFirst("2\\.\\s*", "").trim();
                            }
                        } else {
                            refactored = generated.trim();
                        }
                    }
                }
            } catch (StringIndexOutOfBoundsException e) {
                return createErrorResponse(code, "Error parsing response: " + e.getMessage(),
                        List.of("Parse error", "StringIndexOutOfBoundsException"));
            }

            CodeResponse cr = new CodeResponse();
            cr.setOriginalCode(code);

            boolean hasMeaningful = (!refactored.isBlank()) || (!explanation.isBlank());
            if (!hasMeaningful) {
                if (body != null && !body.isBlank()) {
                    cr.setRefactoredCode(body);
                    cr.setExplanation("Could not parse structured response.");
                } else {
                    cr.setRefactoredCode("No response returned from model.");
                    cr.setExplanation("No response returned from model.");
                }
                cr.setWarnings(new ArrayList<>());
                return cr;
            }

            cr.setRefactoredCode(refactored);
            cr.setExplanation(explanation);
            cr.setWarnings(new ArrayList<>());

            return cr;
        } catch (Exception e) {
            return createErrorResponse(code, "Unexpected error: " + e.getMessage(),
                    List.of("Error", e.getClass().getSimpleName()));
        }
    }

    private CodeResponse createErrorResponse(String code, String explanation, List<String> warnings) {
        CodeResponse errorCr = new CodeResponse();
        errorCr.setOriginalCode(code);
        errorCr.setRefactoredCode("");
        errorCr.setExplanation(explanation);
        errorCr.setWarnings(warnings);
        return errorCr;
    }

    private String extractCodeBlock(String text) {
        try {
            if (text == null || text.isEmpty()) return "";

            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                    "```(?:(?:c|java|python|javascript|js)\\n)?([\\s\\S]*?)```",
                    java.util.regex.Pattern.CASE_INSENSITIVE
            );

            java.util.regex.Matcher matcher = pattern.matcher(text);

            if (matcher.find()) {
                String result = matcher.group(1);
                return result != null ? result.trim() : "";
            }

            return "";
        } catch (Exception e) {
            System.err.println("Error extracting code block: " + e.getMessage());
            return "";
        }
    }

    private String limitToSentences(String text, int numSentences) {
        try {
            if (text == null || text.isEmpty()) return text;

            String[] sentences = text.split("(?<=[.!?])\\s+");
            StringBuilder sb = new StringBuilder();

            int count = 0;
            for (String sentence : sentences) {
                if (count >= numSentences) break;
                if (sentence != null && !sentence.isEmpty()) {
                    sb.append(sentence).append(" ");
                    count++;
                }
            }

            return sb.toString().trim();
        } catch (Exception e) {
            System.err.println("Error limiting sentences: " + e.getMessage());
            return text;
        }
    }

    private String extractGeneratedText(String body) {
        if (body == null) return null;
        try {
            Object parsed = objectMapper.readValue(body, Object.class);
            Object found = findFirstStringValue(parsed);
            if (found != null) return found.toString();
        } catch (JsonProcessingException e) {
            return body;
        }
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
            String[] keys = new String[]{"candidates", "outputs", "output", "result", "response", "message", "content", "text"};
            for (String k : keys) {
                if (m.containsKey(k)) {
                    Object child = m.get(k);
                    Object found = findFirstStringValue(child);
                    if (found != null) return found;
                }
            }
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

