document.addEventListener("DOMContentLoaded", function () {
  const loginForm = document.getElementById("loginForm");

  loginForm.addEventListener("submit", function (event) {
    event.preventDefault();
    const password = document.getElementById("password").value;

    const data = { password: password };

    fetch("/api/auth/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    })
      .then((response) => {
        if (!response.ok) throw new Error("Login fallido");
        return response.json();
      })
      .then((data) => {
        document.cookie = `jwt=${data.token}; path=/; max-age=86400; SameSite=Strict`;

        console.log(data);
        console.log(data.token);

        window.location.href = "/editor";
      })
      .catch((error) => {
        console.error(error);
        alert("Error en el login: " + error.message);
      });
  });
});
