import { newsService } from "../api/newsService.js";

let editor;
let news = {};
let featuredImage = null;

function waitForDependencies() {
  return new Promise((resolve) => {
    const checkInterval = setInterval(() => {
      if (
        typeof EditorJS !== "undefined" &&
        typeof Header !== "undefined" &&
        typeof Quote !== "undefined" &&
        typeof Warning !== "undefined" &&
        typeof Delimiter !== "undefined" &&
        typeof List !== "undefined" &&
        typeof NestedList !== "undefined" &&
        typeof Checklist !== "undefined" &&
        typeof ImageTool !== "undefined" &&
        typeof SimpleImage !== "undefined" &&
        typeof Embed !== "undefined" &&
        typeof LinkTool !== "undefined" &&
        typeof AttachesTool !== "undefined" &&
        typeof Table !== "undefined" &&
        typeof Marker !== "undefined" &&
        typeof Underline !== "undefined"
      ) {
        clearInterval(checkInterval);
        resolve();
      }
    }, 100);
  });
}

async function initEditor(post) {
  if (editor) {
    try {
      await editor.destroy();
      editor = null;
    } catch (err) {}
  }

  try {
    await waitForDependencies();

    editor = new EditorJS({
      holder: "editorjs",
      autofocus: true,
      placeholder: "Escribe la publicación aquí...",

      tools: {
        header: {
          class: Header,
          inlineToolbar: true,
          config: {
            levels: [1, 2, 3],
            defaultLevel: 2,
          },
        },
        quote: {
          class: Quote,
          inlineToolbar: true,
        },
        warning: {
          class: Warning,
          inlineToolbar: true,
        },
        delimiter: Delimiter,
        list: {
          class: List,
          inlineToolbar: true,
        },
        nestedList: {
          class: NestedList,
          inlineToolbar: true,
        },
        checklist: {
          class: Checklist,
          inlineToolbar: true,
        },
        image: {
          class: ImageTool,
          config: {
            endpoints: {
              byFile: "/api/images/upload",
              byUrl: "/api/fetch-url",
            },
          },
        },
        simpleImage: {
          class: SimpleImage,
        },
        embed: {
          class: Embed,
          config: {
            services: {
              youtube: true,
              vimeo: true,
            },
          },
        },
        linkTool: {
          class: LinkTool,
          config: {
            endpoint: "/api/fetch-url",
          },
        },
        attaches: {
          class: AttachesTool,
          config: {
            endpoint: "/api/upload-file",
          },
        },
        table: {
          class: Table,
          inlineToolbar: true,
        },
        marker: {
          class: Marker,
        },
        underline: Underline,
      },

      data: post,

      onReady: () => {
        document
          .getElementById("loading")
          ?.style.setProperty("display", "none");
        document
          .getElementById("editorjs")
          ?.style.setProperty("display", "block");
        document
          .getElementById("buttons")
          ?.style.setProperty("display", "flex");

        showStatus("Editor cargado correctamente", "success");
      },

      onChange: () => {},
    });
  } catch (error) {
    console.error("Error al inicializar Editor.js:", error);
    document.getElementById("loading").textContent =
      "Error al cargar el editor: " + error.message;
    showStatus("Error: " + error.message, "error");
  }
}

async function uploadImage(file) {
  const formData = new FormData();
  formData.append("image", file);

  try {
    const response = await fetch("/api/images/upload", {
      method: "POST",
      body: formData,
    });

    if (!response.ok) {
      throw new Error("Error al subir la imagen");
    }

    const data = await response.json();
    if (data.success) {
      return data.file.url;
    } else {
      throw new Error("Error al procesar la imagen");
    }
  } catch (error) {
    console.error("Error:", error);
    showStatus(error.message, "error");
    throw error;
  }
}

function setupImageUpload() {
  const imageUploadBtn = document.getElementById("imageUploadBtn");
  const imageFile = document.getElementById("imageFile");
  const imagePreview = document.getElementById("imagePreview");

  imageUploadBtn.addEventListener("click", () => {
    imageFile.click();
  });

  imageFile.addEventListener("change", async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    try {
      imageUploadBtn.disabled = true;
      imageUploadBtn.textContent = "Subiendo...";

      const imageUrl = await uploadImage(file);
      featuredImage = imageUrl;

      // Mostrar vista previa
      imagePreview.style.display = "block";
      imagePreview.innerHTML = `<img src="${imageUrl}" alt="Vista previa">`;

      imageUploadBtn.textContent = "Cambiar imagen";
    } catch (error) {
      imageUploadBtn.textContent = "Error al subir. Intentar de nuevo";
    } finally {
      imageUploadBtn.disabled = false;
    }
  });
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", () => {
    initEditor();
    setupImageUpload();
  });
} else {
  initEditor();
  setupImageUpload();
}

