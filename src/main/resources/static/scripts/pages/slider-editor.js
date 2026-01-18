import { ImageProcessor } from "../components/imageProcessor.js";

console.log("[SLIDER-EDITOR] Script cargado");

const SLIDE_NAMES = ['slide1', 'slide2', 'slide3', 'slide4', 'slide5', 'slide6'];
const API_BASE = '/api/slider-images';

// Configurar procesador de imágenes
const imageProcessor = new ImageProcessor({
  maxWidth: 1920,
  maxHeight: 1080,
  quality: 0.85,
  outputFormat: "image/webp",
});

// Almacenar archivos procesados
const processedFiles = {};
const maxChars = 150;

// Almacenar captions originales para validar cambios
const originalCaptions = {};

SLIDE_NAMES.forEach(slideName => {
  processedFiles[slideName] = null;
  originalCaptions[slideName] = '';
});

// Elementos del DOM
const elements = {};

function initializeElements() {
  SLIDE_NAMES.forEach(slideName => {
    const elem = document.getElementById(`file-${slideName}`);
    if (!elem) {
      console.warn(`[SLIDER-EDITOR] Elemento no encontrado: file-${slideName}`);
    }
    elements[slideName] = {
      fileInput: document.getElementById(`file-${slideName}`),
      fileLabel: document.querySelector(`label[for="file-${slideName}"]`),
      preview: document.getElementById(`preview-${slideName}`),
      uploadBtn: document.getElementById(`btn-upload-${slideName}`),
      statusMsg: document.getElementById(`status-${slideName}`),
      spinner: document.getElementById(`spinner-${slideName}`),
      captionTextarea: document.getElementById(`caption-${slideName}`),
      captionBtn: document.getElementById(`btn-caption-${slideName}`),
      captionStatus: document.getElementById(`caption-status-${slideName}`),
      charCount: document.getElementById(`char-count-${slideName}`)
    };
  });
  console.log("[SLIDER-EDITOR] Elementos inicializados:", elements);
}

// Inicializar cuando el DOM esté listo
if (document.readyState === 'loading') {
  console.log("[SLIDER-EDITOR] DOM aún cargándose, esperando DOMContentLoaded");
  document.addEventListener('DOMContentLoaded', () => {
    console.log("[SLIDER-EDITOR] DOMContentLoaded disparado");
    initializeElements();
    initializeEventListeners();
    loadSliderCaptions();
  });
} else {
  console.log("[SLIDER-EDITOR] DOM ya cargado, inicializando inmediatamente");
  initializeElements();
  initializeEventListeners();
  loadSliderCaptions();
}

function initializeEventListeners() {
  SLIDE_NAMES.forEach(slideName => {
    const elem = elements[slideName];

    // Cambio de archivo
    elem.fileInput.addEventListener('change', (e) => {
      handleFileSelect(slideName, e);
    });

    // Botón upload
    elem.uploadBtn.addEventListener('click', () => {
      uploadSliderImage(slideName);
    });

    // Botón caption
    elem.captionBtn.addEventListener('click', () => {
      saveSliderCaption(slideName);
    });

    // Contador de caracteres
    elem.captionTextarea.addEventListener('input', () => {
      updateCharCount(slideName);
    });

    // Drag and drop
    const previewContainer = elem.preview.parentElement;
    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
      previewContainer.addEventListener(eventName, preventDefaults, false);
    });

    ['dragenter', 'dragover'].forEach(eventName => {
      previewContainer.addEventListener(eventName, () => {
        previewContainer.style.borderColor = 'var(--color-accent)';
      });
    });

    ['dragleave', 'drop'].forEach(eventName => {
      previewContainer.addEventListener(eventName, () => {
        previewContainer.style.borderColor = 'var(--color-blue-light)';
      });
    });

    previewContainer.addEventListener('drop', (e) => {
      const dt = e.dataTransfer;
      const files = dt.files;
      elem.fileInput.files = files;
      handleFileSelect(slideName, { target: { files } });
    });
  });
}

function preventDefaults(e) {
  e.preventDefault();
  e.stopPropagation();
}

function updateCharCount(slideName) {
  const elem = elements[slideName];
  const length = elem.captionTextarea.value.length;
  elem.charCount.textContent = `${length} / ${maxChars}`;

  if (length > maxChars) {
    elem.captionTextarea.value = elem.captionTextarea.value.substring(0, maxChars);
    elem.charCount.textContent = `${maxChars} / ${maxChars}`;
    elem.charCount.classList.add('error');
  } else if (length > maxChars * 0.9) {
    elem.charCount.classList.add('warning');
    elem.charCount.classList.remove('error');
  } else {
    elem.charCount.classList.remove('warning', 'error');
  }
}

