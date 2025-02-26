package pl.empik.complaintservice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComplaintRequest(
        @NotBlank(message = "Product ID cannot be empty")
        String productId,

        @NotBlank(message = "Content cannot be empty")
        @Size(max = 1000, message = "Content must be less than 1000 characters")
        String content,

        @NotBlank(message = "Reporter name cannot be empty")
        String reportedBy
) {}
