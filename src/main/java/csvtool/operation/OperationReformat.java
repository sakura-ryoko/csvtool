package csvtool.operation;

import csvtool.data.Const;
import csvtool.data.Context;
import csvtool.data.FileCache;
import csvtool.enums.Operations;
import csvtool.enums.Settings;
import csvtool.header.CSVRemap;
import csvtool.header.CSVRemapList;
import csvtool.header.HeaderParser;
import csvtool.header.RemapType;
import csvtool.utils.LogWrapper;
import csvtool.utils.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.*;
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
            LOGGER.error("runOperation(): Reformat FAILED, a Headers Config file is required.");
            return false;
        }

        if (!ctx.getOpt().hasOutput())
        {
            LOGGER.error("runOperation(): Reformat FAILED, a Output file is required.");
            return false;
        }

        LOGGER.debug("runOperation(): --> Input [{}], Headers Config [{}], Output [{}]", ctx.getInputFile(), ctx.getSettingValue(Settings.HEADERS), ctx.getSettingValue(Settings.OUTPUT));

        this.FILE.setFileName(ctx.getInputFile());
        this.OUT.setFileName(ctx.getSettingValue(Settings.OUTPUT));

        if (this.readFiles(ctx.getInputFile(), false, ctx.getOpt().isDebug()))
        {
            LOGGER.debug("runOperation(): --> Input [{}] read successfully.", ctx.getInputFile());

            if (this.PARSER.init(ctx, false))
            {
                LOGGER.debug("runOperation(): --> Header Config Parser initialized.");

                if (this.PARSER.loadConfig())
                {
                    if (!this.PARSER.checkRemapList())
                    {
                        LOGGER.error("runOperation(): Reformat FAILED, checkRemapList() has failed.");
                        return false;
                    }

                    String exceptionsFile = StringUtils.addFileSuffix(this.OUT.getFileName(), "-exceptions");
                    LOGGER.debug("runOperation(): --> Config Parser loaded config from [{}].", this.PARSER.getHeaderConfigFile());

                    this.EXCEPTIONS.setFileName(exceptionsFile);

                    // Run Reformat
                    if (reformatFile())
                    {
                        LOGGER.info("runOperation(): --> File reformat successful.");

                        if (this.writeFile(this.OUT, ctx.getOpt().isApplyQuotes(), false, ctx.getOpt().isDebug(), null))
                        {
                            LOGGER.info("runOperation(): --> File Output saved as [{}].", ctx.getSettingValue(Settings.OUTPUT));

                            if (!this.EXCEPTIONS.isEmpty())
                            {
                                if (this.writeFile(this.EXCEPTIONS, ctx.getOpt().isApplyQuotes(), false, ctx.getOpt().isDebug(), null))
                                {
                                    LOGGER.info("runOperation(): --> File exceptions saved as [{}].", exceptionsFile);
                                    return true;
                                }
                                else
                                {
                                    LOGGER.error("runOperation(): File exceptions [{}] has failed!", exceptionsFile);
                                    return false;
                                }
                            }

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

    private boolean reformatFile()
    {
        if (this.FILE == null || this.FILE.isEmpty())
        {
            return false;
        }

        LOGGER.debug("reformatFile(): Reformatting ...");
        this.OUT.setHeader(this.PARSER.getOutputHeader());
        this.EXCEPTIONS.setHeader(this.PARSER.getOutputHeader());

        if (Const.DEBUG)
        {
            this.PARSER.dumpRemapList();
        }

        for (int i = 0; i < this.FILE.getFile().size(); i++)
        {
            List<String> entry = this.FILE.getFile().get(i);

            // Ignore header
            if (i != 0 && !entry.isEmpty())
            {
                LOGGER.debug("[{}] IN: [{}]", i, entry.toString());
                Pair<Boolean, List<String>> result = this.applyRemap(entry);

                if (result == null || result.getRight().isEmpty())
                {
                    LOGGER.error("reformatFile(): Remap failure on line [{}]", i);
                    return false;
                }

                LOGGER.debug("[{}] OUT: [{}]", i, result.toString());

                if (result.getLeft())
                {
                    this.EXCEPTIONS.addLine(result.getRight());
                }
                else
                {
                    this.OUT.addLine(result.getRight());
                }
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

        CSVRemapList remapList = new CSVRemapList(this.PARSER.getRemapList().getList());

        if (remapList.size() != data.size())
        {
            LOGGER.error("applyRemap(): Error; Remap List Config size [{}] does not match Input Data size [{}]!", remapList.size(), data.size());
            return null;
        }

        LOGGER.debug("applyRemap(): begin (pass 1) with list size [{}]", data.size());
        // Pass 1 (To process any IF-STATIC/COPY)
        for (int i = 0; i < data.size(); i++)
        {
            String entry = data.get(i);

            if (entry == null)
            {
                LOGGER.error("applyRemap():1: Error; Entry at pos [{}] is empty!", i);
                return null;
            }

            LOGGER.debug("[{}] IN:1: [{}]", i, entry);

            CSVRemap remap = remapList.getRemap(i);

            if (remap == null)
            {
                LOGGER.error("applyRemap():1: Error; Remap at pos [{}] is empty!", i);
                return null;
            }

            LOGGER.debug("applyRemap():1: [{}] toString [{}]", i, remap.toString());
            if (remap.getType() == RemapType.IF_STATIC)
            {
                // If-Static
                List<String> params = remap.getParams();

                if (params == null || params.isEmpty() || params.size() < 3)
                {
                    LOGGER.warn("applyRemap():1: IF_STATIC error; Invalid parameters given");
                    return null;
                }

                try
                {
                    int fieldId = Integer.parseInt(params.getFirst());
                    String otherEntry = data.get(fieldId);

                    LOGGER.debug("applyRemap():1: IF_STATIC test [{}/{}] // otherField [{}/{}]", i, entry, fieldId, otherEntry);
                    String ifResult = this.applyIfStaticEach(entry, otherEntry, params);

                    if (!ifResult.equalsIgnoreCase(entry))
                    {
                        LOGGER.debug("applyRemap():1: IF_STATIC applied to [{}/{}]", i, entry);
                        data.set(i, ifResult);
                    }
                    else
                    {
                        LOGGER.debug("applyRemap():1: IF_STATIC no match found!");
                    }

                    if (remap.getSubRemap() != null)
                    {
                        remapList.setRemap(i, remap.getSubRemap());
                    }
                    else
                    {
                        remapList.setRemap(i, new CSVRemap(i, RemapType.NONE));
                    }
                }
                catch (Exception err)
                {
                    LOGGER.warn("applyRemap():1: IF_STATIC Exception; {}", err.getMessage());
                    return null;
                }
            }
            else if (remap.getType() == RemapType.COPY)
            {
                // Copy (Pass 1)
                List<String> params = remap.getParams();

                if (params == null || params.isEmpty())
                {
                    LOGGER.warn("applyRemap():1: COPY error; No parameters given");
                    return null;
                }

                try
                {
                    int copyId = Integer.parseInt(params.getFirst());
                    String otherEntry = data.get(copyId);

                    LOGGER.debug("applyRemap():1: COPY [{}/{}] apply --> [{}/{}]", copyId, otherEntry, i, entry);
                    data.set(i, otherEntry);
                    remapList.setRemap(i, new CSVRemap(i, RemapType.NONE));
                }
                catch (Exception err)
                {
                    LOGGER.warn("applyRemap():1: COPY Exception; {}", err.getMessage());
                    return null;
                }
            }
        }

        LOGGER.debug("applyRemap(): begin (Pass 2) with list size [{}]", data.size());
        // Pass 2 (To process the swaps)
        for (int i = 0; i < data.size(); i++)
        {
            String entry = data.get(i);

            if (entry == null)
            {
                LOGGER.error("applyRemap():2: Error; Entry at pos [{}] is empty!", i);
                return null;
            }

            //LOGGER.debug("[{}] IN:2: [{}]", i, entry);

            CSVRemap remap = remapList.getRemap(i);

            if (remap == null)
            {
                LOGGER.error("applyRemap():2: Error; Remap at pos [{}] is empty!", i);
                return null;
            }

            //LOGGER.debug("applyRemap():2: [{}] toString [{}]", i, remap.toString());

            if (remap.getType() == RemapType.SWAP)
            {
                // Swap Fields
                List<String> params = remap.getParams();

                if (params == null || params.isEmpty())
                {
                    LOGGER.warn("applyRemap():2: SWAP error; No parameters given");
                    return null;
                }

                try
                {
                    int swapId = Integer.parseInt(params.getFirst());
                    String otherEntry = data.get(swapId);
                    CSVRemap otherRemap = remapList.getRemap(swapId);

                    LOGGER.debug("applyRemap():2: Performing Field swap [{}:{} <-> {}:{}]", i, entry, swapId, otherEntry);
                    data.set(i, otherEntry);

                    if (otherRemap != null)
                    {
                        remapList.setRemap(i, otherRemap);
                    }
                    else
                    {
                        remapList.setRemap(i, new CSVRemap(i, RemapType.NONE));
                    }

                    LOGGER.debug("applyRemap():2:SWAP-A: [{}] toString [{}]", i, Objects.requireNonNullElse(remapList.getRemap(i), "<NULL>").toString());
                    data.set(swapId, entry);

                    if (remap.getSubRemap() != null)
                    {
                        remapList.setRemap(swapId, remap.getSubRemap());
                    }
                    else
                    {
                        remapList.setRemap(swapId, new CSVRemap(swapId, RemapType.NONE));
                    }

                    LOGGER.debug("applyRemap():2:SWAP-B: [{}] toString [{}]", swapId, Objects.requireNonNullElse(remapList.getRemap(swapId), "<NULL>").toString());
                }
                catch (Exception err)
                {
                    LOGGER.warn("applyRemap():2: SWAP error; {}", err.getMessage());
                    return null;
                }
            }
        }

        List<String> result = new ArrayList<>(data);
        boolean exclude = false;

        LOGGER.debug("applyRemap(): begin (Pass 3) with list size [{}]", data.size());
        // Pass 3 (To process the actual each-remaps)
        for (int i = 0; i < data.size(); i++)
        {
            String entry = data.get(i);

            if (entry == null)
            {
                LOGGER.error("applyRemap():3: Error; Entry at pos [{}] is empty!", i);
                return null;
            }

            //LOGGER.debug("[{}] IN:2: [{}]", i, entry);

            CSVRemap remap = remapList.getRemap(i);

            if (remap == null)
            {
                LOGGER.error("applyRemap():3: Error; Remap at pos [{}] is empty!", i);
                return null;
            }

            //LOGGER.debug("applyRemap():2: [{}] toString [{}]", i, remap.toString());

            if (remap.getType() == RemapType.DROP)
            {
                // Drop Field & advance (By simply ignoring it from the results)
                LOGGER.debug("applyRemap():3: Performing Field drop [{}:{}] (Pass 2)", i, entry);
            }
            else if (remap.getType() == RemapType.SWAP)
            {
                result.set(i, entry);
            }
            else if (remap.getType() == RemapType.COPY)
            {
                // Copy (Pass 3 -- from post-swap)
                List<String> params = remap.getParams();

                if (params == null || params.isEmpty())
                {
                    LOGGER.warn("applyRemap():3: COPY error; No parameters given");
                    return null;
                }

                try
                {
                    int copyId = Integer.parseInt(params.getFirst());
                    String otherEntry = data.get(copyId);

                    LOGGER.debug("applyRemap():3: COPY [{}/{}] apply --> [{}/{}]", copyId, otherEntry, i, entry);
                    data.set(i, otherEntry);
                    remapList.setRemap(i, new CSVRemap(i, RemapType.NONE));
                }
                catch (Exception err)
                {
                    LOGGER.warn("applyRemap():3: COPY Exception; {}", err.getMessage());
                    return null;
                }
            }
            else
            {
                Pair<Boolean, String> resultEach = this.applyRemapEach(remap, entry);

                if (resultEach == null || resultEach.getRight() == null)
                {
                    LOGGER.warn("applyRemap():3: Error; ResultEach at pos [{}] is empty!", i);
                    resultEach = Pair.of(false, entry);
                }

                if (resultEach.getLeft())
                {
                    exclude = true;
                }

                //LOGGER.debug("[{}] OUT:2: [{}]", i, resultEach.getRight());
                result.set(i, resultEach.getRight());
            }
        }

        LOGGER.debug("applyRemap(): Post pass 3 data size [{}], result size [{}]", data.size(), result.size());

        if (result.size() > data.size())
        {
            LOGGER.warn("applyRemap(): Excess Results detected!");
        }

        return Pair.of(exclude, result);
    }

    private Pair<Boolean, String> applyRemapEach(@Nonnull CSVRemap remap, String data)
    {
        List<String> params = remap.getParams();
        String result = null;
        boolean exclude = false;

        if (data == null)
        {
            data = "";
        }

        switch (remap.getType())
        {
            case EMPTY ->
            {
                return Pair.of(false, "");
            }
            case PAD ->
            {
                if (params == null || params.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): PAD error; params are empty");
                    return Pair.of(false, data);
                }

                try
                {
                    int count = Integer.parseInt(params.getFirst());

                    if (params.size() > 1)
                    {
                        result = StringUtils.leftPad(data, count, params.get(1));
                    }
                    else if (params.size() == 1)
                    {
                        result = StringUtils.leftPad(data, count);
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
            case TRUNCATE ->
            {
                if (params == null || params.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): TRUNCATE error; params are empty");
                    return Pair.of(false, data);
                }

                try
                {
                    int length = Integer.parseInt(params.getFirst());

                    if (data.length() > length)
                    {
                        result = data.substring(0, length);
                    }
                    else
                    {
                        result = data;
                    }
                }
                catch (NumberFormatException err)
                {
                    LOGGER.warn("applyRemapEach(): TRUNCATE error; {}", err.getMessage());
                }
            }
            case STATIC ->
            {
                if (params == null || params.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): STATIC error; params are empty");
                    return Pair.of(false, data);
                }

                if (params.size() > 1)
                {
                    boolean found = false;

                    // Allow for multi-matching
                    for (int i = 0; i < params.size(); i++)
                    {
                        if (data.equals(params.get(i)))
                        {
                            i++;
                            result = params.get(i);
                            found = true;
                        }
                    }

                    if (!found)
                    {
                        result = data;
                    }
                }
                else
                {
                    result = params.getFirst();
                }
            }
            case IF_EMPTY ->
            {
                if (data.isEmpty())
                {
                    if (params == null || params.isEmpty())
                    {
                        LOGGER.warn("applyRemapEach(): STATIC error; params are empty");
                        return Pair.of(false, data);
                    }

                    result = params.getFirst();
                }
                else if (remap.getSubRemap() != null)
                {
                    remap = remap.setSubRemap(null);
                    result = data;
                }
                else
                {
                    result = data;
                }
            }
            case INCLUDE ->
            {
                if (params == null || params.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): INCLUDE error; params are empty");
                    return Pair.of(false, data);
                }

                if (params.size() > 1)
                {
                    boolean matched = false;

                    for (String param : params)
                    {
                        if (data.equals(param))
                        {
                            matched = true;
                            break;
                        }
                    }

                    if (!matched)
                    {
                        exclude = true;
                    }
                }
            }
            case EXCLUDE ->
            {
                if (params == null || params.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): EXCLUDE error; params are empty");
                    return Pair.of(false, data);
                }

                if (params.size() > 1)
                {
                    // Allow for multi-matching
                    for (String param : params)
                    {
                        if (data.equals(param))
                        {
                            exclude = true;
                            break;
                        }
                    }
                }
            }
            case INCLUDE_REGEX ->
            {
                if (params == null || params.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): INCLUDE_REGEX error; params are empty");
                    return Pair.of(false, data);
                }

                Pattern pattern = Pattern.compile(params.getFirst());
                Matcher matcher = pattern.matcher(data);

                if (!matcher.matches())
                {
                    exclude = true;
                }
            }
            case EXCLUDE_REGEX ->
            {
                if (params == null || params.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): EXCLUDE_REGEX error; params are empty");
                    return Pair.of(false, data);
                }

                Pattern pattern = Pattern.compile(params.getFirst());
                Matcher matcher = pattern.matcher(data);

                if (matcher.matches())
                {
                    exclude = true;
                }
            }
            case DATE ->
            {
                if (params == null || params.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): DATE error; params are empty");
                    return Pair.of(false, data);
                }

                if (params.size() < 2)
                {
                    LOGGER.warn("applyRemapEach(): DATE error; params of 2 not satisfied (Found {})", params.size());
                    return Pair.of(false, data);
                }

                try
                {
                    SimpleDateFormat inFmt = new SimpleDateFormat(params.getFirst());
                    SimpleDateFormat outFmt = new SimpleDateFormat(params.get(1));
                    Date date = inFmt.parse(data);

                    result = outFmt.format(date);
                }
                catch (Exception err)
                {
                    LOGGER.warn("applyRemapEach(): DATE error; {}", err.getMessage());
                }
            }
            case DATE_NOW ->
            {
                if (params == null || params.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): DATE_NOW error; params are empty");
                    return Pair.of(false, data);
                }

                try
                {
                    Date now = new Date(System.currentTimeMillis());
                    SimpleDateFormat outFmt = new SimpleDateFormat(params.getFirst());
                    result = outFmt.format(now);
                }
                catch (Exception err)
                {
                    LOGGER.warn("applyRemapEach(): DATE_NOW error; {}", err.getMessage());
                }
            }
        }

        if (remap.getSubRemap() != null)
        {
            Pair<Boolean, String> subPair = this.applySubRemapNested(remap, result);

            if (subPair == null || subPair.getRight() == null)
            {
                LOGGER.warn("applyRemapEach(): SubRemap error; results are empty!");
                return Pair.of(exclude, result != null ? result : data);
            }
            else if (subPair.getLeft())
            {
                exclude = true;
            }

            result = subPair.getRight();
        }

        return Pair.of(exclude, result != null ? result : data);
    }

    private Pair<Boolean, String> applySubRemapNested(@Nonnull CSVRemap remap, String data)
    {
        if (remap.getSubRemap() != null)
        {
            Pair<Boolean, String> resultEach = this.applyRemapEach(remap.getSubRemap(), data);
            boolean exclude = false;

            if (resultEach == null || resultEach.getRight() == null)
            {
                LOGGER.warn("applySubRemapNested(): Error; ResultEach is empty!");
                resultEach = Pair.of(false, data);
            }

            if (resultEach.getLeft())
            {
                exclude = true;
            }

            LOGGER.debug("applySubRemapNested(): Result: [{}]", resultEach.getRight());
            return Pair.of(exclude, resultEach.getRight());
        }

        return Pair.of(false, data);
    }

    private String applyIfStaticEach(String orig, String target, List<String> conditionalPairs)
    {
//        LOGGER.debug("applyIfStaticEach(): orig: [{}], target: [{}], conditional size [{}]", orig, target, conditionalPairs.size());

        // Skip first Entry
        for (int i = 1; i < conditionalPairs.size(); i++)
        {
            if (i % 2 != 0 && // if Odd & only process if i+1 < size()
                (i+1) < conditionalPairs.size())
            {
                String condition = conditionalPairs.get(i);
                String value = conditionalPairs.get(i+1);

//                LOGGER.warn("applyIfStaticEach(): TEST [{}] vs [{}]", target, condition);

                if (target.equalsIgnoreCase(condition))
                {
                    LOGGER.debug("applyIfStaticEach(): RETURN-MATCH [{}]", value);
                    return value;
                }
            }
        }

        LOGGER.debug("applyIfStaticEach(): RETURN-ORIG [{}]", orig);
        return orig;
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
