package csvtool.operation;

import csvtool.data.Context;
import csvtool.data.FileCache;
import csvtool.enums.Operations;
import csvtool.enums.Settings;
import csvtool.pivot.FilePivotParser;
import csvtool.utils.LogWrapper;

public class OperationFilePivotSave extends Operation implements AutoCloseable
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());
    private final FilePivotParser PARSER;
    private FileCache FILE;

    public OperationFilePivotSave(Operations op)
    {
        super(op);
        this.PARSER = new FilePivotParser();
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

        if (!ctx.getOpt().hasHeaders())
        {
            LOGGER.error("runOperation(): SaveFilePivotHeaders FAILED, a Headers Config file is required.");
            return false;
        }

        LOGGER.debug("runOperation(): --> Input [{}], File Pivot Config [{}]", ctx.getInputFile(), ctx.getSettingValue(Settings.HEADERS));

        if (readFiles(ctx.getInputFile(), false, ctx.getOpt().isDebug()))
        {
            LOGGER.debug("runOperation(): --> Input [{}] read successfully.", ctx.getInputFile());

            if (this.PARSER.init(ctx, true))
            {
                LOGGER.debug("runOperation(): --> Config Parser initialized.");

                this.PARSER.setInputHeader(this.FILE.getHeader(), ctx.getInputFile());

                if (this.PARSER.saveConfig())
                {
                    LOGGER.debug("runOperation(): --> File Pivot Config [{}] saved successfully.", this.PARSER.getHeaderConfigFile());
                    return true;
                }
            }
        }

        LOGGER.error("runOperation(): General Operation failure.");
        return false;
    }

    @Override
    public void displayHelp()
    {
        System.out.print("File Pivot Save Operation:\n");
        System.out.printf("\tAliases: %s\n\n", Operations.FILE_PIVOT_SAVE.getAlias().toString());

        System.out.print("\n");
    }

    private boolean readFiles(String input, boolean ignoreQuotes, boolean debug)
    {
        LOGGER.debug("readFiles(): Reading files ...");

        this.FILE = this.readFileHeadersOnly(input, ignoreQuotes, false);

        if (this.FILE == null || this.FILE.getHeader().isEmpty())
        {
            LOGGER.error("readFiles(): Input File Cache is Empty!");
            return false;
        }

        return true;
    }

    @Override
    public void clear()
    {
        if (this.FILE != null && !this.FILE.isEmpty())
        {
            this.FILE.clear();
        }
        if (this.PARSER != null && !this.PARSER.isEmpty())
        {
            this.PARSER.clear();
        }
    }

    @Override
    public void close() throws Exception
    {
        if (this.FILE != null)
        {
            this.FILE.close();
        }
        if (this.PARSER != null)
        {
            this.PARSER.close();
        }
    }
}
