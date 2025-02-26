package pl.empik.complaintservice.controller;

import pl.empik.complaintservice.constants.ComplaintConstants;
import pl.empik.complaintservice.model.ComplaintRequest;
import pl.empik.complaintservice.model.ComplaintResponse;
import pl.empik.complaintservice.model.ComplaintUpdateRequest;
import pl.empik.complaintservice.service.ComplaintService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ComplaintConstants.API_COMPLAINTS_BASE)
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    /**
     * Retrieves all complaints.
     *
     * @return list of all complaints
     */
    @GetMapping
    public ResponseEntity<List<ComplaintResponse>> getAllComplaints() {
        return ResponseEntity.ok(complaintService.getAllComplaints());
    }

    /**
     * Retrieves a complaint by its ID.
     *
     * @param id the complaint ID
     * @return the complaint
     */
    @GetMapping("/{id}")
    public ResponseEntity<ComplaintResponse> getComplaintById(@PathVariable Long id) {
        return ResponseEntity.ok(complaintService.getComplaintById(id));
    }

    /**
     * Creates a new complaint.
     *
     * @param complaintRequest the complaint data
     * @param request the HTTP request with client IP
     * @return the created complaint
     */
    @PostMapping
    public ResponseEntity<ComplaintResponse> createComplaint(
            @Valid @RequestBody ComplaintRequest complaintRequest,
            HttpServletRequest request) {

        String clientIp = extractClientIp(request);
        ComplaintResponse createdComplaint = complaintService.createComplaint(complaintRequest, clientIp);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComplaint);
    }

    /**
     * Updates a complaint's content.
     *
     * @param id the complaint ID
     * @param updateRequest the updated content
     * @return the updated complaint
     */
    @PutMapping("/{id}")
    public ResponseEntity<ComplaintResponse> updateComplaint(
            @PathVariable Long id,
            @Valid @RequestBody ComplaintUpdateRequest updateRequest) {

        return ResponseEntity.ok(complaintService.updateComplaint(id, updateRequest));
    }

    /**
     * Extracts the client's IP address from the request.
     *
     * @param request the HTTP request
     * @return the client's IP address
     */
    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader(ComplaintConstants.HEADER_X_FORWARDED_FOR
        );
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Get first IP if multiple are present (client IP is the first one)
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
