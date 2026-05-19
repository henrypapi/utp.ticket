package com.pe.limon.api.core.utils.annotation;

import com.pe.limon.api.transactions.authz.business.permissions.PermissionPolicy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermissions {
    RequirePermission[] value();
    PermissionPolicy policy() default PermissionPolicy.ANY;
}