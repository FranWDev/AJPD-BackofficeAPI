import { ImageProcessor } from "../components/imageProcessor.js";

const HERO_NAMES = ['hero1', 'hero2', 'hero3'];
const API_BASE = '/api/hero-images';

// Configurar procesador de imágenes
const imageProcessor = new ImageProcessor({
  maxWidth: 1920,
  maxHeight: 1080,
  quality: 0.85,
  outputFormat: "image/webp",
});

// Almacenar archivos procesados
const processedFiles = {};
HERO_NAMES.forEach(heroName => {
  processedFiles[heroName] = null;
});

// Elementos del DOM
const elements = {};
HERO_NAMES.forEach(heroName => {
  elements[heroName] = {
    fileInput: document.getElementById(`file-${heroName}`),
    fileLabel: document.querySelector(`label[for="file-${heroName}"]`),
    preview: document.getElementById(`preview-${heroName}`),
    uploadBtn: document.getElementById(`btn-upload-${heroName}`),
    resetBtn: document.getElementById(`btn-reset-${heroName}`),
    statusMsg: document.getElementById(`status-${heroName}`),
    spinner: document.getElementById(`spinner-${heroName}`)
  };
});

// Inicializar
document.addEventListener('DOMContentLoaded', () => {
  initializeEventListeners();
  loadHeroImages();
});

function initializeEventListeners() {
  HERO_NAMES.forEach(heroName => {
    const elem = elements[heroName];

    // Cambio de archivo
    elem.fileInput.addEventListener('change', (e) => {
      handleFileSelect(heroName, e);
    });

    // Botón upload
    elem.uploadBtn.addEventListener('click', () => {
      uploadHeroImage(heroName);
    });

    // Botón reset
    elem.resetBtn.addEventListener('click', () => {
      resetHeroImage(heroName);
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
      handleFileSelect(heroName, { target: { files } });
    });
  });
}

function preventDefaults(e) {
  e.preventDefault();
  e.stopPropagation();
}

async function handleFileSelect(heroName, event) {
  const files = event.target.files;
  if (!files || files.length === 0) return;

  const file = files[0];
  const elem = elements[heroName];

  // Validar tipo de archivo
  const allowedTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
  if (!allowedTypes.includes(file.type)) {
    showStatus(heroName, 'Tipo de archivo no válido. Solo se aceptan JPEG, PNG, WebP o GIF.', 'error');
    return;
  }

  // Validar tamaño
  const maxSize = 10 * 1024 * 1024; // 10MB
  if (file.size > maxSize) {
    showStatus(heroName, 'El archivo excede el tamaño máximo de 10MB.', 'error');
    return;
  }

  showStatus(heroName, 'Procesando imagen...', 'loading');
  elem.spinner.style.display = 'block';

  try {
    // Obtener información original
    const originalInfo = await imageProcessor.getImageInfo(file);
    console.log(`[${heroName}] Imagen original:`, originalInfo);

    // Procesar la imagen
    const processedBlob = await imageProcessor.processImage(file);
    const processedFile = imageProcessor.blobToFile(
      processedBlob,
      `${heroName}_${Date.now()}.webp`
    );

    // Guardar el archivo procesado
    processedFiles[heroName] = processedFile;

    // Calcular reducción
    const reduction = ((1 - processedFile.size / file.size) * 100).toFixed(1);
    console.log(
      `[${heroName}] Imagen procesada: ${(processedFile.size / 1024).toFixed(2)} KB (reducción del ${reduction}%)`
    );

    // Mostrar preview
    const reader = new FileReader();
    reader.onload = (e) => {
      elem.preview.src = e.target.result;
      showStatus(heroName, `✓ Imagen lista (${(processedFile.size / 1024).toFixed(1)}KB, -${reduction}%)`, 'loading');
    };
    reader.readAsDataURL(processedBlob);

  } catch (error) {
    console.error(`Error procesando imagen ${heroName}:`, error);
    showStatus(heroName, `Error al procesar la imagen: ${error.message}`, 'error');
  } finally {
    elem.spinner.style.display = 'none';
  }
}

