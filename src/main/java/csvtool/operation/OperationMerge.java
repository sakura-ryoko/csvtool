package csvtool.operation;

import csvtool.data.Const;
import csvtool.data.Context;
import csvtool.data.FileCache;
import csvtool.enums.Operations;
import csvtool.enums.Settings;
import csvtool.utils.LogWrapper;

import java.util.ArrayList;
import java.util.List;

public class OperationMerge extends Operation implements AutoCloseable
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());

    private FileCache FILE_1;
    private FileCache FILE_2;
    private int keyId;

    public OperationMerge(Operations op)
    {
        super(op);
        this.FILE_1 = new FileCache();
        this.FILE_2 = new FileCache();
        this.keyId = -1;
    }

    @Override
    public boolean runOperation(Context ctx)
    {
        if (!ctx.getOpt().hasInput2() || !ctx.getOpt().hasOutput())
        {
            LOGGER.error("runOperation(): Merge FAILED, Second input file and an output is required.");
            return false;
        }

        LOGGER.debug("runOperation(): --> MERGE [{}] + [{}] into [{}].", ctx.getInputFile(), ctx.getSettingValue(Settings.INPUT2));

        if (readFiles(ctx.getInputFile(), ctx.getSettingValue(Settings.INPUT2)))
        {
            LOGGER.debug("runOperation(): --> File1 [{}] & File2 [{}] read successfully.", ctx.getInputFile(), ctx.getSettingValue(Settings.INPUT2));

            if (ctx.getOpt().isDeDupe())
            {
                if (!ctx.getOpt().hasKey())
                {
                    LOGGER.error("runOperation(): Merge FAILED, De-Dupe is set, but key was not.");
                    this.clear();
                    return false;
                }

                this.keyId = this.FILE_1.getHeader().getId(ctx.getSettingValue(Settings.KEY));

                if (this.keyId < 0)
                {
                    LOGGER.error("runOperation(): Merge FAILED, key was NOT found in the Headers.");
                    this.clear();
                    return false;
                }

                if (!deDupeFiles(true))
                {
                    LOGGER.error("runOperation(): Merge FAILED, DeDuplication attempt has failed.");
                    this.clear();
                    return false;
                }
            }

            if (this.writeFile(ctx.getOpt().getOutput(), ctx.getOpt().isApplyQuotes(), false, Const.DEBUG, this.FILE_1, this.FILE_2))
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

        return false;
    }

    private boolean readFiles(String file1, String file2)
    {
        LOGGER.debug("readFiles(): Reading file1 [{}] ...", file1);

        this.FILE_1 = this.readFile(file1, Const.DEBUG);
        this.FILE_2 = this.readFile(file2, Const.DEBUG);

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

    private boolean deDupeFiles(boolean skipHeader)
    {
        if (this.FILE_1.isEmpty() || this.FILE_2.isEmpty() || this.keyId < 0)
        {
            LOGGER.error("deDupeFiles(): Files are empty, or Key not set!");
            return false;
        }

        LOGGER.debug("deDupeFiles(): Attempting to execute De-Dupe by removing duplicates from file2 ...");
        List<List<String>> temp = new ArrayList<>();
        List<Integer> dupes = new ArrayList<>();

        this.FILE_2.getFile().forEach((i, list) -> temp.add(list));
        this.FILE_2.clear();

        for (int i = 0; i < this.FILE_1.getFile().size(); i++)
        {
            List<String> entry = this.FILE_1.getFile().get(i);

            if (!entry.isEmpty())
            {
                String key = entry.get(this.keyId);

                LOGGER.debug("FILE1[{}]: key [{}] checking FILE2 ...", i, key);

                for (int j = 0; j < temp.size(); j++)
                {
                    List<String> entry2 = temp.get(j);

                    if (!entry2.isEmpty())
                    {
                        String key2 = entry2.get(this.keyId);

                        LOGGER.debug("FILE [{}/{}]: key [{}] / key2 [{}]", i, j, key, key2);

                        if (key2.equals(key) && (!skipHeader || j > 0))
                        {
                            LOGGER.info("FILE2[{}]: skipping duplicate ...", j);
                            dupes.add(j);
                        }
                    }
                }
            }
        }

        LOGGER.debug("deDupeFiles(): Restoring and removing duplicates from file2 (Count: {} dupes found)", dupes.size());
        int pos = 0;

        // Copy Back
        for (int i = 0; i < temp.size(); i++)
        {
            if (!dupes.contains(i) || (skipHeader && i == 0))
            {
                LOGGER.debug("LINE[{}]: put {}", pos, temp.get(i).toString());
                this.FILE_2.getFile().put(pos, temp.get(i));
                pos++;
            }
        }

        return true;
    }

    private void clear()
    {
        if (this.FILE_1 != null && !this.FILE_1.isEmpty())
        {
            this.FILE_1.clear();
        }

        if (this.FILE_2 != null && !this.FILE_2.isEmpty())
        {
            this.FILE_2.clear();
        }
    }

    @Override
    public void close()
    {
        this.clear();
    }
}
