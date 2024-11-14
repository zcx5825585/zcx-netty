package org.zcx.netty.coap.common;

public class CoapMessageCode {

    /**
     * Corresponds to Code 0
     */
    public static final int EMPTY = 0;

    /**
     * Corresponds to Request Code 1
     */
    public static final int GET = 1;

    /**
     * Corresponds to Request Code 2
     */
    public static final int POST = 2;

    /**
     * Corresponds to Request Code 3
     */
    public static final int PUT =3;

    /**
     * Corresponds to Request Code 4
     */
    public static final int DELETE = 4;

    /**
     * Corresponds to Response Code 65
     */
    public static final int CREATED_201 = 65;

    /**
     * Corresponds to Response Code 66
     */
    public static final int DELETED_202 = 66;

    /**
     * Corresponds to Response Code 67
     */
    public static final int VALID_203 = 67;

    /**
     * Corresponds to Response Code 68
     */
    public static final int CHANGED_204 = 68;

    /**
     * Corresponds to Response Code 69
     */
    public static final int CONTENT_205 = 69;

    /**
     * Corresponds to Response Code 95
     */
    public static final int CONTINUE_231 = 95;

    /**
     * Corresponds to Response Code 128
     */
    public static final int BAD_REQUEST_400 = 128;

    /**
     * Corresponds to Response Code 129
     */
    public static final int UNAUTHORIZED_401 = 129;

    /**
     * Corresponds to Response Code 130
     */
    public static final int BAD_OPTION_402 = 130;

    /**
     * Corresponds to Response Code 131
     */
    public static final int FORBIDDEN_403 = 131;

    /**
     * Corresponds to Response Code 132
     */
    public static final int NOT_FOUND_404 = 132;

    /**
     * Corresponds to Response Code 133
     */
    public static final int METHOD_NOT_ALLOWED_405 = 133;

    /**
     * Corresponds to Response Code 134
     */
    public static final int NOT_ACCEPTABLE_406 = 134;

    /**
     * Corresponds to Response Code 136
     */
    public static final int REQUEST_ENTITY_INCOMPLETE_408 = 136;

    /**
     * Corresponds to Response Code 140
     */
    public static final int PRECONDITION_FAILED_412 = 140;

    /**
     * Corresponds to Response Code 141
     */
    public static final int REQUEST_ENTITY_TOO_LARGE_413 = 141;

    /**
     * Corresponds to Response Code 143
     */
    public static final int UNSUPPORTED_CONTENT_FORMAT_415 = 143;

    /**
     * Corresponds to Response Code 160
     */
    public static final int INTERNAL_SERVER_ERROR_500 = 160;

    /**
     * Corresponds to Response Code 161
     */
    public static final int NOT_IMPLEMENTED_501 = 161;

    /**
     * Corresponds to Response Code 162
     */
    public static final int BAD_GATEWAY_502 = 162;

    /**
     * Corresponds to Response Code 163
     */
    public static final int SERVICE_UNAVAILABLE_503 = 163;

    /**
     * Corresponds to Response Code 164
     */
    public static final int GATEWAY_TIMEOUT_504 = 164;

    /**
     * Corresponds to Response Code 165
     */
    public static final int PROXYING_NOT_SUPPORTED_505 = 165;
}
