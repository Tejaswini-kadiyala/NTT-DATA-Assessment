import React from "react";

function ResultSection({ result }) {
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

  const warnings = data.warnings ?? [];
  const originalCode = data.originalCode ?? "";
  const refactoredCode = data.refactoredCode ?? "";
  const explanation = data.explanation ?? "";

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
      <pre>{originalCode}</pre>

      <h2>Refactored Code</h2>
      <pre>{refactoredCode}</pre>

      <h2>Explanation</h2>
      <p>{explanation}</p>
    </div>
  );
}

export default ResultSection;
