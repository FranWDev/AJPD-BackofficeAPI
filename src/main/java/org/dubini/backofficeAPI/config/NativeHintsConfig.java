package org.dubini.backofficeAPI.config;

import org.springframework.aot.hint.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints({
        JwtNativeConfig.JwtHintsRegistrar.class,
        NativeHintsConfig.AppHintsRegistrar.class
})
public class NativeHintsConfig {

    static class AppHintsRegistrar implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {

            // ============ ENTIDADES JPA ============
            registerEntity(hints, "org.dubini.backofficeAPI.model.News");

            // ============ DTOs ============
            registerDto(hints, "org.dubini.backofficeAPI.dto.EditorJSBlock");
            registerDto(hints, "org.dubini.backofficeAPI.dto.EditorJSContentDTO");
            registerDto(hints, "org.dubini.backofficeAPI.dto.PublicationDTO");
            registerDto(hints, "org.dubini.backofficeAPI.dto.request.LoginRequest");
            registerDto(hints, "org.dubini.backofficeAPI.dto.response.EditorJSImageResponseDTO");
            registerDto(hints, "org.dubini.backofficeAPI.dto.response.HttpResponse");
            registerDto(hints, "org.dubini.backofficeAPI.dto.response.ImageResponseDTO");
            registerDto(hints, "org.dubini.backofficeAPI.dto.response.JwtResponse");
            
            // DTOs para Slider Images
            registerDto(hints, "org.dubini.backofficeAPI.controller.SliderImageController$SliderCaptionRequest");
            registerDto(hints, "org.dubini.backofficeAPI.controller.SliderImageController$SliderInfoResponse");
            registerDto(hints, "org.dubini.backofficeAPI.controller.SliderImageController$SliderImageUrlResponse");
            registerDto(hints, "org.dubini.backofficeAPI.controller.SliderImageController$SliderCaptionResponse");
            registerDto(hints, "org.dubini.backofficeAPI.controller.SliderImageController$SliderCaptionUpdateResponse");

            // ============ EXCEPCIONES ============
            registerException(hints, "org.dubini.backofficeAPI.exception.ImageProcessingException");
            registerException(hints, "org.dubini.backofficeAPI.exception.ImageStorageException");
            registerException(hints, "org.dubini.backofficeAPI.exception.JwtException");
            registerException(hints, "org.dubini.backofficeAPI.exception.PublicationNotFoundException");
            registerException(hints, "org.dubini.backofficeAPI.exception.PublicationStorageException");

            // ============ SPRING SECURITY ============
            registerSecurityClasses(hints);

            // ============ CONTROLLERS (para Spring MVC) ============
            registerController(hints, "org.dubini.backofficeAPI.controller.AuthController");
            registerController(hints, "org.dubini.backofficeAPI.controller.CacheInvalidatorController");
            registerController(hints, "org.dubini.backofficeAPI.controller.EditorController");
            registerController(hints, "org.dubini.backofficeAPI.controller.ImageUploadController");
            registerController(hints, "org.dubini.backofficeAPI.controller.NewsController");
            registerController(hints, "org.dubini.backofficeAPI.controller.PageController");
            registerController(hints, "org.dubini.backofficeAPI.controller.HeroImageController");
            registerController(hints, "org.dubini.backofficeAPI.controller.SliderImageController");

            // ============ SERVICIOS ============
            registerService(hints, "org.dubini.backofficeAPI.service.AuthService");
            registerService(hints, "org.dubini.backofficeAPI.service.CacheInvalidatorService");
            registerService(hints, "org.dubini.backofficeAPI.service.ImageService");
            registerService(hints, "org.dubini.backofficeAPI.service.NewsService");
            registerService(hints, "org.dubini.backofficeAPI.service.HeroImageService");
            registerService(hints, "org.dubini.backofficeAPI.service.SliderImageService");

            // ============ REPOSITORIES ============
            registerRepository(hints, "org.dubini.backofficeAPI.repository.NewsRepository");

            // ============ RECURSOS ESTÁTICOS ============
            hints.resources().registerPattern("templates/**");
            hints.resources().registerPattern("templates/*.html");
            hints.resources().registerPattern("templates/fragments/*.html");
            hints.resources().registerPattern("static/**");
            hints.resources().registerPattern("static/assets/**");
            hints.resources().registerPattern("static/scripts/**");
            hints.resources().registerPattern("static/styles/**");
            hints.resources().registerPattern("application*.properties");
            hints.resources().registerPattern("application*.yml");
            hints.resources().registerPattern("META-INF/**");
            hints.resources().registerPattern("META-INF/persistence.xml");
            hints.resources().registerPattern("META-INF/orm.xml");

