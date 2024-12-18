package csvtool.data;

import csvtool.header.CSVHeader;
import csvtool.utils.CSVWrapper;
import csvtool.utils.LogWrapper;

import java.util.HashMap;
import java.util.List;

public class FileCache
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());

    public HashMap<Integer, List<String>> FILE;
    public CSVHeader HEADER;

    public FileCache()
    {
        this.FILE = new HashMap<>();
        this.HEADER = new CSVHeader();
    }

    public void copyFile(CSVWrapper wrapper)
    {
        if (!this.FILE.isEmpty())
        {
            this.FILE.clear();
        }

        LOGGER.debug("copyFile(): Caching file [{} lines] ...", wrapper.getSize());
        this.copyHeader(wrapper);
        this.FILE.putAll(wrapper.getAllLines());
    }

    public void copyFileNoHeader(CSVWrapper wrapper)
    {
        if (!this.FILE.isEmpty())
        {
            this.FILE.clear();
        }

        LOGGER.debug("copyFile(): Caching file [{} lines] ...", wrapper.getSize());
        this.FILE.putAll(wrapper.getAllLines());
    }

    public void copyHeader(CSVWrapper wrapper)
    {
        LOGGER.debug("copyHeader(): Caching Header ...", wrapper.getSize());
        this.HEADER = wrapper.getHeader();
    }

    public HashMap<Integer, List<String>> getFile()
    {
        return this.FILE;
    }

    public CSVHeader getHeader()
    {
        return this.HEADER;
    }

    public boolean isEmpty()
    {
        return this.FILE.isEmpty();
    }

    public void clear()
    {
        LOGGER.debug("clear()");
        this.FILE.clear();
        this.HEADER.clear();
    }
}
