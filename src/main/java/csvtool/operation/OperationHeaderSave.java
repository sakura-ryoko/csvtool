package csvtool.operation;

import csvtool.data.Const;
import csvtool.data.Context;
import csvtool.data.FileCache;
import csvtool.enums.Operations;
import csvtool.enums.Settings;
import csvtool.header.HeaderParser;
import csvtool.utils.LogWrapper;

import javax.annotation.Nullable;

public class OperationHeaderSave extends Operation implements AutoCloseable
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());
    private final HeaderParser PARSER;
    private FileCache FILE;
    private FileCache OUT;

    public OperationHeaderSave(Operations op)
    {
        super(op);
        this.PARSER = new HeaderParser();
        this.FILE = new FileCache();
        this.OUT = new FileCache();
    }

    @Override
    public boolean runOperation(Context ctx)
    {
        if (ctx.getOpt().isQuiet())
        {
            LOGGER.toggleQuiet(true);
        }

        if (ctx.getOpt().isDebug())
        {
            LOGGER.toggleDebug(true);
        }

        if (!ctx.getOpt().hasHeaders())
        {
            LOGGER.error("runOperation(): SaveHeaders FAILED, a Headers Config file is required.");
            return false;
        }

        LOGGER.debug("runOperation(): --> Input [{}], Headers Config [{}], Optional Output [{}]", ctx.getInputFile(), ctx.getSettingValue(Settings.HEADERS), ctx.getOpt().hasOutput() ? ctx.getSettingValue(Settings.OUTPUT) : "<not_used>");

        if (readFiles(ctx.getInputFile(), false, ctx.getSettingValue(Settings.OUTPUT)))
        {
            LOGGER.debug("runOperation(): --> Input [{}] & Output [{}] read successfully.", ctx.getInputFile(), ctx.getOpt().hasOutput() ? ctx.getSettingValue(Settings.OUTPUT) : "<not_used>");

            if (this.PARSER.init(ctx))
            {
                LOGGER.debug("runOperation(): --> Config Parser initialized.");

                this.PARSER.setInputHeader(this.FILE.getHeader(), ctx.getInputFile());

                if (ctx.getOpt().hasOutput())
                {
                    if (this.OUT.getHeader().size() > this.FILE.getHeader().size())
                    {
                        LOGGER.error("runOperation(): Output file headers are too large! [{} > {}], the output can't be larger than the input CSV.", this.OUT.getHeader().size(), this.FILE.getHeader().size());
                        return false;
                    }

                    this.PARSER.setOutputHeader(this.OUT.getHeader(), ctx.getSettingValue(Settings.OUTPUT));
                    this.PARSER.buildRemapList();
                }

                if (this.PARSER.saveConfig())
                {
                    LOGGER.debug("runOperation(): --> Header Config [{}] saved successfully.", this.PARSER.getHeaderConfigFile());
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
        System.out.print("HeaderSave Operation:\n");
        System.out.printf("\tAliases: %s\n\n", Operations.HEADER_SAVE.getAlias().toString());
    }

    private boolean readFiles(String input, boolean ignoreQuotes, @Nullable String output)
    {
        LOGGER.debug("readFiles(): Reading files ...");

        this.FILE = this.readFile(input, ignoreQuotes, Const.DEBUG);

        if (this.FILE == null || this.FILE.isEmpty())
        {
            LOGGER.error("readFiles(): Input File Cache is Empty!");
            return false;
        }

        if (output != null && !output.isEmpty())
        {
            this.OUT = this.readFile(output, ignoreQuotes, Const.DEBUG);

            if (this.OUT == null || this.OUT.isEmpty())
            {
                LOGGER.error("readFiles(): Output File Cache is Empty!");
                return false;
            }
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
        if (this.OUT != null && !this.OUT.isEmpty())
        {
            this.OUT.clear();
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
        if (this.OUT != null)
        {
            this.OUT.close();
        }
        if (this.PARSER != null)
        {
            this.PARSER.close();
        }
    }
}
