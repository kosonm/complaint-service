package pl.empik.complaintservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.empik.complaintservice.constants.ComplaintConstants;
import pl.empik.complaintservice.exception.ComplaintNotFoundException;
import pl.empik.complaintservice.model.Complaint;
import pl.empik.complaintservice.model.ComplaintRequest;
import pl.empik.complaintservice.model.ComplaintResponse;
import pl.empik.complaintservice.model.ComplaintUpdateRequest;
import pl.empik.complaintservice.repository.ComplaintRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplaintServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;
    @Mock
    private GeolocationService geolocationService;
    @InjectMocks
    private ComplaintService complaintService;

    private static final String TEST_IP_ADDRESS = "192.168.1.1";
    private static final String TEST_COUNTRY = "United States";

    @Test
    void getAllComplaints_ShouldReturnAllComplaints() {
        // given
        Complaint complaint1 = createTestComplaint(1L, "product1", "Defective product", "user1", "Poland", 1);
        Complaint complaint2 = createTestComplaint(2L, "product2", "Missing parts", "user2", "Germany", 1);
        when(complaintRepository.findAll()).thenReturn(Arrays.asList(complaint1, complaint2));

        // when
        List<ComplaintResponse> result = complaintService.getAllComplaints();

        // then
        assertEquals(2, result.size());
        assertEquals("product1", result.get(0).productId());
        assertEquals("product2", result.get(1).productId());
        verify(complaintRepository, times(1)).findAll();
    }

    @Test
    void getComplaintById_WithValidId_ShouldReturnComplaint() {
        // given
        Complaint complaint = createTestComplaint(1L, "product1", "Defective product", "user1", "Poland", 1);
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(complaint));

        // when
        ComplaintResponse result = complaintService.getComplaintById(1L);

        // then
        assertNotNull(result);
        assertEquals("product1", result.productId());
        assertEquals("Defective product", result.content());
        assertEquals("user1", result.reportedBy());
        verify(complaintRepository, times(1)).findById(1L);
    }

    @Test
    void getComplaintById_WithInvalidId_ShouldThrowException() {
        // given
        Long invalidId = 999L;
        when(complaintRepository.findById(invalidId)).thenReturn(Optional.empty());

        // when & then
        ComplaintNotFoundException exception = assertThrows(
                ComplaintNotFoundException.class,
                () -> complaintService.getComplaintById(invalidId)
        );

        assertEquals(ComplaintConstants.COMPLAINT_NOT_FOUND_MESSAGE + invalidId, exception.getMessage());
        verify(complaintRepository, times(1)).findById(invalidId);
    }

    @Test
    void createComplaint_NewComplaint_ShouldCreateComplaint() {
        // given
        ComplaintRequest request = createTestComplaintRequest("product3", "Wrong color", "user3");
        when(geolocationService.getCountryFromIp(TEST_IP_ADDRESS)).thenReturn(TEST_COUNTRY);
        when(complaintRepository.findByProductIdAndReportedBy(
                request.productId(),
                request.reportedBy()
        )).thenReturn(Optional.empty());

        Complaint savedComplaint = createTestComplaint(3L, request.productId(), request.content(),
                request.reportedBy(), TEST_COUNTRY, 1);

        when(complaintRepository.save(any(Complaint.class))).thenReturn(savedComplaint);

        // when
        ComplaintResponse result = complaintService.createComplaint(request, TEST_IP_ADDRESS);

        // then
        assertNotNull(result);
        assertEquals(request.productId(), result.productId());
        assertEquals(request.content(), result.content());
        assertEquals(request.reportedBy(), result.reportedBy());
        assertEquals(TEST_COUNTRY, result.country());
        assertEquals(1, result.counter());

        verify(geolocationService, times(1)).getCountryFromIp(TEST_IP_ADDRESS);
        verify(complaintRepository, times(1)).findByProductIdAndReportedBy(
                request.productId(),
                request.reportedBy()
        );
        verify(complaintRepository, times(1)).save(any(Complaint.class));
    }

    @Test
    void createComplaint_ExistingComplaint_ShouldIncrementCounter() {
        // given
        ComplaintRequest request = createTestComplaintRequest("product1", "Ignored content", "user1");
        Complaint existingComplaint = createTestComplaint(1L, "product1", "Original content", "user1", "Poland", 1);

        when(geolocationService.getCountryFromIp(TEST_IP_ADDRESS)).thenReturn(TEST_COUNTRY);
        when(complaintRepository.findByProductIdAndReportedBy(
                request.productId(),
                request.reportedBy()
        )).thenReturn(Optional.of(existingComplaint));

        Complaint incrementedComplaint = createTestComplaint(
                existingComplaint.getId(),
                existingComplaint.getProductId(),
                existingComplaint.getContent(),
                existingComplaint.getReportedBy(),
                existingComplaint.getCountry(),
                existingComplaint.getCounter() + 1
        );
        int originalCounter = existingComplaint.getCounter();

        when(complaintRepository.save(any(Complaint.class))).thenReturn(incrementedComplaint);

        // when
        ComplaintResponse result = complaintService.createComplaint(request, TEST_IP_ADDRESS);

        // then
        assertNotNull(result);
        assertEquals(existingComplaint.getProductId(), result.productId());
        assertEquals(existingComplaint.getContent(), result.content());
        assertEquals(existingComplaint.getReportedBy(), result.reportedBy());
        assertEquals(existingComplaint.getCountry(), result.country());
        assertEquals(originalCounter + 1, result.counter());

        verify(geolocationService, times(1)).getCountryFromIp(TEST_IP_ADDRESS);
        verify(complaintRepository, times(1)).findByProductIdAndReportedBy(
                request.productId(),
                request.reportedBy()
        );
        verify(complaintRepository, times(1)).save(any(Complaint.class));
    }

    @Test
    void updateComplaint_WithValidId_ShouldUpdateContent() {
        // given
        Complaint existingComplaint = createTestComplaint(1L, "product1", "Original content", "user1", "Poland", 1);
        ComplaintUpdateRequest updateRequest = createTestUpdateRequest();

        when(complaintRepository.findById(1L)).thenReturn(Optional.of(existingComplaint));

        Complaint updatedComplaint = createTestComplaint(
                existingComplaint.getId(),
                existingComplaint.getProductId(),
                updateRequest.content(),
                existingComplaint.getReportedBy(),
                existingComplaint.getCountry(),
                existingComplaint.getCounter()
        );

        when(complaintRepository.save(any(Complaint.class))).thenReturn(updatedComplaint);

        // when
        ComplaintResponse result = complaintService.updateComplaint(1L, updateRequest);

        // then
        assertNotNull(result);
        assertEquals(existingComplaint.getId(), result.id());
        assertEquals(existingComplaint.getProductId(), result.productId());
        assertEquals(updateRequest.content(), result.content());
        assertEquals(existingComplaint.getReportedBy(), result.reportedBy());

        verify(complaintRepository, times(1)).findById(1L);
        verify(complaintRepository, times(1)).save(any(Complaint.class));
    }

    @Test
    void updateComplaint_WithInvalidId_ShouldThrowException() {
        // given
        Long invalidId = 999L;
        ComplaintUpdateRequest updateRequest = createTestUpdateRequest();
        when(complaintRepository.findById(invalidId)).thenReturn(Optional.empty());

        // when & then
        ComplaintNotFoundException exception = assertThrows(
                ComplaintNotFoundException.class,
                () -> complaintService.updateComplaint(invalidId, updateRequest)
        );

        assertEquals(ComplaintConstants.COMPLAINT_NOT_FOUND_MESSAGE + invalidId, exception.getMessage());
        verify(complaintRepository, times(1)).findById(invalidId);
        verify(complaintRepository, never()).save(any(Complaint.class));
    }

    private Complaint createTestComplaint(Long id, String productId, String content, String reportedBy, String country, int counter) {
        return Complaint.builder()
                .id(id)
                .productId(productId)
                .content(content)
                .createdAt(LocalDateTime.now())
                .reportedBy(reportedBy)
                .country(country)
                .counter(counter)
                .build();
    }

    private ComplaintRequest createTestComplaintRequest(String productId, String content, String reportedBy) {
        return new ComplaintRequest(productId, content, reportedBy);
    }

    private ComplaintUpdateRequest createTestUpdateRequest() {
        return new ComplaintUpdateRequest("Updated content");
    }

}
