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
    CATEGORY_DUPLICATE(1020, "Name is duplicated with another name", HttpStatus.BAD_REQUEST),
    COMPONENT_NOT_FOUND(1021, "Component not found", HttpStatus.NOT_FOUND),
    NAME_NOT_BLANK(1022, "Name must not be empty", HttpStatus.BAD_REQUEST),
    LIST_COMPONENT_NOT_FOUND(1023, "There are components in list that can't be found", HttpStatus.NOT_FOUND),
    UPLOAD_FILE_FAILED(1024, "Upload file failed", HttpStatus.INTERNAL_SERVER_ERROR),
    COMPONENT_EXISTED(1025, "Component already exists", HttpStatus.BAD_REQUEST),
    COMPONENT_EXISTED_IN_CATE(
            1026, "There are components in list that already exist in category", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(1027, "Invalid request", HttpStatus.BAD_REQUEST),
    COMPONENT_DUPLICATE(1028, "Name is duplicated with another name", HttpStatus.BAD_REQUEST),
    NEW_PASSWORD_SAME_OLD_PASSWORD(
            1029, "The new password cannot be the same as the old password.", HttpStatus.BAD_REQUEST),
    LIST_COMPONENT_NOT_BLANK(1030, "List of components must not be empty", HttpStatus.BAD_REQUEST),
            1029, "The new password cannot be the same as the old password.", HttpStatus.BAD_REQUEST),
    LIST_COMPONENT_NOT_BLANK(1030, "List of components must not be empty", HttpStatus.BAD_REQUEST),
    DELETE_FILE_FAILED(1031, "Delete file failed", HttpStatus.INTERNAL_SERVER_ERROR),
    IMAGE_NULL(1032, "Image is null", HttpStatus.BAD_REQUEST),
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
