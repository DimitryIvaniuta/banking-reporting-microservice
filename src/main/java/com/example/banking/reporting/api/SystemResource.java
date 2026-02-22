package com.example.banking.reporting.api;
import jakarta.ws.rs.*; import jakarta.ws.rs.core.MediaType; import java.time.Instant; import java.util.Map; import org.springframework.stereotype.Component;
/** Lightweight system endpoints for smoke checks. */
@Component @Path("/system") @Produces(MediaType.APPLICATION_JSON)
public class SystemResource {
 /** Returns a simple ping response. */
 @GET @Path("/ping") public Map<String,Object> ping(){ return Map.of("status","UP","time", Instant.now().toString()); }
}
