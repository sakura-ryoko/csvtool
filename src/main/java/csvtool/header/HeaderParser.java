package csvtool.header;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import csvtool.data.OptSettings;
import csvtool.utils.LogUtils;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;

public class HeaderParser
{
    private static final LogUtils LOGGER = new LogUtils(HeaderParser.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static HeaderConfig CONFIG = new HeaderConfig();
    private static boolean loaded;

    public static @Nullable CSVHeader getInputHeader()
    {
        if (CONFIG != null)
        {
            return CONFIG.input;
        }

        return null;
    }

    public static @Nullable CSVHeader getOutputHeader()
    {
        if (CONFIG != null)
        {
            return CONFIG.output;
        }

        return null;
    }

    public void setInputHeader(CSVHeader input)
    {
        CONFIG.input = input;
    }

    public void setOutputHeader(CSVHeader output)
    {
        CONFIG.output = output;
    }

    public static void loadConfig(OptSettings opt)
    {
        try
        {
            Path path = Path.of(opt.getHeadersConfig());
            var data = JsonParser.parseString(Files.readString(path));
            CONFIG = GSON.fromJson(data, HeaderConfig.class);
            loaded = true;
        }
        catch (Exception e)
        {
            LOGGER.error("Error loading Headers Config: {}", e.getMessage());
        }
    }

    public static boolean saveConfig(OptSettings opt, String newFile)
    {
        try
        {
            Path path = Path.of(opt.getHeadersConfig());
            Path parent = path.getParent();

            if (parent == null)
            {
                parent = path;
            }

            if (!Files.isDirectory(parent))
            {
                Files.createDirectory(parent);
            }

            var configFile = newFile.contains(".json") ? parent.resolve(newFile) : parent.resolve(newFile + ".json");

            if (Files.exists(configFile))
            {
                Files.delete(configFile);
            }

            if (CONFIG != null)
            {
                Path output = Files.writeString(configFile, GSON.toJson(CONFIG));
                //opt.setHeadersConfig(output.toString());
                LOGGER.info("Saved Headers config to file [{}] -> resolved as [{}]", newFile, output.toString());
                return true;
            }
            else
            {
                LOGGER.error("Error saving Headers file [{}] -- CONFIG is empty!", newFile);
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Error saving Headers Config: {}", e.getMessage());
        }

        return false;
    }

    public static boolean isLoaded() { return loaded; }

    public static HeaderConfig getConfig() { return CONFIG; }
}
