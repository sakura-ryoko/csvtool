package csvtool.operation;

import csvtool.data.Context;
import csvtool.data.FileCache;
import csvtool.enums.Operations;
import csvtool.enums.Settings;
import csvtool.utils.LogWrapper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class OperationSerialize extends Operation implements AutoCloseable
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());

    private FileCache FILE;
    private final FileCache OUT;
    private int keyId1;
    private int keyId2;
    private int keyId3;
    private int keyId4;
    private int keyId5;

    private final HashMap<Integer, List<String>> keyMap;

    public OperationSerialize(Operations op)
    {
        super(op);
        this.FILE = new FileCache();
        this.OUT = new FileCache();
        this.keyId1 = -1;
        this.keyId2 = -1;
        this.keyId3 = -1;
        this.keyId4 = -1;
        this.keyId5 = -1;
        this.keyMap = new HashMap<>();
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
            LOGGER.error("runOperation(): Serialize FAILED, an output is required.");
            return false;
        }

        if (!ctx.getOpt().hasKey())
        {
            LOGGER.error("runOperation(): Serialize FAILED, a key is required.");
            this.clear();
            return false;
        }

        if (!ctx.getOpt().hasSerialKey())
        {
            LOGGER.error("runOperation(): Serialize FAILED, a Serial Key is required.");
            this.clear();
            return false;
        }

        LOGGER.debug("runOperation(): --> SERIALIZE [{}] using key [{}].", ctx.getInputFile(), ctx.getSettingValue(Settings.OUTPUT), ctx.getSettingValue(Settings.KEY));

        if (this.readFiles(ctx.getInputFile(), false, ctx.getOpt().isDebug()))
        {
            LOGGER.debug("runOperation(): --> File [{}] read successfully.", ctx.getInputFile());

            if (!ctx.getOpt().hasKey())
            {
                LOGGER.error("runOperation(): Serialize FAILED, first key is not defined!");
                this.clear();
                return false;
            }

            if (!ctx.getOpt().hasOutput())
            {
                LOGGER.error("runOperation(): Serialize FAILED, output is not defined!");
                this.clear();
                return false;
            }

            this.OUT.setHeader(this.FILE.getHeader());
            this.OUT.setFileName(ctx.getOpt().getOutput());
            this.keyId1 = this.FILE.getHeader().getId(ctx.getSettingValue(Settings.KEY));

            if (this.keyId1 < 0)
            {
                LOGGER.error("runOperation(): Serialize FAILED, key was NOT found in the Headers.");
                this.clear();
                return false;
            }

            if (ctx.getOpt().hasKey2())
            {
                this.keyId2 = this.FILE.getHeader().getId(ctx.getSettingValue(Settings.KEY2));

                if (this.keyId2 < 0)
                {
                    LOGGER.error("runOperation(): Serialize FAILED, key2 was NOT found in the Headers.");
                    this.clear();
                    return false;
                }
            }

            if (ctx.getOpt().hasKey3())
            {
                this.keyId3 = this.FILE.getHeader().getId(ctx.getSettingValue(Settings.KEY3));

                if (this.keyId3 < 0)
                {
                    LOGGER.error("runOperation(): Serialize FAILED, key3 was NOT found in the Headers.");
                    this.clear();
                    return false;
                }
            }

            if (ctx.getOpt().hasKey4())
            {
                this.keyId4 = this.FILE.getHeader().getId(ctx.getSettingValue(Settings.KEY4));

                if (this.keyId4 < 0)
                {
                    LOGGER.error("runOperation(): Serialize FAILED, key4 was NOT found in the Headers.");
                    this.clear();
                    return false;
                }
            }

            if (ctx.getOpt().hasKey5())
            {
                this.keyId5 = this.FILE.getHeader().getId(ctx.getSettingValue(Settings.KEY5));

                if (this.keyId5 < 0)
                {
                    LOGGER.error("runOperation(): Serialize FAILED, key5 was NOT found in the Headers.");
                    this.clear();
                    return false;
                }
            }

            if (!this.serializeFile(this.FILE.getHeader().getId(ctx.getSettingValue(Settings.SERIAL_KEY)), ctx.getSettingValue(Settings.SERIAL_START), ctx.getSettingValue(Settings.SERIAL_END)))
            {
                LOGGER.error("runOperation(): Serialize FAILED, attempt has failed.");
                this.clear();
                return false;
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

    private boolean serializeFile(final int key, @Nullable String start, @Nullable String end)
    {
        if (key < 0)
        {
            LOGGER.error("serializeFile(): Error; SerialKey is invalid.");
            return false;
        }

        int startNum;
        int endNum;
        int currentNum;

        if (start != null)
        {
            try
            {
                startNum = Integer.parseInt(start);
            }
            catch (NumberFormatException err)
            {
                LOGGER.error("serializeFile(): Exception reading SerializeStart value; {}", err.getLocalizedMessage());
                return false;
            }
        }
        else
        {
            startNum = 1;
        }

        if (end != null)
        {
            try
            {
                endNum = Integer.parseInt(end);
            }
            catch (NumberFormatException err)
            {
                LOGGER.error("serializeFile(): Exception reading SerializeEnd value; {}", err.getLocalizedMessage());
                return false;
            }
        }
        else
        {
            endNum = Integer.MAX_VALUE;
        }

        currentNum = startNum;

        LOGGER.debug("serializeFile(): Inserting any existing serials...");

        // Scan for existing serials, place into HashMap<>
        for (int i = 1; i < this.FILE.getFile().size(); i++)
        {
            List<String> entry = this.FILE.getLine(i);
            if (entry.isEmpty()) continue;
            String serialKeyEntry = entry.get(key);

            if (!serialKeyEntry.isEmpty())
            {
                try
                {
                    int entryNum = Integer.parseInt(serialKeyEntry);
                    List<String> serialRec = this.buildSerialEntry(entry);

                    if (serialRec.isEmpty() || !this.tryAddOrCheckMap(entryNum, serialRec))
                    {
                        LOGGER.error("serializeFile(): Error inserting Serial [{}/{}] from line [{}] into map!", entryNum, serialRec.toString(), i);
                        return false;
                    }
                }
                catch (NumberFormatException err)
                {
                    LOGGER.error("serializeFile(): Exception detecting existing serials on line [{}]; {}", i, err.getLocalizedMessage());
                    return false;
                }
            }
        }

        LOGGER.debug("serializeFile(): Scanning and building new serials ...");
        this.dumpKeyMap();

        // Build Serial Entries
        for (int i = 1; i < this.FILE.getFile().size(); i++)
        {
            List<String> entry = this.FILE.getLine(i);
            if (entry.isEmpty()) continue;
            String serialKeyEntry = entry.get(key);

            if (!serialKeyEntry.isEmpty())
            {
                try
                {
                    int entryNum = Integer.parseInt(serialKeyEntry);

                    if (!this.isMapped(entryNum))
                    {
                        LOGGER.error("serializeFile(): Error parsing Serial [{}] from line [{}]; not mapped!", entryNum, i);
                        return false;
                    }

                    // Safe
                    this.OUT.addLine(entry);
                }
                catch (NumberFormatException err)
                {
                    LOGGER.error("serializeFile(): Exception reading existing serials on line [{}]; {}", i, err.getLocalizedMessage());
                    return false;
                }
            }
            else
            {
                if (currentNum >= endNum)
                {
                    LOGGER.error("serializeFile(): Serial values exhausted! [{}] has reached [{}]", currentNum, endNum);
                    return false;
                }

                // Build / Fetch Serial Num
                List<String> serialRec = this.buildSerialEntry(entry);

                if (serialRec.isEmpty())
                {
                    LOGGER.error("serializeFile(): Error building SerialRec from entry [{}] on line [{}]", entry.toString(), i);
                    return false;
                }

                int calcSerial = this.getKeyFromKeySet(serialRec);

                LOGGER.debug("serializeFile(): LINE[{}] calcSerial [{}/{}]", i, calcSerial, serialRec.toString());

                if (calcSerial == -1)
                {
                    do
                    {
                        currentNum++;

                        if (currentNum >= endNum)
                        {
                            LOGGER.error("serializeFile(): Serial values exhausted! [{}] has reached [{}]", currentNum, endNum);
                            return false;
                        }
                    }
                    while (this.isMapped(currentNum));

                    calcSerial = currentNum;
                }

                if (this.tryAddOrCheckMap(calcSerial, serialRec))
                {
                    // Insert/Check Success
                    entry.set(key, String.valueOf(calcSerial));
                    this.OUT.addLine(entry);
                }
                else
                {
                    // Error
                    LOGGER.error("serializeFile(): Serial [{}/{}] mismatch, or insert failure on line [{}]", calcSerial, serialRec, i);
                    return false;
                }
            }
        }

        LOGGER.debug("serializeFile(): Unique serial pair's detected: [{}]", this.keyMap.size());
        this.dumpKeyMap();

        // Everything good?  Okay.
        return true;
    }

    private List<String> buildSerialEntry(List<String> entry)
    {
        List<String> list = new ArrayList<>();

        list.add(entry.get(this.keyId1));

        if (this.keyId2 > -1)
        {
            list.add(entry.get(this.keyId2));
        }

        if (this.keyId3 > -1)
        {
            list.add(entry.get(this.keyId3));
        }

        if (this.keyId4 > -1)
        {
            list.add(entry.get(this.keyId4));
        }

        if (this.keyId5 > -1)
        {
            list.add(entry.get(this.keyId5));
        }

        return list;
    }

    private boolean tryAddOrCheckMap(final int key, List<String> keySet)
    {
        if (this.keyMap.containsKey(key))
        {
            if (this.keyMap.get(key).equals(keySet))
            {
                // Already exists
                return true;
            }
            else
            {
                // Clash
                LOGGER.error("tryAddToMap(): Serial [{}], already exists, but it doesn't match: keySet [{}]", key, keySet.toString());
                return false;
            }
        }

        // Add
        this.keyMap.put(key, keySet);
        return true;
    }

    private boolean isMapped(final int key)
    {
        return this.keyMap.containsKey(key);
    }

    private int getKeyFromKeySet(List<String> keys)
    {
        AtomicInteger atomic = new AtomicInteger(-1);

        this.keyMap.forEach(
                (key, map) ->
                {
                    if (map.equals(keys))
                    {
                        atomic.set(key);
                    }
                }
        );

        return atomic.get();
    }

    private void dumpKeyMap()
    {
        LOGGER.debug("dumpKeyMap() -->");

        this.keyMap.forEach(
                (key, keySet) ->
                        LOGGER.debug("key[{}], set [{}]", key, keySet.toString())
        );

        LOGGER.debug("dumpKeyMap() END");
    }

    @Override
    public void displayHelp()
    {
        System.out.print("Serialize Operation:\n");
        System.out.printf("\tAliases: %s\n\n", Operations.SERIALIZE.getAlias().toString());

//        System.out.print("It accepts one input file (--input), and an output (--output).\n");
//        System.out.print("This operation simply copies the input to the output file to test the inner-workings of this program.\n");
        System.out.print("\n");
    }

    private boolean readFiles(String file1, boolean ignoreQuotes, boolean debug)
    {
        LOGGER.debug("readFiles(): Reading file [{}] ...", file1);

        this.FILE = this.readFile(file1, ignoreQuotes, debug);

        if (this.FILE == null || this.FILE.isEmpty())
        {
            LOGGER.error("readFiles(): Input File Cache is Empty!");
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

        this.keyMap.clear();
    }

    @Override
    public void close() throws Exception
    {
        this.clear();
    }
}
