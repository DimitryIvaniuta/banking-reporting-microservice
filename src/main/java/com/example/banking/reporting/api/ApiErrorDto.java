package com.example.banking.reporting.api;
import lombok.Builder; import lombok.Value;
/** Standard structured API error response. */
@Value @Builder public class ApiErrorDto { String code; String message; }