async function handleFileSelect(slideName, event) {
  const files = event.target.files;
  if (!files || files.length === 0) return;

  const file = files[0];
  const elem = elements[slideName];

  // Validar tipo de archivo
  const allowedTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
  if (!allowedTypes.includes(file.type)) {
    showStatus(slideName, 'Tipo de archivo no válido. Solo se aceptan JPEG, PNG, WebP o GIF.', 'error');
    return;
  }

  // Validar tamaño
  const maxSize = 10 * 1024 * 1024; // 10MB
  if (file.size > maxSize) {
    showStatus(slideName, 'El archivo excede el tamaño máximo de 10MB.', 'error');
    return;
  }

  showStatus(slideName, 'Procesando imagen...', 'loading');
  elem.spinner.style.display = 'block';

  try {
    // Obtener información original
    const originalInfo = await imageProcessor.getImageInfo(file);
    console.log(`[${slideName}] Imagen original:`, originalInfo);

    // Procesar la imagen
    const processedBlob = await imageProcessor.processImage(file);
    const processedFile = imageProcessor.blobToFile(
      processedBlob,
      `${slideName}_${Date.now()}.webp`
    );

    // Guardar el archivo procesado
    processedFiles[slideName] = processedFile;

    // Calcular reducción
    const reduction = ((1 - processedFile.size / file.size) * 100).toFixed(1);
    console.log(
      `[${slideName}] Imagen procesada: ${(processedFile.size / 1024).toFixed(2)} KB (reducción del ${reduction}%)`
    );

    // Mostrar preview
    const reader = new FileReader();
    reader.onload = (e) => {
      elem.preview.src = e.target.result;
      showStatus(slideName, `✓ Imagen lista (${(processedFile.size / 1024).toFixed(1)}KB, -${reduction}%)`, 'loading');
    };
    reader.readAsDataURL(processedBlob);

  } catch (error) {
    console.error(`Error procesando imagen ${slideName}:`, error);
    showStatus(slideName, `Error al procesar la imagen: ${error.message}`, 'error');
  } finally {
    elem.spinner.style.display = 'none';
  }
}

async function uploadSliderImage(slideName) {
  const elem = elements[slideName];

  if (!processedFiles[slideName]) {
    showStatus(slideName, 'Por favor, selecciona una imagen primero.', 'error');
    return;
  }

  showStatus(slideName, 'Subiendo imagen...', 'loading');
  elem.uploadBtn.disabled = true;
  elem.spinner.style.display = 'block';

  try {
    const formData = new FormData();
    formData.append('image', processedFiles[slideName]);

    const response = await fetch(`${API_BASE}/${slideName}`, {
      method: 'PUT',
      body: formData
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    
    elem.preview.src = data.url + '?t=' + Date.now();
    elem.fileInput.value = '';
    processedFiles[slideName] = null;
    
    showStatus(slideName, '✓ Imagen actualizada exitosamente', 'success');
    
  } catch (error) {
    console.error('Error uploading image:', error);
    showStatus(slideName, 'Error al subir la imagen. Intenta de nuevo.', 'error');
  } finally {
    elem.uploadBtn.disabled = false;
    elem.spinner.style.display = 'none';
  }
}

async function saveSliderCaption(slideName) {
  const elem = elements[slideName];
  const caption = elem.captionTextarea.value.trim();

  console.log(`[${slideName}] Saving caption:`, caption.substring(0, 50));

  if (!caption) {
    showCaptionStatus(slideName, 'El pie de texto no puede estar vacío.', 'error');
    return;
  }

  if (caption.length > maxChars) {
    showCaptionStatus(slideName, `El pie de texto no puede exceder ${maxChars} caracteres.`, 'error');
    return;
  }

  // Validar que sea diferente al original
  if (caption === originalCaptions[slideName]) {
    showCaptionStatus(slideName, 'El pie de texto no ha cambiado.', 'error');
    return;
  }

  showCaptionStatus(slideName, 'Guardando pie de texto...', 'loading');
  elem.captionBtn.disabled = true;

  try {
    console.log(`[${slideName}] Sending request to ${API_BASE}/${slideName}/caption`);
    
    const response = await fetch(`${API_BASE}/${slideName}/caption`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ caption })
    });

    console.log(`[${slideName}] Response status:`, response.status);

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    // Actualizar el caption original después de guardar exitosamente
    originalCaptions[slideName] = caption;
    showCaptionStatus(slideName, '✓ Pie de texto guardado exitosamente', 'success');
    console.log(`[${slideName}] Caption saved successfully`);
    
  } catch (error) {
    console.error('Error saving caption:', error);
    showCaptionStatus(slideName, 'Error al guardar el pie de texto. Intenta de nuevo.', 'error');
  } finally {
    elem.captionBtn.disabled = false;
  }
}

async function loadSliderCaptions() {
  for (const slideName of SLIDE_NAMES) {
    try {
      const response = await fetch(`${API_BASE}/${slideName}/caption`);
      if (response.ok) {
        const data = await response.json();
        
        // Cargar caption si existe
        if (data.caption) {
          elements[slideName].captionTextarea.value = data.caption;
          // Guardar el caption original para validar cambios
          originalCaptions[slideName] = data.caption;
          updateCharCount(slideName);
        }
      }
    } catch (error) {
      console.debug(`Caption no encontrado para ${slideName}:`, error);
    }
  }
}

function showStatus(slideName, message, type) {
  const elem = elements[slideName];
  elem.statusMsg.textContent = message;
  elem.statusMsg.className = `status-message show ${type}`;

  if (type === 'success') {
    setTimeout(() => {
      elem.statusMsg.classList.remove('show');
    }, 4000);
  }
}

function showCaptionStatus(slideName, message, type) {
  const elem = elements[slideName];
  elem.captionStatus.textContent = message;
  elem.captionStatus.className = `status-message show ${type}`;

  if (type === 'success') {
    setTimeout(() => {
      elem.captionStatus.classList.remove('show');
    }, 4000);
  }
}
