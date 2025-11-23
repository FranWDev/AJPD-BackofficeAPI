import { ImageProcessor } from "./imageProcessor.js";

/**
 * Custom Uploader para EditorJS Image Tool
 * Procesa las imágenes antes de subirlas al backend
 */
class CustomImageUploader {
  constructor(config = {}) {
    this.config = config;
    this.imageProcessor = new ImageProcessor({
      maxWidth: config.maxWidth || 1920,
      maxHeight: config.maxHeight || 1080,
      quality: config.quality || 0.85,
      outputFormat: config.outputFormat || 'image/webp'
    });
  }

  async uploadByFile(file) {
    try {
      console.log('EditorJS - Procesando imagen:', file.name);

      if (!file.type.startsWith('image/')) {
        throw new Error('El archivo debe ser una imagen');
      }

      const processedBlob = await this.imageProcessor.processImage(file);
      
      const processedFile = this.imageProcessor.blobToFile(
        processedBlob,
        `${Date.now()}.webp`
      );

      const reduction = ((1 - processedFile.size / file.size) * 100).toFixed(1);
      console.log(`EditorJS - Imagen optimizada: ${(processedFile.size / 1024).toFixed(2)} KB (${reduction}% reducción)`);

      const formData = new FormData();
      formData.append("image", processedFile);

      const response = await fetch(this.config.endpoint, {
        method: "POST",
        body: formData,
      });

      if (!response.ok) {
        throw new Error(`Error del servidor: ${response.status}`);
      }

      const data = await response.json();

      if (data.success) {
        return {
          success: 1,
          file: {
            url: data.file.url,
          },
        };
      } else {
        throw new Error("Error al procesar la imagen en el servidor");
      }
    } catch (error) {
      console.error("Error al subir imagen en EditorJS:", error);
      return {
        success: 0,
        file: {
          url: "",
        },
      };
    }
  }

  async uploadByUrl(url) {
    try {
      const response = await fetch(this.config.endpointByUrl || "/api/fetch-url", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ url }),
      });

      if (!response.ok) {
        throw new Error(`Error del servidor: ${response.status}`);
      }

      const data = await response.json();

      return {
        success: 1,
        file: {
          url: data.file?.url || url,
        },
      };
    } catch (error) {
      console.error("Error al subir imagen por URL:", error);
      return {
        success: 0,
        file: {
          url: "",
        },
      };
    }
  }
}

export { CustomImageUploader };