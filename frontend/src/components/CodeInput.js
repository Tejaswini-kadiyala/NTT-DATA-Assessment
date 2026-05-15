import React, { useState } from "react";

function CodeInput({ code, setCode, analyzeCode }) {
  const [error, setError] = useState("");

  const handleAnalyzeClick = () => {
    // Clear previous error
    setError("");

    // Validate if code is empty
    if (!code || code.trim().length === 0) {
      setError("❌ Please paste your code before analyzing.");
      return;
    }

    // Call the API
    analyzeCode();
  };

  return (
    <div>
      {error && <div className="error-message">{error}</div>}

      <textarea
        rows="15"
        value={code}
        onChange={(e) => {
          setCode(e.target.value);
          setError(""); // Clear error when user starts typing
        }}
        placeholder="Paste your code here..."
      />

      <button onClick={handleAnalyzeClick}>
        Analyze Code
      </button>
    </div>
  );
}

export default CodeInput;
