package csvtool.operation;

import csvtool.data.Context;
import csvtool.data.FileCache;
import csvtool.enums.Operations;
import csvtool.enums.Settings;
import csvtool.header.CSVHeader;
import csvtool.utils.LogWrapper;
import csvtool.utils.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OperationJoin extends Operation implements AutoCloseable
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());

    private FileCache FILE_1;
    private FileCache FILE_2;
    private final FileCache OUT;
    private final FileCache EXCEPTIONS;
    private final List<Integer> matchedOuter;
    private int keyId1;
    private int keyId2;
    private int keyId3;
    private int keyId4;
    private int keyId5;
    private int jKeyId1;
    private int jKeyId2;
    private int jKeyId3;
    private int jKeyId4;
    private int jKeyId5;

    public OperationJoin(Operations op)
    {
        super(op);
        this.FILE_1 = new FileCache();
        this.FILE_2 = new FileCache();
        this.OUT = new FileCache();
        this.EXCEPTIONS = new FileCache();
        this.matchedOuter = new ArrayList<>();
        this.keyId1 = -1;
        this.keyId2 = -1;
        this.keyId3 = -1;
        this.keyId4 = -1;
        this.keyId5 = -1;
        this.jKeyId1 = -1;
        this.jKeyId2 = -1;
        this.jKeyId3 = -1;
        this.jKeyId4 = -1;
        this.jKeyId5 = -1;
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

        if (!ctx.getOpt().hasInput2() || !ctx.getOpt().hasOutput())
        {
            LOGGER.error("runOperation(): Join FAILED, Second input file and an output is required.");
            return false;
        }

        if (!ctx.getOpt().hasKey())
        {
            LOGGER.error("runOperation(): Join FAILED, a key is required.");
            this.clear();
            return false;
        }

        if (!ctx.getOpt().hasJoinKey())
        {
            LOGGER.error("runOperation(): Join FAILED, a join key is required.");
            this.clear();
            return false;
        }

        LOGGER.debug("runOperation(): --> JOIN [{}] + [{}] into [{}].", ctx.getInputFile(), ctx.getSettingValue(Settings.INPUT2), ctx.getSettingValue(Settings.OUTPUT));
        this.OUT.setFileName(ctx.getSettingValue(Settings.OUTPUT));
        String exceptionsFileName = StringUtils.addFileSuffix(this.OUT.getFileName(), "-exceptions");
        this.EXCEPTIONS.setFileName(exceptionsFileName);

        if (this.readFiles(ctx.getInputFile(), ctx.getSettingValue(Settings.INPUT2), false, ctx.getOpt().isDebug(), ctx.getOpt().isOuterJoin()))
        {
            LOGGER.debug("runOperation(): --> File1 [{}] & File2 [{}] read successfully.", ctx.getInputFile(), ctx.getSettingValue(Settings.INPUT2));

            this.keyId1 = this.FILE_1.getHeader().getId(ctx.getSettingValue(Settings.KEY));

            if (this.keyId1 < 0)
            {
                LOGGER.error("runOperation(): Join FAILED, key was NOT found in the Input Headers.");
                this.clear();
                return false;
            }

            this.jKeyId1 = this.FILE_2.getHeader().getId(ctx.getSettingValue(Settings.JOIN_KEY));

            if (this.jKeyId1 < 0)
            {
                LOGGER.error("runOperation(): Join FAILED, join key was NOT found in the Input2 Headers.");
                this.clear();
                return false;
            }

            // Technically we can allow any combination / pairs -- as long as they match.
            if (ctx.getOpt().hasKey2())
            {
                this.keyId2 = this.FILE_1.getHeader().getId(ctx.getSettingValue(Settings.KEY2));

                if (this.keyId2 < 0)
                {
                    LOGGER.error("runOperation(): Join FAILED, key2 was NOT found in the Input Headers.");
                    this.clear();
                    return false;
                }

                if (ctx.getOpt().hasJoinKey2())
                {
                    this.jKeyId2 = this.FILE_2.getHeader().getId(ctx.getSettingValue(Settings.JOIN_KEY2));

                    if (this.jKeyId2 < 0)
                    {
                        LOGGER.error("runOperation(): Join FAILED, join key2 was NOT found in the Input2 Headers.");
                        this.clear();
                        return false;
                    }
                }
                else
                {
                    LOGGER.error("runOperation(): Join FAILED, join key2 was NOT found in the settings while key2 was defined.");
                    this.clear();
                    return false;
                }
            }

            if (ctx.getOpt().hasKey3())
            {
                this.keyId3 = this.FILE_1.getHeader().getId(ctx.getSettingValue(Settings.KEY3));

                if (this.keyId3 < 0)
                {
                    LOGGER.error("runOperation(): Join FAILED, key3 was NOT found in the Input Headers.");
                    this.clear();
                    return false;
                }

                if (ctx.getOpt().hasJoinKey3())
                {
                    this.jKeyId3 = this.FILE_2.getHeader().getId(ctx.getSettingValue(Settings.JOIN_KEY3));

                    if (this.jKeyId3 < 0)
                    {
                        LOGGER.error("runOperation(): Join FAILED, join key3 was NOT found in the Input2 Headers.");
                        this.clear();
                        return false;
                    }
                }
                else
                {
                    LOGGER.error("runOperation(): Join FAILED, join key3 was NOT found in the settings while key3 was defined.");
                    this.clear();
                    return false;
                }
            }

            if (ctx.getOpt().hasKey4())
            {
                this.keyId4 = this.FILE_1.getHeader().getId(ctx.getSettingValue(Settings.KEY4));

                if (this.keyId4 < 0)
                {
                    LOGGER.error("runOperation(): Join FAILED, key4 was NOT found in the Input Headers.");
                    this.clear();
                    return false;
                }

                if (ctx.getOpt().hasJoinKey4())
                {
                    this.jKeyId4 = this.FILE_2.getHeader().getId(ctx.getSettingValue(Settings.JOIN_KEY4));

                    if (this.jKeyId4 < 0)
                    {
                        LOGGER.error("runOperation(): Join FAILED, join key4 was NOT found in the Input2 Headers.");
                        this.clear();
                        return false;
                    }
                }
                else
                {
                    LOGGER.error("runOperation(): Join FAILED, join key4 was NOT found in the settings while key4 was defined.");
                    this.clear();
                    return false;
                }
            }

            if (ctx.getOpt().hasKey5())
            {
                this.keyId5 = this.FILE_1.getHeader().getId(ctx.getSettingValue(Settings.KEY5));

                if (this.keyId5 < 0)
                {
                    LOGGER.error("runOperation(): Join FAILED, key5 was NOT found in the Input Headers.");
                    this.clear();
                    return false;
                }

                if (ctx.getOpt().hasJoinKey5())
                {
                    this.jKeyId5 = this.FILE_2.getHeader().getId(ctx.getSettingValue(Settings.JOIN_KEY5));

                    if (this.jKeyId5 < 0)
                    {
                        LOGGER.error("runOperation(): Join FAILED, join key5 was NOT found in the Input2 Headers.");
                        this.clear();
                        return false;
                    }
                }
                else
                {
                    LOGGER.error("runOperation(): Join FAILED, join key5 was NOT found in the settings while key5 was defined.");
                    this.clear();
                    return false;
                }
            }

            if (this.joinFiles(ctx.getOpt().isOuterJoin()))
            {
                if (this.writeFile(this.OUT, ctx.getOpt().isApplyQuotes(), false, ctx.getOpt().isDebug(), null))
                {
                    LOGGER.debug("runOperation(): --> File [{}] written successfully.", ctx.getSettingValue(Settings.OUTPUT));

                    if (!ctx.getOpt().isOuterJoin())
                    {
                        if (this.writeFile(this.EXCEPTIONS, ctx.getOpt().isApplyQuotes(), false, ctx.getOpt().isDebug(), null))
                        {
                            LOGGER.debug("runOperation(): --> File [{}] written successfully.", exceptionsFileName);
                        }
                    }

                    this.clear();
                    return true;
                }
                else
                {
                    LOGGER.error("runOperation(): Write file FAILED.");
                    return false;
                }
            }
            else
            {
                LOGGER.error("runOperation(): File Join has FAILED.");
                return false;
            }
        }

        LOGGER.error("runOperation(): General Operation failure.");
        return false;
    }

    @Override
    public void displayHelp()
    {
        System.out.print("Join Operation:\n");
        System.out.printf("\tAliases: %s\n\n", Operations.JOIN.getAlias().toString());

        System.out.print("This operation merges two files into a single output file,\n");
        System.out.print("While scanning input2 for a matching set of key pairs; such that:\n");
        System.out.print("  - key matches join-key\n");
        System.out.print("  - key2 matches join-key2\n");
        System.out.print("  - ... and so on.\n");
        System.out.print("The default join type is of the INNER type, but you can specify\n");
        System.out.print("this operation to be in OUTER mode by using the --outer setting.\n");
        System.out.print("\n");
    }

    private boolean readFiles(String file1, String file2, boolean ignoreQuotes, boolean debug, boolean outer)
    {
        LOGGER.debug("readFiles(): Reading files [{}] and [{}] ...", file1, file2);

        this.FILE_1 = this.readFile(file1, ignoreQuotes, debug);

        if (this.FILE_1 == null || this.FILE_1.isEmpty())
        {
            LOGGER.error("readFiles(): File Cache 1 is Empty!");
            return false;
        }

        this.FILE_2 = this.readFile(file2, ignoreQuotes, debug);

        if (this.FILE_2 == null || this.FILE_2.isEmpty())
        {
            LOGGER.error("readFiles(): File Cache 2 is Empty!");
            return false;
        }

        // Append headers
        CSVHeader header1 = this.FILE_1.getHeader();
        CSVHeader header2 = this.FILE_2.getHeader();
        this.OUT.setHeader(header1);

        if (outer)
        {
            this.EXCEPTIONS.setHeader(header2);
        }
        else
        {
            this.EXCEPTIONS.setHeader(header1);
        }

        Iterator<String> iter = header2.iterator();

        while (iter.hasNext())
        {
            this.OUT.appendHeader(iter.next());
        }

        return true;
    }

    private boolean joinFiles(boolean outer)
    {
        final int header1Size = this.FILE_1.getHeader().size();
        final int header2Size = this.FILE_2.getHeader().size();

        for (int i = 1; i < this.FILE_1.getFile().size(); i++)
        {
            List<String> entry = this.FILE_1.getFile().get(i);

            if (!entry.isEmpty())
            {
                List<String> lKeys = this.getKeys(entry);
                List<String> result = new ArrayList<>(entry);
                List<String> match = this.getFirstMatchingKey(lKeys);

                if (!match.isEmpty())
                {
                    result.addAll(match);
                    this.OUT.addLine(result);
                }
                else if (outer)
                {
                    // OuterJoin style, No Exceptions
                    for (int j = header1Size; j < header2Size; j++)
                    {
                        result.add("");
                    }

                    this.OUT.addLine(result);
                }
                else
                {
                    this.EXCEPTIONS.addLine(entry);
                }
            }
        }

        // Write All unmatched-outer Exceptions
        if (outer)
        {
            for (int i = 1; i < this.FILE_2.getFile().size(); i++)
            {
                if (!this.matchedOuter.contains(i))
                {
                    List<String> entry = this.FILE_2.getFile().get(i);

                    if (!entry.isEmpty())
                    {
                        this.EXCEPTIONS.addLine(entry);
                    }
                }
            }
        }

        return true;
    }

    private List<String> getKeys(List<String> list)
    {
        List<String> result = new ArrayList<>();

        if (list.size() > this.keyId1)
        {
            result.add(list.get(this.keyId1));
        }
        else
        {
            return result;
        }

        if (this.keyId2 > 0 && list.size() > this.keyId2)
        {
            result.add(list.get(this.keyId2));
        }
        else
        {
            return result;
        }

        if (this.keyId3 > 0 && list.size() > this.keyId3)
        {
            result.add(list.get(this.keyId3));
        }
        else
        {
            return result;
        }

        if (this.keyId4 > 0 && list.size() > this.keyId4)
        {
            result.add(list.get(this.keyId4));
        }
        else
        {
            return result;
        }

        if (this.keyId5 > 0 && list.size() > this.keyId5)
        {
            result.add(list.get(this.keyId5));
        }
        else
        {
            return result;
        }

        return result;
    }

    private List<String> getJoinKeys(List<String> list)
    {
        List<String> result = new ArrayList<>();

        if (list.size() > this.jKeyId1)
        {
            result.add(list.get(this.jKeyId1));
        }
        else
        {
            return result;
        }

        if (this.jKeyId2 > 0 && list.size() > this.jKeyId2)
        {
            result.add(list.get(this.jKeyId2));
        }
        else
        {
            return result;
        }

        if (this.jKeyId3 > 0 && list.size() > this.jKeyId3)
        {
            result.add(list.get(this.jKeyId3));
        }
        else
        {
            return result;
        }

        if (this.jKeyId4 > 0 && list.size() > this.jKeyId4)
        {
            result.add(list.get(this.jKeyId4));
        }
        else
        {
            return result;
        }

        if (this.jKeyId5 > 0 && list.size() > this.jKeyId5)
        {
            result.add(list.get(this.jKeyId5));
        }
        else
        {
            return result;
        }

        return result;
    }

    private boolean matchKeys(List<String> left, List<String> right)
    {
        if (left.size() != right.size() ||
            left.isEmpty())
        {
            return false;
        }

        for (int i = 0; i < left.size(); i++)
        {
            String lEntry = left.get(i);
            String rEntry = right.get(i);

            if (!lEntry.equals(rEntry))
            {
                return false;
            }
        }

        return true;
    }

    private List<String> getFirstMatchingKey(List<String> lKeys)
    {
        for (int i = 1; i < this.FILE_2.getFile().size(); i++)
        {
            List<String> entry = this.FILE_2.getFile().get(i);

            if (!entry.isEmpty())
            {
                List<String> rKeys = this.getJoinKeys(entry);

                if (this.matchKeys(lKeys, rKeys))
                {
                    this.matchedOuter.add(i);
                    return entry;
                }
            }
        }

        return List.of();
    }

    @Override
    public void clear()
    {
        if (this.FILE_1 != null && !this.FILE_1.isEmpty())
        {
            this.FILE_1.clear();
        }

        if (this.FILE_2 != null && !this.FILE_2.isEmpty())
        {
            this.FILE_2.clear();
        }

        if (this.OUT != null && !this.OUT.isEmpty())
        {
            this.OUT.clear();
        }

        if (this.EXCEPTIONS != null && !this.EXCEPTIONS.isEmpty())
        {
            this.EXCEPTIONS.clear();
        }

        this.matchedOuter.clear();
    }

    @Override
    public void close()
    {
        this.clear();
    }
}
