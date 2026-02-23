package com.scalebackend.dto.response;

public class ServiceMessageResponse<T> {
    public String severity;
    public String summary;
    public String detail;
    public boolean status;
    public T data;

    public static <T> ServiceMessageResponse<T> success(String detail, T data) {
        ServiceMessageResponse<T> r = new ServiceMessageResponse<>();
        r.severity = "success";
        r.summary = "Success!";
        r.detail = detail;
        r.status = true;
        r.data = data;
        return r;
    }

    public static <T> ServiceMessageResponse<T> error(String detail) {
        ServiceMessageResponse<T> r = new ServiceMessageResponse<>();
        r.severity = "error";
        r.summary = "Error!";
        r.detail = detail;
        r.status = false;
        r.data = null;
        return r;
    }
}
