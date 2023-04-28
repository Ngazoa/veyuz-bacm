package com.akouma.veyuzwebapp.exceptionhandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartException;

import javax.servlet.http.HttpServletRequest;

public class RestGlobalExceptionHandler {
        // Catch max file size Exception.
        @ExceptionHandler(MultipartException.class)
        @ResponseBody
        public ResponseEntity<?> handleControllerException(HttpServletRequest request, Throwable ex) {

            HttpStatus status = this.getStatus(request);
            return new ResponseEntity<String>("(Message in RestGlobalExceptionHandler *): " + ex.getMessage(), status);
        }

        // Cache Other Exception
        @ExceptionHandler(Exception.class)
        @ResponseBody
        public ResponseEntity<?> handleControllerRootException(HttpServletRequest request, Throwable ex) {

            HttpStatus status = this.getStatus(request);
            return new ResponseEntity<String>("(Message in RestGlobalExceptionHandler **): " + ex.getMessage(), status);
        }

        private HttpStatus getStatus(HttpServletRequest request) {
            Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
            if (statusCode == null) {
                return HttpStatus.INTERNAL_SERVER_ERROR;
            }
            return HttpStatus.valueOf(statusCode);
        }
}
