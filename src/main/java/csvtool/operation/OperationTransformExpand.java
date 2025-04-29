package csvtool.operation;

import csvtool.data.Const;
import csvtool.data.Context;
import csvtool.data.FileCache;
import csvtool.enums.Operations;
import csvtool.enums.Settings;
import csvtool.header.CSVHeader;
import csvtool.transform.HeaderTransformParser;
import csvtool.utils.LogWrapper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class OperationTransformExpand extends Operation implements AutoCloseable
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());
    private HeaderTransformParser PARSER;
    private FileCache FILE;
    private FileCache OUT;
    private String lineKey;
    private List<String> lineBuffer;
    private int lineNum;
    private int colNum;
    private int totalColumns;

    public OperationTransformExpand(Operations op)
    {
        super(op);
        this.PARSER = new HeaderTransformParser();
        this.FILE = new FileCache();
        this.OUT = new FileCache();
        this.lineKey = "";
        this.lineBuffer = new ArrayList<>();
        this.lineNum = 0;
        this.colNum = 0;
        this.totalColumns = 0;
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
            LOGGER.error("runOperation(): Transform FAILED, a Transform Config file is required.");
            return false;
        }

        if (!ctx.getOpt().hasOutput())
        {
            LOGGER.error("runOperation(): Transform FAILED, a Output file is required.");
            return false;
        }

        if (!ctx.getOpt().hasKey())
        {
            LOGGER.error("runOperation(): Transform FAILED, a key (Primary) field is required.");
            return false;
        }

        if (!ctx.getOpt().hasKey2())
        {
            LOGGER.error("runOperation(): Transform FAILED, a key2 (SubKey) field is required.");
            return false;
        }

        if (!ctx.getOpt().hasSide())
        {
            LOGGER.error("runOperation(): Transform FAILED, a side (Data) field is required.");
            return false;
        }

        LOGGER.debug("runOperation(): --> Input [{}], Transform Config [{}], Output [{}]", ctx.getInputFile(), ctx.getSettingValue(Settings.HEADERS), ctx.getSettingValue(Settings.OUTPUT));
        this.FILE.setFileName(ctx.getInputFile());
        this.OUT.setFileName(ctx.getSettingValue(Settings.OUTPUT));

        if (this.readFiles(ctx.getInputFile(), false, ctx.getOpt().isDebug()))
        {
            LOGGER.debug("runOperation(): --> File [{}] read successfully.", ctx.getInputFile());

            if (this.PARSER.init(ctx, false))
            {
                LOGGER.debug("runOperation(): --> Transform Config Parser initialized.");

                if (this.PARSER.loadConfig())
                {
                    if (!this.PARSER.checkTransformList())
                    {
                        LOGGER.error("runOperation(): Transform FAILED, checkTransformList() has failed.");
                        return false;
                    }

                    LOGGER.debug("runOperation(): --> Transform Parser loaded config from [{}].", this.PARSER.getHeaderConfigFile());

                    if (this.applyTransforms(ctx.getOpt().getKey(), ctx.getOpt().getKey2(), ctx.getOpt().getSide()))
                    {
                        LOGGER.info("runOperation(): --> File transform successful.");

                        if (this.writeFile(this.OUT, ctx.getOpt().isApplyQuotes(), false, ctx.getOpt().isDebug(), null))
                        {
                            LOGGER.info("runOperation(): --> File Output saved as [{}].", ctx.getSettingValue(Settings.OUTPUT));
                            return true;
                        }
                        else
                        {
                            LOGGER.error("runOperation(): File output [{}] has failed!", ctx.getSettingValue(Settings.OUTPUT));
                            return false;
                        }
                    }
                    else
                    {
                        LOGGER.error("runOperation(): File transform has failed!");
                        return false;
                    }
                }
                else
                {
                    LOGGER.error("runOperation(): --> Failed to load transform config [{}]!", this.PARSER.getHeaderConfigFile());
                    return false;
                }
            }
        }

        LOGGER.error("runOperation(): General Operation failure.");
        return false;
    }

    private boolean readFiles(String input, boolean ignoreQuotes, boolean debug)
    {
        LOGGER.debug("readFiles(): Reading files ...");

        this.FILE = this.readFile(input, ignoreQuotes, debug);

        if (this.FILE == null || this.FILE.isEmpty())
        {
            LOGGER.error("readFiles(): Input File Cache is Empty!");
            return false;
        }

        return true;
    }

    private boolean applyTransforms(String key, String key2, String side)
    {
        if (this.FILE == null || this.FILE.isEmpty())
        {
            return false;
        }

        LOGGER.debug("applyTransforms(): Applying Transforms ...");
        CSVHeader inHeader = this.FILE.getHeader();
        CSVHeader parserHeader = this.PARSER.getInputHeader();

        if (inHeader == null || inHeader.isEmpty() ||
            parserHeader == null || parserHeader.isEmpty() ||
            !inHeader.matches(parserHeader))
        {
            LOGGER.error("applyTransforms(): Headers do not match or are Empty!");
            return false;
        }

        this.OUT.setHeader(inHeader);
        CSVHeader newHeader = new CSVHeader(inHeader.stream().toList());
        final int keyId = inHeader.getId(key);
        final int subkeyId = inHeader.getId(key2);
        final int dataId = inHeader.getId(side);

        if (Const.DEBUG)
        {
            this.PARSER.dumpTransformList();
        }

        for (int i = 0; i < this.FILE.getFile().size(); i++)
        {
            List<String> entry = this.FILE.getFile().get(i);

            // Ignore header
            if (i != 0 && !entry.isEmpty())
            {
                LOGGER.debug("[{}] IN: [{}]", i, entry.toString());
                Pair<Boolean, List<String>> result = this.applyTransformEachLine(newHeader, keyId, subkeyId, dataId, entry);

                if (result == null)
                {
                    LOGGER.error("applyTransforms(): Transform failure on line [{}]", i);
                    return false;
                }

                LOGGER.debug("HEADERS: {}", newHeader.toString());

                if (result.getLeft() && !result.getRight().isEmpty())
                {
                    LOGGER.debug("[{}/{}] OUT (AddLine): [{}]", i, this.lineNum, result.toString());
                    this.OUT.addLine(result.getRight());
                    this.lineNum++;
                }
                else
                {
                    LOGGER.debug("[{}/{}] OUT (Each): [{}]", i, this.lineNum, result.toString());
                }
            }
        }

        return true;
    }

    private Pair<Boolean, List<String>> applyTransformEachLine(CSVHeader headers, final int keyId, final int subkeyId, final int dataId, List<String> list)
    {
        if (keyId > list.size() || subkeyId > list.size() || dataId > list.size())
        {
            LOGGER.error("applyTransformEachLine(): Transform failure; keyIds are too large!");
            return null;
        }

        // TODO
        String keyEntry = list.get(keyId);
        String subkeyEntry = list.get(subkeyId);
        String dataEntry = list.get(dataId);
        boolean newLine = false;

        if (this.lineKey.isEmpty())
        {
            // First entry
            this.lineBuffer.clear();
            this.colNum = list.size();
        }
        else if (this.lineKey.equals(keyEntry))
        {
            // Continue the line
        }
        else
        {
            // New Line
            this.lineKey = keyEntry;
            this.colNum = list.size();
            newLine = true;
        }

        return Pair.of(newLine, list);
    }

    private String buildTransformedName(String in)
    {
        return in;
    }

    private @Nullable CSVHeader checkHeaderColumn(@Nonnull CSVHeader header, final String column)
    {
        String colHeader = header.getFromId(this.colNum);

        if (colHeader == null)
        {
            // Not found, add column
            header.add(column);
            this.colNum++;
            this.totalColumns++;
            return header;
        }
        else if (colHeader.equals(column))
        {
            // OK
            return header;
        }
        else
        {
            // Not matched!
            LOGGER.error("checkHeaderColumn(): Invalid match at column number [{}] ('{}' != '{}')", this.colNum, colHeader, column);
            return null;
        }
    }

    @Override
    public void displayHelp()
    {
        System.out.print("Header Expand Operation:\n");
        System.out.printf("\tAliases: %s\n\n", Operations.TRANSFORM_EXPAND.getAlias().toString());

//        System.out.print("It accepts one input file (--input), and an output (--output).\n");
//        System.out.print("This operation simply copies the input to the output file to test the inner-workings of this program.\n");
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
