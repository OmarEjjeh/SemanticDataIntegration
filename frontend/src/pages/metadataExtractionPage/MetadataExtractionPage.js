import React, { useCallback, useEffect, useState } from "react";

import { DatasetList } from "./components/datasetList";
import { DateFilter } from "./components/dateFilter";
import { QualityFilter } from "./components/qualityFilter";
import { SearchInput } from "./components/searchInput";

import { fetchDatasets } from "./fetchDatasets";

import "./metadataExtractionPage.css";

const EMPTY_ARRAY = [];
const INITIAL_QUERY = {
  keywords: "",
  dataQuality: "all"
};
const INITIAL_DATASET_STATE = {
  isLoading: false,
  results: EMPTY_ARRAY,
  loaded: false
};

const MetadataExtractionPage = () => {
  const [query, setQuery] = useState(INITIAL_QUERY);
  const [datasetState, setDatasetState] = useState(INITIAL_DATASET_STATE);

  const onKeywordChange = useCallback((value) => {
    setQuery((prevQuery) => ({ ...prevQuery, keywords: value }));
  }, []);

  const onQualityChange = useCallback((value) => {
    setQuery((prevQuery) => ({ ...prevQuery, dataQuality: value }));
  }, []);

  const onFromDateChange = useCallback((value) => {
    setQuery((prevQuery) => ({ ...prevQuery, fromDate: value }));
  }, []);

  const onToDateChange = useCallback((value) => {
    setQuery((prevQuery) => ({ ...prevQuery, toDate: value }));
  }, []);

  useEffect(() => {
    setDatasetState((prevState) => ({ ...prevState, isLoading: true }));
    fetchDatasets(query)
      .then((data) => {
        if (!data.success) {
          throw new Error();
        }
        console.log("data >>>>>",data.data)
        setDatasetState((prevState) => ({
          ...prevState,
          results: data.data,
          isLoading: false,
          loaded: true
        }));
      })
      .catch((error) => {
        console.log("catch data >>>>>",error)
        setDatasetState((prevState) => ({
          ...prevState,
          isLoading: false,
          loaded: false
        }));
      });
  }, [query]);

  return (
    <div className="metadata-page-container">
      <div className="filters-row">
        <SearchInput onChange={onKeywordChange} />
      </div>
      <div className="filters-row">
        <QualityFilter onChange={onQualityChange} />
        <DateFilter onChange={onFromDateChange} label="From Date:" />
        <DateFilter onChange={onToDateChange} label="To Date:" />
      </div>

      <DatasetList
        isLoading={datasetState.isLoading}
        datasets={datasetState.results}
      />
    </div>
  );
};

export default MetadataExtractionPage;
