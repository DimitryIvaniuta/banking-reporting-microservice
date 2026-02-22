package com.example.banking.reporting.api;
import jakarta.ws.rs.*; import jakarta.ws.rs.core.*; import jakarta.ws.rs.ext.*; import org.slf4j.*; 
/** Maps exceptions to structured API responses. */
@Provider public class ApiExceptionMapper implements ExceptionMapper<Throwable>{
 private static final Logger log = LoggerFactory.getLogger(ApiExceptionMapper.class);
 /** Converts exception to response. */
 public Response toResponse(Throwable ex){
   if(ex instanceof BadRequestException) return Response.status(400).entity(ApiErrorDto.builder().code("BAD_REQUEST").message(ex.getMessage()).build()).type(MediaType.APPLICATION_JSON).build();
   if(ex instanceof NotFoundException) return Response.status(404).entity(ApiErrorDto.builder().code("NOT_FOUND").message(ex.getMessage()).build()).type(MediaType.APPLICATION_JSON).build();
   log.error("Unhandled API error", ex);
   return Response.status(500).entity(ApiErrorDto.builder().code("INTERNAL_ERROR").message("Unexpected error").build()).type(MediaType.APPLICATION_JSON).build();
 }
}
