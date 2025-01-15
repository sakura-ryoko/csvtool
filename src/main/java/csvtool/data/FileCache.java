package csvtool.data;

import csvtool.header.CSVHeader;
import csvtool.utils.CSVWrapper;
import csvtool.utils.LogWrapper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileCache implements AutoCloseable
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());

    public HashMap<Integer, List<String>> FILE;
    public CSVHeader HEADER;
    public String fileName;

    public FileCache()
    {
        this.FILE = new HashMap<>();
        this.HEADER = new CSVHeader();
        this.fileName = "";
    }

    public FileCache(@Nonnull CSVHeader newHeader)
    {
        this.FILE = new HashMap<>();
        this.setHeader(newHeader);
        this.fileName = "";
    }

    public FileCache(@Nonnull CSVHeader newHeader, String fileName)
    {
        this.FILE = new HashMap<>();
        this.setHeader(newHeader);
        this.fileName = fileName;
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

    // TODO simplify file name handling
    public String getFileName()
    {
        return this.fileName;
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

    // TODO simplify file name handling
    public FileCache setFileName(String name)
    {
        this.fileName = name;
        return this;
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
        if (this.FILE != null && !this.FILE.isEmpty())
        {
            this.FILE.clear();
        }

        if (this.HEADER != null && this.HEADER.size() > 0)
        {
            this.HEADER.clear();
        }
    }

    @Override
    public void close() throws Exception
    {
        if (this.FILE != null)
        {
            this.FILE.clear();
        }
        if (this.HEADER != null)
        {
            this.HEADER.close();
        }
    }
}
