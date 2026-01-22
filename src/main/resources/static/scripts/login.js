document.addEventListener("DOMContentLoaded", function () {
  const loginForm = document.getElementById("loginForm");
  const loginButton = document.getElementById("loginButton");
  const buttonText = document.getElementById("buttonText");
  const spinner = document.getElementById("spinner");
  const passwordInput = document.getElementById("password");
  const errorMessage = document.getElementById("errorMessage");
  const errorTitle = document.getElementById("errorTitle");
  const errorText = document.getElementById("errorText");

  // Verificar si ya hay un token JWT válido (ej: cuando se abre la PWA)
  function checkExistingAuth() {
    const cookies = document.cookie.split(';');
    for (let cookie of cookies) {
      const [name, value] = cookie.trim().split('=');
      if (name === 'jwt' && value) {
        // Si hay un token JWT, redirigir a editor
        console.log('JWT válido encontrado, redirigiendo a editor...');
        window.location.href = "/editor";
        return;
      }
    }
  }

  // Verificar autenticación al cargar la página
  checkExistingAuth();

  passwordInput.addEventListener("input", function () {
    errorMessage.style.display = "none";
  });

  loginForm.addEventListener("submit", function (event) {
    event.preventDefault();

    errorMessage.style.display = "none";

    loginButton.disabled = true;
    buttonText.style.display = "none";
    spinner.style.display = "block";

    const password = passwordInput.value;
    const data = { password: password };

    fetch("/api/auth/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    })
      .then((response) => {
        if (!response.ok) {
          if (response.status === 401) {
            throw {
              title: "Contraseña incorrecta",
              message: "La contraseña ingresada no es válida. Intenta nuevamente.",
              status: 401,
            };
          } else if (response.status === 503) {
            throw {
              title: "Aplicación iniciándose",
              message:
                "La aplicación se está iniciando. Esto puede tardar hasta 1 minuto. Intenta nuevamente en unos momentos.",
              status: 503,
            };
          } else {
            throw {
              title: "Error en el login",
              message: "Ocurrió un error al procesar tu solicitud. Intenta nuevamente.",
              status: response.status,
            };
          }
        }
        return response.json();
      })
      .then((data) => {
        if (data.token) {
          document.cookie = `jwt=${data.token}; path=/; max-age=86400; SameSite=Strict`;
          console.log("Login exitoso");
          window.location.href = "/editor";
        } else {
          throw {
            title: "Error en la respuesta",
            message: "No se recibió el token. Intenta nuevamente.",
            status: 500,
          };
        }
      })
      .catch((error) => {
        console.error("Login error:", error);

        // Restore button state
        loginButton.disabled = false;
        buttonText.style.display = "inline";
        spinner.style.display = "none";
        passwordInput.focus();

        // Show error message
        if (error.title && error.message) {
          errorTitle.textContent = error.title;
          errorText.textContent = error.message;
        } else {
          errorTitle.textContent = "Error en el login";
          errorText.textContent = error.message || "Algo salió mal. Intenta nuevamente.";
        }
        errorMessage.style.display = "flex";
      });
  });
});
