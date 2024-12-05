package csvtool.utils;

import java.io.File;

public class FileUtils
{
    private static final LogWrapper LOGGER = new LogWrapper(FileUtils.class);

    public static boolean fileExists(String path)
    {
        try
        {
            File file = new File(path);
            return file.exists();
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
            File file = new File(path);

            if (file.exists())
            {
                LOGGER.debug("deleteIfExists(): Deleting file [{}] ...", path);
                return file.delete();
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
