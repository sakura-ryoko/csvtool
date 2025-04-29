package csvtool.operation;

import csvtool.data.Context;
import csvtool.data.FileCache;
import csvtool.enums.Operations;
import csvtool.enums.Settings;
import csvtool.transform.HeaderTransformParser;
import csvtool.utils.LogWrapper;

public class OperationTransformSave extends Operation implements AutoCloseable
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());
    private final HeaderTransformParser PARSER;
    private FileCache FILE;

    public OperationTransformSave(Operations op)
    {
        super(op);
        this.PARSER = new HeaderTransformParser();
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
            LOGGER.error("runOperation(): SaveTransformHeaders FAILED, a Headers Config file is required.");
            return false;
        }

        LOGGER.debug("runOperation(): --> Input [{}], Transform Config [{}]", ctx.getInputFile(), ctx.getSettingValue(Settings.HEADERS));

        if (readFiles(ctx.getInputFile(), false, ctx.getOpt().isDebug()))
        {
            LOGGER.debug("runOperation(): --> Input [{}] read successfully.", ctx.getInputFile());

            if (this.PARSER.init(ctx, true))
            {
                LOGGER.debug("runOperation(): --> Config Parser initialized.");

                this.PARSER.setInputHeader(this.FILE.getHeader(), ctx.getInputFile());
                this.PARSER.buildTransformList();

                if (this.PARSER.saveConfig())
                {
                    LOGGER.debug("runOperation(): --> Transform Config [{}] saved successfully.", this.PARSER.getHeaderConfigFile());
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
        System.out.print("TransformSave Operation:\n");
        System.out.printf("\tAliases: %s\n\n", Operations.TRANSFORM_SAVE.getAlias().toString());
    }

    private boolean readFiles(String input, boolean ignoreQuotes, boolean debug)
    {
        LOGGER.debug("readFiles(): Reading files ...");

        this.FILE = this.readFileHeadersOnly(input, ignoreQuotes, debug);

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
