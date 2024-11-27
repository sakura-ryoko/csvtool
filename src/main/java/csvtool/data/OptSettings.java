package csvtool.data;

import csvtool.utils.FileUtils;
import csvtool.utils.LogUtils;

public class OptSettings
{
    private final LogUtils LOGGER = new LogUtils(OptSettings.class);
    private String input2;
    private String output;
    private String headersConfig;
    private String key;
    private boolean utf8;

    public OptSettings()
    {
        this.input2 = null;
        this.output = null;
        this.headersConfig = null;
        this.key = "";
        this.utf8 = false;
    }

    public String getInput2()
    {
        return this.input2;
    }

    public String getOutput()
    {
        return this.output;
    }

    public String getHeadersConfig()
    {
        return this.headersConfig;
    }

    public String getKey()
    {
        return this.key;
    }

    public boolean isUtf8()
    {
        return this.utf8;
    }

    public void setInput2(String input2)
    {
        if (FileUtils.fileExists(input2))
        {
            this.input2 = input2;
            LOGGER.info("Input2 File [{}] exists.", input2);
        }
        else
        {
            LOGGER.error("ERROR: Input2 file [{}] does not exist!", input2);
        }
    }

    public void setOutput(String output)
    {
        if (FileUtils.fileExists(output))
        {
            this.output = output;
            LOGGER.info("Output File [{}] exists.", output);
        }
        else
        {
            LOGGER.error("ERROR: Output file [{}] does not exist!", output);
        }
    }

    public void setHeadersConfig(String headersConfig)
    {
        if (FileUtils.fileExists(headersConfig))
        {
            this.headersConfig = headersConfig;
            LOGGER.info("Headers Config File [{}] exists.", headersConfig);
        }
        else
        {
            LOGGER.warn("ERROR: Headers Config file [{}] does not exist!", headersConfig);
        }
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public void setUtf8(boolean toggle)
    {
        this.utf8 = toggle;
    }
}
