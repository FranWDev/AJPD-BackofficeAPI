export const newsService = {
  getNew,
  getNews,
  addNew,
  deleteNew,
};

async function getNew(id) {
  const response = await fetch(`/api/news/${id}`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
  });
  return response.json();
}
async function getNews() {
  const response = await fetch("/api/news", {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
  });
  return await response.json();
}

async function addNew(data) {
  const response = await fetch("/api/news", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });
  return response;
}
async function deleteNew(id) {
  const response = await fetch(`/api/news/${id}`, {
    method: "DELETE",
    headers: {
      "Content-Type": "application/json",
    },
  });
  return response.json();
}
