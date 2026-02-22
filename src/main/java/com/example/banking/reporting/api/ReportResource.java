package com.example.banking.reporting.api;
import com.example.banking.reporting.service.*; import jakarta.annotation.security.RolesAllowed; import jakarta.validation.Valid; import jakarta.ws.rs.*; import jakarta.ws.rs.core.MediaType; import java.util.List; import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Component;
/** JAX-RS resource for banking reports. */
@Component @Path("/reports") @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON) @RequiredArgsConstructor
public class ReportResource {
 private final ReportService service; private final ReportMapper mapper;
 /** Generates a report. Requires REPORT_ADMIN role. */
 @POST @RolesAllowed({"REPORT_ADMIN"}) public GeneratedReportDto generate(@Valid GenerateReportRequestDto request){ return service.generate(mapper.toDomain(request)); }
 /** Gets a report by id. Requires REPORT_VIEWER or REPORT_ADMIN role. */
 @GET @Path("/{id}") @RolesAllowed({"REPORT_VIEWER","REPORT_ADMIN"}) public GeneratedReportDto get(@PathParam("id") long id){ return service.getById(id); }
 /** Lists reports by account. Requires REPORT_VIEWER or REPORT_ADMIN role. */
 @GET @RolesAllowed({"REPORT_VIEWER","REPORT_ADMIN"}) public List<GeneratedReportDto> list(@QueryParam("accountId") long accountId){ return service.listByAccount(accountId); }
}
