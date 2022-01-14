import axios from "axios";

const axiosReq = axios.create();

function download(blob, filename) {
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.style.display = 'none';
  a.href = url;
  // the filename you want
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  window.URL.revokeObjectURL(url);
}

const downloadDataset = (query) => {
  const encodedUrl = encodeURIComponent(query.url);
  const URL = `http://localhost:8080/download?link=${encodedUrl}&type=${query.type}`;

  console.log("downloadDataset URL::::::::::::::::", URL);

   //return axiosReq.get(URL).then(response=>response.blob()).then(blob => download(blob));
  //return Promise.resolve({});
  var fetch = require('node-fetch');

return fetch(URL, {
    headers: {
        'accept': '*/*'
    }
}).then(res => res.blob()).then(blob => download(blob,"metadata."+query.type));
};

export { downloadDataset };
