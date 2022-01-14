import axios from "axios";

const axiosReq = axios.create();

export const submitData = (payload) => {
  return axiosReq.post("http://localhost:8080/submit", payload);
};
