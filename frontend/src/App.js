import React from "react";

import { MetadataExtractionPage } from "./pages/metadataExtractionPage";
import { UploadPage } from "./pages/uploadPage";
import { Header } from "./components/header";

import "./styles.css";

const PATHS = {
  DEFAULT: "/",
  UPLOAD: "/upload"
};

const getPageComponent = (pathname) => {
  switch (pathname) {
    case PATHS.DEFAULT:
      return <MetadataExtractionPage />;
    case PATHS.UPLOAD:
      return <UploadPage />;
    default:
      return null;
  }
};

const getGoToLabel = (pathname) => {
  switch (pathname) {
    case PATHS.DEFAULT:
      return "Go to Upload Page";
    case PATHS.UPLOAD:
      return "Go to Home Page";
    default:
      return null;
  }
};

const getGoToLink = (pathname) => {
  switch (pathname) {
    case PATHS.DEFAULT:
      return PATHS.UPLOAD;
    case PATHS.UPLOAD:
      return PATHS.DEFAULT;
    default:
      return null;
  }
};

const App = () => {
  const pathname = window.location.pathname;
  return (
    <div className="app-container">
      <Header />
      <div className="app">
        <a href={getGoToLink(pathname)} className="route-link">
          {getGoToLabel(pathname)}
        </a>
        {getPageComponent(pathname)}
      </div>
    </div>
  );
};

export { App };
