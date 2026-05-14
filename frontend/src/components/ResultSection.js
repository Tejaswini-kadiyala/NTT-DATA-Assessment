import React from "react";

function ResultSection({ result }) {
  return (
    <div className="results">

      <h2>Warnings</h2>
      <ul>
        {result.warnings.map((warning, index) => (
          <li key={index}>{warning}</li>
        ))}
      </ul>

      <h2>Original Code</h2>
      <pre>{result.originalCode}</pre>

      <h2>Refactored Code</h2>
      <pre>{result.refactoredCode}</pre>

      <h2>Explanation</h2>
      <p>{result.explanation}</p>

    </div>
  );
}

export default ResultSection;
