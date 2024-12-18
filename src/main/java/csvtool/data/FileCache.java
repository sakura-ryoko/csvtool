package csvtool.data;

import csvtool.header.CSVHeader;
import csvtool.utils.CSVWrapper;
import csvtool.utils.LogWrapper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
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

    public FileCache(@Nonnull CSVHeader newHeader)
    {
        this.FILE = new HashMap<>();
        this.setHeader(newHeader);
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

    public void copyHeader(CSVWrapper wrapper)
    {
        LOGGER.debug("copyHeader(): Caching Header ...", wrapper.getSize());
        this.HEADER = wrapper.getHeader();
    }

    public HashMap<Integer, List<String>> getFile()
    {
        return this.FILE;
    }

    public void setHeader(CSVHeader header)
    {
        LOGGER.debug("setHeader(): Setting Header");
        this.HEADER = header;

        // Add header to FILE
        if (this.FILE.isEmpty())
        {
            this.FILE.put(0, header.stream().toList());
        }
    }

    public void addLine(List<String> line)
    {
        List<String> out = new ArrayList<>();

        for (int i = 0; i < this.HEADER.size(); i++)
        {
            if (line.size() >= i)
            {
                out.add(line.get(i));
            }
        }

        LOGGER.debug("addLine({}): out [{}]", this.FILE.size(), out.toString());
        this.FILE.put(this.FILE.size(), out);
    }

    public CSVHeader getHeader()
    {
        return this.HEADER;
    }

    public boolean isEmpty()
    {
        return this.FILE.isEmpty() || this.FILE.size() == 1;
    }

    public void clear()
    {
        LOGGER.debug("clear()");
        if (this.FILE != null && !this.FILE.isEmpty())
        {
            this.FILE.clear();
        }

        if (this.HEADER != null && this.HEADER.size() > 0)
        {
            this.HEADER.clear();
        }
    }
}
