package csvtool.operation;

import csvtool.data.Context;
import csvtool.data.FileCache;
import csvtool.enums.Operations;
import csvtool.enums.Settings;
import csvtool.utils.LogWrapper;
import csvtool.utils.StringUtils;

import java.util.HashMap;
import java.util.List;

public class OperationSplit extends Operation implements AutoCloseable
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());

    private FileCache FILE;
    private FileCache OUT;
    private final HashMap<String, Integer> keyCounts;
    private String lastKey;
    private int keyId;
    private final int maxLines = 2500;

    public OperationSplit(Operations op)
    {
        super(op);
        this.FILE = new FileCache();
        this.OUT = new FileCache();
        this.keyCounts = new HashMap<>();
        this.lastKey = "";
        this.keyId = -1;
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

        if (!ctx.getOpt().hasOutput())
        {
            LOGGER.error("runOperation(): Split FAILED, Second input file and an output is required.");
            return false;
        }

        if (!ctx.getOpt().hasKey())
        {
            LOGGER.error("runOperation(): Split FAILED, a key field is required.");
            return false;
        }

        LOGGER.debug("runOperation(): --> SPLIT [{}] into [{}].", ctx.getInputFile(), ctx.getSettingValue(Settings.OUTPUT));

        if (this.readFiles(ctx.getInputFile(), false, ctx.getOpt().isDebug()))
        {
            LOGGER.debug("runOperation(): --> File [{}] read successfully.", ctx.getInputFile());

            if (this.writeFiles(ctx.getOpt().isApplyQuotes(), ctx.getOpt().isDebug(), ctx.getOpt().getOutput(), ctx.getOpt().getKey()))
            {
                LOGGER.debug("runOperation(): --> Files [{}] written successfully.", ctx.getSettingValue(Settings.OUTPUT));
                this.clear();
                return true;
            }
            else
            {
                LOGGER.error("runOperation(): Write file FAILED.");
            }
        }

        LOGGER.error("runOperation(): General Operation failure.");
        return false;
    }

    private boolean writeFiles(boolean applyQuotes, boolean debug, String out, String key)
    {
        int fileNum = 0;
        this.keyId = this.FILE.getHeader().getId(key);

        String outName = StringUtils.addFileSuffix(out, "_" + fileNum);
        this.OUT.setHeader(this.FILE.getHeader());
        this.OUT.setFileName(outName);

        for (int i = 1; i < this.FILE.getFile().size(); i++)
        {
            List<String> entry = this.FILE.getLine(i);
            String keyEntry = entry.get(this.keyId);

            LOGGER.debug("writeFiles(): LINE[{}]: {}", i, entry.toString());

            if (this.countKeysAndSplit(keyEntry, this.OUT.getFile().size()))
            {
                // Write Advance File #
                this.writeFile(this.OUT, applyQuotes, false, debug, null);
                this.OUT.clear();

                fileNum++;
                outName = StringUtils.addFileSuffix(out, "_" + fileNum);
                this.OUT = new FileCache();
                this.OUT.setHeader(this.FILE.getHeader());
                this.OUT.setFileName(outName);
                LOGGER.debug("writeFiles(): LINE[{}]: --> SPLIT FILE ({})", i, outName);
            }

            this.OUT.addLine(entry);
        }

        if (!this.OUT.isEmpty())
        {
            this.writeFile(this.OUT, applyQuotes, false, debug, null);
            this.OUT.clear();
        }

        return true;
    }

    private boolean countKeysAndSplit(String key, int line)
    {
        int count;

        if (this.keyCounts.containsKey(key))
        {
            count = this.keyCounts.get(key);
            count++;
            this.keyCounts.put(key, count);
        }
        else
        {
            this.keyCounts.put(key, 1);
            count = 1;
        }

        if (this.lastKey.isEmpty())
        {
            this.lastKey = key;
        }

        if (this.lastKey.equals(key))
        {
            return false;
        }
        else
        {
            this.lastKey = key;
        }

        boolean result = count > this.maxLines || line > this.maxLines;

        if (result)
        {
            this.keyCounts.clear();
        }
        else
        {
            this.lastKey = key;
        }

        return result;
    }

    @Override
    public void displayHelp()
    {
        System.out.print("Split Operation:\n");
//        System.out.printf("\tAliases: %s\n\n", Operations.SPLIT.getAlias().toString());

//        System.out.print("This operation merges two files into a single output file.\n");
//        System.out.print("It accepts two input files (--input), and an output (--output).\n");
//        System.out.print("You can also pass the (--de-dupe) operation with requires a key field (--key) to be set.\n");
//        System.out.print("De-Dupe compares the files, and removes duplicate rows based on the key field given.\n");
    }

    private boolean readFiles(String file, boolean ignoreQuotes, boolean debug)
    {
        LOGGER.debug("readFiles(): Reading file [{}] ...", file);

        this.FILE = this.readFile(file, ignoreQuotes, false);

        if (this.FILE == null)
        {
            LOGGER.error("readFiles(): File Cache is Empty!");
            return false;
        }

        return true;
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
    }

    @Override
    public void close()
    {
        this.clear();
    }
}
