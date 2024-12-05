package csvtool.operation;

import csvtool.data.Context;
import csvtool.enums.Operations;
import csvtool.enums.Settings;
import csvtool.header.CSVHeader;
import csvtool.utils.CSVWrapper;
import csvtool.utils.LogWrapper;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;

public class OperationTest extends Operation implements AutoCloseable
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());

    private final HashMap<Integer, List<String>> LINES;

    public OperationTest(Operations op)
    {
        super(op);
        this.LINES = new HashMap<>();
    }

    @Override
    public boolean runOperation(Context ctx)
    {
        LOGGER.debug("runOperation(): --> TEST");

        if (readFileAndDump(ctx.getInputFile()))
        {
            LOGGER.debug("runOperation(): --> File [{}] read successfully.", ctx.getInputFile());

            if (ctx.getOpt().hasOutput())
            {
                if (writeFileAndDump(ctx.getOpt().getOutput(), ctx.getOpt().isApplyQuotes(), ctx.getOpt().isAppendOutput()))
                {
                    LOGGER.debug("runOperation(): --> File [{}] written successfully.", ctx.getSettingValue(Settings.OUTPUT));
                    this.clear();
                    return true;
                }
                else
                {
                    LOGGER.error("runOperation(): Write file FAILED.");
                }
            }
            else
            {
                LOGGER.error("runOperation(): No Output given!");
            }
        }

        return false;
    }

    private boolean readFileAndDump(String file)
    {
        try (CSVWrapper wrapper = new CSVWrapper(file))
        {
            if (wrapper.read())
            {
                LOGGER.info("readFileAndDump(): File read!");
                dumpFile(wrapper);
                cacheFile(wrapper);
                wrapper.close();
                return true;
            }
        }
        catch (Exception e)
        {
            LOGGER.error("readFileAndDump(): Exception reading file! Error: {}", e.getMessage());
        }

        return false;
    }

    private void cacheFile(CSVWrapper wrapper)
    {
        if (!this.LINES.isEmpty())
        {
            this.LINES.clear();
        }

        LOGGER.debug("cacheFile(): Caching file [{} lines] ...", wrapper.getSize());

        this.LINES.putAll(wrapper.getAllLines());
    }

    private boolean writeFileAndDump(String file, boolean applyQuotes, boolean append)
    {
        try (CSVWrapper wrapper = new CSVWrapper(file, false))
        {
            if (wrapper.putAllLines(this.LINES, true))
            {
                if (wrapper.write(applyQuotes, append))
                {
                    LOGGER.info("writeFileAndDump(): File written!");
                    dumpFile(wrapper);
                    wrapper.close();
                    return true;
                }
            }
            else
            {
                LOGGER.error("writeFileAndDump(): Error copying Cache to new file.");
                wrapper.close();
                return true;
            }
        }
        catch (Exception e)
        {
            LOGGER.error("writeFileAndDump(): Exception writing file! Error: {}", e.getMessage());
        }

        return false;
    }

    private void dumpFile(@Nonnull CSVWrapper wrapper)
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
            LOGGER.error("dumpFile(): eader is NULL!");
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

    private void clear()
    {
        if (this.LINES != null && !this.LINES.isEmpty())
        {
            this.LINES.clear();
        }
    }

    @Override
    public void close()
    {
        this.clear();
    }
}
