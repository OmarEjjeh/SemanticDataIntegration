import React from "react";

import "./textInput.css";

const TextInput = (props) => {
  return (
    <div className="text-input-container">
      <label className="text-input-label" htmlFor="text-input">
        {props.label}
      </label>
      <input
        className="text-input"
        type="text"
        placeholder={props.placeholder}
        id="text-input"
        value={props.value}
        onChange={props.onChange}
      />
    </div>
  );
};

export default TextInput;
