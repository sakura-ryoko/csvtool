package csvtool.enums;

public enum ExitCode
{
    SUCCESS             (0, "Success"),
    INVALID_OPERATION   (1, "Invalid Operation"),
    INVALID_SYNTAX      (2, "Invalid Syntax"),
    MISSING_INPUT2      (3, "Required Input File Is Missing"),
    MISSING_OUTPUT      (4, "Required Output File Is Missing"),
    MISSING_HEADERS     (5, "Required Headers Are Missing"),
    FILE_NOT_FOUND      (6, "File Not Found");

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
