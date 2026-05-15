import React, { useState } from "react";
import axios from "axios";
import "./App.css";
import CodeInput from "./components/CodeInput";
import ResultSection from "./components/ResultSection";
import Loading from "./components/Loading";

function App() {
  const [code, setCode] = useState("");
  const [result, setResult] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");


  const analyzeCode = async () => {
    try {
      setError("");
      setIsLoading(true);
      const response = await axios.post(
        "http://localhost:8080/api/refactor",
        { code }
      );

      // Check if response has data
      if (!response.data) {
        setError("Error: Empty response from server");
        setResult(null);
        return;
      }

      setResult(response.data);
    } catch (error) {
      // Handle different types of errors
      let errorMessage = "An error occurred while analyzing code.";

      if (error.response) {
        // Server responded with error status
        errorMessage = `Server error: ${error.response.status} - ${error.response.statusText}`;
      } else if (error.request) {
        // Request made but no response received
        errorMessage = "No response from server. Please check if the backend is running on http://localhost:8080";
      } else if (error.message) {
        // Error in request setup or other errors
        errorMessage = `Error: ${error.message}`;
      }

      console.error("API Error:", error);
      setError(errorMessage);
      setResult(null);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="container">
      <h1>Code Refactor Assistant</h1>
      {error && <div className="error-message">{error}</div>}
      <CodeInput code={code} setCode={setCode} analyzeCode={analyzeCode} />
      {result && <ResultSection result={result} />}
      {isLoading && <Loading />}
    </div>
  );
}

export default App;
