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

    public HashMap<Integer, List<String>> file;
    public CSVHeader header;
    public String fileName;

    public FileCache()
    {
        this.file = new HashMap<>();
        this.header = new CSVHeader();
        this.fileName = "";
    }

    public FileCache(@Nonnull CSVHeader newHeader)
    {
        this.file = new HashMap<>();
        this.setHeader(newHeader);
        this.fileName = "";
    }

    public FileCache(@Nonnull CSVHeader newHeader, String fileName)
    {
        this.file = new HashMap<>();
        this.setHeader(newHeader);
        this.fileName = fileName;
    }

    public void copyFile(CSVWrapper wrapper)
    {
        if (!this.file.isEmpty())
        {
            this.file.clear();
        }

        LOGGER.debug("copyFile(): Caching file [{} lines] ...", wrapper.getSize());
        this.copyHeader(wrapper);
        this.setFileName(wrapper.getFile());
        this.file.putAll(wrapper.getAllLines());
    }

    public void copyFileHeadersOnly(CSVWrapper wrapper)
    {
        if (!this.file.isEmpty())
        {
            this.file.clear();
        }

        LOGGER.debug("copyFileHeadersOnly(): Copying file Headers ...");
        this.copyHeader(wrapper);
        this.setFileName(wrapper.getFile());
    }

    public void copyHeader(CSVWrapper wrapper)
    {
        LOGGER.debug("copyHeader(): Caching Header ...", wrapper.getSize());
        this.header = wrapper.getHeader();
    }

    public HashMap<Integer, List<String>> getFile()
    {
        return this.file;
    }

    public String getFileName()
    {
        return this.fileName;
    }

    public void setHeader(CSVHeader header)
    {
        LOGGER.debug("setHeader(): Setting Header");
        this.header = header;

        // Add header to FILE
        if (this.file.isEmpty())
        {
            this.file.put(0, header.stream().toList());
        }
    }

    public void appendHeader(String header)
    {
        LOGGER.debug("appendHeader(): Appending Header");
        this.header.add(header);

        // Replace headers to FILE
        List<String> newHeaders = this.header.stream().toList();
        this.file.put(0, newHeaders);
    }

    public FileCache setFileName(String name)
    {
        this.fileName = name;
        return this;
    }

    public boolean hasLine(int line)
    {
        return this.file.containsKey(line);
    }

    public List<String> getLine(int line)
    {
        if (line > this.file.size())
        {
            return List.of();
        }

        return this.file.get(line);
    }

    public void addLine(List<String> line)
    {
        List<String> out = new ArrayList<>();

        for (int i = 0; i < this.header.size(); i++)
        {
//            if (line.size() >= i)
//            {
                out.add(line.get(i));
//            }
        }

        LOGGER.debug("addLine({}): out [{}]", this.file.size(), out.toString());
        this.file.put(this.file.size(), out);
    }

    public void setLine(int line, List<String> data)
    {
        List<String> out = new ArrayList<>();

        for (int i = 0; i < this.header.size(); i++)
        {
//            if (data.size() >= i)
//            {
                out.add(data.get(i));
//            }
        }

//        LOGGER.debug("setLine({}): out [{}]", this.file.size(), out.toString());
        this.file.put(line, out);
    }

    public CSVHeader getHeader()
    {
        return this.header;
    }

    public boolean isEmpty()
    {
        return this.file.isEmpty() || this.file.size() == 1;
    }

    public void clear()
    {
        if (this.file != null && !this.file.isEmpty())
        {
            this.file.clear();
        }

        if (this.header != null && !this.header.isEmpty())
        {
            this.header.clear();
        }
    }

    @Override
    public void close() throws Exception
    {
        if (this.file != null)
        {
            this.file.clear();
        }
        if (this.header != null)
        {
            this.header.close();
        }
    }
}
