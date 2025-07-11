package csvtool.operation;

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
        if (ctx.getOpt().isQuiet())
        {
            super.toggleQuiet(true);
            LOGGER.toggleQuiet(true);
        }

        if (ctx.getOpt().isDebug())
        {
            super.toggleDebug(true);
            LOGGER.toggleDebug(true);
        }

        if (ctx.getOpt().isAnsiColors())
        {
            super.toggleAnsiColor(true);
            LOGGER.toggleAnsiColor(true);
        }

        LOGGER.debug("runOperation(): --> TEST");

        if (readFileAndDump(ctx.getInputFile(), false, ctx.getOpt().isDebug()))
        {
            LOGGER.debug("runOperation(): --> File [{}] read successfully.", ctx.getInputFile());

            if (ctx.getOpt().hasOutput())
            {
                if (writeFileAndDump(ctx.getOpt().getOutput(), ctx.getOpt().isApplyQuotes(), ctx.getOpt().isAppendOutput(), ctx.getOpt().isDebug()))
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

        LOGGER.error("runOperation(): General Operation failure.");
        return false;
    }

    @Override
    public void displayHelp()
    {
        System.out.print("Test Operation:\n");
        System.out.printf("\tAliases: %s\n\n", Operations.TEST.getAlias().toString());

        System.out.print("It accepts one input file (--input), and an output (--output).\n");
        System.out.print("This operation simply copies the input to the output file to test the inner-workings of this program.\n");
        System.out.print("\n");
    }

    private boolean readFileAndDump(String file, boolean ignoreQuotes, boolean debug)
    {
        this.FILE = this.readFile(file, ignoreQuotes, debug);
        return this.FILE != null && !this.FILE.isEmpty();
    }

    private boolean writeFileAndDump(String file, boolean applyQuotes, boolean append, boolean debug)
    {
        this.FILE.setFileName(file);
        return this.writeFile(this.FILE, applyQuotes, append, debug, null);
    }

    @Override
    public void clear()
    {
        if (this.FILE != null && !this.FILE.isEmpty())
        {
            this.FILE.clear();
        }
    }

    @Override
    public void close() throws Exception
    {
        if (this.FILE != null)
        {
            this.FILE.close();
        }
    }
}