            // ============ THYMELEAF ============
            registerThymeleafClasses(hints);

            // ============ JACKSON (JSON serialization) ============
            registerJacksonClasses(hints);

            // ============ POSTGRESQL DRIVER ============
            registerPostgresClasses(hints);

            // ============ WEBP IMAGE PROCESSING ============
            registerImageProcessingClasses(hints);
        }

        private void registerEntity(RuntimeHints hints, String className) {
            try {
                Class<?> clazz = Class.forName(className);
                hints.reflection().registerType(
                        clazz,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS,
                        MemberCategory.DECLARED_FIELDS,
                        MemberCategory.INVOKE_PUBLIC_METHODS,
                        MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
                // Serialización solo si implementa Serializable
                if (java.io.Serializable.class.isAssignableFrom(clazz)) {
                    hints.serialization().registerType((Class<? extends java.io.Serializable>) clazz);
                }
            } catch (ClassNotFoundException e) {
                System.err.println("Entidad no encontrada: " + className);
            }
        }

        private void registerDto(RuntimeHints hints, String className) {
            try {
                Class<?> clazz = Class.forName(className);
                hints.reflection().registerType(
                        clazz,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                        MemberCategory.DECLARED_FIELDS,
                        MemberCategory.INVOKE_PUBLIC_METHODS,
                        MemberCategory.INVOKE_DECLARED_METHODS);
                // Serialización solo si implementa Serializable
                if (java.io.Serializable.class.isAssignableFrom(clazz)) {
                    hints.serialization().registerType((Class<? extends java.io.Serializable>) clazz);
                }
            } catch (ClassNotFoundException e) {
                System.err.println("DTO no encontrado: " + className);
            }
        }

        private void registerException(RuntimeHints hints, String className) {
            try {
                hints.reflection().registerType(
                        Class.forName(className),
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                        MemberCategory.DECLARED_FIELDS);
            } catch (ClassNotFoundException e) {
                System.err.println("Exception no encontrada: " + className);
            }
        }

        private void registerController(RuntimeHints hints, String className) {
            try {
                hints.reflection().registerType(
                        Class.forName(className),
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS,
                        MemberCategory.INVOKE_PUBLIC_METHODS);
            } catch (ClassNotFoundException e) {
                System.err.println("Controller no encontrado: " + className);
            }
        }

        private void registerService(RuntimeHints hints, String className) {
            try {
                hints.reflection().registerType(
                        Class.forName(className),
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS,
                        MemberCategory.INVOKE_PUBLIC_METHODS);
            } catch (ClassNotFoundException e) {
                System.err.println("Service no encontrado: " + className);
            }
        }

        private void registerRepository(RuntimeHints hints, String className) {
            try {
                hints.reflection().registerType(
                        Class.forName(className),
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS,
                        MemberCategory.INVOKE_PUBLIC_METHODS);
            } catch (ClassNotFoundException e) {
                System.err.println("Repository no encontrado: " + className);
            }
        }

        private void registerSecurityClasses(RuntimeHints hints) {
            try {
                // Spring Security User
                hints.reflection().registerType(
                        org.springframework.security.core.userdetails.User.class,
                        MemberCategory.values());

                // Spring Security Authority
                hints.reflection().registerType(
                        org.springframework.security.core.authority.SimpleGrantedAuthority.class,
                        MemberCategory.values());

                // JWT Filter y Provider
                hints.reflection().registerType(
                        Class.forName("org.dubini.backofficeAPI.security.JwtFilter"),
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS);

                hints.reflection().registerType(
                        Class.forName("org.dubini.backofficeAPI.security.JwtProvider"),
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS);

            } catch (ClassNotFoundException e) {
                System.err.println("Security class no encontrada: " + e.getMessage());
            }
        }

        private void registerThymeleafClasses(RuntimeHints hints) {
            try {
                // Thymeleaf core classes
                hints.reflection().registerType(
                        Class.forName("org.thymeleaf.spring6.view.ThymeleafViewResolver"),
                        MemberCategory.values());
                hints.reflection().registerType(
                        Class.forName("org.thymeleaf.spring6.SpringTemplateEngine"),
                        MemberCategory.values());
            } catch (ClassNotFoundException e) {
                System.err.println("Thymeleaf class no encontrada: " + e.getMessage());
            }
        }

        private void registerJacksonClasses(RuntimeHints hints) {
            try {
                // Jackson ObjectMapper y clases core
                hints.reflection().registerType(
                        com.fasterxml.jackson.databind.ObjectMapper.class,
                        MemberCategory.values());
                hints.reflection().registerType(
                        com.fasterxml.jackson.databind.JsonNode.class,
                        MemberCategory.values());
                hints.reflection().registerType(
                        com.fasterxml.jackson.databind.node.ObjectNode.class,
                        MemberCategory.values());
                hints.reflection().registerType(
                        com.fasterxml.jackson.databind.node.ArrayNode.class,
                        MemberCategory.values());
            } catch (Exception e) {
                System.err.println("Jackson class issue: " + e.getMessage());
            }
        }

        private void registerPostgresClasses(RuntimeHints hints) {
            try {
                hints.reflection().registerType(
                        Class.forName("org.postgresql.Driver"),
                        MemberCategory.values());
                hints.reflection().registerType(
                        Class.forName("org.postgresql.util.PGobject"),
                        MemberCategory.values());

                // Postgres connection y statement classes
                registerClassIfExists(hints, "org.postgresql.jdbc.PgConnection");
                registerClassIfExists(hints, "org.postgresql.jdbc.PgStatement");
                registerClassIfExists(hints, "org.postgresql.jdbc.PgPreparedStatement");

                // Converters
                registerClassIfExists(hints, "org.dubini.backofficeAPI.config.PGobjectToStringConverter");
                registerClassIfExists(hints, "org.dubini.backofficeAPI.config.PostgresJsonbWritingConverter");
                registerClassIfExists(hints, "org.dubini.backofficeAPI.config.JsonbBeforeSaveCallback");

                // Spring Data JDBC Converters
                registerClassIfExists(hints, "org.springframework.data.convert.ReadingConverter");
                registerClassIfExists(hints, "org.springframework.data.convert.WritingConverter");
                registerClassIfExists(hints, "org.springframework.core.convert.converter.Converter");
            } catch (ClassNotFoundException e) {
                System.err.println("PostgreSQL class no encontrada: " + e.getMessage());
            }
        }

        private void registerImageProcessingClasses(RuntimeHints hints) {
            // WebP ImageIO - registrar si existe
            registerClassIfExists(hints, "org.sejda.imageio.webp.WebPImageReaderSpi");
            registerClassIfExists(hints, "org.sejda.imageio.webp.WebPImageWriterSpi");
            registerClassIfExists(hints, "org.sejda.imageio.webp.WebPImageReader");
            registerClassIfExists(hints, "org.sejda.imageio.webp.WebPImageWriter");

            // Thumbnailator
            registerClassIfExists(hints, "net.coobird.thumbnailator.Thumbnails");
            registerClassIfExists(hints, "net.coobird.thumbnailator.Thumbnailator");
            
            // ImageIO core
            registerClassIfExists(hints, "javax.imageio.ImageIO");
            registerClassIfExists(hints, "javax.imageio.spi.IIORegistry");
            registerClassIfExists(hints, "javax.imageio.ImageReader");
            registerClassIfExists(hints, "javax.imageio.ImageWriter");
            
            // Standard image providers
            registerClassIfExists(hints, "com.sun.imageio.plugins.jpeg.JPEGImageReaderSpi");
            registerClassIfExists(hints, "com.sun.imageio.plugins.jpeg.JPEGImageWriterSpi");
            registerClassIfExists(hints, "com.sun.imageio.plugins.png.PNGImageReaderSpi");
            registerClassIfExists(hints, "com.sun.imageio.plugins.png.PNGImageWriterSpi");

            // Java ImageIO providers
            hints.resources().registerPattern("META-INF/services/javax.imageio.spi.*");
        }

        private void registerClassIfExists(RuntimeHints hints, String className) {
            try {
                Class<?> clazz = Class.forName(className);
                hints.reflection().registerType(
                        clazz,
                        MemberCategory.values());
            } catch (ClassNotFoundException e) {
                // Ignorar si no existe (es normal para clases opcionales)
            }
        }
    }
}