import React from "react";

import fraunhofer_logo from "./assets/fraunhofer_logo.png";
import rwth_logo from "./assets/rwth-logo.png";

import "./header.css";

const Header = (props) => {
  return (
    <div className="header-container">
      <img className="logo-image-1" alt="fraunhofer" src={fraunhofer_logo} />
      <div className="header-content">
        <p className="header-title">Metadata Extraction</p>
        <p className="header-desc">Summer 2021</p>
      </div>
      <img className="logo-image-2" alt="rwth" src={rwth_logo} />
    </div>
  );
};

export default Header;
