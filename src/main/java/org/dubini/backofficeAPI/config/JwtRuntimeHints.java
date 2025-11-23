package org.dubini.backofficeAPI.config;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints(JwtRuntimeHints.JwtHintsRegistrar.class)
public class JwtRuntimeHints {

    static class JwtHintsRegistrar implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            registerJjwtClasses(hints);
        }

        private void registerJjwtClasses(RuntimeHints hints) {
            try {
                hints.reflection()
                    .registerType(Class.forName("io.jsonwebtoken.impl.security.KeysBridge"), 
                        hint -> hint.withMembers(
                            org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                            org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_METHODS,
                            org.springframework.aot.hint.MemberCategory.DECLARED_FIELDS
                        ));
                
                hints.reflection()
                    .registerType(Class.forName("io.jsonwebtoken.impl.DefaultJwtBuilder"),
                        hint -> hint.withMembers(
                            org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                            org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_METHODS
                        ));
                
                hints.reflection()
                    .registerType(Class.forName("io.jsonwebtoken.impl.DefaultJwtParser"),
                        hint -> hint.withMembers(
                            org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                            org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_METHODS
                        ));
                
                hints.reflection()
                    .registerType(Class.forName("io.jsonwebtoken.impl.DefaultJwtParserBuilder"),
                        hint -> hint.withMembers(
                            org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                            org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_METHODS
                        ));

                // Security classes
                hints.reflection()
                    .registerType(Class.forName("io.jsonwebtoken.impl.security.DefaultKeyPairBuilder"),
                        hint -> hint.withMembers(
                            org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                            org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_METHODS
                        ));

                hints.reflection()
                    .registerType(Class.forName("io.jsonwebtoken.impl.security.DefaultSecretKeyBuilder"),
                        hint -> hint.withMembers(
                            org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                            org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_METHODS
                        ));

            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Failed to register JJWT classes for native image", e);
            }
        }
    }
}