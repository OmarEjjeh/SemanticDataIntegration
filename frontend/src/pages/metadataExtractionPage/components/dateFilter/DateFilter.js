import React, { useCallback } from "react";

import "./dateFilter.css";

const DateFilter = (props) => {
  const { onChange } = props;

  const handleDateChange = useCallback(
    (event) => {
      onChange(event.target.value);
    },
    [onChange]
  );

  return (
    <div className="date-filter-container">
      <label className="date-filter-label" htmlFor="date-filter">
        {props.label}
      </label>
      <input onChange={handleDateChange} type="date" id="date-filter" />
    </div>
  );
};

export default React.memo(DateFilter);
