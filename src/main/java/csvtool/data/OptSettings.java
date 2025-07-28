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
    private String key3;
    private String key4;
    private String key5;
    private String joinKey;
    private String joinKey2;
    private String joinKey3;
    private String joinKey4;
    private String joinKey5;
    private String serialKey;
    private String serialStart;
    private String serialEnd;
    private String side;
    private boolean deDupe;
    private boolean squashDupe;
    private boolean applyQuotes;
    private boolean appendOutput;
    private boolean outer;
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
        this.key3 = "";
        this.key4 = "";
        this.key5 = "";
        this.joinKey = "";
        this.joinKey2 = "";
        this.joinKey3 = "";
        this.joinKey4 = "";
        this.joinKey5 = "";
        this.serialKey = "";
        this.serialStart = "";
        this.serialEnd = "";
        this.side = "";
        this.deDupe = false;
        this.squashDupe = false;
        this.applyQuotes = false;
        this.appendOutput = false;
        this.outer = false;
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

    public boolean hasKey3()
    {
        return this.key3 != null && !this.key3.isEmpty();
    }

    public boolean hasKey4()
    {
        return this.key4 != null && !this.key4.isEmpty();
    }

    public boolean hasKey5()
    {
        return this.key5 != null && !this.key5.isEmpty();
    }

    public boolean hasJoinKey()
    {
        return this.joinKey != null && !this.joinKey.isEmpty();
    }

    public boolean hasJoinKey2()
    {
        return this.joinKey2 != null && !this.joinKey2.isEmpty();
    }

    public boolean hasJoinKey3()
    {
        return this.joinKey3 != null && !this.joinKey3.isEmpty();
    }

    public boolean hasJoinKey4()
    {
        return this.joinKey4 != null && !this.joinKey4.isEmpty();
    }

    public boolean hasJoinKey5()
    {
        return this.joinKey5 != null && !this.joinKey5.isEmpty();
    }

    public boolean hasSerialKey()
    {
        return this.serialKey != null && !this.serialKey.isEmpty();
    }

    public boolean hasSerialStart()
    {
        return this.serialStart != null && !this.serialStart.isEmpty();
    }

    public boolean hasSerialEnd()
    {
        return this.serialEnd != null && !this.serialEnd.isEmpty();
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

    public @Nullable String getKey3()
    {
        return this.key3;
    }

    public @Nullable String getKey4()
    {
        return this.key4;
    }

    public @Nullable String getKey5()
    {
        return this.key5;
    }

    public @Nullable String getJoinKey()
    {
        return this.joinKey;
    }

    public @Nullable String getJoinKey2()
    {
        return this.joinKey2;
    }

    public @Nullable String getJoinKey3()
    {
        return this.joinKey3;
    }

    public @Nullable String getJoinKey4()
    {
        return this.joinKey4;
    }

    public @Nullable String getJoinKey5()
    {
        return this.joinKey5;
    }

    public @Nullable String getSerialKey()
    {
        return this.serialKey;
    }

    public @Nullable String getSerialStart()
    {
        return this.serialStart;
    }

    public @Nullable String getSerialEnd()
    {
        return this.serialEnd;
    }

    public @Nullable String getSide()
    {
        return this.side;
    }

    public boolean isDeDupe()
    {
        return this.deDupe;
    }

    public boolean isSquashDupe()
    {
        return this.squashDupe;
    }

    public boolean isApplyQuotes()
    {
        return this.applyQuotes;
    }

    public boolean isAppendOutput()
    {
        return this.appendOutput;
    }

    public boolean isOuterJoin()
    {
        return this.outer;
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

    public void setKey3(String key3)
    {
        this.key3 = key3;
    }

    public void setKey4(String key4)
    {
        this.key4 = key4;
    }

    public void setKey5(String key5)
    {
        this.key5 = key5;
    }

    public void setJoinKey(String key)
    {
        this.joinKey = key;
    }

    public void setJoinKey2(String key2)
    {
        this.joinKey2 = key2;
    }

    public void setJoinKey3(String key3)
    {
        this.joinKey3 = key3;
    }

    public void setJoinKey4(String key4)
    {
        this.joinKey4 = key4;
    }

    public void setJoinKey5(String key5)
    {
        this.joinKey5 = key5;
    }

    public void setSerialKey(String key)
    {
        this.serialKey = key;
    }

    public void setSerialStart(String start)
    {
        this.serialStart = start;
    }

    public void setSerialEnd(String end)
    {
        this.serialEnd = end;
    }

    public void setSide(String side)
    {
        this.side = side;
    }

    public void setDeDupe(boolean toggle)
    {
        this.deDupe = toggle;
    }

    public void setSquashDupe(boolean toggle)
    {
        this.squashDupe = toggle;
    }

    public void setApplyQuotes(boolean toggle)
    {
        this.applyQuotes = toggle;
    }

    public void setAppendOutput(boolean toggle)
    {
        this.appendOutput = toggle;
    }

    public void setOuterJoin(boolean toggle)
    {
        this.outer = toggle;
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
