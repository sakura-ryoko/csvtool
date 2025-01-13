package csvtool.operation;

import csvtool.data.Const;
import csvtool.data.Context;
import csvtool.data.FileCache;
import csvtool.enums.Operations;
import csvtool.enums.Settings;
import csvtool.header.CSVRemap;
import csvtool.header.HeaderParser;
import csvtool.header.RemapType;
import csvtool.utils.LogWrapper;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OperationReformat extends Operation implements AutoCloseable
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());
    private final HeaderParser PARSER;
    private FileCache FILE;
    private FileCache OUT;

    public OperationReformat(Operations op)
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
            LOGGER.error("runOperation(): Reformat FAILED, a Headers Config file is required.");
            return false;
        }

        if (!ctx.getOpt().hasOutput())
        {
            LOGGER.error("runOperation(): Reformat FAILED, a Output file is required.");
        }

        LOGGER.debug("runOperation(): --> Input [{}], Headers Config [{}], Output [{}]", ctx.getInputFile(), ctx.getSettingValue(Settings.HEADERS), ctx.getSettingValue(Settings.OUTPUT));

        if (readFiles(ctx.getInputFile(), false))
        {
            LOGGER.debug("runOperation(): --> Input [{}] read successfully.", ctx.getInputFile());

            if (this.PARSER.init(ctx))
            {
                LOGGER.debug("runOperation(): --> Header Config Parser initialized.");

                if (this.PARSER.loadConfig())
                {
                    LOGGER.debug("runOperation(): --> Config Parser loaded config from [{}].", this.PARSER.getHeaderConfigFile());

                    // Run Reformat
                    if (reformatFile())
                    {
                        LOGGER.info("runOperation(): --> File reformat successful.");

                        if (this.writeFile(ctx.getSettingValue(Settings.OUTPUT), ctx.getOpt().isApplyQuotes(), false, Const.DEBUG, this.OUT, null))
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
                        LOGGER.error("runOperation(): File reformat has failed!");
                        return false;
                    }
                }
                else
                {
                    LOGGER.error("runOperation(): --> Failed to load headers config [{}]!", this.PARSER.getHeaderConfigFile());
                    return false;
                }
            }
        }

        LOGGER.error("runOperation(): General Operation failure.");
        return false;
    }

    private boolean readFiles(String input, boolean ignoreQuotes)
    {
        LOGGER.debug("readFiles(): Reading files ...");

        this.FILE = this.readFile(input, ignoreQuotes, Const.DEBUG);

        if (this.FILE == null || this.FILE.isEmpty())
        {
            LOGGER.error("readFiles(): Input File Cache is Empty!");
            return false;
        }

        return true;
    }

    private boolean reformatFile()
    {
        if (this.FILE == null || this.FILE.isEmpty())
        {
            return false;
        }

        LOGGER.debug("reformatFile(): Reformatting ...");
        this.OUT.setHeader(this.PARSER.getOutputHeader());

        for (int i = 0; i < this.FILE.getFile().size(); i++)
        {
            List<String> entry = this.FILE.getFile().get(i);

            // Ignore header
            if (i != 0 && !entry.isEmpty())
            {
                LOGGER.debug("[{}] IN: [{}]", i, entry.toString());
                List<String> result = this.applyRemap(entry);

                if (result == null || result.isEmpty())
                {
                    LOGGER.error("reformatFile(): Remap failure on line [{}]", i);
                    return false;
                }

                LOGGER.debug("[{}] OUT: [{}]", i, result.toString());
                this.OUT.addLine(result);
            }
        }

        return true;
    }

    private @Nullable List<String> applyRemap(List<String> data)
    {
        if (this.PARSER.getRemapList() == null)
        {
            LOGGER.error("applyRemap(): Error, Remap List Config is empty!");
            return null;
        }

        List<CSVRemap> remapList = this.PARSER.getRemapList().list();
        List<String> result = new ArrayList<>(data);

        if (remapList.size() != data.size())
        {
            LOGGER.error("applyRemap(): Error, Remap List Config size [{}] does not match Input Data size [{}]!", remapList.size(), data.size());
            return null;
        }

        LOGGER.debug("applyRemap(): begin with list size [{}]", data.size());

        for (int i = 0; i < data.size(); i++)
        {
            String entry = data.get(i);

            if (entry == null)
            {
                LOGGER.error("applyRemap(): Error, Entry at pos [{}] is empty!", i);
                return null;
            }

            LOGGER.debug("[{}] IN: [{}]", i, entry);

            CSVRemap remap = remapList.get(i);

            // Swap Fields
            if (remap.getType() == RemapType.SWAP)
            {
                List<String> params = remap.getParams();

                try
                {
                    int swapId = Integer.parseInt(params.getFirst());
                    String otherEntry = result.get(swapId);
                    CSVRemap otherRemap = remapList.get(swapId);

                    LOGGER.debug("applyRemap(): Performing Field swap [{}:{} <-> {}:{}]", i, entry, swapId, otherEntry);
                    result.set(swapId, entry);
                    result.set(i, otherEntry);
                    remapList.set(swapId, otherRemap);
                }
                catch (NumberFormatException err)
                {
                    LOGGER.warn("applyRemap(): SWAP error; {}", err.getMessage());
                    return null;
                }
            }
            else
            {
                String resultEach = this.applyRemapEach(remap, entry);

                if (resultEach == null)
                {
                    LOGGER.warn("applyRemap(): Error, ResultEach at pos [{}] is empty!", i);
                    resultEach = entry;
                }

                LOGGER.debug("[{}] OUT: [{}]", i, resultEach);
                result.set(i, resultEach);
            }
        }

        return result;
    }

    private @Nullable String applyRemapEach(@Nonnull CSVRemap remap, String data)
    {
        List<String> params = remap.getParams();

        switch (remap.getType())
        {
            case PAD ->
            {
                try
                {
                    int count = Integer.parseInt(params.getFirst());

                    if (params.size() > 1)
                    {
                        return StringUtils.leftPad(data, count, params.get(1));
                    }
                    else if (params.size() == 1)
                    {
                        return StringUtils.leftPad(data, count);
                    }
                    else
                    {
                        LOGGER.warn("applyRemapEach(): PAD error; required params (count) not mapped");
                    }
                }
                catch (NumberFormatException err)
                {
                    LOGGER.warn("applyRemapEach(): PAD error; {}", err.getMessage());
                }
            }
            case STATIC ->
            {
                if (params.size() > 1)
                {
                    if (data.equals(params.getFirst()))
                    {
                        return params.get(1);
                    }
                    else
                    {
                        LOGGER.warn("applyRemapEach(): STATIC error; param [{}] was not matched", params.getFirst());
                    }
                }
                else if (params.size() == 1)
                {
                    return params.getFirst();
                }
                else
                {
                    LOGGER.warn("applyRemapEach(): STATIC error; required params (old) not mapped");
                }
            }
            case DATE ->
            {
                if (params.size() < 2)
                {
                    LOGGER.warn("applyRemapEach(): DATE error; params of 2 not satisfied (Found {})", params.size());
                }
                else
                {
                    try
                    {
                        SimpleDateFormat inFmt = new SimpleDateFormat(params.getFirst());
                        SimpleDateFormat outFmt = new SimpleDateFormat(params.get(1));
                        Date date = inFmt.parse(data);

                        return outFmt.format(date);
                    }
                    catch (Exception err)
                    {
                        LOGGER.warn("applyRemapEach(): DATE error; {}", err.getMessage());
                    }
                }
            }
        }

        return data;
    }

    @Override
    public void displayHelp()
    {
        System.out.print("Reformat Operation:\n");
        System.out.printf("\tAliases: %s\n\n", Operations.REFORMAT.getAlias().toString());
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