// Botones
document.addEventListener("DOMContentLoaded", function () {
  const saveBtn = document.getElementById("save-btn");
  const clearBtn = document.getElementById("clear-btn");

  if (saveBtn) {
    saveBtn.addEventListener("click", async () => {
      if (!editor) return showStatus("Editor no está inicializado", "error");
      if (!featuredImage)
        return showStatus("Debes seleccionar una imagen destacada", "error");

      try {
        saveBtn.disabled = true;
        saveBtn.textContent = "Guardando...";

        const outputData = await editor.save();
        await sendToBackend(outputData);

        // El mensaje de éxito y la limpieza se manejan en sendToBackend
      } catch (error) {
        console.error("Error al guardar:", error);
        showStatus("Error al guardar: " + error.message, "error");
      } finally {
        saveBtn.disabled = false;
        saveBtn.textContent = "Guardar contenido";
      }
    });
  }

  if (clearBtn) {
    clearBtn.addEventListener("click", async () => {
      if (!editor) return;
      if (confirm("¿Estás seguro de que quieres limpiar todo el contenido?")) {
        await editor.clear();
        document.getElementById("output").textContent =
          "El contenido aparecerá aquí después de guardar...";
        // Limpiar imagen destacada
        featuredImage = null;
        document.getElementById("imagePreview").style.display = "none";
        document.getElementById("imagePreview").innerHTML = "";
        document.getElementById("imageUploadBtn").textContent =
          "Seleccionar imagen destacada";
        showStatus("Editor limpiado", "success");
      }
    });
  }
});

function showStatus(message, type) {
  const statusEl = document.getElementById("status");
  if (!statusEl) return;

  statusEl.textContent = message;
  statusEl.className = "status-message " + type;
  statusEl.style.display = "block";

  setTimeout(() => {
    statusEl.style.display = "none";
  }, 5000);
}

async function sendToBackend(data) {
  let post = {};
  post.title = document.getElementById("title").value;
  post.description = document.getElementById("description").value;
  post.imageUrl = featuredImage;
  post.editorContent = data;

  const res = await newsService.addNew(post);
  if (!res.ok) throw new Error("Error del servidor: " + res.status);
  showStatus("¡Publicación guardada correctamente!", "success");
  document.getElementById("title").value = "";
  document.getElementById("description").value = "";
  featuredImage = null;
  document.getElementById("imagePreview").style.display = "none";
  document.getElementById("imagePreview").innerHTML = "";
  document.getElementById("imageUploadBtn").textContent =
    "Seleccionar imagen destacada";
  await editor.clear();
  showAll();
  return res;
}

async function showAll() {
  news = await newsService.getNews();
  const container = document.getElementById("all-news");
  container.innerHTML = "";
  let id = 0;
  news.forEach((post) => {
    const card = document.createElement("div");
    card.className = "news-card";

    const imageDiv = document.createElement("div");
    imageDiv.className = "news-card-image";

    const img = document.createElement("img");
    img.src = post.imageUrl;
    img.alt = post.title;
    img.id = `imageUrl-${id}`;

    imageDiv.appendChild(img);
    card.appendChild(imageDiv);

    const contentDiv = document.createElement("div");

    const title = document.createElement("h2");
    title.id = `title-${id}`;
    title.textContent = post.title;

    const description = document.createElement("p");
    description.id = `description-${id}`;

    description.textContent = post.description;

    const editButton = document.createElement("button");
    editButton.className = "edit-button";
    editButton.id = `edit-${id}`;
    editButton.textContent = "Editar";

    const deleteButton = document.createElement("button");
    deleteButton.className = "delete-button";
    deleteButton.id = `delete-${id}`;
    deleteButton.textContent = "Eliminar";

    contentDiv.appendChild(title);
    contentDiv.appendChild(description);
    contentDiv.appendChild(editButton);
    contentDiv.appendChild(deleteButton);

    card.appendChild(contentDiv);

    container.appendChild(card);
    id++;
  });

  document
    .getElementById("all-news")
    .addEventListener("click", async function (e) {
      if (e.target.className === "edit-button") {
        const id = e.target.id.replace("edit-", "");
        console.log(news[id]);
        document.getElementById(`title`).value = news[id].title;
        document.getElementById(`description`).value = news[id].description;
        // Mostrar la imagen destacada actual
        if (news[id].imageUrl) {
          featuredImage = news[id].imageUrl;
          document.getElementById("imagePreview").style.display = "block";
          document.getElementById(
            "imagePreview"
          ).innerHTML = `<img src="${news[id].imageUrl}" alt="Vista previa">`;
          document.getElementById("imageUploadBtn").textContent =
            "Cambiar imagen";
        }
        await initEditor(news[id].editorContent);
      }
      if (e.target.className === "delete-button") {
        const id = e.target.id.replace("delete-", "");
        if (confirm("¿Estás seguro de que quieres eliminar este post?")) {
          await deletePost(news[id].title);
          document.getElementById("all-news").innerHTML = "";
          showAll();
        }
      }
    });
}

async function deletePost(id) {
  const res = await newsService.deleteNew(id);
  showStatus("Post eliminado correctamente", "success");
  return res;
}
showAll();
