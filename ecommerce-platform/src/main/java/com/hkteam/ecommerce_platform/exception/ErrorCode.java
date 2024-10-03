package com.hkteam.ecommerce_platform.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public enum ErrorCode {
    INVALID_KEY(1001, "Invalid key", HttpStatus.BAD_REQUEST),
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized Error", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "{field} be between {min} and {max} characters", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1004, "{field} must be between {min} and {max} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHORIZED(1006, "You do not have permission", HttpStatus.FORBIDDEN),
    UNAUTHENTICATED(1007, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(1009, "Role not found", HttpStatus.NOT_FOUND),
    NOT_BLANK(1014, "{field} must not be empty", HttpStatus.BAD_REQUEST),
    PASSWORD_FORMAT_INVALID(
            1015,
            "Password must contain at least one uppercase letter, one lowercase letter, and one number",
            HttpStatus.BAD_REQUEST),
    PASSWORDS_DO_NOT_MATCH(1016, "Password and confirm password do not match.", HttpStatus.BAD_REQUEST),
    CATEGORY_EXISTED(1017, "Category already exists", HttpStatus.BAD_REQUEST),
    PARENT_CATEGORY_NOT_FOUND(1018, "Parent category not found", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND(1019, "Category not found", HttpStatus.NOT_FOUND),
    CATEGORY_DUPLICATE(1020, "Category name is duplicated with another name", HttpStatus.BAD_REQUEST),
    COMPONENT_NOT_FOUND(1021, "Component not found", HttpStatus.NOT_FOUND),
    NAME_NOT_BLANK(1022, "Name must not be empty", HttpStatus.BAD_REQUEST),
    LIST_COMPONENT_NOT_FOUND(1023, "There are components in list that can't be found", HttpStatus.NOT_FOUND),
    UPLOAD_FILE_FAILED(1024, "Upload file failed", HttpStatus.INTERNAL_SERVER_ERROR),
    COMPONENT_EXISTED(1025, "Component already exists", HttpStatus.BAD_REQUEST),
    COMPONENT_EXISTED_IN_CATE(
            1026, "There are components in list that already exist in category", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(1027, "Invalid request", HttpStatus.BAD_REQUEST),
    COMPONENT_DUPLICATE(1028, "Component name is duplicated with another name", HttpStatus.BAD_REQUEST),
    NEW_PASSWORD_SAME_OLD_PASSWORD(
            1029, "The new password cannot be the same as the old password.", HttpStatus.BAD_REQUEST),
    LIST_COMPONENT_NOT_BLANK(1030, "List of components must not be empty", HttpStatus.BAD_REQUEST),
    IMAGE_NULL(1031, "Image is null", HttpStatus.BAD_REQUEST),
    BRAND_EXISTED(1032, "Brand already exists", HttpStatus.BAD_REQUEST),
    BRAND_NOT_FOUND(1033, "Brand not found", HttpStatus.NOT_FOUND),
    BRAND_DUPLICATE(1034, "Brand name is duplicated with another name", HttpStatus.BAD_REQUEST),
    FILE_LIMIT_OF_1MB(1035, "File size exceeds the maximum limit of 1MB", HttpStatus.BAD_REQUEST),
    ACCEPTED_FILE_TYPES(1036, "Invalid file type only JPG, PNG, JPEG, GIF are accepted", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_BLANK(1037, "Email must not be empty", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL_FORMAT(1038, "Please provide a valid email address", HttpStatus.BAD_REQUEST),
    NEW_EMAIL_SAME_CURRENT_EMAIL(
            1039, "The new email cannot be the same as the current email.", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1040, "Email existed", HttpStatus.BAD_REQUEST),
    EMAIL_SEND_FAILURE(1041, "Send mail failure", HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_TOKEN_TOO_RECENT(
            1042, "A new email token cannot be generated within 30 seconds of the last token.", HttpStatus.BAD_REQUEST),
    PAGE_NOT_FOUND(1043, "Page not found", HttpStatus.NOT_FOUND),
    TOKEN_INVALID(1044, "Token is invalid or expired", HttpStatus.BAD_REQUEST),
    VALIDATION_EMAIL_FAILURE(1045, "Email verification failed", HttpStatus.INTERNAL_SERVER_ERROR),
    ALREADY_VERIFIED(1046, "Email already verified", HttpStatus.BAD_REQUEST),
    UNKNOWN_ERROR(1047, "An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR),
    ADDRESS_EXISTED(1044, "Address already exists", HttpStatus.BAD_REQUEST),
    PHONE_10_DIGITS(1045, "Phone number must be 10 digits", HttpStatus.BAD_REQUEST),
    INVALID_PHONE(1046, "Phone only accepts digits, must be 10 digits and start with 0", HttpStatus.BAD_REQUEST),
    ADDRESS_NOT_FOUND(1047, "Address not found", HttpStatus.NOT_FOUND),
    ADDRESS_NOT_BELONG_TO_USER(1048, "Address not belong to user", HttpStatus.BAD_REQUEST),
    DATE_OF_BIRTH_INVALID(1053, "Date of birth is invalid", HttpStatus.BAD_REQUEST),
    GENDER_INVALID(1054, "Gender is invalid", HttpStatus.BAD_REQUEST),
    DUPLICATE_COMPONENT_IDS(1055, "Duplicate component id", HttpStatus.BAD_REQUEST),
    INVALID_SPACE(1056, "{field} must not blank and not have spaces at start or end", HttpStatus.BAD_REQUEST),
    ;

    int code;
    String message;
    HttpStatusCode httpStatusCode;

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
