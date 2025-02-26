package pl.empik.complaintservice.constants;

public final class ComplaintConstants {
    public static final String API_COMPLAINTS_BASE = "/api/complaints";
    public static final String COMPLAINT_NOT_FOUND_MESSAGE = "Complaint not found with id: ";
    public static final String UNKNOWN_COUNTRY = "Unknown";
    public static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";

    // JSON keys for Geolocation API response
    public static final String JSON_STATUS = "status";
    public static final String JSON_SUCCESS = "success";
    public static final String JSON_COUNTRY = "country";
}

