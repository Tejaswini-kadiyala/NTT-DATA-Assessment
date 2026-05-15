import React from "react";

function Loading() {
  return (
    <div className="loading-overlay">
      <div className="loading-container">
        <div className="spinner"></div>
        <p className="loading-text">Processing your code...</p>
      </div>
    </div>
  );
}

export default Loading;

