package pl.empik.complaintservice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComplaintUpdateRequest(
        @NotBlank(message = "Content cannot be empty")
        @Size(max = 1000, message = "Content must be less than 1000 characters")
        String content
) {}
