cont = document.getElementById("logout-btn");
cont.addEventListener("click", function () {
  fetch("/api/auth/logout", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
  })
    .then((response) => response.json())
    .then((data) => {
      window.location.href = "/";
    })
    .catch((error) => {
      console.error(error);
      alert("Error al cerrar sesión: " + error.message);
    });
});
