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
    private int keyId1;
    private int keyId2;

    public OperationJoin(Operations op)
    {
        super(op);
        this.FILE_1 = new FileCache();
        this.FILE_2 = new FileCache();
        this.OUT = new FileCache();
        this.EXCEPTIONS = new FileCache();
        this.keyId1 = -1;
        this.keyId2 = -1;
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

        if (!ctx.getOpt().hasKey2())
        {
            LOGGER.error("runOperation(): Join FAILED, a key2 is required.");
            this.clear();
            return false;
        }

        LOGGER.debug("runOperation(): --> JOIN [{}] + [{}] into [{}].", ctx.getInputFile(), ctx.getSettingValue(Settings.INPUT2), ctx.getSettingValue(Settings.OUTPUT));
        this.OUT.setFileName(ctx.getSettingValue(Settings.OUTPUT));
        String exceptionsFileName = StringUtils.addFileSuffix(this.OUT.getFileName(), "-exceptions");
        this.EXCEPTIONS.setFileName(exceptionsFileName);

        if (this.readFiles(ctx.getInputFile(), ctx.getSettingValue(Settings.INPUT2), false, ctx.getOpt().isDebug()))
        {
            LOGGER.debug("runOperation(): --> File1 [{}] & File2 [{}] read successfully.", ctx.getInputFile(), ctx.getSettingValue(Settings.INPUT2));

            this.keyId1 = this.FILE_1.getHeader().getId(ctx.getSettingValue(Settings.KEY));

            if (this.keyId1 < 0)
            {
                LOGGER.error("runOperation(): Join FAILED, key was NOT found in the Input Headers.");
                this.clear();
                return false;
            }

            this.keyId2 = this.FILE_2.getHeader().getId(ctx.getSettingValue(Settings.KEY2));

            if (this.keyId2 < 0)
            {
                LOGGER.error("runOperation(): Join FAILED, key2 was NOT found in the Input2 Headers.");
                this.clear();
                return false;
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

//        System.out.print("This operation merges two files into a single output file.\n");
//        System.out.print("It accepts two input files (--input), and an output (--output).\n");
//        System.out.print("You can also pass the (--de-dupe) operation with requires a key field (--key) to be set.\n");
//        System.out.print("Optionally, you can enable (--squash-dupe) which combines de-duplicated data values.\n");
//        System.out.print("De-Dupe compares the files, and removes duplicate rows based on the key field given.\n");
//        System.out.print("\n");
    }

    private boolean readFiles(String file1, String file2, boolean ignoreQuotes, boolean debug)
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
        this.EXCEPTIONS.setHeader(header1);

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
                String key = entry.get(this.keyId1);
                List<String> result = new ArrayList<>(entry);
                List<String> match = this.getFirstMatchingKey(key);

                if (!match.isEmpty())
                {
                    result.addAll(match);
                    this.OUT.addLine(result);
                }
                else if (outer)
                {
                    // OuterJoin style, No Exceptions
                    for (int j = 0; j < header2Size; j++)
                    {
                        result.add("");
                        this.OUT.addLine(result);
                    }
                }
                else
                {
                    this.EXCEPTIONS.addLine(entry);
                }
            }
        }

        return true;
    }

    private List<String> getFirstMatchingKey(String key)
    {
        for (int i = 1; i < this.FILE_2.getFile().size(); i++)
        {
            List<String> entry = this.FILE_2.getFile().get(i);

            if (!entry.isEmpty())
            {
                String key2 = entry.get(this.keyId2);

                if (key.matches(key2))
                {
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
    }

    @Override
    public void close()
    {
        this.clear();
    }
}
