package csvtool.operation;

import csvtool.data.Context;
import csvtool.data.FileCache;
import csvtool.enums.Operations;
import csvtool.header.CSVHeader;
import csvtool.utils.CSVWrapper;
import csvtool.utils.LogWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class Operation
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());

    public Operation(Operations op)
    {
        LOGGER.debug("new abstract Operation for op [{}]", op.getName());
    }

    public boolean runOperation(Context ctx)
    {
        LOGGER.debug("runOperation() [SUPER]");
        return false;
    }

    public void displayHelp() { }

    protected @Nullable FileCache readFile(String file)
    {
        return this.readFile(file, false);
    }

    protected @Nullable FileCache readFile(String file, boolean dump)
    {
        LOGGER.debug("readFile(): Reading file [{}] ...", file);

        try (CSVWrapper wrapper = new CSVWrapper(file))
        {
            if (wrapper.read())
            {
                LOGGER.info("readFile(): File read!");

                if (dump)
                {
                    dumpFile(wrapper);
                }

                FileCache cache = new FileCache();
                cache.copyFile(wrapper);
                //wrapper.close();
                return cache;
            }
        }
        catch (Exception e)
        {
            LOGGER.error("readFile(): Exception reading file! Error: {}", e.getMessage());
        }

        return null;
    }

    protected void dumpFile(@Nonnull CSVWrapper wrapper)
    {
        LOGGER.debug("dumpFile(): Dump file [{}]:", wrapper.getFile());

        if (wrapper.isEmpty())
        {
            LOGGER.error("dumpFile(): Wrapper is EMPTY!");
            return;
        }

        CSVHeader header = wrapper.getHeader();

        if (header == null)
        {
            LOGGER.error("dumpFile(): Header is NULL!");
            return;
        }

        LOGGER.debug("dumpFile(): Header {} // Line Size: [{}]", header.toString(), wrapper.getSize());

        // Start at Line 1
        for (int i = 1; i < wrapper.getSize(); i++)
        {
            List<String> line = wrapper.getLine(i);

            if (line == null)
            {
                LOGGER.error("dumpFile(): LINE[{}] --> NULL!", i);
                continue;
            }
            else if (line.isEmpty())
            {
                LOGGER.error("dumpFile(): LINE[{}] --> EMPTY!", i);
                continue;
            }

            LOGGER.debug("dumpFile(): LINE[{}] --> {}", i, line.toString());
        }

        LOGGER.debug("dumpFile(): EOF");
    }

    protected boolean compareHeaders(CSVHeader header1, CSVHeader header2)
    {
        if (header1 == null || header2 == null)
        {
            return false;
        }

        LOGGER.debug("compareHeaders(): ...");
        return header1.matches(header2);
    }

    protected boolean writeFile(String file, boolean applyQuotes, @Nonnull FileCache FILE)
    {
        return this.writeFile(file, applyQuotes, false, false, FILE, null);
    }

    protected boolean writeFile(String file, boolean applyQuotes, boolean append, @Nonnull FileCache FILE)
    {
        return this.writeFile(file, applyQuotes, append, false, FILE, null);
    }

    protected boolean writeFile(String file, boolean applyQuotes, boolean append, boolean dump, @Nonnull FileCache FILE, @Nullable FileCache APPEND)
    {
        LOGGER.debug("writeFile(): Write file [{}]:", file);

        try (CSVWrapper wrapper = new CSVWrapper(file, false))
        {
            if (wrapper.putAllLines(FILE.getFile(), true))
            {
                if (APPEND != null)
                {
                    if (!appendFile(wrapper, APPEND))
                    {
                        LOGGER.error("writeFile(): Error appending file Cache.");
                        wrapper.close();
                        return false;
                    }
                }

                if (wrapper.write(applyQuotes, append))
                {
                    LOGGER.info("writeFile(): File written!");

                    if (dump)
                    {
                        dumpFile(wrapper);
                    }

                    wrapper.close();
                    return true;
                }
            }
            else
            {
                LOGGER.error("writeFile(): Error copying file Cache to new file.");
                wrapper.close();
            }
        }
        catch (Exception e)
        {
            LOGGER.error("writeFile(): Exception writing file! Error: {}", e.getMessage());
        }

        return false;
    }

    protected boolean appendFile(CSVWrapper wrapper, FileCache FILE)
    {
        if (wrapper == null || FILE == null || FILE.isEmpty() || wrapper.isEmpty())
        {
            return false;
        }

        LOGGER.debug("appendFile(): Appending file to wrapper...");

        for (int i = 1; i < FILE.getFile().size(); i++)
        {
            List<String> entry = FILE.getFile().get(i);

            if (!entry.isEmpty())
            {
                wrapper.putLine(entry);
            }
        }

        return true;
    }

    public void clear() { }
}
