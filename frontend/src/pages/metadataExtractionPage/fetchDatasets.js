import axios from "axios";

import DUMMY_DATA from "./dummyData";
//import DUMMY_RESPONSE from "./dummyResponse";

const axiosReq = axios.create();

const fetchDatasets = (query) => {
  let URL = `http://localhost:8080/query?dataquality=${query.dataQuality}`;
  URL = query.keywords ? `${URL}&keywords=${query.keywords}` : URL ;
  URL = query.fromDate ? `${URL}&timeframeStart=${query.fromDate}` : URL;
  URL = query.toDate ? `${URL}&timeframeEnd=${query.toDate}` : URL;

  console.log("fetchDatasets URL::::::::::::::::", URL);

  // return Promise.resolve({ success: true, data: DUMMY_DATA });
  //if (query.keywords) {
    //return Promise.resolve({ success: true, data: [] });
  //}
var fetch = require('node-fetch');

return fetch(URL, {
    headers: {
        'accept': '*/*'
    }
}).then(res => res.json());
//
//  //return Promise.resolve(DUMMY_RESPONSE);
//return axiosReq.get(URL);
};
  
export { fetchDatasets };
