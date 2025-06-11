package csvtool.pivot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import csvtool.data.Context;
import csvtool.enums.Settings;
import csvtool.header.CSVHeader;
import csvtool.utils.FileUtils;
import csvtool.utils.LogWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FilePivotParser implements AutoCloseable
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private FilePivotConfig CONFIG;
    private String configFile;
    private boolean loaded;

    public FilePivotParser()
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
            LOGGER.error("init():  No headers file provided!");
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

    public FilePivotConfig newConfig()
    {
        return new FilePivotConfig();
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

    public @Nullable List<FilePivotDirectoryBuilder.Entry> getDirectoryEntries()
    {
        if (this.CONFIG != null)
        {
            return this.CONFIG.directoryEntryList;
        }

        return null;
    }

    public @Nullable FilePivotTransform getFileTransformFrom()
    {
        if (this.CONFIG != null)
        {
            return this.CONFIG.fileTransformFrom;
        }

        return null;
    }

    public @Nullable FilePivotTransform getFileTransformTo()
    {
        if (this.CONFIG != null)
        {
            return this.CONFIG.fileTransformTo;
        }

        return null;
    }

    public FilePivotParser setHeaderConfigFile(@Nonnull String newConfig)
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

    public void setDirectoryEntries(@Nonnull List<FilePivotDirectoryBuilder.Entry> list)    {
        if (this.CONFIG == null)
        {
            this.CONFIG = this.newConfig();
        }

        this.CONFIG.directoryEntryList = list;
    }

    public void setFileTransformFrom(@Nonnull FilePivotTransform transform)
    {
        if (this.CONFIG == null)
        {
            this.CONFIG = this.newConfig();
        }

        this.CONFIG.fileTransformFrom = transform;
    }

    public void setFileTransformTo(@Nonnull FilePivotTransform transform)
    {
        if (this.CONFIG == null)
        {
            this.CONFIG = this.newConfig();
        }

        this.CONFIG.fileTransformTo = transform;
    }

    public boolean loadConfig()
    {
        try
        {
            Path path = Path.of(this.configFile);
            var data = JsonParser.parseString(Files.readString(path));
            this.CONFIG = GSON.fromJson(data, FilePivotConfig.class);
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

    public FilePivotConfig getConfig() { return this.CONFIG; }

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
            if (this.CONFIG.input != null)
            {
                this.CONFIG.input.clear();
            }

            if (this.CONFIG.fileTransformFrom != null)
            {
                this.CONFIG.fileTransformFrom = null;
            }

            if (this.CONFIG.fileTransformTo != null)
            {
                this.CONFIG.fileTransformTo = null;
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
