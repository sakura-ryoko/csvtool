package csvtool.data;

import csvtool.enums.Operation;
import csvtool.enums.Settings;

import javax.annotation.Nullable;
import java.util.*;

public class Context
{
    private final Operation op;
    private Map<Settings, String> settings;
    private String inputFile1;
    private String inputFile2;
    private String outputFile;
    private String key;
    private String headers;

    public Context(Operation op)
    {
        this(op, null, "", "", "", "", "");
    }

    public Context(Operation op, Map<Settings, String> settings, String inputFile1, String inputFile2, String outputFile, String key, String headers)
    {
        this.op = op;
        this.settings = settings;
        this.inputFile1 = inputFile1;
        this.inputFile2 = inputFile2;
        this.outputFile = outputFile;
        this.key = key;
        this.headers = headers;
    }

    public Operation getOp() { return this.op; }

    public Map<Settings, String> getSettings() { return this.settings; }

    public String getInputFile1() { return this.inputFile1; }

    public String getInputFile2() { return this.inputFile2; }

    public String getOutputFile() { return this.outputFile; }

    public String getKey() { return this.key; }

    public String getHeaders() { return this.headers; }

    public Context addSettings(Settings settings, @Nullable String param)
    {
        if (this.settings == null)
        {
            this.settings = new HashMap<>();
        }

        this.settings.put(settings, Objects.requireNonNullElse(param, ""));
        return this;
    }

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

    public Context setKey(String key)
    {
        this.key = key;
        return this;
    }

    public Context setHeaders(String headers)
    {
        this.headers = headers;
        return this;
    }
}
