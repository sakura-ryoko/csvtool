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
import csvtool.utils.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OperationReformat extends Operation implements AutoCloseable
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());
    private final HeaderParser PARSER;
    private FileCache FILE;
    private final FileCache OUT;
    private final FileCache EXCEPTIONS;

    public OperationReformat(Operations op)
    {
        super(op);
        this.PARSER = new HeaderParser();
        this.FILE = new FileCache();
        this.OUT = new FileCache();
        this.EXCEPTIONS = new FileCache();
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

        this.FILE.setFileName(ctx.getInputFile());

        if (readFiles(ctx.getInputFile(), false))
        {
            LOGGER.debug("runOperation(): --> Input [{}] read successfully.", ctx.getInputFile());

            if (this.PARSER.init(ctx))
            {
                LOGGER.debug("runOperation(): --> Header Config Parser initialized.");

                if (this.PARSER.loadConfig())
                {
                    LOGGER.debug("runOperation(): --> Config Parser loaded config from [{}].", this.PARSER.getHeaderConfigFile());

                    this.OUT.setFileName(ctx.getSettingValue(Settings.OUTPUT));
                    this.EXCEPTIONS.setFileName(StringUtils.addFileSuffix(this.OUT.getFileName(), "-exceptions"));

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
        this.EXCEPTIONS.setHeader(this.PARSER.getOutputHeader());

        for (int i = 0; i < this.FILE.getFile().size(); i++)
        {
            List<String> entry = this.FILE.getFile().get(i);

            // Ignore header
            if (i != 0 && !entry.isEmpty())
            {
                LOGGER.debug("[{}] IN: [{}]", i, entry.toString());
                Pair<Boolean, List<String>> result = this.applyRemap(entry);

                // TODO handle exceptions

                if (result == null || result.getRight().isEmpty())
                {
                    LOGGER.error("reformatFile(): Remap failure on line [{}]", i);
                    return false;
                }

                LOGGER.debug("[{}] OUT: [{}]", i, result.toString());
                this.OUT.addLine(result.getRight());
            }
        }

        return true;
    }

    private @Nullable Pair<Boolean, List<String>> applyRemap(List<String> data)
    {
        if (this.PARSER.getRemapList() == null)
        {
            LOGGER.error("applyRemap(): Error, Remap List Config is empty!");
            return null;
        }

        List<CSVRemap> remapList = this.PARSER.getRemapList().list();
        List<String> result = new ArrayList<>(data);
        List<Integer> handled = new ArrayList<>();

        if (remapList.size() != data.size())
        {
            LOGGER.error("applyRemap(): Error, Remap List Config size [{}] does not match Input Data size [{}]!", remapList.size(), data.size());
            return null;
        }

        LOGGER.debug("applyRemap(): begin (pass 1) with list size [{}]", data.size());

        for (int i = 0; i < data.size(); i++)
        {
            String entry = data.get(i);

            if (entry == null)
            {
                LOGGER.error("applyRemap():1: Error, Entry at pos [{}] is empty!", i);
                return null;
            }

            LOGGER.debug("[{}] IN:1: [{}]", i, entry);

            CSVRemap remap = remapList.get(i);

            if (remap.getType() == RemapType.DROP)
            {
                // Drop Field & advance (By simply ignoring it from the results)
                LOGGER.debug("applyRemap():1: Performing Field drop [{}:{}]", i, entry);
                handled.add(i);
            }
            else if (remap.getType() == RemapType.SWAP)
            {
                // Swap Fields
                List<String> params = remap.getParams();

                try
                {
                    int swapId = Integer.parseInt(params.getFirst());
                    String otherEntry = result.get(swapId);
                    CSVRemap otherRemap = remapList.get(swapId);

                    LOGGER.debug("applyRemap():1: Performing Field swap [{}:{} <-> {}:{}]", i, entry, swapId, otherEntry);
                    result.set(swapId, entry);
                    result.set(i, otherEntry);
                    remapList.set(swapId, otherRemap);
                    handled.add(i);
                }
                catch (NumberFormatException err)
                {
                    LOGGER.warn("applyRemap():1: SWAP error; {}", err.getMessage());
                    return null;
                }
            }
            else
            {
                Pair<Boolean, String> resultEach = this.applyRemapEach(remap, entry);

                // TODO handle exceptions

                if (resultEach == null || resultEach.getRight() == null)
                {
                    LOGGER.warn("applyRemap():1: Error, ResultEach at pos [{}] is empty!", i);
                    resultEach = Pair.of(false, entry);
                }

                LOGGER.debug("[{}] OUT:1: [{}]", i, resultEach);
                result.set(i, resultEach.getRight());
                handled.add(i);
            }
        }

        LOGGER.debug("applyRemap(): begin (Pass 2) with list size [{}] and handled size [{}]", data.size(), handled.size());
        // Pass 2 (To process preceding Swaps that might not yet have been remapped)
        for (int i = 0; i < data.size(); i++)
        {
            String entry = data.get(i);

            if (entry == null)
            {
                LOGGER.error("applyRemap():2: Error, Entry at pos [{}] is empty!", i);
                return null;
            }

            if (!handled.contains(i))
            {
                LOGGER.debug("[{}] IN:2: [{}]", i, entry);

                CSVRemap remap = remapList.get(i);

                if (remap.getType() == RemapType.DROP)
                {
                    // Drop Field & advance (By simply ignoring it from the results)
                    LOGGER.debug("applyRemap():2: Performing Field drop [{}:{}]", i, entry);
                    handled.add(i);
                }
                else if (remap.getType() == RemapType.SWAP)
                {
                    // Swap Fields
                    List<String> params = remap.getParams();

                    try
                    {
                        int swapId = Integer.parseInt(params.getFirst());
                        String otherEntry = result.get(swapId);
                        CSVRemap otherRemap = remapList.get(swapId);

                        LOGGER.debug("applyRemap():2: Performing Field swap [{}:{} <-> {}:{}]", i, entry, swapId, otherEntry);
                        result.set(swapId, entry);
                        result.set(i, otherEntry);
                        remapList.set(swapId, otherRemap);
                        handled.add(i);
                    }
                    catch (NumberFormatException err)
                    {
                        LOGGER.warn("applyRemap():2: SWAP error; {}", err.getMessage());
                        return null;
                    }
                }
                else
                {
                    Pair<Boolean, String> resultEach = this.applyRemapEach(remap, entry);

                    // TODO handle exceptions

                    if (resultEach == null || resultEach.getRight() == null)
                    {
                        LOGGER.warn("applyRemap():2: Error, ResultEach at pos [{}] is empty!", i);
                        resultEach = Pair.of(false, entry);
                    }

                    LOGGER.debug("[{}] OUT:2: [{}]", i, resultEach);
                    result.set(i, resultEach.getRight());
                    handled.add(i);
                }
            }
        }

        LOGGER.debug("applyRemap(): Post pass 2 data size [{}], result size [{}], handled size [{}]", data.size(), result.size(), handled.size());

        if (data.size() != handled.size())
        {
            LOGGER.warn("applyRemap(): Unhandled remaps detected!");
        }

        if (result.size() > handled.size() || result.size() > data.size())
        {
            LOGGER.warn("applyRemap(): Excess Results detected!");
        }

        return Pair.of(false, result);
    }

    private Pair<Boolean, String> applyRemapEach(@Nonnull CSVRemap remap, String data)
    {
        List<String> params = remap.getParams();

        if (data == null)
        {
            data = "";
        }

        switch (remap.getType())
        {
            case PAD ->
            {
                try
                {
                    int count = Integer.parseInt(params.getFirst());

                    if (params.size() > 1)
                    {
                        return Pair.of(false, StringUtils.leftPad(data, count, params.get(1)));
                    }
                    else if (params.size() == 1)
                    {
                        return Pair.of(false, StringUtils.leftPad(data, count));
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
                        return Pair.of(false, params.get(1));
                    }
                    else
                    {
                        LOGGER.warn("applyRemapEach(): STATIC error; param [{}] was not matched", params.getFirst());
                    }
                }
                else if (params.size() == 1)
                {
                    return Pair.of(false, params.getFirst());
                }
                else
                {
                    LOGGER.warn("applyRemapEach(): STATIC error; required params (old) not mapped");
                }
            }
            case EXCLUDE ->
            {
                if (params.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): EXCLUDE error; param are empty");
                }
                else
                {
                    Pattern pattern = Pattern.compile(params.getFirst());
                    Matcher matcher = pattern.matcher(data);

                    if (matcher.matches())
                    {
                        return Pair.of(true, data);
                    }
                    else
                    {
                        return Pair.of(false, data);
                    }
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

                        return Pair.of(false, outFmt.format(date));
                    }
                    catch (Exception err)
                    {
                        LOGGER.warn("applyRemapEach(): DATE error; {}", err.getMessage());
                    }
                }
            }
        }

        return Pair.of(false, data);
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
