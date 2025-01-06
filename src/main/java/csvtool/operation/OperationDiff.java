package csvtool.operation;

import csvtool.data.Const;
import csvtool.data.Context;
import csvtool.data.FileCache;
import csvtool.enums.Operations;
import csvtool.enums.Settings;
import csvtool.utils.LogWrapper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class OperationDiff extends Operation implements AutoCloseable
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());

    private FileCache FILE_1;
    private FileCache FILE_2;
    private FileCache DIFF;
    private String side;
    private int keyId;
    private int key2Id;

    public OperationDiff(Operations op)
    {
        super(op);
        this.FILE_1 = new FileCache();
        this.FILE_2 = new FileCache();
        this.DIFF = new FileCache();
        this.side = "";
        this.keyId = -1;
        this.key2Id = -1;
    }

    @Override
    public boolean runOperation(Context ctx)
    {
        if (!ctx.getOpt().hasInput2() || !ctx.getOpt().hasOutput())
        {
            LOGGER.error("runOperation(): Diff FAILED, Second input file and an output is required.");
            return false;
        }

        LOGGER.debug("runOperation(): --> DIFF [{}] of [{}] into [{}].", ctx.getInputFile(), ctx.getSettingValue(Settings.INPUT2), ctx.getSettingValue(Settings.OUTPUT));

        if (readFiles(ctx.getInputFile(), ctx.getSettingValue(Settings.INPUT2), false))
        {
            LOGGER.debug("runOperation(): --> File1 [{}] & File2 [{}] read successfully.", ctx.getInputFile(), ctx.getSettingValue(Settings.INPUT2));

            // Key for checking the DIFF
            if (!ctx.getOpt().hasKey())
            {
                LOGGER.error("runOperation(): Diff FAILED, key was not set.");
                this.clear();
                return false;
            }

            this.keyId = this.FILE_1.getHeader().getId(ctx.getSettingValue(Settings.KEY));

            // 2nd Key Param for checking the DIFF [Optional]
            if (ctx.getOpt().hasKey2())
            {
                this.key2Id = this.FILE_1.getHeader().getId(ctx.getSettingValue(Settings.KEY2));
            }

            // Get Side Column Param
            if (ctx.getOpt().hasSide())
            {
                this.side = ctx.getSettingValue(Settings.SIDE);
                this.DIFF = new FileCache(this.FILE_1.getHeader().add(this.side));
            }
            else
            {
                this.DIFF = new FileCache(this.FILE_1.getHeader());
            }

            if (!runDiff(true, ctx.getInputFile(), this.FILE_1, this.FILE_2))
            {
                LOGGER.error("runOperation(): Diff FAILED, Diff1 execution attempt has failed.");
                this.clear();
                return false;
            }

            if (!runDiff(true, ctx.getSettingValue(Settings.INPUT2), this.FILE_2, this.FILE_1))
            {
                LOGGER.error("runOperation(): Diff FAILED, Diff2 execution attempt has failed.");
                this.clear();
                return false;
            }

            if (!this.DIFF.isEmpty())
            {
                if (this.writeFile(ctx.getSettingValue(Settings.OUTPUT), ctx.getOpt().isApplyQuotes(), ctx.getOpt().isAppendOutput(), Const.DEBUG, this.DIFF, null))
                {
                    LOGGER.debug("runOperation(): --> File [{}] written successfully.", ctx.getSettingValue(Settings.OUTPUT));
                    this.clear();
                    return true;
                }
                else
                {
                    LOGGER.error("runOperation(): Write file FAILED.");
                }
            }
            else
            {
                LOGGER.info("runOperation(): No differences found.");
                return true;
            }
        }

        LOGGER.error("runOperation(): General Operation failure.");
        return false;
    }

    @Override
    public void displayHelp()
    {
        System.out.print("Diff Operation Help:\n");
        System.out.printf("\tAliases: %s\n\n", Operations.DIFF.getAlias().toString());

        System.out.print("This operation compares two files and returns the differences into an output file.\n");
        System.out.print("It accepts two input files (--input), and an output (--output); and also requires a key field (--key) to be set.\n");
        System.out.print("You can also pass the second key field (--key2) and the side key field (--side) as options.\n");
        System.out.print("The key field #2 adds a secondary comparison point for more-specific comparisons,\nor an optional side field for adding a column displaying which file the difference came from.\n");
    }

    private boolean readFiles(String file1, String file2, boolean ignoreQuotes)
    {
        LOGGER.debug("readFiles(): Reading files ...");

        this.FILE_1 = this.readFile(file1, ignoreQuotes, Const.DEBUG);
        this.FILE_2 = this.readFile(file2, ignoreQuotes, Const.DEBUG);

        if (this.FILE_1.isEmpty() || this.FILE_2.isEmpty())
        {
            LOGGER.error("readFiles(): Either File Cache is Empty!");
            return false;
        }

        if (this.compareHeaders(this.FILE_1.getHeader(), this.FILE_2.getHeader()))
        {
            LOGGER.debug("readFiles(): Successfully read both files!");
            return true;
        }
        else
        {
            LOGGER.error("readFiles(): Headers don't match!");
        }

        return false;
    }

    // This has to run twice, once each direction file1 -> file2, then file2 -> file1
    private boolean runDiff(boolean skipHeaders, String side, @Nonnull FileCache file1, @Nonnull FileCache file2)
    {
        if (file1.isEmpty() || file2.isEmpty() || this.keyId < 0)
        {
            LOGGER.error("runDiff(): Files are empty, or Key not set!");
            return false;
        }

        LOGGER.debug("runDiff(): Attempting to compare files ...");
        List<List<String>> temp = new ArrayList<>();

        file2.getFile().forEach((i, list) -> temp.add(list));

        // Run DIFF from FILE_1 -> FILE_2
        for (int i = 0; i < file1.getFile().size(); i++)
        {
            List<String> entry = file1.getFile().get(i);

            if (!entry.isEmpty() && (!skipHeaders || i > 0))
            {
                String key1 = entry.get(this.keyId);
                String key2 = this.key2Id > -1 ? entry.get(this.key2Id) : "";
                boolean matched = false;

                LOGGER.debug("FILE1[{}]: key [{}] (key2 {}) checking FILE2 ...", i, key1, key2.isEmpty() ? "<empty>" : key2);

                for (int j = 0; j < temp.size(); j++)
                {
                    List<String> entry2 = temp.get(j);

                    if (!entry2.isEmpty() && (!skipHeaders || j > 0))
                    {
                        String key1x = entry2.get(this.keyId);
                        String key2x = this.key2Id > -1 ? entry2.get(this.key2Id) : "";

                        LOGGER.debug("FILE [{}/{}]:A: key1 [{}] vs [{}] // key2 [{}] vs [{}]", i, j, key1, key1x, key2, key2x);

                        if (key1x.matches(key1) && key2x.matches(key2))
                        {
                            LOGGER.debug("FILE [{}/{}]: key1 & key2 MATCHED!", i, j);
                            matched = true;
                            break;
                        }
                    }
                }

                if (matched)
                {
                    LOGGER.debug("FILE1[{}]: matched [{}] -- OK", i, matched);
                }
                else
                {
                    LOGGER.debug("FILE1[{}]: matched [{}] -- ADD LINE!", i, matched);

                    if (this.side.isEmpty())
                    {
                        this.DIFF.addLine(entry);
                    }
                    else
                    {
                        entry.add(side);
                        this.DIFF.addLine(entry);
                    }
                }
            }
        }

        return true;
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

        if (this.DIFF != null && !this.DIFF.isEmpty())
        {
            this.DIFF.clear();
        }
    }

    @Override
    public void close() throws Exception
    {
        if (this.FILE_1 != null)
        {
            this.FILE_1.close();
        }

        if (this.FILE_2 != null)
        {
            this.FILE_2.close();
        }

        if (this.DIFF != null)
        {
            this.DIFF.close();
        }
    }
}
