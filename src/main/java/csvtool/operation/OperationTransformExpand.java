package csvtool.operation;

import csvtool.data.Const;
import csvtool.data.Context;
import csvtool.data.FileCache;
import csvtool.enums.Operations;
import csvtool.enums.Settings;
import csvtool.header.CSVHeader;
import csvtool.transform.HeaderTransformList;
import csvtool.transform.HeaderTransformParser;
import csvtool.utils.LogWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OperationTransformExpand extends Operation implements AutoCloseable
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());

    private final HeaderTransformParser PARSER;
    private FileCache FILE;
    private final FileCache OUT;
    private final HashMap<String, Integer> transformKeys;
    private final HashMap<String, Integer> transformSubkeys;
    private int colNum;

    public OperationTransformExpand(Operations op)
    {
        super(op);
        this.PARSER = new HeaderTransformParser();
        this.FILE = new FileCache();
        this.OUT = new FileCache();
        this.transformKeys = new HashMap<>();
        this.transformSubkeys = new HashMap<>();
        this.colNum = 0;
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

                    if (this.applyTransforms(ctx.getOpt().getKey()))
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

    private boolean applyTransforms(String key)
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
        final int keyId = inHeader.getId(key);
        final int subkeyId = inHeader.getId(this.PARSER.getSubkey());

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
                if (!this.applyTransformEachLine(keyId, subkeyId, entry))
                {
                    LOGGER.error("applyTransforms(): Transform failure on line [{}]", i);
                    return false;
                }
            }
        }

        return true;
    }

    private boolean applyTransformEachLine(final int keyId, final int subkeyId, List<String> list)
    {
        if (keyId > list.size() || subkeyId > list.size())
        {
            LOGGER.error("applyTransformEachLine(): Transform failure; keyIds are too large!");
            return false;
        }

        String keyEntry = list.get(keyId);
        String subkeyEntry = list.get(subkeyId);

//        LOGGER.debug("applyTransformEachLine(): key [{}], subkey [{}], list [{}]", keyEntry, subkeyEntry, list.toString());

        final int lineKeyIndex = this.calcLineKeyIndex(keyEntry);
        final int subkeyIndex = this.calcTransformSubkeyIndex(subkeyEntry);
//        LOGGER.debug("applyTransformEachLine(): lineKeyIndex [{}], subkeyIndex [{}], colNum [{}]", lineKeyIndex, subkeyIndex, this.colNum);
        HeaderTransformList transforms = this.PARSER.getTransformList();

        if (transforms == null)
        {
            LOGGER.error("applyTransformEachLine(): transforms is null!");
            return false;
        }

        for (int i = 0; i < transforms.size(); i++)
        {
            HeaderTransformList.Entry entry = transforms.getEntry(i);

            if (entry != null)
            {
//                LOGGER.debug("applyTransformEachLine(): Transform [{}] debug [{}]", i, entry.toString());
                String result = entry.reformat(subkeyEntry, subkeyIndex, list);
//                LOGGER.debug("applyTransformEachLine(): Transform [{}] result [{}]", i, result);

                final int col = this.calcHeaderColumn(result);

                if (col > 0)
                {
                    List<String> data = new ArrayList<>(this.OUT.getHeader().size());

                    if (this.OUT.hasLine(lineKeyIndex))
                    {
                        data.addAll(this.OUT.getLine(lineKeyIndex));
                    }
                    else
                    {
                        data.addAll(list);
                    }

                    if (data.size() < this.OUT.getHeader().size())
                    {
                        for (int j = data.size(); j < this.OUT.getHeader().size(); j++)
                        {
                            data.add("");
                        }
                    }

                    if (entry.data() < 0 || entry.data() > list.size())
                    {
                        data.set(col, "");
                    }
//                    else if (entry.subRemap() != null)
//                    {
//                        Pair<Boolean, String> pair = this.applyRemapEach(entry.subRemap(), list.get(entry.data()));
//
//                        if (pair == null || pair.getRight() == null)
//                        {
//                            data.set(col, list.get(entry.data()));
//                        }
//                        else
//                        {
//                            if (pair.getLeft())
//                            {
//                                data.set(col, "");
//                            }
//                            else
//                            {
//                                data.set(col, pair.getRight());
//                            }
//                        }
//                    }
                    else
                    {
                        data.set(col, list.get(entry.data()));
                    }

//                    LOGGER.debug("applyTransformEachLine(): Setline [{}] data [{}]", lineKeyIndex, data.toString());
                    this.OUT.setLine(lineKeyIndex, data);
                }
                else
                {
                    LOGGER.error("applyTransformEachLine(): Transform Entry [{}] checkHeaderColumn() result is false", i);
                    return false;
                }
            }
            else
            {
                LOGGER.error("applyTransformEachLine(): Transform Entry [{}] is null!", i);
                return false;
            }
        }

        // Default OK, move onto next line
        return true;
    }

    private int calcLineKeyIndex(String key)
    {
        if (this.transformKeys.isEmpty())
        {
            this.transformKeys.put(key, 1);
            return 1;
        }
        else if (this.transformKeys.containsKey(key))
        {
            return this.transformKeys.get(key);
        }
        else
        {
            int result = this.transformKeys.size() + 1;
            this.transformKeys.put(key, result);
            return result;
        }
    }

    private int calcTransformSubkeyIndex(String subkey)
    {
        if (this.transformSubkeys.containsKey(subkey))
        {
            int result = this.transformSubkeys.get(subkey);
            this.transformSubkeys.put(subkey, ++result);
            return result;
        }
        else
        {
            this.transformSubkeys.put(subkey, 0);
            return 0;
        }
    }

    private int calcHeaderColumn(final String column)
    {
        String colHeader = this.OUT.getHeader().getFromId(this.colNum);
        final int col = this.OUT.getHeader().getId(column);

        if (colHeader == null || col == -1)
        {
            // Not found, add column
            this.colNum++;
//            LOGGER.debug("calcHeaderColumn(): Add column number [{}] with [{}]", this.colNum, column);
            this.OUT.appendHeader(column);
        }

        final int check = this.OUT.getHeader().getId(column);
//        LOGGER.debug("calcHeaderColumn(): column '{}' found at index [{}]", column, check);
        return check;
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
