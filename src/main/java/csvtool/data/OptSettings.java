package csvtool.data;

import csvtool.utils.FileUtils;
import csvtool.utils.LogWrapper;

import javax.annotation.Nullable;

public class OptSettings
{
    private final LogWrapper LOGGER = new LogWrapper(OptSettings.class);
    private String input2;
    private String output;
    private String headersConfig;
    private String key;
    private String key2;
    private String side;
    private boolean deDupe;
    private boolean applyQuotes;
    private boolean appendOutput;
    private boolean quiet;
    private boolean debug;
    private boolean ansiColors;

    public OptSettings()
    {
        this.input2 = "";
        this.output = "";
        this.headersConfig = "";
        this.key = "";
        this.key2 = "";
        this.side = "";
        this.deDupe = false;
        this.applyQuotes = false;
        this.appendOutput = false;
        this.quiet = Const.QUIET;
        this.debug = Const.DEBUG;
        this.ansiColors = Const.ANSI_COLOR;
    }

    public boolean hasInput2()
    {
        return this.input2 != null && !this.input2.isEmpty();
    }

    public boolean hasOutput()
    {
        return this.output != null && !this.output.isEmpty();
    }

    public boolean hasHeaders()
    {
        return this.headersConfig != null && !this.headersConfig.isEmpty();
    }

    public boolean hasKey()
    {
        return this.key != null && !this.key.isEmpty();
    }

    public boolean hasKey2()
    {
        return this.key2 != null && !this.key2.isEmpty();
    }

    public boolean hasSide()
    {
        return this.side != null && !this.side.isEmpty();
    }

    public @Nullable String getInput2()
    {
        return this.input2;
    }

    public @Nullable String getOutput()
    {
        return this.output;
    }

    public @Nullable String getHeadersConfig()
    {
        return this.headersConfig;
    }

    public @Nullable String getKey()
    {
        return this.key;
    }

    public @Nullable String getKey2()
    {
        return this.key2;
    }

    public @Nullable String getSide()
    {
        return this.side;
    }

    public boolean isDeDupe()
    {
        return this.deDupe;
    }

    public boolean isApplyQuotes()
    {
        return this.applyQuotes;
    }

    public boolean isAppendOutput()
    {
        return this.appendOutput;
    }

    public boolean isQuiet()
    {
        return this.quiet;
    }

    public boolean isDebug()
    {
        return this.debug;
    }

    public boolean isAnsiColors()
    {
        return this.ansiColors;
    }

    public void setInput2(String input2)
    {
        this.input2 = input2;

        if (FileUtils.fileExists(input2))
        {
            LOGGER.debug("setInput2(): Input2 File [{}] exists.", input2);
        }
        else
        {
            LOGGER.error("setInput2(): ERROR: Input2 file [{}] does not exist!", input2);
        }
    }

    public void setOutput(String output)
    {
        this.output = output;

        if (FileUtils.fileExists(output))
        {
            LOGGER.debug("setOutput(): Output File [{}] exists.", output);
        }
        else
        {
            LOGGER.debug("setOutput(): Output file [{}] does not exist.", output);
        }
    }

    public void setHeadersConfig(String headersConfig)
    {
        this.headersConfig = headersConfig;

        if (FileUtils.fileExists(headersConfig))
        {
            LOGGER.debug("setHeadersConfig(): Headers Config File [{}] exists.", headersConfig);
        }
        else
        {
            LOGGER.debug("setHeadersConfig(): Headers Config file [{}] does not exist.", headersConfig);
        }
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public void setKey2(String key2)
    {
        this.key2 = key2;
    }

    public void setSide(String side)
    {
        this.side = side;
    }

    public void setDeDupe(boolean toggle)
    {
        this.deDupe = toggle;
    }

    public void setApplyQuotes(boolean toggle)
    {
        this.applyQuotes = toggle;
    }

    public void setAppendOutput(boolean toggle)
    {
        this.appendOutput = toggle;
    }

    public void setQuiet(boolean toggle)
    {
        this.quiet = toggle;
    }

    public void setDebug(boolean toggle)
    {
        this.debug = toggle;
    }

    public void setAnsiColors(boolean toggle)
    {
        this.ansiColors = toggle;
    }
}
