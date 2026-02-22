package com.example.banking.reporting.security;

import com.example.banking.reporting.api.ReportResource;
import jakarta.annotation.security.RolesAllowed;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies role annotations on report endpoints.
 */
class ReportResourceSecurityAnnotationsTest {

    /** Ensures generate endpoint is admin-only. */
    @Test
    void generateIsRestrictedToAdmin() throws Exception {
        Method m = Arrays.stream(ReportResource.class.getDeclaredMethods())
            .filter(x -> x.getName().equals("generate"))
            .findFirst()
            .orElseThrow();
        RolesAllowed ra = m.getAnnotation(RolesAllowed.class);
        assertNotNull(ra);
        assertArrayEquals(new String[]{"REPORT_ADMIN"}, ra.value());
    }

    /** Ensures read endpoints are available to viewer/admin roles. */
    @Test
    void readEndpointsRequireViewerOrAdmin() throws Exception {
        assertViewerOrAdmin("get");
        assertViewerOrAdmin("list");
    }

    private void assertViewerOrAdmin(String methodName) throws Exception {
        Method m = Arrays.stream(ReportResource.class.getDeclaredMethods())
            .filter(x -> x.getName().equals(methodName))
            .findFirst()
            .orElseThrow();
        RolesAllowed ra = m.getAnnotation(RolesAllowed.class);
        assertNotNull(ra, () -> methodName + " must be annotated with @RolesAllowed");
        assertArrayEquals(new String[]{"REPORT_VIEWER", "REPORT_ADMIN"}, ra.value());
    }
}
