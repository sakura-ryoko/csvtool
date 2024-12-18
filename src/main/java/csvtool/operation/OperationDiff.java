package csvtool.operation;

import csvtool.data.Const;
import csvtool.data.Context;
import csvtool.data.FileCache;
import csvtool.enums.Operations;
import csvtool.enums.Settings;
import csvtool.utils.LogWrapper;

public class OperationDiff extends Operation implements AutoCloseable
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());

    private FileCache FILE_1;
    private FileCache FILE_2;
    private FileCache DIFF;
    private int keyId;

    public OperationDiff(Operations op)
    {
        super(op);
        this.FILE_1 = new FileCache();
        this.FILE_2 = new FileCache();
        this.DIFF = new FileCache();
        this.keyId = -1;
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

        if (readFiles(ctx.getInputFile(), ctx.getSettingValue(Settings.INPUT2)))
        {
            LOGGER.debug("runOperation(): --> File1 [{}] & File2 [{}] read successfully.", ctx.getInputFile(), ctx.getSettingValue(Settings.INPUT2));

            if (!ctx.getOpt().hasKey())
            {
                LOGGER.error("runOperation(): Diff FAILED, key was not set.");
                this.clear();
                return false;
            }

            this.keyId = this.FILE_1.getHeader().getId(ctx.getSettingValue(Settings.KEY));

            // TODO

            return true;
        }

        return false;
    }

    private boolean readFiles(String file1, String file2)
    {
        LOGGER.debug("readFiles(): Reading files ...");

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

        if (this.DIFF != null && !this.DIFF.isEmpty())
        {
            this.DIFF.clear();
        }
    }

    @Override
    public void close()
    {
        this.clear();
    }
}
