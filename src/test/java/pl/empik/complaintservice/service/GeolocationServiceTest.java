package pl.empik.complaintservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import pl.empik.complaintservice.constants.ComplaintConstants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeolocationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GeolocationService geolocationService;

    private static final String TEST_IP_ADDRESS = "8.8.8.8";
    private static final String TEST_COUNTRY = "United States";
    private static final String TEST_API_URL = "http://ip-api.com/json/";

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(geolocationService, "geolocationApiUrl", TEST_API_URL);
    }

    @Test
    void getCountryFromIp_SuccessfulResponse_ShouldReturnCountry() {
        // given
        JsonNode responseNode = createSuccessfulResponse();
        ResponseEntity<JsonNode> responseEntity = new ResponseEntity<>(responseNode, HttpStatus.OK);

        when(restTemplate.getForEntity(TEST_API_URL + TEST_IP_ADDRESS, JsonNode.class))
                .thenReturn(responseEntity);

        // when
        String result = geolocationService.getCountryFromIp(TEST_IP_ADDRESS);

        // then
        assertEquals(TEST_COUNTRY, result);
    }

    @Test
    void getCountryFromIp_UnsuccessfulResponseStatus_ShouldReturnUnknown() {
        // given
        JsonNode responseNode = createUnsuccessfulResponse();
        ResponseEntity<JsonNode> responseEntity = new ResponseEntity<>(responseNode, HttpStatus.OK);

        when(restTemplate.getForEntity(TEST_API_URL + TEST_IP_ADDRESS, JsonNode.class))
                .thenReturn(responseEntity);

        // when
        String result = geolocationService.getCountryFromIp(TEST_IP_ADDRESS);

        // then
        assertEquals(ComplaintConstants.UNKNOWN_COUNTRY, result);
    }

    @Test
    void getCountryFromIp_NullResponseBody_ShouldReturnUnknown() {
        // given
        ResponseEntity<JsonNode> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.getForEntity(TEST_API_URL + TEST_IP_ADDRESS, JsonNode.class))
                .thenReturn(responseEntity);

        // when
        String result = geolocationService.getCountryFromIp(TEST_IP_ADDRESS);

        // then
        assertEquals(ComplaintConstants.UNKNOWN_COUNTRY, result);
    }

    @Test
    void getCountryFromIp_ErrorResponse_ShouldReturnUnknown() {
        // given
        ResponseEntity<JsonNode> responseEntity = new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.getForEntity(TEST_API_URL + TEST_IP_ADDRESS, JsonNode.class))
                .thenReturn(responseEntity);

        // when
        String result = geolocationService.getCountryFromIp(TEST_IP_ADDRESS);

        // then
        assertEquals(ComplaintConstants.UNKNOWN_COUNTRY, result);
    }

    @Test
    void getCountryFromIp_RestClientException_ShouldReturnUnknown() {
        // given
        when(restTemplate.getForEntity(anyString(), eq(JsonNode.class)))
                .thenThrow(new RestClientException("Connection error"));

        // when
        String result = geolocationService.getCountryFromIp(TEST_IP_ADDRESS);

        // then
        assertEquals(ComplaintConstants.UNKNOWN_COUNTRY, result);
    }

    private JsonNode createSuccessfulResponse() {
        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put(ComplaintConstants.JSON_STATUS, ComplaintConstants.JSON_SUCCESS);
        rootNode.put(ComplaintConstants.JSON_COUNTRY, GeolocationServiceTest.TEST_COUNTRY);
        return rootNode;
    }

    private JsonNode createUnsuccessfulResponse() {
        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put(ComplaintConstants.JSON_STATUS, "fail");
        return rootNode;
    }
}