async function uploadHeroImage(heroName) {
  const elem = elements[heroName];

  if (!processedFiles[heroName]) {
    showStatus(heroName, 'Por favor, selecciona una imagen primero.', 'error');
    return;
  }

  showStatus(heroName, 'Subiendo imagen...', 'loading');
  elem.uploadBtn.disabled = true;
  elem.spinner.style.display = 'block';

  try {
    const formData = new FormData();
    formData.append('image', processedFiles[heroName]);

    const response = await fetch(`${API_BASE}/${heroName}`, {
      method: 'PUT',
      body: formData
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    
    elem.preview.src = data.url + '?t=' + Date.now();
    elem.fileInput.value = '';
    processedFiles[heroName] = null;
    
    showStatus(heroName, '✓ Imagen actualizada exitosamente', 'success');
    
  } catch (error) {
    console.error('Error uploading image:', error);
    showStatus(heroName, 'Error al subir la imagen. Intenta de nuevo.', 'error');
  } finally {
    elem.uploadBtn.disabled = false;
    elem.spinner.style.display = 'none';
  }
}

async function resetHeroImage(heroName) {
  if (heroName === 'hero1') {
    showStatus(heroName, 'Hero 1 no se puede restaurar, es la imagen base.', 'error');
    return;
  }

  if (!confirm(`¿Restaurar "${heroName}" con la imagen de hero1?`)) {
    return;
  }

  showStatus(heroName, 'Restaurando imagen...', 'loading');
  const elem = elements[heroName];
  elem.resetBtn.disabled = true;
  elem.spinner.style.display = 'block';

  try {
    // Obtener hero1
    const hero1Response = await fetch(`${API_BASE}/hero1/url`);
    if (!hero1Response.ok) {
      throw new Error('No se pudo obtener hero1');
    }
    const hero1Data = await hero1Response.json();
    const hero1Url = hero1Data.url;

    // Descargar imagen de hero1
    const imageResponse = await fetch(hero1Url);
    const blob = await imageResponse.blob();

    // Crear archivo simulado
    const file = new File([blob], 'hero1.webp', { type: blob.type });
    
    // Usar DataTransfer para actualizar el input files
    const dataTransfer = new DataTransfer();
    dataTransfer.items.add(file);
    elem.fileInput.files = dataTransfer.files;

    // Mostrar preview
    elem.preview.src = hero1Url;

    // Subir como heroName
    const formData = new FormData();
    formData.append('image', file);

    const uploadResponse = await fetch(`${API_BASE}/${heroName}`, {
      method: 'PUT',
      body: formData
    });

    if (!uploadResponse.ok) {
      throw new Error(`HTTP error! status: ${uploadResponse.status}`);
    }

    elem.preview.src = hero1Url + '?t=' + Date.now();
    elem.fileInput.value = '';

    showStatus(heroName, '✓ Imagen restaurada a partir de hero1', 'success');

  } catch (error) {
    console.error('Error resetting image:', error);
    showStatus(heroName, 'Error al restaurar la imagen. Intenta de nuevo.', 'error');
  } finally {
    elem.resetBtn.disabled = false;
    elem.spinner.style.display = 'none';
  }
}

async function loadHeroImages() {
  for (const heroName of HERO_NAMES) {
    try {
      const response = await fetch(`${API_BASE}/${heroName}/url`);
      if (response.ok) {
        const data = await response.json();
        elements[heroName].preview.src = data.url;
      }
    } catch (error) {
      console.error(`Error loading ${heroName}:`, error);
    }
  }
}

function showStatus(heroName, message, type) {
  const elem = elements[heroName];
  elem.statusMsg.textContent = message;
  elem.statusMsg.className = `status-message show ${type}`;

  if (type === 'success') {
    setTimeout(() => {
      elem.statusMsg.classList.remove('show');
    }, 4000);
  }
}
