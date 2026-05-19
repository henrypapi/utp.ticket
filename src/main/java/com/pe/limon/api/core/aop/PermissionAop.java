package com.pe.limon.api.core.aop;

import com.pe.limon.api.core.utils.annotation.EventId;
import com.pe.limon.api.core.utils.annotation.RequirePermission;
import com.pe.limon.api.core.utils.annotation.RequirePermissions;
import com.pe.limon.api.transactions.authz.business.permissions.IPermissionService;
import com.pe.limon.api.transactions.authz.business.permissions.PermissionPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Parameter;
import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionAop {

    private final IPermissionService permissionService;

    @Around("@annotation(com.pe.limon.api.core.utils.annotation.RequirePermission) || @annotation(com.pe.limon.api.core.utils.annotation.RequirePermissions)")
    public Object check(ProceedingJoinPoint jp) throws Throwable {
        var sig = (MethodSignature) jp.getSignature();
        var method = sig.getMethod();

        // 1) traer todas las @RequirePermission (soporta repetibles)
        var annos = method.getAnnotationsByType(RequirePermission.class);

        if (annos == null || annos.length == 0) return jp.proceed();
        log.debug("[check] annotations {}", Arrays.toString(annos));
        // 2) resolver userId desde @RequestAttribute String userId
        String userId = resolveUserIdFromRequestAttribute(jp);

        // 3) resolver eventId (por @EventId o nombre del parámetro de la 1ra anotación)
        long eventId = resolveEventId(jp, annos[0]);

        // 4) permiso efectivo una sola vez
        var eff = permissionService.getEffective(eventId, userId);
        var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        log.info("attrs {}",attrs);
        if (attrs != null) {
            attrs.getRequest().setAttribute("effectivePerm", eff);
        }
        if (eff.isAdmin()) return jp.proceed();

        // 4.1) (nuevo) determinar política desde el contenedor si existe
        var container = method.getAnnotation(RequirePermissions.class);
        var policy = (container != null) ? container.policy() : PermissionPolicy.ANY;

        // 4.2) validar que no haya módulos vacíos (igual que antes)
        for (var r : annos) {
            var module = r.module();
            if (module == null || module.isBlank())
                throw new AccessDeniedException("Forbidden: module not set");
        }

        // 5) Evaluación según política (CAMBIO MINIMO)
        boolean allowed = (policy == PermissionPolicy.ANY)
                ? Arrays.stream(annos).anyMatch(r -> eff.hasModule(r.module()))   // OR
                : Arrays.stream(annos).allMatch(r -> eff.hasModule(r.module()));  // AND

        if (!allowed) throw new AccessDeniedException("Forbidden: module access");

        return jp.proceed();
    }

    // ---------- helpers ----------
    private String resolveUserIdFromRequestAttribute(ProceedingJoinPoint jp) {
        var sig = (MethodSignature) jp.getSignature();
        Parameter[] params = sig.getMethod().getParameters();
        Object[] args = jp.getArgs();

        for (int i = 0; i < params.length; i++) {
            var ra = params[i].getAnnotation(org.springframework.web.bind.annotation.RequestAttribute.class);
            if (ra != null && args[i] instanceof String s && !s.isBlank()) {
                return s; // parámetro ya contiene el userId
            }
        }
        // fallback por nombre "userId" si compilas con -parameters
        var names = sig.getParameterNames();
        if (names != null) {
            for (int i = 0; i < names.length; i++) {
                if ("userId".equals(names[i]) && args[i] instanceof String s && !s.isBlank()) return s;
            }
        }
        throw new AccessDeniedException("401 - userId (RequestAttribute) requerido");
    }

    private long resolveEventId(ProceedingJoinPoint jp, RequirePermission refAnno) {
        var sig = (MethodSignature) jp.getSignature();
        Parameter[] params = sig.getMethod().getParameters();
        Object[] args = jp.getArgs();

        // 1) prioridad: @EventId
        for (int i = 0; i < params.length; i++) {
            if (params[i].isAnnotationPresent(EventId.class)) return toLong(args[i]);
        }
        // 2) por nombre (eventIdParam)
        var names = sig.getParameterNames();
        if (names != null) {
            for (int i = 0; i < names.length; i++) {
                if (refAnno.eventIdParam().equals(names[i])) return toLong(args[i]);
            }
        }
        throw new IllegalStateException("No se pudo resolver eventId (use @EventId o '" + refAnno.eventIdParam() + "')");
    }

    private static long toLong(Object v) {
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(v)); }
        catch (NumberFormatException e) { throw new IllegalStateException("Valor inválido para eventId: " + v); }
    }
}
