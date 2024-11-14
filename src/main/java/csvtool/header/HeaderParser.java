package csvtool.header;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import csvtool.data.OptSettings;
import csvtool.utils.LogUtils;

import java.nio.file.Files;
import java.nio.file.Path;

public class HeaderParser
{
    private static final LogUtils LOGGER = new LogUtils(HeaderParser.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static HeaderConfig CONFIG = new HeaderConfig();
    private static boolean loaded;

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

    public static boolean isLoaded() { return loaded; }

    public static HeaderConfig getConfig() { return CONFIG; }
}
