package csvtool.enums;

public enum ExitCode
{
    SUCCESS(0),
    INVALID_OPERATION(1),
    INVALID_SYNTAX(2);

    private final int code;

    ExitCode(int code)
    {
        this.code = code;
    }

    public int get() { return this.code; }
}
