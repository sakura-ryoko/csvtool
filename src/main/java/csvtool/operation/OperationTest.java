package csvtool.operation;

import csvtool.data.Context;
import csvtool.enums.Operations;
import csvtool.header.CSVHeader;
import csvtool.utils.CSVWrapper;
import csvtool.utils.LogWrapper;

import javax.annotation.Nonnull;
import java.util.List;

public class OperationTest extends Operation
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());

    public OperationTest(Operations op)
    {
        super(op);
        LOGGER.debug("TEST!");
    }

    @Override
    public boolean runOperation(Context ctx)
    {
        try (CSVWrapper wrapper = new CSVWrapper(ctx.getInputFile()))
        {
            if (wrapper.read())
            {
                LOGGER.info("File read!");
                dumpFile(wrapper);
                return true;
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Exception reading file! Error: {}", e.getMessage());
        }

        return false;
    }

    private void dumpFile(@Nonnull CSVWrapper wrapper)
    {
        LOGGER.error("Dump file [{}]:", wrapper.getFile());

        if (wrapper.isEmpty())
        {
            LOGGER.error("Wrapper is EMPTY!");
            return;
        }

        CSVHeader header = wrapper.getHeader();

        if (header == null)
        {
            LOGGER.error("Header is NULL!");
            return;
        }

        LOGGER.warn("Header {} // Line Size: [{}]", header.toString(), wrapper.getSize());

        // Start at Line 1
        for (int i = 1; i < wrapper.getSize(); i++)
        {
            List<String> line = wrapper.getLine(i);

            if (line == null)
            {
                LOGGER.error("LINE[{}] --> NULL!", i);
                continue;
            }
            else if (line.isEmpty())
            {
                LOGGER.error("LINE[{}] --> EMPTY!", i);
                continue;
            }

            LOGGER.info("LINE[{}] --> {}", i, line.toString());
        }

        LOGGER.error("EOF");
    }
}
