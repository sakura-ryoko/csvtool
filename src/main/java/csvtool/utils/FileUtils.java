package csvtool.utils;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils
{
    private static final LogWrapper LOGGER = new LogWrapper(FileUtils.class);
    private static final String REGEX_SANITIZE = "[\\\\/:*?\"<>|]|\\p{C}|\\p{M}";

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

    public static boolean checkIfDirectoryExists(Path dir)
    {
        try
        {
            return Files.exists(dir) && Files.isDirectory(dir) && Files.isReadable(dir);
        }
        catch (Exception err)
        {
            LOGGER.error("checkIfDirectoryExists(): Exception checking for directory '{}'; {}", dir.toAbsolutePath().toString(), err.getLocalizedMessage());
        }

        return false;
    }

    public static String sanitizeFileName(String fileIn)
    {
        return fileIn.replaceAll(REGEX_SANITIZE, "");
    }
}
