package csvtool.transform;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import csvtool.data.Context;
import csvtool.enums.Settings;
import csvtool.header.*;
import csvtool.utils.FileUtils;
import csvtool.utils.LogWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class HeaderTransformParser implements AutoCloseable
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private HeaderTransformConfig CONFIG;
    private String configFile;
    private boolean loaded;

    public HeaderTransformParser()
    {
        this.CONFIG = this.newConfig();
        this.configFile = "";
        this.loaded = false;
    }

    public boolean init(Context ctx, boolean delete)
    {
        if (ctx.getOpt().isQuiet())
        {
            LOGGER.toggleQuiet(true);
        }

        if (ctx.getOpt().isDebug())
        {
            LOGGER.toggleDebug(true);
        }

        if (ctx.getOpt().isAnsiColors())
        {
            LOGGER.toggleAnsiColor(true);
        }

        if (!ctx.getOpt().hasHeaders())
        {
            LOGGER.error("init():  No transform headers file provided!");
            return false;
        }

        this.configFile = ctx.getSettingValue(Settings.HEADERS);

        if (delete)
        {
            LOGGER.debug("init(): Deleting existing config [{}] ...", this.configFile);
            FileUtils.deleteIfExists(this.configFile);
        }

        return true;
    }

    public HeaderTransformConfig newConfig()
    {
        return new HeaderTransformConfig();
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

    public @Nullable HeaderTransformList getTransformList()
    {
        if (this.CONFIG != null)
        {
            return this.CONFIG.transformList;
        }

        return null;
    }

    public int getTransformListSize()
    {
        if (this.CONFIG != null && this.CONFIG.transformList != null)
        {
            return this.CONFIG.transformList.size();
        }

        return -1;
    }

    public @Nullable HeaderTransformList.Entry getTransformListEntry(int entry)
    {
        if (this.CONFIG.transformList.size() < entry)
        {
            return null;
        }

        return this.CONFIG.transformList.getEntry(entry);
    }

    public void setRemapListEntry(int entry, @Nonnull HeaderTransformList.Entry newEntry)
    {
        if (this.CONFIG.transformList.size() < entry)
        {
            return;
        }

        this.CONFIG.transformList.setEntry(entry, newEntry);
    }

    public HeaderTransformParser setHeaderConfigFile(@Nonnull String newConfig)
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

    public void setTransformList(@Nonnull HeaderTransformList transformList)
    {
        if (this.CONFIG == null)
        {
            this.CONFIG = this.newConfig();
        }

        this.CONFIG.transformList = transformList;
    }

    public void buildTransformList()
    {
        if (this.CONFIG.input.isEmpty())
        {
            LOGGER.error("buildTransformList(): Error building Remap List; Input/Output headers are empty!");
            return;
        }

        LOGGER.debug("buildTransformList(): Creating default (NONE) List.");

        if (this.CONFIG.transformList == null)
        {
            this.CONFIG.transformList = new HeaderTransformList();
        }

        this.CONFIG.transformList.clear();

        for (int i = 0; i < this.CONFIG.input.size(); i++)
        {
            this.CONFIG.transformList = this.CONFIG.transformList.addEntry(new HeaderTransformList.Entry(i, "{u}", List.of()));
        }
    }

    public boolean checkTransformList()
    {
        if (this.CONFIG.input.isEmpty())
        {
            LOGGER.error("checkTransformList(): Error checking Transform List; Input/Output headers are empty!");
            return false;
        }

        if (this.CONFIG.input.size() < this.CONFIG.transformList.size() || this.CONFIG.transformList.isEmpty())
        {
            LOGGER.warn("checkTransformList(): Transform List is invalid, rebuilding with defaults.");
            this.buildTransformList();
        }

        return true;
    }

    public void dumpTransformList()
    {
        if (this.CONFIG.transformList == null || this.CONFIG.transformList.isEmpty())
        {
            LOGGER.error("dumpTransformList(): Error; List is empty/null!");
            return;
        }

        LOGGER.warn("dumpTransformList(): Dumping Remap List ...");
        for (int i = 0; i < this.getTransformListSize(); i++)
        {
            LOGGER.warn("[{}] transform: [{}]", i, Objects.requireNonNullElse(this.getTransformListEntry(i), "<NULL>").toString());
        }

        LOGGER.warn("dumpTransformList(): EOL");
    }

    public boolean loadConfig()
    {
        try
        {
            Path path = Path.of(this.configFile);
            var data = JsonParser.parseString(Files.readString(path));
            this.CONFIG = GSON.fromJson(data, HeaderTransformConfig.class);
            this.loaded = true;
            LOGGER.info("loadConfig(): Loaded Transform Headers config file [{}]", path.toString());
            return true;
        }
        catch (Exception e)
        {
            LOGGER.error("loadConfig(): Error loading Transform Headers Config: {}", e.getMessage());
        }

        return false;
    }

    public boolean isLoaded() { return this.loaded; }

    public HeaderTransformConfig getConfig() { return this.CONFIG; }

    public boolean isEmpty()
    {
        return this.CONFIG == null;
    }

    public boolean saveConfig()
    {
        if (!this.configFile.isEmpty())
        {
            return this.saveConfig(this.configFile);
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
                LOGGER.info("saveConfig(): Saved Transform Headers config to file [{}]", output.toString());
                return true;
            }
            else
            {
                LOGGER.error("saveConfig(): Error saving Transform Headers file [{}] -- CONFIG is empty!", newConfig);
            }
        }
        catch (Exception e)
        {
            LOGGER.error("saveConfig(): Error saving Transform Headers Config: {}", e.getMessage());
        }

        return false;
    }

    public void clear()
    {
        if (this.CONFIG != null)
        {
            if (this.CONFIG.input != null)
            {
                this.CONFIG.input.clear();
            }

            if (this.CONFIG.transformList != null)
            {
                this.CONFIG.transformList.clear();
            }

            this.CONFIG = null;
        }
    }

    @Override
    public void close() throws Exception
    {
        this.clear();
    }
}
