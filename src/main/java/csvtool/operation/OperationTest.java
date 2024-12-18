package csvtool.operation;

import csvtool.data.Const;
import csvtool.data.Context;
import csvtool.data.FileCache;
import csvtool.enums.Operations;
import csvtool.enums.Settings;
import csvtool.utils.LogWrapper;

public class OperationTest extends Operation implements AutoCloseable
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());

    private FileCache FILE;

    public OperationTest(Operations op)
    {
        super(op);
        this.FILE = new FileCache();
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
        this.FILE = this.readFile(file, Const.DEBUG);
        return this.FILE != null && !this.FILE.isEmpty();
    }

    private boolean writeFileAndDump(String file, boolean applyQuotes, boolean append)
    {
        return this.writeFile(file, applyQuotes, append, Const.DEBUG, this.FILE, null);
    }

    private void clear()
    {
        if (this.FILE != null && !this.FILE.isEmpty())
        {
            this.FILE.clear();
        }
    }

    @Override
    public void close()
    {
        this.clear();
    }
}
