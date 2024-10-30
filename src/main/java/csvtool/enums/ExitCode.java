package csvtool.enums;

public enum ExitCode
{
    SUCCESS(0),
    INVALID_SYNTAX(1);

    private final int code;

    ExitCode(int code)
    {
        this.code = code;
    }

    public int get() { return this.code; }
}
