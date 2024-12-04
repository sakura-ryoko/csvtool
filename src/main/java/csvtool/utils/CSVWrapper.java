package csvtool.utils;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import csvtool.header.CSVHeader;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CSVWrapper implements AutoCloseable
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());
    private CSVParser parser;
    private CSVReader reader;

    private final String file;
    private final HashMap<Integer, String> header;
    private HashMap<Integer, List<String>> lines;

    public CSVWrapper(String file)
    {
        this.file = file;
        this.parser = null;
        this.reader = null;
        this.header = new HashMap<>();
        this.lines = new HashMap<>();

        if (!verifyFile())
        {
            LOGGER.warn("File: [{}] does not exist!");
        }
    }

    private boolean verifyFile()
    {
        File file = new File(this.file);

        return file.exists();
    }

    private CSVParser getParser()
    {
        if (this.parser == null)
        {
            this.LOGGER.debug("Building Parser ...");
            this.parser = new CSVParserBuilder()
                    .withSeparator(',')
                    .withIgnoreQuotations(true)
                    .build();
        }

        return this.parser;
    }

    private @Nullable CSVReader getReader()
    {
        try
        {
            this.LOGGER.debug("Building Reader ...");
            this.reader = new CSVReaderBuilder(new FileReader(this.file))
                    .withCSVParser(this.getParser())
                    .build();
        }
        catch (Exception e)
        {
            this.LOGGER.error("Exception reading input file [{}], error: [{}]", this.file, e.getMessage());
            return null;
        }

        return this.reader;
    }

    public boolean read()
    {
        if (this.reader != null)
        {
            try
            {
                this.reader.close();
            }
            catch (Exception ignored) { }
        }

        if (this.getReader() == null)
        {
            return false;
        }

        this.lines = new HashMap<>();
        AtomicInteger line = new AtomicInteger();

        this.LOGGER.debug("Reading file ...");

        this.reader.forEach((str) ->
        {
            // Read header
            if (line.get() == 0)
            {
                LOGGER.debug("Reading headers... ");

                for (int i = 0; i < str.length; i++)
                {
                    this.header.put(i, str[i]);
                }
            }

            this.lines.put((line.getAndIncrement()), new ArrayList<>(Arrays.asList(str)));
        });

        LOGGER.debug("Lines read [{}]", this.getSize());
        return true;
    }

    @Override
    public void close() throws Exception
    {
        if (this.reader != null)
        {
            this.reader.close();
        }
        this.parser = null;
        this.lines.clear();
        this.header.clear();
    }

    public String getFile()
    {
        return this.file;
    }

    public @Nullable CSVHeader getHeader()
    {
        if (this.header.isEmpty())
        {
            return null;
        }

        CSVHeader csvHeader = new CSVHeader();
        List<String> list = new ArrayList<>();

        this.header.forEach((h, s) -> list.add(s));
        csvHeader.setHeaders(list);

        return csvHeader;
    }

    public HashMap<Integer, List<String>> getAllLines()
    {
        return this.lines;
    }

    public int getSize()
    {
        return this.lines.size();
    }

    public boolean isEmpty()
    {
        return this.getSize() < 1 || this.header.isEmpty();
    }

    public @Nullable List<String> getLine(int l)
    {
        if (this.lines.isEmpty())
        {
            this.LOGGER.error("getLine() - File not read!");
            return null;
        }
        else if (this.lines.size() < l)
        {
            this.LOGGER.error("getLine() - line [{}] does not exist!", l);
            return null;
        }

        if (!this.lines.containsKey(l))
        {
            this.LOGGER.error("getLine() - line [{}] Not found!", l);
            return null;
        }

        List<String> line = this.lines.get(l);

        if (line == null)
        {
            this.LOGGER.error("getLine() - line [{}] is empty!", l);
            return null;
        }

        return line;
    }
}
