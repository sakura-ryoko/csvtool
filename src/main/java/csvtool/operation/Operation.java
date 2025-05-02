package csvtool.operation;

import csvtool.data.Context;
import csvtool.data.FileCache;
import csvtool.enums.Operations;
import csvtool.header.CSVHeader;
import csvtool.header.CSVRemap;
import csvtool.utils.CSVWrapper;
import csvtool.utils.LogWrapper;
import csvtool.utils.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Operation
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());

    public Operation(Operations op)
    {
        LOGGER.debug("new abstract Operation for op [{}]", op.getName());
    }

    public boolean runOperation(Context ctx)
    {
        LOGGER.debug("runOperation() [SUPER]");
        return false;
    }

    public void displayHelp() { }

    protected void toggleDebug(boolean toggle)
    {
        LOGGER.toggleDebug(toggle);
    }

    protected void toggleQuiet(boolean toggle)
    {
        LOGGER.toggleQuiet(toggle);
    }

    protected void toggleAnsiColor(boolean toggle)
    {
        LOGGER.toggleAnsiColor(toggle);
    }

    protected @Nullable FileCache readFile(String file)
    {
        return this.readFile(file, true, false);
    }

    protected @Nullable FileCache readFile(String file, boolean ignoreQuotes, boolean dump)
    {
        LOGGER.debug("readFile(): Reading file [{}] ...", file);

        try (CSVWrapper wrapper = new CSVWrapper(file))
        {
            if (wrapper.read(true, ignoreQuotes))
            {
                LOGGER.info("readFile(): File read!");

                if (dump)
                {
                    dumpFile(wrapper);
                }

                FileCache cache = new FileCache();
                cache.copyFile(wrapper);
                //wrapper.close();
                return cache;
            }
        }
        catch (Exception e)
        {
            LOGGER.error("readFile(): Exception reading file! Error: {}", e.getMessage());
        }

        return null;
    }

    protected @Nullable FileCache readFileHeadersOnly(String file, boolean ignoreQuotes, boolean dump)
    {
        LOGGER.debug("readFileHeadersOnly(): Reading file [{}] ...", file);

        try (CSVWrapper wrapper = new CSVWrapper(file))
        {
            if (wrapper.readHeadersOnly(ignoreQuotes))
            {
                LOGGER.info("readFileHeadersOnly(): File read!");

                if (dump)
                {
                    dumpFile(wrapper);
                }

                FileCache cache = new FileCache();
                cache.copyFileHeadersOnly(wrapper);
                //wrapper.close();
                return cache;
            }
        }
        catch (Exception e)
        {
            LOGGER.error("readFileHeadersOnly(): Exception reading file! Error: {}", e.getMessage());
        }

        return null;
    }

    protected void dumpFile(@Nonnull CSVWrapper wrapper)
    {
        LOGGER.debug("dumpFile(): Dump file [{}]:", wrapper.getFile());

        CSVHeader header = wrapper.getHeader();

        if (header == null)
        {
            LOGGER.error("dumpFile(): Header is NULL!");
            return;
        }

        LOGGER.debug("dumpFile(): Header {} // Line Size: [{}]", header.toString(), wrapper.getSize());

        if (wrapper.isEmpty())
        {
            LOGGER.error("dumpFile(): Wrapper is EMPTY!");
            return;
        }

        // Start at Line 1
        for (int i = 1; i < wrapper.getSize(); i++)
        {
            List<String> line = wrapper.getLine(i);

            if (line == null)
            {
                LOGGER.error("dumpFile(): LINE[{}] --> NULL!", i);
                continue;
            }
            else if (line.isEmpty())
            {
                LOGGER.error("dumpFile(): LINE[{}] --> EMPTY!", i);
                continue;
            }

            LOGGER.debug("dumpFile(): LINE[{}] --> {}", i, line.toString());
        }

        LOGGER.debug("dumpFile(): EOF");
    }

    protected boolean compareHeaders(CSVHeader header1, CSVHeader header2)
    {
        if (header1 == null || header2 == null)
        {
            return false;
        }

        LOGGER.debug("compareHeaders(): ...");
        return header1.matches(header2);
    }

    protected boolean writeFile(@Nonnull FileCache FILE, boolean applyQuotes)
    {
        return this.writeFile(FILE, applyQuotes, false, false, null);
    }

    protected boolean writeFile(@Nonnull FileCache FILE, boolean applyQuotes, boolean append)
    {
        return this.writeFile(FILE, applyQuotes, append, false, null);
    }

    protected boolean writeFile(@Nonnull FileCache FILE, boolean applyQuotes, boolean append, boolean dump, @Nullable FileCache APPEND)
    {
        LOGGER.debug("writeFile(): Write file [{}]:", FILE.getFileName());

        try (CSVWrapper wrapper = new CSVWrapper(FILE.getFileName(), false))
        {
            if (wrapper.putAllLines(FILE.getFile(), true))
            {
                if (APPEND != null)
                {
                    if (!appendFile(wrapper, APPEND))
                    {
                        LOGGER.error("writeFile(): Error appending file Cache.");
                        wrapper.close();
                        return false;
                    }
                }

                if (wrapper.write(applyQuotes, append))
                {
                    LOGGER.info("writeFile(): File written!");

                    if (dump)
                    {
                        dumpFile(wrapper);
                    }

                    wrapper.close();
                    return true;
                }
            }
            else
            {
                LOGGER.error("writeFile(): Error copying file Cache to new file.");
                wrapper.close();
            }
        }
        catch (Exception e)
        {
            LOGGER.error("writeFile(): Exception writing file! Error: {}", e.getMessage());
        }

        return false;
    }

    protected boolean appendFile(CSVWrapper wrapper, FileCache FILE)
    {
        if (wrapper == null || FILE == null || FILE.isEmpty() || wrapper.isEmpty())
        {
            return false;
        }

        LOGGER.debug("appendFile(): Appending file to wrapper...");

        for (int i = 1; i < FILE.getFile().size(); i++)
        {
            List<String> entry = FILE.getFile().get(i);

            if (!entry.isEmpty())
            {
                wrapper.putLine(entry);
            }
        }

        return true;
    }

    protected Pair<Boolean, String> applyRemapEach(@Nonnull CSVRemap remap, String data, List<String> row)
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
            case APPEND ->
            {
                if (params == null || params.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): APPEND error; params are empty");
                    return Pair.of(false, data);
                }

                result = data + " " + params.getFirst();
            }
            case MERGE ->
            {
                if (params == null || params.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): MERGE error; params are empty");
                    return Pair.of(false, data);
                }

                StringBuilder builder = new StringBuilder(data);

                for (String param : params)
                {
                    try
                    {
                        int obj = Integer.parseInt(param);

                        if (obj >= 0 && obj < row.size())
                        {
                            builder.append(" ").append(row.get(obj));
                        }
                        else
                        {
                            LOGGER.warn("applyRemapEach(): MERGE error; params are out of bounds");
                            return Pair.of(false, builder.toString());
                        }
                    }
                    catch (Exception e)
                    {
                        LOGGER.warn("applyRemapEach(): MERGE error; params are invalid; {}", e.getLocalizedMessage());
                        return Pair.of(false, builder.toString());
                    }
                }

                result = builder.toString();
            }
            case IF_EMPTY ->
            {
                if (data.isEmpty())
                {
                    if (params == null || params.isEmpty())
                    {
                        LOGGER.warn("applyRemapEach(): IF_EMPTY error; params are empty");
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
            case NOT_EMPTY ->
            {
                if (data.isEmpty())
                {
                    if (remap.getSubRemap() != null)
                    {
                        remap = remap.setSubRemap(null);
                    }

                    result = "";
                }
                else
                {
                    if (params == null || params.isEmpty())
                    {
                        result = data;
                    }
                    else
                    {
                        result = params.getFirst();
                    }
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
            Pair<Boolean, String> subPair = this.applySubRemapNested(remap, result, row);

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

    private Pair<Boolean, String> applySubRemapNested(@Nonnull CSVRemap remap, String data, List<String> row)
    {
        if (remap.getSubRemap() != null)
        {
            Pair<Boolean, String> resultEach = this.applyRemapEach(remap.getSubRemap(), data, row);
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

    public void clear() { }
}
