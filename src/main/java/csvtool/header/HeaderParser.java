package csvtool.header;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import csvtool.data.Context;
import csvtool.enums.Settings;
import csvtool.utils.FileUtils;
import csvtool.utils.LogWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;

public class HeaderParser implements AutoCloseable
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private HeaderConfig CONFIG;
    private String configFile;
    private boolean loaded;

    public HeaderParser()
    {
        this.CONFIG = this.newConfig();
        this.configFile = "";
        this.loaded = false;
    }

    public boolean init(Context ctx)
    {
        if (!ctx.getOpt().hasHeaders())
        {
            LOGGER.error("init():  No headers file provided!");
            return false;
        }

        this.configFile = ctx.getSettingValue(Settings.HEADERS);

        // Read file if it exists
        if (FileUtils.fileExists(this.configFile))
        {
            LOGGER.debug("init(): Loading config [{}] ...", this.configFile);

            if (this.loadConfig())
            {
                if (this.checkRemapList())
                {
                    LOGGER.info("init(): Config Remap List loaded/rebuilt successfully.");
                }

                LOGGER.info("init(): Config loaded successfully.");
            }
            else
            {
                LOGGER.error("init(): Config failed to load.");
                return false;
            }
        }

        // Return true if the file doesn't exist, in case we are saving it.
        return true;
    }

    public HeaderConfig newConfig()
    {
        return new HeaderConfig();
    }

    public String getHeaderConfigFile()
    {
        return this.configFile;
    }

    public @Nullable CSVHeader getInputHeader()
    {
        if (this.CONFIG != null)
        {
            return CONFIG.input;
        }

        return null;
    }

    public @Nullable CSVHeader getOutputHeader()
    {
        if (this.CONFIG != null)
        {
            return this.CONFIG.output;
        }

        return null;
    }

    public @Nullable CSVRemapList getRemapList()
    {
        if (this.CONFIG != null)
        {
            return this.CONFIG.remapList;
        }

        return null;
    }

    public HeaderParser setHeaderConfigFile(@Nonnull String newConfig)
    {
        this.configFile = newConfig;
        return this;
    }

    public void setInputHeader(@Nonnull CSVHeader input, String fileName)
    {
        if (this.CONFIG == null)
        {
            this.CONFIG = this.newConfig();
        }

        this.CONFIG.inputFile = fileName;
        this.CONFIG.input = input;
    }

    public void setOutputHeader(@Nonnull CSVHeader output, String fileName)
    {
        if (this.CONFIG == null)
        {
            this.CONFIG = this.newConfig();
        }

        this.CONFIG.outputFile = fileName;
        this.CONFIG.output = output;
    }

    public void setRemapList(@Nonnull CSVRemapList remaps)
    {
        if (this.CONFIG == null)
        {
            this.CONFIG = this.newConfig();
        }

        this.CONFIG.remapList = remaps;
    }

    public boolean ensureOutputExists()
    {
        if (this.CONFIG.output == null || this.CONFIG.output.isEmpty())
        {
            if (this.CONFIG.input == null || this.CONFIG.input.isEmpty())
            {
                LOGGER.error("ensureOutputExists(): Error Input headers are empty!");
                return false;
            }

            this.CONFIG.outputFile = this.CONFIG.inputFile;
            this.CONFIG.output = new CSVHeader();
            this.CONFIG.output.setHeaders(this.CONFIG.input.stream().toList());
            this.buildRemapList();
        }

        return true;
    }

    public void buildRemapList()
    {
        if (this.CONFIG.input.isEmpty() || this.CONFIG.output.isEmpty())
        {
            LOGGER.error("buildRemapList(): Error building Remap List; Input/Output headers are empty!");
            return;
        }

        LOGGER.debug("buildRemapList(): Creating default (NONE) Remap List.");

        if (this.CONFIG.remapList == null)
        {
            this.CONFIG.remapList = new CSVRemapList();
        }

        this.CONFIG.remapList.clear();

        for (int i = 0; i < this.CONFIG.input.size(); i++)
        {
            this.CONFIG.remapList.addRemap(new CSVRemap(i, RemapType.NONE, null));
        }
    }

    public boolean checkRemapList()
    {
        if (this.CONFIG.input.isEmpty() || this.CONFIG.output.isEmpty())
        {
            LOGGER.error("checkRemapList(): Error checking Remap List; Input/Output headers are empty!");
            return false;
        }

        if (this.CONFIG.input.size() != this.CONFIG.remapList.size() || this.CONFIG.remapList.isEmpty())
        {
            LOGGER.warn("checkRemapList(): Remap List is invalid, rebuilding with defaults.");
            this.buildRemapList();
        }

        return true;
    }

    public boolean loadConfig()
    {
        try
        {
            Path path = Path.of(this.configFile);
            var data = JsonParser.parseString(Files.readString(path));
            this.CONFIG = GSON.fromJson(data, HeaderConfig.class);
            this.loaded = true;
            LOGGER.info("loadConfig(): Loaded Headers config file [{}]", path.toString());
            return true;
        }
        catch (Exception e)
        {
            LOGGER.error("loadConfig(): Error loading Headers Config: {}", e.getMessage());
        }

        return false;
    }

    public boolean isLoaded() { return this.loaded; }

    public HeaderConfig getConfig() { return this.CONFIG; }

    public boolean isEmpty()
    {
        return this.CONFIG == null;
    }

    public boolean saveConfig()
    {
        if (!this.configFile.isEmpty())
        {
            if (this.ensureOutputExists())
            {
                return this.saveConfig(this.configFile);
            }
        }

        return false;
    }

    public boolean saveConfig(String newConfig)
    {
        try
        {
            newConfig = newConfig.contains(".json") ? newConfig : newConfig + ".json";

            FileUtils.deleteIfExists(newConfig);

            if (this.CONFIG != null)
            {
                Path output = Files.writeString(Path.of(newConfig), GSON.toJson(this.CONFIG));
                //opt.setHeadersConfig(output.toString());
                LOGGER.info("saveConfig(): Saved Headers config to file [{}]", output.toString());
                return true;
            }
            else
            {
                LOGGER.error("saveConfig(): Error saving Headers file [{}] -- CONFIG is empty!", newConfig);
            }
        }
        catch (Exception e)
        {
            LOGGER.error("saveConfig(): Error saving Headers Config: {}", e.getMessage());
        }

        return false;
    }

    public void clear()
    {
        if (this.CONFIG != null)
        {
            this.CONFIG = null;
        }
    }

    @Override
    public void close() throws Exception
    {
        if (this.CONFIG != null)
        {
            this.CONFIG.input.close();
            this.CONFIG.output.close();
            this.CONFIG.remapList.close();
        }

        this.clear();
    }
}
