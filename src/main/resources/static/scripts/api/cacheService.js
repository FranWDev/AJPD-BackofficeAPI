export const invalidateServiceWorkersCache = () => {
  return fetch("/api/cache/invalidate/service-workers")
    .then((response) => response.json())
    .then((data) => {
      return data;
    });
};
