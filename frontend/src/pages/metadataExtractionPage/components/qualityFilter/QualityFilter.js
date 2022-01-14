import React, { useCallback } from "react";

import "./qualityFilter.css";

const QUALITY_FILTER_OPTIONS = [
  {
    id: "all",
    label: "All"
  },
  {
    id: "excellent",
    label: "Excellent"
  },
  {
    id: "good",
    label: "Good"
  },
  {
    id: "sufficient",
    label: "Sufficient"
  },
  {
    id: "bad",
    label: "Bad"
  }
];

const QualityFilter = (props) => {
  const { onChange } = props;

  const handleQualityChange = useCallback(
    (event) => {
      onChange(event.target.value);
    },
    [onChange]
  );

  return (
    <div className="quality-filter-container">
      <label className="quality-filter-label" htmlFor="quality-select">
        Dataset Quality:
      </label>
      <select
        onChange={handleQualityChange}
        name="dataset-quality"
        id="quality-select"
      >
        {QUALITY_FILTER_OPTIONS.map((filter) => (
          <option key={filter.id} value={filter.id}>
            {filter.label}
          </option>
        ))}
      </select>
    </div>
  );
};

export default React.memo(QualityFilter);
