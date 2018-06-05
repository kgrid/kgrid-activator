import http from "k6/http";
import { sleep } from "k6";

export default function() {
  http.batch([
    // "http://localhost:8080",
    // "http://localhost:8080/99999/newko/v0.0.1",
    "http://localhost:8080/hello/world/v0.0.1",
    // "http://localhost:8080/99999/newkotwo"
  ]);
  // sleep(1);
};