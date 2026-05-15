import React from "react";

function ResultSection({ result }) {
  try {
    if (!result) return null;

    // If backend returned a raw string, try to parse it as JSON. If parsing fails,
    // display the raw string as the full response.
    let data = result;
    if (typeof result === "string") {
      try {
        data = JSON.parse(result);
      } catch (e) {
        return (
          <div className="results">
            <h2>Result</h2>
            <pre>{result}</pre>
          </div>
        );
      }
    }

    // Safely handle undefined or null values
    const warnings = Array.isArray(data.warnings) ? data.warnings : [];
    const originalCode = data.originalCode ?? "";
    const refactoredCode = data.refactoredCode ?? "";
    const explanation = data.explanation ?? "";

    // Check if response contains error indicators
    const isError = explanation.toLowerCase().includes("error") ||
                   refactoredCode === "" ||
                   refactoredCode.toLowerCase().includes("error");

    return (
      <div className="results">
        <h2>Warnings</h2>
        <ul>
          {warnings.length > 0 ? (
            warnings.map((warning, index) => <li key={index}>{warning}</li>)
          ) : (
            <li>No warnings</li>
          )}
        </ul>

        <h2>Original Code</h2>
        <pre>{originalCode || "No code provided"}</pre>

        <h2>Refactored Code</h2>
        <pre>{refactoredCode || "No refactored code available"}</pre>

        <h2>Explanation</h2>
        <p>{explanation || "No explanation available"}</p>

        {isError && (
          <div className="error-message">
            ⚠️ There was an issue processing your code. Please check the error details above.
          </div>
        )}
      </div>
    );
  } catch (e) {
    // Fallback error handling for any unexpected exceptions
    console.error("Error rendering ResultSection:", e);
    return (
      <div className="results error-message">
        <h2>Error</h2>
        <p>Failed to display results. Error: {e.message}</p>
      </div>
    );
  }
}

export default ResultSection;
