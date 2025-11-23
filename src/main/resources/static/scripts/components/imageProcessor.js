class ImageProcessor {
  constructor(options = {}) {
    this.maxWidth = options.maxWidth || 1920;
    this.maxHeight = options.maxHeight || 1080;
    this.quality = options.quality || 0.85;
    this.outputFormat = options.outputFormat || 'image/webp';
  }

  async processImage(file) {
    return new Promise((resolve, reject) => {

      if (!file.type.startsWith('image/')) {
        reject(new Error('El archivo no es una imagen válida'));
        return;
      }

      const reader = new FileReader();

      reader.onload = (e) => {
        const img = new Image();

        img.onload = () => {
          try {
            const processedBlob = this._resizeAndCompress(img);
            resolve(processedBlob);
          } catch (error) {
            reject(error);
          }
        };

        img.onerror = () => {
          reject(new Error('Error al cargar la imagen'));
        };

        img.src = e.target.result;
      };

      reader.onerror = () => {
        reject(new Error('Error al leer el archivo'));
      };

      reader.readAsDataURL(file);
    });
  }

  _resizeAndCompress(img) {
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');

    // Calcular nuevas dimensiones manteniendo aspect ratio
    let { width, height } = this._calculateDimensions(
      img.width,
      img.height
    );

    canvas.width = width;
    canvas.height = height;

    // Dibujar imagen redimensionada
    ctx.drawImage(img, 0, 0, width, height);

    // Convertir canvas a Blob
    return new Promise((resolve, reject) => {
      canvas.toBlob(
        (blob) => {
          if (blob) {
            resolve(blob);
          } else {
            reject(new Error('Error al crear el blob de la imagen'));
          }
        },
        this.outputFormat,
        this.quality
      );
    });
  }

  _calculateDimensions(originalWidth, originalHeight) {
    let width = originalWidth;
    let height = originalHeight;

    // Si la imagen es más grande que los límites, redimensionar
    if (width > this.maxWidth || height > this.maxHeight) {
      const aspectRatio = width / height;

      if (width > height) {
        width = this.maxWidth;
        height = Math.round(width / aspectRatio);
      } else {
        height = this.maxHeight;
        width = Math.round(height * aspectRatio);
      }
    }

    return { width, height };
  }

  async getImageInfo(file) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();

      reader.onload = (e) => {
        const img = new Image();

        img.onload = () => {
          resolve({
            width: img.width,
            height: img.height,
            size: file.size,
            type: file.type,
            name: file.name
          });
        };

        img.onerror = () => reject(new Error('Error al cargar la imagen'));
        img.src = e.target.result;
      };

      reader.onerror = () => reject(new Error('Error al leer el archivo'));
      reader.readAsDataURL(file);
    });
  }

  blobToFile(blob, fileName) {
    return new File([blob], fileName, {
      type: blob.type,
      lastModified: Date.now()
    });
  }
}

export { ImageProcessor };