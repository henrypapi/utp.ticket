package com.pe.limon.api.core.aop;

import com.pe.limon.api.core.utils.generics.ErrorDTO;
import com.pe.limon.api.core.utils.generics.ResponseWrapDTO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RequiredArgsConstructor
@ControllerAdvice
@Slf4j
public class ResponseAop implements ResponseBodyAdvice<Object> {

    private final HttpServletResponse httpServletResponse;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        String path = request.getURI().getPath();

        if (path.startsWith(contextPath+"v3/api-docs")
                || path.startsWith(contextPath+"swagger-ui")
                || path.startsWith(contextPath+"swagger-resources")
                || path.startsWith(contextPath+"webjars")
                || path.startsWith(contextPath+"actuator")) {
            return body; // NO envolvemos Swagger ni Actuator
        }

        int status = httpServletResponse.getStatus();

        if (body instanceof ErrorDTO || body instanceof ResponseWrapDTO<?> || body instanceof FileSystemResource || body instanceof String) {
            return body;
        }

        return new ResponseWrapDTO<>(status, HttpStatus.valueOf(status).getReasonPhrase(), body);
    }
}