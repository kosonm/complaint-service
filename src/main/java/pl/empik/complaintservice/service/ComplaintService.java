package pl.empik.complaintservice.service;

import pl.empik.complaintservice.constants.ComplaintConstants;
import pl.empik.complaintservice.exception.ComplaintNotFoundException;
import pl.empik.complaintservice.model.Complaint;
import pl.empik.complaintservice.model.ComplaintRequest;
import pl.empik.complaintservice.model.ComplaintResponse;
import pl.empik.complaintservice.model.ComplaintUpdateRequest;
import pl.empik.complaintservice.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final GeolocationService geolocationService;

    /**
     * Retrieves all complaints from the database.
     *
     * @return list of all complaints
     */
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getAllComplaints() {
        return complaintRepository.findAll().stream()
                .map(ComplaintResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a complaint by its ID.
     *
     * @param id the complaint ID
     * @return the complaint
     * @throws ComplaintNotFoundException if complaint not found
     */
    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintById(Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ComplaintNotFoundException(ComplaintConstants.COMPLAINT_NOT_FOUND_MESSAGE + id));
        return ComplaintResponse.fromEntity(complaint);
    }

    /**
     * Creates a new complaint or increments counter for an existing one.
     *
     * @param complaintRequest the complaint data
     * @param ipAddress client IP address
     * @return the created complaint
     */
    @Transactional
    public ComplaintResponse createComplaint(ComplaintRequest complaintRequest, String ipAddress) {
        String country = geolocationService.getCountryFromIp(ipAddress);

        // Check if a complaint with the same productId and reportedBy already exists
        return complaintRepository.findByProductIdAndReportedBy(
                complaintRequest.productId(),
                complaintRequest.reportedBy()
        ).map(existingComplaint -> {
            // Increment counter for duplicate complaint
            existingComplaint.setCounter(existingComplaint.getCounter() + 1);
            return ComplaintResponse.fromEntity(complaintRepository.save(existingComplaint));
        }).orElseGet(() -> {
            // Create new complaint
            Complaint newComplaint = Complaint.builder()
                    .productId(complaintRequest.productId())
                    .content(complaintRequest.content())
                    .reportedBy(complaintRequest.reportedBy())
                    .country(country)
                    .createdAt(LocalDateTime.now())
                    .counter(1)
                    .build();
            return ComplaintResponse.fromEntity(complaintRepository.save(newComplaint));
        });
    }

    /**
     * Updates the content of an existing complaint.
     *
     * @param id the complaint ID
     * @param updateRequest the updated content
     * @return the updated complaint
     * @throws ComplaintNotFoundException if complaint not found
     */
    @Transactional
    public ComplaintResponse updateComplaint(Long id, ComplaintUpdateRequest updateRequest) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ComplaintNotFoundException(ComplaintConstants.COMPLAINT_NOT_FOUND_MESSAGE + id));

        complaint.setContent(updateRequest.content());
        return ComplaintResponse.fromEntity(complaintRepository.save(complaint));
    }
}
