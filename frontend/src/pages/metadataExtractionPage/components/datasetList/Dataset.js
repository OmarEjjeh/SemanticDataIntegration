import React, { useCallback, useState } from "react";
import moment from "moment";
import cx from "classnames";
import _isEmpty from "lodash/isEmpty";
import _map from "lodash/map";

import { downloadDataset } from "./downloadDataset";

import { DOWNLOAD_TYPE_OPTIONS } from "./constants";

import downloadIcon from "./download.svg";

const formatDate = (time) => moment(time).format("YYYY-MM-DD");

const Dataset = ({ dataset }) => {
  const [isExpanded, setIsExpanded] = useState(false);
  const { url } = dataset;

  const handleExpandToggle = useCallback(() => {
    setIsExpanded((prevValue) => !prevValue);
  }, []);

  const handleDownload = useCallback(() => {
    window.open(url, "_blank");
  }, [url]);

  const handleDownloadTypeChange = useCallback(
    (event) => {
      const type = event.target.value;
      downloadDataset({ type, url });
    },
    [url]
  );

  const renderDownloadIcon = () => (
    <button className="download-icon-container" onClick={handleDownload}>
      <img
        className="download-icon"
        title="Download"
        alt="download"
        src={downloadIcon}
      />
    </button>
  );

  const renderDownloadOption = () => (
    <div className="download-option">
      <label className="download-type-label" htmlFor="downloadType-select">
        Download Metadata As:
      </label>
      <select
        onChange={handleDownloadTypeChange}
        name="download-type"
        id="downloadType-select"
      >
        <option hidden disabled selected key=" " value=" ">
          {" "}
        </option>
        {DOWNLOAD_TYPE_OPTIONS.map((type) => (
          <option key={type.id} value={type.id}>
            {type.label}
          </option>
        ))}
      </select>
    </div>
  );

  const renderValue = (label, value) => {
    return (
      <div className="dataset-extra-values">
        <span className="dataset-extra-values-label">{label}</span>:{" "}
        <span className="dataset-extra-values-value">{value}</span>
      </div>
    );
  };

  const renderExtraContent = () => {
    const filemetadata = dataset.filemetadata || {};
    const dataQualityDetailed = dataset.dataQualityDetailed || {};
    return (
      <div className="dataset-extrafields">
        <div className="dataset-file-metadata-cnt">
          <div className="dataset-file-metadata">
            <p className="file-metadata-title">File Metadata</p>
            {renderValue("Filename", filemetadata.file_name)}
            {renderValue("Type", filemetadata.file_type)}
            {renderValue("Creation Date", filemetadata.creation_date)}
            {renderValue("Size", `${filemetadata.file_size} bytes`)}
          </div>
        </div>
        <div className="dataset-file-dataQualityDetails-cnt">
          <div className="dataset-file-dataQualityDetails">
            <p className="file-metadata-title">Data Quality Details</p>
            {renderValue(
              "File Quality Score",
              dataQualityDetailed.file_quality_score
            )}
            {renderValue("Percent NA", dataQualityDetailed.percentNA)}
            {renderValue(
              "Percent Missing",
              dataQualityDetailed.percentage_missing
            )}
          </div>
        </div>
        {renderDownloadOption()}
      </div>
    );
  };

  return (
    <div className="dataset">
      <div className="dataset-title">{dataset.title}</div>
      <div
        className={cx("dataset-description", {
          "dataset-description--clamp": !isExpanded
        })}
      >
        {dataset.description}
      </div>
      {_isEmpty(dataset.keywords) ? null : (
        <div className="dataset-keywords-container">
          {_map(dataset.keywords, (keyword, index) => (
            <div key={index} className="dataset-keyword">
              {keyword}
            </div>
          ))}
        </div>
      )}
      <div className="dataset-footer">
        <div className="dataset-timeframe">
          Timeframe: {formatDate(dataset.timeframeStart)} to{" "}
          {formatDate(dataset.timeframeEnd)}
        </div>
        <div className="dataset-footer--right">
          <div
            className={`dataset-quality dataset-quality--${dataset.dataQuality}`}
          >
            {dataset.dataQuality}
          </div>
          {renderDownloadIcon()}
        </div>
      </div>
      {isExpanded ? renderExtraContent() : null}
      <button className="dataset-expand-btn" onClick={handleExpandToggle}>
        {isExpanded ? "Show Less" : "Show More Details"}
      </button>
    </div>
  );
};

export { Dataset };
