package csvtool.operation;

import csvtool.data.Context;
import csvtool.data.FileCache;
import csvtool.enums.Operations;
import csvtool.enums.Settings;
import csvtool.utils.LogWrapper;
import csvtool.utils.StringUtils;

import java.util.HashMap;
import java.util.List;

public class OperationDedupe extends Operation implements AutoCloseable
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());

    private FileCache FILE;
    private final FileCache OUT;
    private final FileCache FILE_DUPES;
    private int keyId1;
    private int keyId2;
    private int keyId3;

    private final HashMap<String, Integer> key1Map;
    private final HashMap<String, Integer> key2Map;
    private final HashMap<String, Integer> key3Map;

    public OperationDedupe(Operations op)
    {
        super(op);
        this.FILE = new FileCache();
        this.OUT = new FileCache();
        this.FILE_DUPES = new FileCache();
        this.keyId1 = -1;
        this.keyId2 = -1;
        this.keyId3 = -1;
        this.key1Map = new HashMap<>();
        this.key2Map = new HashMap<>();
        this.key3Map = new HashMap<>();
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
            LOGGER.error("runOperation(): Dedupe FAILED, an output is required.");
            return false;
        }

        if (!ctx.getOpt().hasKey())
        {
            LOGGER.error("runOperation(): Dedupe FAILED, a key is required.");
            this.clear();
            return false;
        }

        LOGGER.debug("runOperation(): --> DEDUPE [{}] using key [{}].", ctx.getInputFile(), ctx.getSettingValue(Settings.OUTPUT), ctx.getSettingValue(Settings.KEY));

        if (this.readFiles(ctx.getInputFile(), false, ctx.getOpt().isDebug()))
        {
            LOGGER.debug("runOperation(): --> File [{}] read successfully.", ctx.getInputFile());

            if (!ctx.getOpt().hasKey())
            {
                LOGGER.error("runOperation(): Dedupe FAILED, first key is not defined!");
                this.clear();
                return false;
            }

            if (!ctx.getOpt().hasOutput())
            {
                LOGGER.error("runOperation(): Dedupe FAILED, output is not defined!");
                this.clear();
                return false;
            }

            this.OUT.setHeader(this.FILE.getHeader());
            this.OUT.setFileName(ctx.getOpt().getOutput());
            this.keyId1 = this.FILE.getHeader().getId(ctx.getSettingValue(Settings.KEY));

            if (this.keyId1 < 0)
            {
                LOGGER.error("runOperation(): Dedupe FAILED, key was NOT found in the Headers.");
                this.clear();
                return false;
            }

            if (ctx.getOpt().hasKey2())
            {
                this.keyId2 = this.FILE.getHeader().getId(ctx.getSettingValue(Settings.KEY2));

                if (this.keyId2 < 0)
                {
                    LOGGER.error("runOperation(): Dedupe FAILED, key2 was NOT found in the Headers.");
                    this.clear();
                    return false;
                }
            }

            if (ctx.getOpt().hasKey3())
            {
                this.keyId3 = this.FILE.getHeader().getId(ctx.getSettingValue(Settings.KEY3));

                if (this.keyId3 < 0)
                {
                    LOGGER.error("runOperation(): Dedupe FAILED, key3 was NOT found in the Headers.");
                    this.clear();
                    return false;
                }
            }

            if (!this.deDupeFiles(true, ctx.getOpt().isSquashDupe()))
            {
                LOGGER.error("runOperation(): Dedupe FAILED, DeDuplication attempt has failed.");
                this.clear();
                return false;
            }

            // Write DUPES file, if anything was found
            if (!this.FILE_DUPES.isEmpty() && ctx.getOpt().getOutput() != null)
            {
                String dupesFile = StringUtils.addFileSuffix(ctx.getOpt().getOutput(), "-dupes");
                this.FILE_DUPES.setFileName(dupesFile);

                if (this.writeFile(this.FILE_DUPES, ctx.getOpt().isApplyQuotes(), false, ctx.getOpt().isDebug(), null))
                {
                    LOGGER.debug("runOperation(): --> Dupes File [{}] written successfully.", dupesFile);
                }
                else
                {
                    LOGGER.error("runOperation(): Write dupes file FAILED.");
                }
            }

            if (this.writeFile(this.OUT, ctx.getOpt().isApplyQuotes(), false, ctx.getOpt().isDebug(), null))
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

        LOGGER.error("runOperation(): General Operation failure.");
        return false;
    }

    @Override
    public void displayHelp()
    {
        System.out.print("Dedupe Operation:\n");
        System.out.printf("\tAliases: %s\n\n", Operations.DEDUPE.getAlias().toString());

        System.out.print("This operation de-dupes two files into a single output file.\n");
        System.out.print("It accepts one input file, and an output (--output).\n");
        System.out.print("You can use multiple key fields (--key, --key2, --key3) which can be set.\n");
        System.out.print("Optionally, you can enable (--squash-dupe) which combines de-duplicated data values.\n");
        System.out.print("De-Dupe compares the files, and removes duplicate rows based on the key field(s) given.\n");
    }

    private boolean readFiles(String file1, boolean ignoreQuotes, boolean debug)
    {
        LOGGER.debug("readFiles(): Reading file1 [{}] ...", file1);

        this.FILE = this.readFile(file1, ignoreQuotes, debug);

        if (this.FILE == null || this.FILE.isEmpty())
        {
            LOGGER.error("readFiles(): Input File Cache is Empty!");
            return false;
        }

        return true;
    }

    private boolean deDupeFiles(boolean skipHeader, boolean squash)
    {
        if (this.FILE.isEmpty() || this.keyId1 < 0)
        {
            LOGGER.error("deDupeFiles(): File is empty, or Key not set!");
            return false;
        }

        LOGGER.debug("deDupeFiles(): Attempting to execute De-Dupe by removing duplicates from file ...");
//        List<List<String>> temp = new ArrayList<>();
//        List<Integer> dupes = new ArrayList<>();
        int dupes = 0;
        this.OUT.setHeader(this.FILE.getHeader());
        this.FILE_DUPES.setHeader(this.FILE.getHeader());

        for (int i = 0; i < this.FILE.getFile().size(); i++)
        {
            List<String> entry = this.FILE.getFile().get(i);

            if (!entry.isEmpty() && (!skipHeader || i > 0))
            {
                if (this.checkKeyMaps(entry, i))
                {
                    // Attempt to squash values
                    if (squash)
                    {
                        String key1 = entry.get(this.keyId1);
                        int existing = this.key1Map.get(key1);
                        List<String> currentLine = this.OUT.getFile().get(existing);

                        if (currentLine.get(this.keyId1).equals(key1))
                        {
                            // Squash
                            List<String> newLine = this.squashLines(currentLine, entry);

                            if (!newLine.equals(currentLine))
                            {
                                LOGGER.debug("FILE [{}]: SQUASHED LINE: [{}//{}] --> [{}]", existing, currentLine, newLine);
                                this.OUT.getFile().put(existing, newLine);
                            }
                        }
                    }

                    LOGGER.debug("FILE [{}]: LINE: [{}] --> DUPE FOUND", i, entry.toString());
                    this.FILE_DUPES.addLine(entry);
                    dupes++;
                }
                else
                {
                    this.OUT.addLine(entry);
                }

//                String key = entry.get(this.keyId1);
//
//                LOGGER.debug("FILE1[{}]: key [{}] checking FILE2 ...", i, key);
//
//                for (int j = 0; j < temp.size(); j++)
//                {
//                    List<String> entry2 = temp.get(j);
//
//                    if (!entry2.isEmpty())
//                    {
//                        String key2 = entry2.get(this.keyId1);
//
//                        LOGGER.debug("FILE [{}/{}]: key [{}] / key2 [{}]", i, j, key, key2);
//
//                        if (key2.equals(key) && (!skipHeader || j > 0))
//                        {
//                            LOGGER.info("FILE2[{}]: skipping duplicate ...", j);
//                            this.FILE_DUPES.addLine(entry2);
//                            dupes.add(j);
//                        }
//                    }
//                }
            }
        }

        LOGGER.debug("deDupeFiles(): {} dupes found (IN: {}, OUT: {}, DUPES: {})",
                dupes, this.FILE.getFile().size(), this.OUT.getFile().size(), this.FILE_DUPES.getFile().size());

//        LOGGER.debug("deDupeFiles(): Restoring and removing duplicates from file2 (Count: {} dupes found)", dupes.size());
//        int pos = 0;
//
//        // Copy Back
//        for (int i = 0; i < temp.size(); i++)
//        {
//            if (!dupes.contains(i) || (skipHeader && i == 0))
//            {
//                LOGGER.debug("LINE[{}]: put {}", pos, temp.get(i).toString());
////                this.FILE_2.getFile().put(pos, temp.get(i));
//                pos++;
//            }
//        }

        return true;
    }

    // Returns true if it's a duplicate match
    private boolean checkKeyMaps(List<String> data, int line)
    {
        if (this.keyId3 > -1)
        {
            final int key3match = this.calcKey3Map(data.get(this.keyId1), data.get(this.keyId2), data.get(this.keyId3), line);
            LOGGER.debug("checkKeyMaps(): LINE[{}]: key3match: [{}]", line, key3match);
            return key3match > -1;
        }

        if (this.keyId2 > -1)
        {
            final int key2match = this.calcKey2Map(data.get(this.keyId1), data.get(this.keyId2), line);
            LOGGER.debug("checkKeyMaps(): LINE[{}]: key2match: [{}]", line, key2match);
            return key2match > -1;
        }

        final int key1match = this.calcKey1Map(data.get(this.keyId1), line);
        LOGGER.debug("checkKeyMaps(): LINE[{}]: key1match: [{}]", line, key1match);
        return key1match > -1;
    }

    // -1 means it was just added
    private int calcKey1Map(String key, int line)
    {
        if (this.key1Map.containsKey(key))
        {
            return this.key1Map.get(key);
        }

        this.key1Map.put(key, line);
        return -1;
    }

    // -1 means it was just added
    private int calcKey2Map(String key1, String key2, int line)
    {
        int key1line = this.calcKey1Map(key1, line);

        if (key1line < 0)
        {
            this.key2Map.remove(key2);
        }

        if (this.key2Map.containsKey(key2))
        {
            return this.key2Map.get(key2);
        }

        this.key2Map.put(key2, line);
        return -1;
    }

    // -1 means it was just added
    private int calcKey3Map(String key1, String key2, String key3, int line)
    {
        int key2line = this.calcKey2Map(key1, key2, line);

        if (key2line < 0)
        {
            this.key3Map.remove(key3);
        }

        if (this.key3Map.containsKey(key3))
        {
            return this.key3Map.get(key3);
        }

        this.key3Map.put(key3, line);
        return -1;
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

        if (this.FILE_DUPES != null && !this.FILE_DUPES.isEmpty())
        {
            this.FILE_DUPES.clear();
        }

        this.key1Map.clear();
        this.key2Map.clear();
        this.key3Map.clear();
    }

    @Override
    public void close()
    {
        this.clear();
    }
}
