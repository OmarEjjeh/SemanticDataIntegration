import React, { useCallback, useState } from "react";
import { TextInput } from "./components/textInput";

import { submitData } from "./submitData";

import "./uploadPage.css";

const UploadPage = () => {
  const [url, setUrl] = useState("");
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");

  const handleUrlChange = useCallback((e) => {
    setUrl(e.target.value);
  }, []);

  const handleTitleChange = useCallback((e) => {
    setTitle(e.target.value);
  }, []);

  const handleDescriptionChange = useCallback((e) => {
    setDescription(e.target.value);
  }, []);

  const handleSubmit = useCallback(() => {
    submitData({ title, url, description }).then(() => {
      window.location.reload();
    });
  }, [description, title, url]);

  return (
    <div className="upload-page-container">
      <TextInput
        label="Enter the Title for Dataset"
        placeholder="Enter Title"
        value={title}
        onChange={handleTitleChange}
      />
      <TextInput
        label="Enter the URL for Dataset"
        placeholder="Enter URL"
        value={url}
        onChange={handleUrlChange}
      />
      <div className="text-area-container">
        <label className="text-area-label" htmlFor="text-area">
          Enter the description
        </label>
        <textarea
          className="text-area"
          placeholder="Enter description"
          id="text-area"
          value={description}
          onChange={handleDescriptionChange}
        />
      </div>
      <div className="upload-submit-btn-container">
        <button className="upload-submit-btn" onClick={handleSubmit}>
          Submit
        </button>
      </div>
    </div>
  );
};

export default UploadPage;
