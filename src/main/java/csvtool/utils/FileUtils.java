package csvtool.utils;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils
{
    private static final LogWrapper LOGGER = new LogWrapper(FileUtils.class);

    public static boolean fileExists(String path)
    {
        try
        {
            return Files.exists(Path.of(path));
        }
        catch (Exception err)
        {
            LOGGER.error("fileExists(): Exception checking if file [\"{}\"] exists. [{}]", path, err.getMessage());
        }

        return false;
    }

    public static boolean deleteIfExists(String path)
    {
        try
        {
            Path file = Path.of(path);

            if (Files.exists(file))
            {
                LOGGER.debug("deleteIfExists(): Deleting file [{}] ...", path);
                Files.delete(file);
            }

            return true;
        }
        catch (Exception err)
        {
            LOGGER.error("deleteIfExists(): Exception deleting file [\"{}\"]. [{}]", path, err.getMessage());
        }

        return false;
    }
}
