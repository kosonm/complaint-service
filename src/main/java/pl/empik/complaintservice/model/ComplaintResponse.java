package pl.empik.complaintservice.model;

import java.time.LocalDateTime;

public record ComplaintResponse(
        Long id,
        String productId,
        String content,
        LocalDateTime createdAt,
        String reportedBy,
        String country,
        Integer counter
) {
    public static ComplaintResponse fromEntity(Complaint complaint) {
        return new ComplaintResponse(
                complaint.getId(),
                complaint.getProductId(),
                complaint.getContent(),
                complaint.getCreatedAt(),
                complaint.getReportedBy(),
                complaint.getCountry(),
                complaint.getCounter()
        );
    }
}