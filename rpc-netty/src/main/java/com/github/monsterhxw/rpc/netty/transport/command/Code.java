package com.github.monsterhxw.rpc.netty.transport.command;

import java.util.HashMap;

/**
 * @author huangxuewei
 * @since 2023/12/26
 */
public enum Code {

    SUCCESS(0, "SUCCESS"),
    UNKNOWN_ERROR(-1, "UNKNOWN_ERROR"),
    NO_PROVIDER(-2, "NO_PROVIDER"),
    UNSUPPORTED_TYPE(-3, "UNSUPPORTED_TYPE");

    private static final HashMap<Integer, Code> CODES_TABLE = new HashMap<>();

    static {
        for (Code code : Code.values()) {
            CODES_TABLE.put(code.code, code);
        }
    }

    public static Code valueOf(int code) {
        return CODES_TABLE.get(code);
    }

    public String getMessage(Object... args) {
        if (args.length < 1) {
            return message;
        }
        return String.format(message, args);
    }

    private int code;
    private String message;

    Code(int code, String message) {
        this.code = code;
        this.message = message;
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    }
