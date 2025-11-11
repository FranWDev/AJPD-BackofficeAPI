import { invalidateServiceWorkersCache } from "../api/cacheService.js";

document.addEventListener("DOMContentLoaded", () => {
  const cacheDeleteButton = document.getElementById("cache-delete");
  const cacheResponse = document.getElementById("cache-response");

  cacheDeleteButton.addEventListener("click", () => {
    invalidateServiceWorkersCache()
      .then((data) => {
        cacheResponse.innerHTML = data.message;
      })
      .catch((err) => {
        cacheResponse.innerHTML = err.message;
      });
  });
});
