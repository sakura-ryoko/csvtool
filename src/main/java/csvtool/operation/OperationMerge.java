package csvtool.operation;

import csvtool.data.Context;
import csvtool.enums.Operations;
import csvtool.enums.Settings;
import csvtool.header.CSVHeader;
import csvtool.utils.CSVWrapper;
import csvtool.utils.LogWrapper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OperationMerge extends Operation implements AutoCloseable
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());

    private final HashMap<Integer, List<String>> FILE_1;
    private final HashMap<Integer, List<String>> FILE_2;
    private CSVHeader HEADER_1;
    private CSVHeader HEADER_2;
    private int keyId;

    public OperationMerge(Operations op)
    {
        super(op);
        this.FILE_1 = new HashMap<>();
        this.FILE_2 = new HashMap<>();
        this.HEADER_1 = null;
        this.HEADER_2 = null;
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

                this.keyId = this.HEADER_1.getId(ctx.getSettingValue(Settings.KEY));

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

            if (writeFile(ctx.getOpt().getOutput(), ctx.getOpt().isApplyQuotes()))
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

        try (CSVWrapper wrapper = new CSVWrapper(file1))
        {
            if (wrapper.read())
            {
                LOGGER.info("readFiles(): File1 read!");
                dumpFile(wrapper);
                cacheFile1(wrapper);
                //wrapper.close();
            }
        }
        catch (Exception e)
        {
            LOGGER.error("readFiles(): Exception reading file1! Error: {}", e.getMessage());
            return false;
        }

        LOGGER.debug("readFiles(): Reading file2 [{}] ...", file2);

        try (CSVWrapper wrapper = new CSVWrapper(file2))
        {
            if (wrapper.read())
            {
                LOGGER.info("readFiles(): File2 read!");
                dumpFile(wrapper);
                cacheFile2(wrapper);
                //wrapper.close();
            }
        }
        catch (Exception e)
        {
            LOGGER.error("readFiles(): Exception reading file2! Error: {}", e.getMessage());
        }

        if (this.FILE_1.isEmpty() || this.FILE_2.isEmpty())
        {
            LOGGER.error("readFiles(): File Cache is Empty!");
            return false;
        }

        if (compareHeaders())
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

    private void cacheFile1(CSVWrapper wrapper)
    {
        if (!this.FILE_1.isEmpty())
        {
            this.FILE_1.clear();
        }

        LOGGER.debug("cacheFile1(): Caching file1 [{} lines] ...", wrapper.getSize());

        this.HEADER_1 = wrapper.getHeader();
        this.FILE_1.putAll(wrapper.getAllLines());
    }

    private void cacheFile2(CSVWrapper wrapper)
    {
        if (!this.FILE_2.isEmpty())
        {
            this.FILE_2.clear();
        }

        LOGGER.debug("cacheFile1(): Caching file2 [{} lines] ...", wrapper.getSize());

        this.HEADER_2 = wrapper.getHeader();
        this.FILE_2.putAll(wrapper.getAllLines());
    }

    private boolean compareHeaders()
    {
        if (this.HEADER_1 == null || this.HEADER_2 == null)
        {
            return false;
        }

        LOGGER.debug("compareHeaders(): ...");

        return this.HEADER_1.matches(this.HEADER_2);
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

        this.FILE_2.forEach((i, list) -> temp.add(list));
        this.FILE_2.clear();

        for (int i = 0; i < this.FILE_1.size(); i++)
        {
            List<String> entry = this.FILE_1.get(i);

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
                this.FILE_2.put(pos, temp.get(i));
                pos++;
            }
        }

        return true;
    }

    private boolean appendFile2(CSVWrapper wrapper)
    {
        if (wrapper == null || this.FILE_2.isEmpty() || wrapper.isEmpty())
        {
            return false;
        }

        LOGGER.debug("appendFile2(): Appending file2 to wrapper...");

        for (int i = 1; i < this.FILE_2.size(); i++)
        {
            List<String> entry = this.FILE_2.get(i);

            if (!entry.isEmpty())
            {
                wrapper.putLine(entry);
            }
        }

        return true;
    }

    private boolean writeFile(String file, boolean applyQuotes)
    {
        LOGGER.debug("writeFile(): Write file [{}]:", file);

        try (CSVWrapper wrapper = new CSVWrapper(file, false))
        {
            if (wrapper.putAllLines(this.FILE_1, true))
            {
                if (!appendFile2(wrapper))
                {
                    LOGGER.error("writeFile(): Error appending file2 Cache.");
                    wrapper.close();
                    return false;
                }

                if (wrapper.write(applyQuotes, false))
                {
                    LOGGER.info("writeFile(): File written!");
                    dumpFile(wrapper);
                    wrapper.close();
                    return true;
                }
            }
            else
            {
                LOGGER.error("writeFile(): Error copying file2 Cache to new file.");
                wrapper.close();
            }
        }
        catch (Exception e)
        {
            LOGGER.error("writeFile(): Exception writing file! Error: {}", e.getMessage());
        }

        return false;
    }

    private void dumpFile(@Nonnull CSVWrapper wrapper)
    {
        LOGGER.debug("dumpFile(): Dump file [{}]:", wrapper.getFile());

        if (wrapper.isEmpty())
        {
            LOGGER.error("dumpFile(): Wrapper is EMPTY!");
            return;
        }

        CSVHeader header = wrapper.getHeader();

        if (header == null)
        {
            LOGGER.error("dumpFile(): eader is NULL!");
            return;
        }

        LOGGER.debug("dumpFile(): Header {} // Line Size: [{}]", header.toString(), wrapper.getSize());

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

        if (this.HEADER_1 != null)
        {
            this.HEADER_1.clear();
            this.HEADER_1 = null;
        }

        if (this.HEADER_2 != null)
        {
            this.HEADER_2.clear();
            this.HEADER_2 = null;
        }
    }

    @Override
    public void close()
    {
        this.clear();
    }
}
