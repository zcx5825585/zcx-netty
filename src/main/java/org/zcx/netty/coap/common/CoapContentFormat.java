package org.zcx.netty.coap.common;

public class CoapContentFormat {

    /**
     * Corresponds to number -1
     */
    public static final long UNDEFINED = -1;

    /**
     * Corresponds to number 0
     */
    public static final long TEXT_PLAIN_UTF8 = 0;

    /**
     * Corresponds to number 40
     */
    public static final long APP_LINK_FORMAT = 40;

    /**
     * Corresponds to number 41
     */
    public static final long APP_XML = 41;

    /**
     * Corresponds to number 42
     */
    public static final long APP_OCTET_STREAM = 42;

    /**
     * Corresponds to number 47
     */
    public static final long APP_EXI = 47;

    /**
     * Corresponds to number 50
     */
    public static final long APP_JSON = 50;

    /**
     * Corresponds to number 201 (no standard but defined for very selfish reasons)
     */
    public static final long APP_RDF_XML = 201;

    /**
     * Corresponds to number 202 (no standard but defined for very selfish reasons)
     */
    public static final long APP_TURTLE = 202;

    /**
     * Corresponds to number 203 (no standard but defined for very selfish reasons)
     */
    public static final long APP_N3 = 203;

    /**
     * Corresponds to number 205 (no standard but defined for very selfish reasons)
     */
    public static final long APP_SHDT = 205;
}
