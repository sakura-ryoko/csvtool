package csvtool.utils;

import java.io.File;

public class FileUtils
{
    public static boolean fileExists(String path)
    {
        try
        {
            File file = new File(path);
            return file.exists();
        }
        catch (Exception err)
        {
            System.out.printf("ERR: Exception checking if file [\"%s\"] exists. [%s]", path, err.getMessage());
        }

        return false;
    }
}
