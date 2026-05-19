package com.pe.limon.api.core.aop;

import com.pe.limon.api.core.utils.exception.BusinessException;
import com.pe.limon.api.core.utils.exception.InternalServerException;
import com.pe.limon.api.core.utils.exception.NotFoundException;
import com.pe.limon.api.core.utils.exception.PermissionException;
import com.pe.limon.api.core.utils.generics.ErrorDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class ExceptionAop {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleGlobalException(Exception ex, WebRequest request) {
        ErrorDTO errorResponse = new ErrorDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        printLog(ex,errorResponse,request);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorDTO> handleResourceBusinessException(BusinessException ex, WebRequest request) {
        ErrorDTO errorResponse = new ErrorDTO(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        printLog(ex,errorResponse,request);
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorDTO> handleResourceNotFoundException(NotFoundException ex, WebRequest request) {
        ErrorDTO errorResponse = new ErrorDTO(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        printLog(ex,errorResponse,request);
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PermissionException.class)
    public ResponseEntity<ErrorDTO> handleResourceNotFoundException(PermissionException ex, WebRequest request) {
        ErrorDTO errorResponse = new ErrorDTO(
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        printLog(ex,errorResponse,request);
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }


    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<ErrorDTO> handleResourceInternalServerException(InternalServerException ex, WebRequest request) {
        ErrorDTO errorResponse = new ErrorDTO(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        printLog(ex,errorResponse,request);
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> handleResourceNotFoundException(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        String firstErrorMessage = null;  // Variable para almacenar el primer mensaje de error

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
            if (firstErrorMessage == null) {
                firstErrorMessage = fieldError.getDefaultMessage();  // Tomar el primer mensaje de error
            }
        }

        ErrorDTO errorResponse = new ErrorDTO(
                HttpStatus.BAD_REQUEST.value(), // Código de estado HTTP 400
                firstErrorMessage != null ? firstErrorMessage : "Validación de datos fallida",  // Primer mensaje de error
                LocalDateTime.now()
        );
        printLog(ex,errorResponse,request);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);  // Código 400 (Bad Request)
    }

    private static void printLog(Exception e,ErrorDTO error, WebRequest request){
        log.error("[ControllerAdvice] Exception:", e); // <-- imprime stack completo
        log.error("[ControllerAdvice] Error: {}", error);
        log.debug("[ControllerAdvice] Request: {}", request);
    }
}
