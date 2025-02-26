package pl.empik.complaintservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import pl.empik.complaintservice.constants.ComplaintConstants;
import pl.empik.complaintservice.exception.ComplaintNotFoundException;
import pl.empik.complaintservice.exception.GlobalExceptionController;
import pl.empik.complaintservice.model.ComplaintRequest;
import pl.empik.complaintservice.model.ComplaintResponse;
import pl.empik.complaintservice.model.ComplaintUpdateRequest;
import pl.empik.complaintservice.service.ComplaintService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = ComplaintControllerTest.TestConfig.class)
class ComplaintControllerTest {

    @Configuration
    @Import({ComplaintController.class, GlobalExceptionController.class})
    static class TestConfig {
        @Bean
        public ComplaintService complaintService() {
            return Mockito.mock(ComplaintService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ComplaintService complaintService;

    @Test
    void getAllComplaints_ShouldReturnListOfComplaints() throws Exception {
        // given
        List<ComplaintResponse> complaints = Arrays.asList(
                createTestComplaintResponse(1L, "product1", "Defective product", "user1", "Poland"),
                createTestComplaintResponse(2L, "product2", "Missing parts", "user2", "Germany")
        );

        when(complaintService.getAllComplaints()).thenReturn(complaints);

        // when & then
        mockMvc.perform(get(ComplaintConstants.API_COMPLAINTS_BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].productId", is("product1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].productId", is("product2")));
    }

    @Test
    void getComplaintById_WithValidId_ShouldReturnComplaint() throws Exception {
        // given
        ComplaintResponse complaint = createTestComplaintResponse(1L, "product1", "Defective product", "user1", "Poland");

        when(complaintService.getComplaintById(1L)).thenReturn(complaint);

        // when & then
        mockMvc.perform(get(ComplaintConstants.API_COMPLAINTS_BASE + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.productId", is("product1")))
                .andExpect(jsonPath("$.content", is("Defective product")))
                .andExpect(jsonPath("$.reportedBy", is("user1")))
                .andExpect(jsonPath("$.country", is("Poland")))
                .andExpect(jsonPath("$.counter", is(1)));
    }

    @Test
    void getComplaintById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // given
        when(complaintService.getComplaintById(999L))
                .thenThrow(new ComplaintNotFoundException(ComplaintConstants.COMPLAINT_NOT_FOUND_MESSAGE + 999));

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get(ComplaintConstants.API_COMPLAINTS_BASE + "/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is(ComplaintConstants.COMPLAINT_NOT_FOUND_MESSAGE + 999)));
    }

    @Test
    void createComplaint_WithValidData_ShouldReturnCreatedComplaint() throws Exception {
        // given
        ComplaintRequest request = new ComplaintRequest("product3", "Wrong color", "user3");
        ComplaintResponse response = createTestComplaintResponse(3L, "product3", "Wrong color", "user3", "United States");

        when(complaintService.createComplaint(any(ComplaintRequest.class), anyString())).thenReturn(response);

        // when & then
        mockMvc.perform(post(ComplaintConstants.API_COMPLAINTS_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.productId", is("product3")))
                .andExpect(jsonPath("$.content", is("Wrong color")))
                .andExpect(jsonPath("$.reportedBy", is("user3")))
                .andExpect(jsonPath("$.country", is("United States")))
                .andExpect(jsonPath("$.counter", is(1)));
    }

    @Test
    void createComplaint_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // given
        ComplaintRequest request = new ComplaintRequest("", "", "");

        // when
        ResultActions result = mockMvc.perform(post(ComplaintConstants.API_COMPLAINTS_BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void updateComplaint_WithValidData_ShouldReturnUpdatedComplaint() throws Exception {
        // given
        ComplaintUpdateRequest updateRequest = new ComplaintUpdateRequest("Updated content");
        ComplaintResponse updatedResponse = createTestComplaintResponse(1L, "product1", "Updated content", "user1", "Poland");

        when(complaintService.updateComplaint(eq(1L), any(ComplaintUpdateRequest.class))).thenReturn(updatedResponse);

        // when & then
        mockMvc.perform(put(ComplaintConstants.API_COMPLAINTS_BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.content", is("Updated content")));
    }

    @Test
    void updateComplaint_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // given
        ComplaintUpdateRequest updateRequest = new ComplaintUpdateRequest("Updated content");

        when(complaintService.updateComplaint(eq(999L), any(ComplaintUpdateRequest.class)))
                .thenThrow(new ComplaintNotFoundException(ComplaintConstants.COMPLAINT_NOT_FOUND_MESSAGE + 999));

        // when & then
        mockMvc.perform(put(ComplaintConstants.API_COMPLAINTS_BASE + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is(ComplaintConstants.COMPLAINT_NOT_FOUND_MESSAGE + 999)));
    }

    private ComplaintResponse createTestComplaintResponse(Long id, String productId, String content,
                                                          String reportedBy, String country) {
        return new ComplaintResponse(
                id,
                productId,
                content,
                LocalDateTime.now(),
                reportedBy,
                country,
                1
        );
    }
}