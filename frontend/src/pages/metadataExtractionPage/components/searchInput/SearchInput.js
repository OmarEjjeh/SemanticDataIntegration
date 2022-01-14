import React, { useCallback, useState } from "react";
import _debounce from "lodash/debounce";

import closeIcon from "./close.svg";
import searchIcon from "./search.svg";

import "./searchInput.css";

const DEBOUNCE_TIMER = 500;

const SearchInput = (props) => {
  const { onChange } = props;
  const [value, setValue] = useState("");

  // eslint-disable-next-line
  const debouncedOnChange = useCallback(_debounce(onChange, DEBOUNCE_TIMER), [
    onChange
  ]);

  const handleTextChange = useCallback(
    (event) => {
      const newValue = event.target.value;
      setValue(newValue);
      debouncedOnChange(newValue);
    },
    [debouncedOnChange]
  );

  const handleResetValue = useCallback(() => {
    setValue("");
    onChange("");
  }, [onChange]);

  return (
    <div className="search-input-container">
      <img className="search-icon" alt="search" src={searchIcon} />
      <input
        className="search-input"
        type="text"
        placeholder="Search datasets..."
        value={value}
        onChange={handleTextChange}
      />
      {!!value.length && (
        <div className="close-icon-container" onClick={handleResetValue}>
          <img className="close-icon" alt="close" src={closeIcon} />
        </div>
      )}
    </div>
  );
};

export default React.memo(SearchInput);
