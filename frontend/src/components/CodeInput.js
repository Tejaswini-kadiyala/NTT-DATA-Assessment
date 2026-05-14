import React from "react";

function CodeInput({ code, setCode, analyzeCode }) {
  return (
    <div>
      <textarea
        rows="15"
        value={code}
        onChange={(e) => setCode(e.target.value)}
        placeholder="Paste your code here..."
      />

      <button onClick={analyzeCode}>
        Analyze Code
      </button>
    </div>
  );
}

export default CodeInput;
