package kz.school.security.exception;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ApiException.class) ResponseEntity<?> api(ApiException e) { return ResponseEntity.status(e.status).body(Map.of("detail",e.getMessage())); }
    @ExceptionHandler(AccessDeniedException.class) ResponseEntity<?> denied() { return ResponseEntity.status(403).body(Map.of("detail","You do not have permission to access this resource")); }
    @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<?> conflict() { return ResponseEntity.status(409).body(Map.of("detail","Record conflicts with an existing record or database constraint")); }
    @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<?> invalid(MethodArgumentNotValidException e) {
        return ResponseEntity.unprocessableEntity().body(Map.of("detail", e.getBindingResult().getFieldErrors().stream().map(f -> Map.of("loc",List.of("body",snake(f.getField())),"msg",Objects.toString(f.getDefaultMessage(),"Invalid value"),"type","value_error")).toList()));
    }
    @ExceptionHandler({HandlerMethodValidationException.class,MethodArgumentTypeMismatchException.class,MissingServletRequestParameterException.class,HttpMessageNotReadableException.class})
    ResponseEntity<?> invalidInput(Exception e) { return ResponseEntity.unprocessableEntity().body(Map.of("detail",List.of(Map.of("loc",List.of("request"),"msg","Invalid or missing request value","type","value_error")))); }
    private static String snake(String s) { return s.replaceAll("([a-z])([A-Z])","$1_$2").toLowerCase(Locale.ROOT); }
}
