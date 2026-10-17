package org.example.bankapi.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiErrorHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String,Object>> status(ResponseStatusException ex) {
        return body(ex.getStatusCode().value(), ex.getReason()==null?"Request failed.":ex.getReason());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> validation(MethodArgumentNotValidException ex) {
        String message=ex.getBindingResult().getFieldErrors().stream().findFirst()
                .map(e->e.getField()+" "+(e.getDefaultMessage()==null?"is invalid.":e.getDefaultMessage())).orElse("Request validation failed.");
        return body(HttpStatus.BAD_REQUEST.value(),message);
    }
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String,Object>> conflict(ObjectOptimisticLockingFailureException ex) {
        return body(HttpStatus.CONFLICT.value(), "This account changed during the request. Refresh and try again.");
    }
    private ResponseEntity<Map<String,Object>> body(int status,String message){
        Map<String,Object> response=new LinkedHashMap<>();response.put("status",status);response.put("message",message);
        return ResponseEntity.status(status).body(response);
    }
}
