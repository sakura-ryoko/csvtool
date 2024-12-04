package csvtool.enums;

public enum ExitCode
{
    SUCCESS             (0, "Success"),
    INVALID_OPERATION   (1, "Invalid Operation"),
    INVALID_SYNTAX      (2, "Invalid Syntax"),
    MISSING_INPUT       (3, "Required Input File Is Missing"),
    MISSING_OUTPUT      (4, "Required Output File Is Missing"),
    MISSING_KEY         (5, "Required Key is Missing"),
    MISSING_HEADERS     (6, "Required Headers Are Missing"),
    FILE_NOT_FOUND      (7, "File Not Found"),
    OPERATION_FAILURE   (8, "Operation has failed");

    private final int code;
    private final String message;

    ExitCode(int code, String msg)
    {
        this.code = code;
        this.message = msg;
    }

    public int get() { return this.code; }

    public String getMessage() { return this.message; }
}
