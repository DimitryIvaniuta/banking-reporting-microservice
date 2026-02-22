package com.example.banking.reporting.config;
import com.example.banking.reporting.api.ReportResource;
import com.example.banking.reporting.api.SystemResource;
import com.example.banking.reporting.api.ApiExceptionMapper;
import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;
/** Registers JAX-RS resources and providers. */
@Configuration @ApplicationPath("/api")
public class JerseyConfig extends ResourceConfig {
  /** Creates Jersey configuration. */
  public JerseyConfig(){ register(ReportResource.class); register(SystemResource.class); register(ApiExceptionMapper.class); packages("org.glassfish.jersey.jackson"); }
}
