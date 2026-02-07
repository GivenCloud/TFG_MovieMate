package com.moviemate.annotation;

import org.springframework.security.access.prepost.PreAuthorize;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("@profileSecurity.canViewProfile(authentication.name, #userId)")
public @interface RequirePublicProfile {
    String userId() default "";  // SpEL para pathVar
}
