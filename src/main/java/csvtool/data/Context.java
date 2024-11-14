package csvtool.data;

import csvtool.enums.Operation;
import csvtool.enums.Settings;

import javax.annotation.Nullable;
import java.util.*;

public class Context
{
    private final Operation op;
    private Map<Settings, String> settings;
    private String inputFile;
    private OptSettings opt;

    public Context(Operation op)
    {
        this(op, null, "");
    }

    public Context(Operation op, Map<Settings, String> settings, String inputFile)
    {
        this.op = op;
        this.settings = settings;
        this.inputFile = inputFile;
        this.opt = new OptSettings();
    }

    public Operation getOp() { return this.op; }

    public Map<Settings, String> getSettings() { return this.settings; }

    public @Nullable String getSettingValue(Settings setting)
    {
        if (this.settings.containsKey(setting))
        {
            return this.settings.get(setting);
        }

        return null;
    }

    public String getInputFile() { return this.inputFile; }

    public Context addSettings(Settings settings, @Nullable String param)
    {
        if (this.settings == null)
        {
            this.settings = new HashMap<>();
        }

        this.settings.put(settings, Objects.requireNonNullElse(param, ""));
        return this;
    }

    public Context setInputFile(String file)
    {
        this.inputFile = file;
        return this;
    }

    public OptSettings getOpt()
    {
        return this.opt;
    }

    public Context setOptSettings(OptSettings opt)
    {
        this.opt = opt;
        return this;
    }
}
