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
import java.util.Objects;

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

    public int getRemapListSize()
    {
        if (this.CONFIG != null && this.CONFIG.remapList != null)
        {
            return this.CONFIG.remapList.size();
        }

        return -1;
    }

    public @Nullable CSVRemap getRemapListEntry(int entry)
    {
        if (this.CONFIG.remapList.size() < entry)
        {
            return null;
        }

        return this.CONFIG.remapList.getRemap(entry);
    }

    public void setRemapListEntry(int entry, @Nonnull CSVRemap newRemap)
    {
        if (this.CONFIG.remapList.size() < entry)
        {
            return;
        }

        this.CONFIG.remapList.setRemap(entry, newRemap);
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
            if (i < this.CONFIG.output.size())
            {
                this.CONFIG.remapList = this.CONFIG.remapList.addRemap(new CSVRemap(i, RemapType.NONE));
            }
            else
            {
                this.CONFIG.remapList = this.CONFIG.remapList.addRemap(new CSVRemap(i, RemapType.DROP));
            }
        }
    }

    public boolean checkRemapList()
    {
        if (this.CONFIG.input.isEmpty() || this.CONFIG.output.isEmpty())
        {
            LOGGER.error("checkRemapList(): Error checking Remap List; Input/Output headers are empty!");
            return false;
        }

        if (this.CONFIG.input.size() < this.CONFIG.remapList.size() || this.CONFIG.remapList.isEmpty())
        {
            LOGGER.warn("checkRemapList(): Remap List is invalid, rebuilding with defaults.");
            this.buildRemapList();
        }

        if (this.CONFIG.input.size() > this.CONFIG.output.size())
        {
            int dropCount = 0;

            // Count the DROP's
            for (int i = 0; i < this.CONFIG.remapList.size(); i++)
            {
                CSVRemap entry = this.CONFIG.remapList.getRemap(i);

                if (entry != null && entry.getType() == RemapType.DROP)
                {
                    dropCount++;
                }
            }

            if (this.CONFIG.input.size() - dropCount > this.CONFIG.output.size())
            {
                LOGGER.error("checkRemapList(): Error checking Remap List; DROP count is too low!");
                return false;
            }
            else if (this.CONFIG.output.size() + dropCount > this.CONFIG.input.size())
            {
                LOGGER.error("checkRemapList(): Error checking Remap List; DROP count is too high!");
                return false;
            }
        }

        return true;
    }

    public void dumpRemapList()
    {
        if (this.CONFIG.remapList == null || this.CONFIG.remapList.isEmpty())
        {
            LOGGER.error("dumpRemapList(): Error; List is empty/null!");
            return;
        }

        LOGGER.warn("dumpRemapList(): Dumping Remap List ...");
        for (int i = 0; i < this.getRemapListSize(); i++)
        {
            LOGGER.warn("[{}] remap: [{}]", i, Objects.requireNonNullElse(this.getRemapListEntry(i), "<NULL>").toString());
        }

        LOGGER.warn("dumpRemapList(): EOL");
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
            if (this.CONFIG.input != null)
            {
                this.CONFIG.input.clear();
            }

            if (this.CONFIG.output != null)
            {
                this.CONFIG.output.clear();
            }

            if (this.CONFIG.remapList != null)
            {
                this.CONFIG.remapList.clear();
            }

            if (this.CONFIG.remap_examples != null)
            {
                this.CONFIG.remap_examples.clear();
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
