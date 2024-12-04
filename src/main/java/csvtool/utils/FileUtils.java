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
            LOGGER.error("Exception checking if file [\"{}\"] exists. [{}]", path, err.getMessage());
        }

        return false;
    }
}
