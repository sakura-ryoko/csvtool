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
    //private boolean utf8;
    private boolean applyQuotes;
    private boolean appendOutput;

    public OptSettings()
    {
        this.input2 = "";
        this.output = "";
        this.headersConfig = "";
        this.key = "";
        //this.utf8 = false;
        this.applyQuotes = false;
        this.appendOutput = false;
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

    /*
    public boolean isUtf8()
    {
        return this.utf8;
    }
     */

    public boolean isApplyQuotes()
    {
        return this.applyQuotes;
    }

    public boolean isAppendOutput()
    {
        return this.appendOutput;
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

    /*
    public void setUtf8(boolean toggle)
    {
        this.utf8 = toggle;
    }
     */

    public void setApplyQuotes(boolean toggle)
    {
        this.applyQuotes = toggle;
    }

    public void setAppendOutput(boolean toggle)
    {
        this.appendOutput = toggle;
    }
}
