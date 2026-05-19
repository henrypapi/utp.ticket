package com.pe.limon.api.core.utils.annotation;

import com.pe.limon.api.transactions.authz.business.permissions.PermissionPolicy;

import java.lang.annotation.*;

@Repeatable(RequirePermissions.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    String module();
    String[] actions();                 // varias acciones
    PermissionPolicy policy() default PermissionPolicy.ANY;  // OR por defecto
    String eventIdParam() default "eventId";
}