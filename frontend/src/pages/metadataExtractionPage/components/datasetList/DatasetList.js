import React from "react";
import Loader from "react-loader-spinner";
import _map from "lodash/map";
import _isEmpty from "lodash/isEmpty";

import { Dataset } from "./Dataset";

import noResultsIcon from "./no_results.svg";

import "./datasetList.css";

const DatasetList = (props) => {
  if (props.isLoading) {
    return (
      <Loader
        className="datasets-loader"
        type="ThreeDots"
        color="#73737d"
        height={40}
        width={40}
      />
    );
  }

  if (_isEmpty(props.datasets)) {
    return (
      <div className="datasets-empty">
        <img
          className="no-results-icon"
          title="No results"
          alt="No results"
          src={noResultsIcon}
        />
        No results found!
      </div>
    );
  }

  return (
    <div className="datasets-container">
      {_map(props.datasets, (dataset) => (
        <Dataset dataset={dataset} key={dataset.id} />
      ))}
    </div>
  );
};

export default React.memo(DatasetList);
