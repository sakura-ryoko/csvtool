package csvtool.operation;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
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

    protected @Nullable FileCache readFileNoHeaders(String file, boolean ignoreQuotes, boolean dump)
    {
        LOGGER.debug("readFileNoHeaders(): Reading file [{}] ...", file);

        try (CSVWrapper wrapper = new CSVWrapper(file))
        {
            if (wrapper.read(false, ignoreQuotes))
            {
                LOGGER.info("readFileNoHeaders(): File read!");

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
            LOGGER.error("readFileNoHeaders(): Exception reading file! Error: {}", e.getMessage());
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

    protected List<String> squashLines(List<String> line1, List<String> line2)
    {
        if (line1.size() != line2.size())
        {
            LOGGER.error("squashLines(): Error; line1.size != line2.size!");
            return line1;
        }

        List<String> result = new ArrayList<>(line1);

        for (int i = 0; i < line1.size(); i++)
        {
            String left = line1.get(i);
            String right = line2.get(i);

            // Squash values; meaning if it exists in one and not the other; use it.
            // If data exists in both; use the first (left) entry.
            if (left.isEmpty() && !right.isEmpty())
            {
                result.set(i, right);
            }
            else if (!left.isEmpty() && right.isEmpty())
            {
                result.set(i, left);
            }
        }

        return result;
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
            case EMPTY -> result = "";
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
            case REPLACE ->
            {
                if (params == null || params.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): REPLACE error; params are empty");
                    return Pair.of(false, data);
                }

                String obj = params.getFirst();
                String token = "";

                if (params.size() > 1)
                {
                    token = params.get(1);
                }

                if (data.contains(obj))
                {
                    result = data.replace(obj, token);
                }
                else
                {
                    result = data;
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

                            if (i < params.size())
                            {
                                result = params.get(i);
                                found = true;
                            }
                            else
                            {
                                LOGGER.warn("applyRemapEach(): STATIC error; next param is out of bounds.");
                                break;
                            }
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
            case IF_STATIC ->
            {
                if (params == null || params.isEmpty() || params.size() < 3)
                {
                    LOGGER.warn("applyRemapEach(): IF_STATIC error; Invalid parameters given");
                    return Pair.of(false, data);
                }

                try
                {
                    int fieldId = Integer.parseInt(params.getFirst());

                    if (fieldId < 0 || fieldId > row.size())
                    {
                        LOGGER.warn("applyRemapEach(): IF_STATIC error; fieldId is out of bounds.");
                        return Pair.of(false, data);
                    }

                    String otherEntry = row.get(fieldId);
//                    LOGGER.debug("applyRemapEach(): IF_STATIC test [{}] // otherField [{}/{}]", data, fieldId, otherEntry);
                    String ifResult = this.applyIfStaticEach(data, otherEntry, params);

                    if (!ifResult.equalsIgnoreCase(data))
                    {
//                        LOGGER.debug("applyRemapEach(): IF_STATIC applied to [{}]", data);
                        result = ifResult;
                    }
                    else
                    {
                        LOGGER.debug("applyRemapEach(): IF_STATIC no match found!");
                    }
                }
                catch (Exception err)
                {
                    LOGGER.warn("applyRemapEach(): IF_STATIC Exception; {}", err.getLocalizedMessage());
                    return Pair.of(false, data);
                }
            }
            case APPEND ->
            {
                if (params == null || params.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): APPEND error; params are empty");
                    return Pair.of(false, data);
                }

                String token = " ";

                if (params.size() > 1)
                {
                    token = params.get(1);
                }

                result = data + token + params.getFirst();
            }
            case SANITIZE ->
            {
                if (!data.isEmpty())
                {
                    result = StringUtils.sanitizeString(data);
                }
                else
                {
                    result = data;
                }
            }
            case COPY ->
            {
                if (params == null || params.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): COPY error; params are empty");
                    return Pair.of(false, data);
                }

                try
                {
                    int param = Integer.parseInt(params.getFirst());

                    if (param > row.size())
                    {
                        LOGGER.warn("applyRemapEach(): COPY error; params are out of bounds");
                        return Pair.of(false, data);
                    }

                    result = row.get(param);
                }
                catch (NumberFormatException err)
                {
                    LOGGER.warn("applyRemapEach(): COPY error; params are invalid; {}", err.getLocalizedMessage());
                    return Pair.of(false, data);
                }
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
                            String token = " ";

                            if (params.size() > 1)
                            {
                                token = params.get(1);
                            }

                            builder.append(token).append(row.get(obj));
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
            case PHONE_NUMBER ->
            {
                if (!data.isEmpty())
                {
                    PhoneNumberUtil parser = PhoneNumberUtil.getInstance();

                    try
                    {
                        Phonenumber.PhoneNumber number = parser.parse(data, "US");
                        PhoneNumberUtil.PhoneNumberFormat format;

                        if (params != null && !params.isEmpty())
                        {
                            switch (params.getFirst().toUpperCase())
                            {
                                case "INTERNATIONAL", "INTL" -> format = PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL;
                                case "NATIONAL", "NAT" -> format = PhoneNumberUtil.PhoneNumberFormat.NATIONAL;
                                case "E164", "E.164" -> format = PhoneNumberUtil.PhoneNumberFormat.E164;
                                default -> format = PhoneNumberUtil.PhoneNumberFormat.RFC3966;
                            }
                        }
                        else
                        {
                            format = PhoneNumberUtil.PhoneNumberFormat.RFC3966;
                        }

                        result = parser.format(number, format);
                    }
                    catch (NumberParseException err)
                    {
                        LOGGER.warn("applyRemapEach(): PHONE_NUMBER error; invalid format; {}", err.getLocalizedMessage());
                        return Pair.of(false, data);
                    }
                }
                else
                {
                    result = data;
                }
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
            case IF_EMPTY_FIELD ->
            {
                if (params == null || params.isEmpty() || params.size() < 2)
                {
                    LOGGER.warn("applyRemapEach(): IF_EMPTY_FIELD error; params are empty");
                    return Pair.of(false, data);
                }

                int fieldNum;

                try
                {
                    fieldNum = Integer.parseInt(params.getFirst());

                    if (fieldNum < 0 || fieldNum > row.size())
                    {
                        LOGGER.warn("applyRemapEach(): IF_EMPTY_FIELD error; fieldNum is out of range.");
                        return Pair.of(false, data);
                    }
                }
                catch (NumberFormatException err)
                {
                    LOGGER.warn("applyRemapEach(): IF_EMPTY_FIELD error; exception parsing fieldNum; {}", err.getLocalizedMessage());
                    return Pair.of(false, data);
                }

                if (row.get(fieldNum).isEmpty())
                {
                    result = params.get(1);
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
            case IF_EMPTY_COPY ->
            {
                if (params == null || params.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): IF_EMPTY_COPY error; params are empty");
                    return Pair.of(false, data);
                }

                if (data.isEmpty())
                {
                    int fieldNum;

                    try
                    {
                        fieldNum = Integer.parseInt(params.getFirst());

                        if (fieldNum < 0 || fieldNum > row.size())
                        {
                            LOGGER.warn("applyRemapEach(): IF_EMPTY_COPY error; fieldNum is out of range.");
                            return Pair.of(false, data);
                        }
                    }
                    catch (NumberFormatException err)
                    {
                        LOGGER.warn("applyRemapEach(): IF_EMPTY_COPY error; exception parsing fieldNum; {}", err.getLocalizedMessage());
                        return Pair.of(false, data);
                    }

                    result = row.get(fieldNum);
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
            case IF_EQUAL ->
            {
                if (params == null || params.isEmpty() || params.size() < 2)
                {
                    LOGGER.warn("applyRemapEach(): IF_EQUAL error; params are empty or less than 2.");
                    return Pair.of(false, data);
                }

                int fieldNum;

                try
                {
                    fieldNum = Integer.parseInt(params.getFirst());

                    if (fieldNum < 0 || fieldNum > row.size())
                    {
                        LOGGER.warn("applyRemapEach(): IF_EQUAL error; fieldNum is out of range.");
                        return Pair.of(false, data);
                    }
                }
                catch (NumberFormatException err)
                {
                    LOGGER.warn("applyRemapEach(): IF_EQUAL error; exception parsing fieldNum; {}", err.getLocalizedMessage());
                    return Pair.of(false, data);
                }

                String otherField = row.get(fieldNum);

                if (data.isEmpty() || otherField.isEmpty())
                {
                    result = data;
                }
                else if (data.equals(otherField))
                {
                    result = params.get(1);
                }
                else if (params.size() > 2)
                {
                    result = params.get(2);
                }
                else
                {
                    result = data;
                }
            }
            case IF_EQUAL_COPY ->
            {
                if (params == null || params.isEmpty() || params.size() < 2)
                {
                    LOGGER.warn("applyRemapEach(): IF_EQUAL_COPY error; params are empty or less than 2.");
                    return Pair.of(false, data);
                }

                int fieldNum1;
                int fieldNum2;

                try
                {
                    fieldNum1 = Integer.parseInt(params.getFirst());

                    if (fieldNum1 < 0 || fieldNum1 > row.size())
                    {
                        LOGGER.warn("applyRemapEach(): IF_EQUAL_COPY error; fieldNum1 is out of range.");
                        return Pair.of(false, data);
                    }
                }
                catch (NumberFormatException err)
                {
                    LOGGER.warn("applyRemapEach(): IF_EQUAL_COPY error; exception parsing fieldNum1; {}", err.getLocalizedMessage());
                    return Pair.of(false, data);
                }

                try
                {
                    fieldNum2 = Integer.parseInt(params.get(1));

                    if (fieldNum2 < 0 || fieldNum2 > row.size())
                    {
                        LOGGER.warn("applyRemapEach(): IF_EQUAL_COPY error; fieldNum2 is out of range.");
                        return Pair.of(false, data);
                    }
                }
                catch (NumberFormatException err)
                {
                    LOGGER.warn("applyRemapEach(): IF_EQUAL_COPY error; exception parsing fieldNum2; {}", err.getLocalizedMessage());
                    return Pair.of(false, data);
                }

                if (data.equals(row.get(fieldNum1)))
                {
                    result = row.get(fieldNum2);
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
            case IF_EQUAL_APPEND ->
            {
                if (params == null || params.isEmpty() || params.size() < 2)
                {
                    LOGGER.warn("applyRemapEach(): IF_EQUAL_APPEND error; params are empty or less than 2.");
                    return Pair.of(false, data);
                }

                int fieldNum;

                try
                {
                    fieldNum = Integer.parseInt(params.getFirst());

                    if (fieldNum < 0 || fieldNum > row.size())
                    {
                        LOGGER.warn("applyRemapEach(): IF_EQUAL_APPEND error; fieldNum is out of range.");
                        return Pair.of(false, data);
                    }
                }
                catch (NumberFormatException err)
                {
                    LOGGER.warn("applyRemapEach(): IF_EQUAL_APPEND error; exception parsing fieldNum; {}", err.getLocalizedMessage());
                    return Pair.of(false, data);
                }

                if (data.equals(row.get(fieldNum)))
                {
                    String token = " ";

                    if (params.size() > 2)
                    {
                        token = params.get(2);
                    }

                    result = data + token + params.get(1);
                }
                else
                {
                    result = data;
                }
            }
            case IF_EQUAL_PREFIX ->
            {
                if (params == null || params.isEmpty() || params.size() < 2)
                {
                    LOGGER.warn("applyRemapEach(): IF_EQUAL_PREFIX error; params are empty or less than 2.");
                    return Pair.of(false, data);
                }

                int fieldNum;

                try
                {
                    fieldNum = Integer.parseInt(params.getFirst());

                    if (fieldNum < 0 || fieldNum > row.size())
                    {
                        LOGGER.warn("applyRemapEach(): IF_EQUAL_PREFIX error; fieldNum is out of range.");
                        return Pair.of(false, data);
                    }
                }
                catch (NumberFormatException err)
                {
                    LOGGER.warn("applyRemapEach(): IF_EQUAL_PREFIX error; exception parsing fieldNum; {}", err.getLocalizedMessage());
                    return Pair.of(false, data);
                }

                if (data.equals(row.get(fieldNum)))
                {
                    String token = " ";

                    if (params.size() > 2)
                    {
                        token = params.get(2);
                    }

                    result = params.get(1) + token + data;
                }
                else
                {
                    result = data;
                }
            }
            case IF_FIELDS_EQUAL ->
            {
                if (params == null || params.isEmpty() || params.size() < 4)
                {
                    LOGGER.warn("applyRemapEach(): IF_FIELDS_EQUAL error; params are empty or less than 4.");
                    return Pair.of(false, data);
                }

                int field1;
                int field2;

                try
                {
                    field1 = Integer.parseInt(params.getFirst());

                    if (field1 < 0 || field1 > row.size())
                    {
                        LOGGER.warn("applyRemapEach(): IF_FIELDS_EQUAL error; field1 is out of range.");
                        return Pair.of(false, data);
                    }
                }
                catch (NumberFormatException err)
                {
                    LOGGER.warn("applyRemapEach(): IF_FIELDS_EQUAL error; exception parsing field1; {}", err.getLocalizedMessage());
                    return Pair.of(false, data);
                }

                try
                {
                    field2 = Integer.parseInt(params.get(1));

                    if (field2 < 0 || field2 > row.size())
                    {
                        LOGGER.warn("applyRemapEach(): IF_FIELDS_EQUAL error; field2 is out of range.");
                        return Pair.of(false, data);
                    }
                }
                catch (NumberFormatException err)
                {
                    LOGGER.warn("applyRemapEach(): IF_FIELDS_EQUAL error; exception parsing field2; {}", err.getLocalizedMessage());
                    return Pair.of(false, data);
                }

                if (row.get(field1).equals(row.get(field2)))
                {
                    result = params.get(3);
                }
                else
                {
                    result = params.get(4);
                }
            }
            case IF_RANGE ->
            {
                if (params == null || params.isEmpty() || params.size() < 4)
                {
                    LOGGER.warn("applyRemapEach(): IF_RANGE error; params are empty or less than 4.");
                    return Pair.of(false, data);
                }

                int dataTest;

                try
                {
                    dataTest = Integer.parseInt(data);
                }
                catch (Exception err)
                {
                    LOGGER.warn("applyRemapEach(): IF_RANGE error; tested data is not a number; {}", err.getLocalizedMessage());
                    return Pair.of(false, data);
                }

                int swPos = 0;
                int minRange = Integer.MIN_VALUE;
                int maxRange = Integer.MAX_VALUE;
                String matchValue = "";
                String elseValue = data;

                for (int i = 0; i < params.size(); i++)
                {
                    LOGGER.debug("applyRemapEach(): IF_RANGE[{}]; min: [{}], max: [{}], match: [{}], else: [{}]", i, minRange, maxRange, matchValue, elseValue);

                    if (swPos > 2)
                    {
                        // Next set of params.
                        if ((params.size() - i) > 2)
                        {
                            swPos = 0;

                            if (matchValue.isEmpty() || minRange == Integer.MIN_VALUE || maxRange == Integer.MAX_VALUE)
                            {
                                continue;
                            }

                            LOGGER.debug("applyRemapEach(): IF_RANGE; TEST --> min: [{}], max: [{}], data: [{}], match: [{}], else: [{}]", minRange, maxRange, dataTest, matchValue, elseValue);

                            // Perform test
                            if (dataTest >= minRange && dataTest <= maxRange)
                            {
                                return Pair.of(false, matchValue);
                            }
                            else if (!elseValue.matches(data))
                            {
                                return Pair.of(false, elseValue);
                            }
                        }
                        else
                        {
                            // Defaults to "data"
                            elseValue = params.get(i);
                            break;
                        }
                    }

                    try
                    {
                        switch (swPos)
                        {
                            case 0 -> minRange = Integer.parseInt(params.get(i));
                            case 1 -> maxRange = Integer.parseInt(params.get(i));
                            case 2 -> matchValue = params.get(i);
                        }

                        swPos++;
                    }
                    catch (Exception err)
                    {
                        LOGGER.warn("applyRemapEach(): IF_RANGE error; exception processing params; {}", err.getLocalizedMessage());
                        return Pair.of(false, data);
                    }
                }

                LOGGER.debug("applyRemapEach(): IF_RANGE; DEFAULT --> min: [{}], max: [{}], data: [{}], match: [{}], else: [{}]", minRange, maxRange, dataTest, matchValue, elseValue);

                if (!matchValue.isEmpty() && minRange > Integer.MIN_VALUE && maxRange < Integer.MAX_VALUE)
                {
                    // Perform test
                    if (dataTest >= minRange && dataTest <= maxRange)
                    {
                        return Pair.of(false, matchValue);
                    }
                }

                // If we got here, return elseValue.
                if (!elseValue.matches(data))
                {
                    return Pair.of(false, elseValue);
                }
                else
                {
                    return Pair.of(false, data);
                }
            }
            case IF_DATE_RANGE ->
            {
                if (params == null || params.isEmpty() || params.size() < 8)
                {
                    LOGGER.warn("applyRemapEach(): IF_DATE_RANGE error; params are empty or less than 8.");
                    return Pair.of(false, data);
                }

                SimpleDateFormat fmtData = new SimpleDateFormat(params.getFirst());
                SimpleDateFormat fmtMin = new SimpleDateFormat(params.get(2));
                SimpleDateFormat fmtMax = new SimpleDateFormat(params.get(4));
                int minField;
                int maxField;

                try
                {
                    minField = Integer.parseInt(params.get(1));
                }
                catch (NumberFormatException err)
                {
                    LOGGER.warn("applyRemapEach(): IF_DATE_RANGE error; Failed to parse minField; {}.", err.getLocalizedMessage());
                    return Pair.of(false, data);
                }

                try
                {
                    maxField = Integer.parseInt(params.get(3));
                }
                catch (NumberFormatException err)
                {
                    LOGGER.warn("applyRemapEach(): IF_DATE_RANGE error; Failed to parse maxField; {}.", err.getLocalizedMessage());
                    return Pair.of(false, data);
                }

                if (minField < 0 || minField > row.size())
                {
                    LOGGER.warn("applyRemapEach(): IF_DATE_RANGE error; minField '{}' is out of range!.", minField);
                    return Pair.of(false, data);
                }

                if (maxField < 0 || maxField > row.size())
                {
                    LOGGER.warn("applyRemapEach(): IF_DATE_RANGE error; maxField '{}' is out of range!.", maxField);
                    return Pair.of(false, data);
                }

                LocalDateTime dateData;
                LocalDateTime dateMin;
                LocalDateTime dateMax;

                try
                {
                    dateData = fmtData.parse(data).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                }
                catch (Exception err)
                {
                    LOGGER.warn("applyRemapEach(): IF_DATE_RANGE error; exception parsing data date; {}", err.getLocalizedMessage());
                    return Pair.of(false, data);
                }

                try
                {
                    dateMin = fmtMin.parse(row.get(minField)).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    dateMax = fmtMax.parse(row.get(maxField)).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                }
                catch (Exception err)
                {
                    LOGGER.warn("applyRemapEach(): IF_DATE_RANGE error; exception parsing min/max dates; {}", err.getLocalizedMessage());
                    return Pair.of(false, params.get(8));
                }

                if (dateData.isBefore(dateMin))
                {
                    result = params.get(5);
                }
                else if (dateData.isAfter(dateMax))
                {
                    result = params.get(7);
                }
                else
                {
                    result = params.get(6);
                }
            }
            case IF_NUMBER_EMPTY ->
            {
                try
                {
                    Long.parseLong(data);
                    result = "";
                }
                catch (NumberFormatException ignored)
                {
                    result = data;
                }
            }
            case IF_FLOAT_EMPTY ->
            {
                try
                {
                    Double.parseDouble(data);
                    result = "";
                }
                catch (NumberFormatException ignored)
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
            case NOT_EMPTY_FIELD ->
            {
                if (params == null || params.isEmpty() || params.size() < 2)
                {
                    LOGGER.warn("applyRemapEach(): NOT_EMPTY_FIELD error; params are empty");
                    return Pair.of(false, data);
                }

                int fieldNum;

                try
                {
                    fieldNum = Integer.parseInt(params.getFirst());

                    if (fieldNum < 0 || fieldNum > row.size())
                    {
                        LOGGER.warn("applyRemapEach(): NOT_EMPTY_FIELD error; fieldNum is out of range.");
                        return Pair.of(false, data);
                    }
                }
                catch (NumberFormatException err)
                {
                    LOGGER.warn("applyRemapEach(): NOT_EMPTY_FIELD error; exception parsing fieldNum; {}", err.getLocalizedMessage());
                    return Pair.of(false, data);
                }

                if (row.get(fieldNum).isEmpty())
                {
                    if (remap.getSubRemap() != null)
                    {
                        remap = remap.setSubRemap(null);
                    }

                    result = "";
                }
                else
                {
                    result = params.get(1);
                }
            }
            case NOT_EMPTY_APPEND ->
            {
                if (params == null || params.isEmpty() || params.size() < 2)
                {
                    LOGGER.warn("applyRemapEach(): NOT_EMPTY_APPEND error; params are empty");
                    return Pair.of(false, data);
                }

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
                    int fieldNum;

                    try
                    {
                        fieldNum = Integer.parseInt(params.getFirst());

                        if (fieldNum < 0 || fieldNum > row.size())
                        {
                            LOGGER.warn("applyRemapEach(): NOT_EMPTY_APPEND error; fieldNum is out of range.");
                            return Pair.of(false, data);
                        }
                    }
                    catch (NumberFormatException err)
                    {
                        LOGGER.warn("applyRemapEach(): NOT_EMPTY_APPEND error; exception parsing fieldNum; {}", err.getLocalizedMessage());
                        return Pair.of(false, data);
                    }

                    String otherField = row.get(fieldNum);
                    if (data.equalsIgnoreCase(otherField) || otherField.isEmpty())
                    {
                        return Pair.of(false, data);
                    }

                    String value = params.get(1);
                    String token = " ";

                    if (params.size() > 2)
                    {
                        token = params.get(2);
                    }

                    result = data + token + value;
                }
            }
            case NOT_EMPTY_COPY ->
            {
                if (params == null || params.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): NOT_EMPTY_COPY error; params are empty");
                    return Pair.of(false, data);
                }

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
                    int fieldNum;

                    try
                    {
                        fieldNum = Integer.parseInt(params.getFirst());

                        if (fieldNum < 0 || fieldNum > row.size())
                        {
                            LOGGER.warn("applyRemapEach(): NOT_EMPTY_COPY error; fieldNum is out of range.");
                            return Pair.of(false, data);
                        }
                    }
                    catch (NumberFormatException err)
                    {
                        LOGGER.warn("applyRemapEach(): NOT_EMPTY_COPY error; exception parsing fieldNum; {}", err.getLocalizedMessage());
                        return Pair.of(false, data);
                    }

                    result = row.get(fieldNum);
                }
            }
            case NOT_EMPTY_MERGE ->
            {
                if (params == null || params.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): NOT_EMPTY_MERGE error; params are empty");
                    return Pair.of(false, data);
                }

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
                    int fieldNum;

                    try
                    {
                        fieldNum = Integer.parseInt(params.getFirst());

                        if (fieldNum < 0 || fieldNum > row.size())
                        {
                            LOGGER.warn("applyRemapEach(): NOT_EMPTY_MERGE error; fieldNum is out of range.");
                            return Pair.of(false, data);
                        }
                    }
                    catch (NumberFormatException err)
                    {
                        LOGGER.warn("applyRemapEach(): NOT_EMPTY_MERGE error; exception parsing fieldNum; {}", err.getLocalizedMessage());
                        return Pair.of(false, data);
                    }

                    String otherField = row.get(fieldNum);

                    if (data.equalsIgnoreCase(otherField) || otherField.isEmpty())
                    {
                        return Pair.of(false, data);
                    }

                    String token = " ";

                    if (params.size() > 1)
                    {
                        token = params.get(1);
                    }

                    result = data + token + otherField;
                }
            }
            case NOT_EMPTY_PREFIX ->
            {
                if (params == null || params.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): NOT_EMPTY_PREFIX error; params are empty");
                    return Pair.of(false, data);
                }

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
                    String token = " ";

                    if (params.size() > 1)
                    {
                        token = params.get(1);
                    }

                    result = params.getFirst() + token + data;
                }
            }
            case NOT_NUMBER_EMPTY ->
            {
                try
                {
                    Long.parseLong(data);
                    result = data;
                }
                catch (NumberFormatException ignored)
                {
                    result = "";
                }
            }
            case NOT_FLOAT_EMPTY ->
            {
                try
                {
                    Double.parseDouble(data);
                    result = data;
                }
                catch (NumberFormatException ignored)
                {
                    result = "";
                }
            }
            case PREFIX ->
            {
                if (params == null || params.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): PREFIX error; params are empty");
                    return Pair.of(false, data);
                }

                String token = " ";

                if (params.size() > 1)
                {
                    token = params.get(1);
                }

                result = params.getFirst() + token + data;
            }
            case INCLUDE ->
            {
                if (params == null || params.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): INCLUDE error; params are empty");
                    return Pair.of(false, data);
                }

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
            case EXCLUDE ->
            {
                if (params == null || params.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): EXCLUDE error; params are empty");
                    return Pair.of(false, data);
                }

                boolean matched = false;

                // Allow for multi-matching
                for (String param : params)
                {
                    if (data.equals(param))
                    {
                        matched = true;
                        break;
                    }
                }

                if (matched)
                {
                    exclude = true;
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

                if (data.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): DATE error; data is empty");
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

                if (data.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): DATE_NOW error; data is empty");
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
            case DATE_YEARS ->
            {
                if (params == null || params.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): DATE_YEARS error; params are empty");
                    return Pair.of(false, data);
                }

                if (data.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): DATE_YEARS error; data is empty");
                    return Pair.of(false, data);
                }

                try
                {
                    SimpleDateFormat fmt = new SimpleDateFormat(params.getFirst());
                    Period duration = Period.between(fmt.parse(data)
                                    .toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate(),
                            LocalDate.now()
                    );

                    result = String.valueOf(duration.getYears());
                }
                catch (Exception err)
                {
                    LOGGER.warn("applyRemapEach(): DATE_YEARS error; {}", err.getMessage());
                }
            }
            case DATE_MONTHS ->
            {
                if (params == null || params.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): DATE_MONTHS error; params are empty");
                    return Pair.of(false, data);
                }

                if (data.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): DATE_MONTHS error; data is empty");
                    return Pair.of(false, data);
                }

                try
                {
                    SimpleDateFormat fmt = new SimpleDateFormat(params.getFirst());
                    Period duration = Period.between(fmt.parse(data)
                                    .toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate(),
                            LocalDate.now()
                    );

                    result = String.valueOf(duration.getMonths());
                }
                catch (Exception err)
                {
                    LOGGER.warn("applyRemapEach(): DATE_MONTHS error; {}", err.getMessage());
                }
            }
            case DATE_DAYS ->
            {
                if (params == null || params.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): DATE_DAYS error; params are empty");
                    return Pair.of(false, data);
                }

                if (data.isEmpty())
                {
                    LOGGER.warn("applyRemapEach(): DATE_DAYS error; data is empty");
                    return Pair.of(false, data);
                }

                try
                {
                    SimpleDateFormat fmt = new SimpleDateFormat(params.getFirst());
                    Period duration = Period.between(fmt.parse(data)
                                    .toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate(),
                            LocalDate.now()
                    );

                    result = String.valueOf(duration.getDays());
                }
                catch (Exception err)
                {
                    LOGGER.warn("applyRemapEach(): DATE_DAYS error; {}", err.getMessage());
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

    protected String applyIfStaticEach(String orig, String target, List<String> conditionalPairs)
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

    protected CSVHeader expandHeaders(CSVHeader header, int size)
    {
        CSVHeader result = CSVHeader.copy(header);

        if (size <= result.size())
        {
            return result;
        }

        for (int i = result.size(); i < size; i++)
        {
            result = result.add("FIELD_"+i);
        }

        return result;
    }

    public void clear() { }
}
