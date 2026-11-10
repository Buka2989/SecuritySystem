package kz.school.security.exception;
public class ApiException extends RuntimeException {
    public final int status;
    public ApiException(int status, String detail) { super(detail); this.status=status; }
    public static ApiException missing(String detail) { return new ApiException(404,detail); }
}
