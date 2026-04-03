package com.keldorn.phenylalaninecalculatorapi.constant;

public final class ApiResponses {

    public static final String CLIENT_ERROR = "Client Error";
    public static final String INTERNAL_ERROR = "Internal Error";

    public static final String EMAIL_IS_TAKEN_RESPONSE = "This email address is already in use.";
    public static final String USERNAME_IS_TAKEN_RESPONSE = "This username is already taken.";
    public static final String PASSWORD_MISMATCH_RESPONSE = "The password provided does not match our records.";
    public static final String DELETED_ACCOUNT_RESPONSE = "This account no longer exists.";
    public static final String UNAUTHORIZED_RESPONSE = "Invalid or expired authentication token or bad credentials";
    public static final String RESOURCE_NOT_FOUND_RESPONSE = "The requested resource could not be found.";
    public static final String DAILY_INTAKE_NEGATIVE_RESPONSE = "Daily intake values must be zero or greater.";
    public static final String INTERNAL_RESPONSE = "An internal server error occurred. Please try again later.";
    public static final String DEFAULT_RESPONSE = "Invalid request parameters.";
    public static final String MALFORMED_RESPONSE = "Malformed data received";
    public static final String REQUIRED_MISSING_RESPONSE = "Required parameter is missing: %s";
    public static final String AUTHENTICATION_REQUIRED_RESPONSE =
            "Full authentication is required to access this resource";

}
