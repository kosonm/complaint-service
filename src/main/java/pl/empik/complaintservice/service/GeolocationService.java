package pl.empik.complaintservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import pl.empik.complaintservice.constants.ComplaintConstants;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeolocationService {

    private final RestTemplate restTemplate;
    @Value("${geolocation.api.url}")
    private String geolocationApiUrl;

    /**
     * Retrieves the country based on the provided IP address.
     *
     * @param ipAddress the IP address
     * @return the country name or "Unknown" if unable to determine
     */
    public String getCountryFromIp(String ipAddress) {
        try {
            String apiUrl = geolocationApiUrl + ipAddress;
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(apiUrl, JsonNode.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode responseBody = response.getBody();

                if (responseBody.has(ComplaintConstants.JSON_STATUS) && ComplaintConstants.JSON_SUCCESS.equals(responseBody.get(ComplaintConstants.JSON_STATUS).asText())) {
                    return responseBody.get(ComplaintConstants.JSON_COUNTRY).asText();
                }

            }

            log.warn("Unable to determine country for IP: {}", ipAddress);
            return ComplaintConstants.UNKNOWN_COUNTRY;
        } catch (RestClientException e) {
            log.error("Error while calling geolocation API for IP: {}", ipAddress, e);
            return ComplaintConstants.UNKNOWN_COUNTRY;
        }
    }

}
