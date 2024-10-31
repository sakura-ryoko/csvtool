package csvtool.data;

import csvtool.enums.Operation;

public class Context
{
    private final Operation op;
    private String inputFile1;
    private String inputFile2;
    private String outputFile;
    private String headers;

    public Context(Operation op)
    {
        this(op, "", "", "", "");
    }

    public Context(Operation op, String inputFile1, String inputFile2, String outputFile, String headers)
    {
        this.op = op;
        this.inputFile1 = inputFile1;
        this.inputFile2 = inputFile2;
        this.outputFile = outputFile;
        this.headers = headers;
    }

    public Operation getOp() { return this.op; }

    public String getInputFile1() { return this.inputFile1; }

    public String getInputFile2() { return this.inputFile2; }

    public String getOutputFile() { return this.outputFile; }

    public String getHeaders() { return this.headers; }

    public Context setInputFile1(String file)
    {
        this.inputFile1 = file;
        return this;
    }

    public Context setInputFile2(String file)
    {
        this.inputFile2 = file;
        return this;
    }

    public Context setOutputFile(String file)
    {
        this.outputFile = file;
        return this;
    }

    public Context setHeaders(String headers)
    {
        this.headers = headers;
        return this;
    }
}
