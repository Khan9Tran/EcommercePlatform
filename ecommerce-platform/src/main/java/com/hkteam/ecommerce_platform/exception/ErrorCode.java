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
    FILE_NULL(1031, "File is null", HttpStatus.BAD_REQUEST),
    BRAND_EXISTED(1032, "Brand already exists", HttpStatus.BAD_REQUEST),
    BRAND_NOT_FOUND(1033, "Brand not found", HttpStatus.NOT_FOUND),
    BRAND_DUPLICATE(1034, "Brand name is duplicated with another name", HttpStatus.BAD_REQUEST),
    FILE_LIMIT_OF_1MB(1035, "File size exceeds the maximum limit of 1MB", HttpStatus.BAD_REQUEST),
    ACCEPTED_IMAGE_TYPES(1036, "Invalid file type only JPG, PNG, JPEG, GIF are accepted", HttpStatus.BAD_REQUEST),
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
    ADDRESS_EXISTED(1048, "Address already exists", HttpStatus.BAD_REQUEST),
    INVALID_PHONE(1049, "Phone only accepts digits, must be 10 digits and start with 0", HttpStatus.BAD_REQUEST),
    ADDRESS_NOT_FOUND(1051, "Address not found", HttpStatus.NOT_FOUND),
    ADDRESS_NOT_BELONG_TO_USER(1052, "Address not belong to user", HttpStatus.BAD_REQUEST),
    DATE_OF_BIRTH_INVALID(1053, "Date of birth is invalid", HttpStatus.BAD_REQUEST),
    GENDER_INVALID(1054, "Gender is invalid", HttpStatus.BAD_REQUEST),
    PASSWORD_ALREADY_CREATED(1055, "Password already created", HttpStatus.BAD_REQUEST),
    DUPLICATE_COMPONENT_IDS(1056, "Duplicate component id", HttpStatus.BAD_REQUEST),
    INVALID_SPACE(1057, "{field} must not blank and not have spaces at start or end", HttpStatus.BAD_REQUEST),
    FORMAT_ERROR(1058, "Please provide a valid format", HttpStatus.BAD_REQUEST),
    COMPONENT_NOT_EXIST_IN_CATEGORY(1059, "Component not exist in category", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_FOUND(1060, "Product not found", HttpStatus.NOT_FOUND),
    STORE_NOT_FOUND(1061, "Store not found", HttpStatus.NOT_FOUND),
    DELETE_FILE_FAILED(1062, "Delete file failed", HttpStatus.INTERNAL_SERVER_ERROR),
    PASSWORD_INCORRECT(1058, "Password incorrect", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_VERIFIED(1059, "Email not verified", HttpStatus.BAD_REQUEST),
    FILE_LIMIT_OF_50MB(1060, "File size exceeds maximum limit of 50MB", HttpStatus.BAD_REQUEST),
    ACCEPTED_VIDEO_TYPES(1061, "Invalid file type only mp4, avi, mov, mkv", HttpStatus.BAD_REQUEST),
    PRODUCT_IMAGE_NOT_FOUND(1062, "Product image not found", HttpStatus.NOT_FOUND),
    INVALID_PRODUCT_IMAGE_RELATION(1063, "Invalid product image relation", HttpStatus.BAD_REQUEST),
    USER_HAS_BEEN_BLOCKED(1064, "Account is locked. Please contact support", HttpStatus.FORBIDDEN),
    NEW_PHONE_SAME_CURRENT_PHONE(
            1065, "The new phone cannot be the same as the current phone.", HttpStatus.BAD_REQUEST),
    PHONE_EXISTED(1066, "Phone existed", HttpStatus.BAD_REQUEST),
    LIST_PRODUCT_IMAGE_NOT_BLANK(1062, "List product image must be not blank", HttpStatus.NOT_FOUND),
    IMAGE_DOES_NOT_BELONG_TO_PRODUCT(
            1063, "There are product image in list that does not belong to product", HttpStatus.BAD_REQUEST),
    DUPLICATE_PRODUCT_IMAGE_IDS(1064, "Duplicate product image id", HttpStatus.BAD_REQUEST),
    LIST_PRODUCT_IMAGE_NOT_FOUND(1065, "There are product image in list that can't be found", HttpStatus.NOT_FOUND),
    BRAND_LATER_EXISTED(1066, "Brand already exists or a similar copy has been deleted", HttpStatus.BAD_REQUEST),
    CATEGORY_LATER_EXISTED(1067, "Category already exists or a similar copy has been deleted", HttpStatus.BAD_REQUEST),
    SELLER_ALREADY_REGISTER(1068, "You registered store", HttpStatus.BAD_REQUEST),
    PRODUCT_INVALID(1679, "{field} be between {min} and {max} characters", HttpStatus.BAD_REQUEST),
    PRICE_INVALID(1680, "Price must be [0 - 999.999.999] ", HttpStatus.BAD_REQUEST),
    VARIANT_NOT_FOUND(1681, "Variant not found", HttpStatus.BAD_REQUEST),
    VALUE_NOT_FOUND(1682, "Value not found", HttpStatus.BAD_REQUEST),
    COMPONENT_VALUE_REQUIRED(1683, "Component value required", HttpStatus.BAD_REQUEST),
    SORT_BY_INVALID(1684, "Sort by is invalid", HttpStatus.BAD_REQUEST),
    ORDER_INVALID(1685, "Order is invalid", HttpStatus.BAD_REQUEST),
    TAB_INVALID(1686, "Tab is invalid", HttpStatus.BAD_REQUEST),
    QUANTITY_NOT_ENOUGH(1687, "Quantity is not enough", HttpStatus.BAD_REQUEST),
    CART_ITEM_NOT_FOUND(1688, "Cart item not found", HttpStatus.NOT_FOUND),
    CART_NOT_FOUND(1689, "Cart  not found", HttpStatus.NOT_FOUND),
    PRODUCT_COMPONENT_VALUE_NOT_FOUND(1690, "Product component value not found", HttpStatus.NOT_FOUND),
    REQUIRED_NOT_EMPTY(1691, "Need fill value in this field", HttpStatus.BAD_REQUEST),
    PAYMENT_FAILED(1692, "Payment failed", HttpStatus.BAD_REQUEST),
    ORDER_NOT_FOUND(1693, "Order not found", HttpStatus.NOT_FOUND),
    STATUS_NOT_FOUND(1694, "Status not found", HttpStatus.NOT_FOUND),
    NOT_PERMISSION_ORDER(1695, "You do not have permission to update this order", HttpStatus.BAD_REQUEST),
    ORDER_NOT_BELONG_TO_STORE(1696, "This order doesn't belong to store", HttpStatus.BAD_REQUEST),
    ORDER_CANCELLED(1697, "Order cancelled", HttpStatus.BAD_REQUEST),
    PRODUCT_PRICE_HAS_CHANGE(1698, "Product has new price, please reload", HttpStatus.BAD_REQUEST),
    RETRY_FAILED(1699, "There are too many orders at the moment. Please try again shortly.", HttpStatus.BAD_REQUEST),
    STATUS_HISTORY_NOT_FOUND(1700, "Order history status not found", HttpStatus.NOT_FOUND),
    SELLER_PREPARING_COMPLETED_ORDER(
            1701, "Seller is preparing goods or order has been delivered successfully", HttpStatus.BAD_REQUEST),
    CANNOT_UN_DEFAULT(1702, "Cannot un-default address", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_DEFAULT_ADDRESS(1703, "Cannot delete default address", HttpStatus.BAD_REQUEST),
    NOT_NULL(1704, "{field} must not be null", HttpStatus.BAD_REQUEST),
    ORDER_NOT_BELONG_TO_USER(1705, "This order doesn't belong to you", HttpStatus.BAD_REQUEST),
    PAYMENT_NOT_FOUND(1706, "Payment not found", HttpStatus.NOT_FOUND),
    TRANSACTION_STATUS_HISTORY_NOT_FOUND(1707, "Transaction status history not found", HttpStatus.NOT_FOUND),
    STORE_BANNED(1708, "Store is banned", HttpStatus.BAD_REQUEST),
    PRODUCT_HAS_FOLLOWED(1709, "Product has been followed", HttpStatus.BAD_REQUEST),
    LIMIT_FOLLOW_40_PRODUCT(1710, "You has limit follow product is 40", HttpStatus.BAD_REQUEST),
    SALE_CANT_GREATER_THAN_ORIGINAL_PRICE(1711, "Sale price can't greater than original price", HttpStatus.BAD_REQUEST),
    HAS_MORE_PRODUCT_IN_CART(1712, "You has more products....", HttpStatus.BAD_REQUEST),
    RETRY_LOGIN(9900, "You need re login", HttpStatus.BAD_REQUEST),
    COMMENT_INVALID(1713, "{field} must less than {max} characters", HttpStatus.BAD_REQUEST),
    NOT_PURCHASED(1714, "You haven't purchased this products", HttpStatus.BAD_REQUEST),
    REVIEW_NOT_FOUND(1715, "Review not found", HttpStatus.NOT_FOUND),
    ORDER_NOT_DELIVERED(1716, "Order not delivered", HttpStatus.BAD_REQUEST),
    RATING_INVALID(1717, "Rating must be between 0 and 5 star", HttpStatus.BAD_REQUEST),
    PRODUCT_MIN_PRICE(1718, "You need set price > 0", HttpStatus.BAD_REQUEST),
    PRODUCT_MAX_PRICE(1719, "You need set price < 999,999,999d", HttpStatus.BAD_REQUEST),
    ADDRESS_LIMIT_EXCEEDED(1720, "The number of addresses created has reached the limit.", HttpStatus.BAD_REQUEST),
    ALREADY_REVIEWED(1721, "You reviewed this order", HttpStatus.BAD_REQUEST),
    SIZE_TOO_LARGE(1722,"Size too large" ,HttpStatus.BAD_REQUEST );

    int code;
    String message;
    HttpStatusCode httpStatusCode;

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
