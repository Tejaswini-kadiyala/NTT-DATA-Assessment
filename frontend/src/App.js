import React, { useState } from "react";
import axios from "axios";
import "./App.css";
import CodeInput from "./components/CodeInput";
import ResultSection from "./components/ResultSection";

function App() {
  const [code, setCode] = useState("");
  const [result, setResult] = useState(null);

  const analyzeCode = async () => {
    try {
      const response = await axios.post(
        "http://localhost:8080/api/refactor",
        { code }
      );
      setResult(response.data);
    } catch (error) {
      console.log(error);
    }
  };

  return (
    <div className="container">
      <h1>Code Refactor Assistant</h1>
      <CodeInput code={code} setCode={setCode} analyzeCode={analyzeCode} />
      {result && <ResultSection result={result} />}
    </div>
  );
}

export default App;
