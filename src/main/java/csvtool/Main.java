package csvtool;

import csvtool.data.Const;
import csvtool.data.Context;
import csvtool.data.OptSettings;
import csvtool.enums.ExitCode;
import csvtool.enums.Operation;
import csvtool.enums.Settings;
import csvtool.utils.FileUtils;
import csvtool.utils.LogUtils;

public class Main
{
    private static final LogUtils LOGGER = new LogUtils(Main.class);
    private static Context ctx;

    public static void main(String[] args)
    {
        processArgs(args);
        displayContext();
        verifyOperation();
        processSettings();

        exit(ExitCode.SUCCESS);
    }

    private static void exit(ExitCode code)
    {
        LOGGER.fatal("EXIT: {}", code.getMessage());
        System.exit(code.get());
    }

    private static void processArgs(String[] args)
    {
        int argIndex = 0;

        if (args.length < 1)
        {
            exit(ExitCode.INVALID_SYNTAX);
        }

        // Debug args
        for (int i = 0; i < args.length; i++)
        {
           LOGGER.debug("ARG[{}]: {}", i, args[i]);
        }

        // Get Operation
        ctx = new Context(Operation.fromArg(args[argIndex]));
        Settings setting;
        argIndex++;

        if (ctx.getOp() == null)
        {
            exit(ExitCode.INVALID_OPERATION);
        }
        else if (ctx.getOp() == Operation.HELP)
        {
            displayHelp();
            exit(ExitCode.SUCCESS);
        }

        if (args.length < 2)
        {
            exit(ExitCode.INVALID_SYNTAX);
        }

        if (args.length > argIndex)
        {
            setting = Settings.fromArg(args[argIndex]);
            if (setting != null)
            {
                if (setting.needsParam())
                {
                    String param = args[argIndex];
                    argIndex++;
                    ctx = ctx.addSettings(setting, param);
                }
                else
                {
                    ctx = ctx.addSettings(setting, null);
                }
                argIndex++;
            }
        }

        // Get Input File
        String inputStr = args[argIndex];
        argIndex++;

        if (FileUtils.fileExists(inputStr))
        {
            LOGGER.debug("input file [{}] exists.", inputStr);
        }
        else
        {
            exit(ExitCode.FILE_NOT_FOUND);
        }

        ctx = ctx.setInputFile(inputStr);

        if (argIndex >= args.length)
        {
            return;
        }
        for (int i = argIndex; i < args.length; i++)
        {
            setting = Settings.fromArg(args[i]);

            if (setting != null)
            {
                if (setting.needsParam() && args.length > (i + 1))
                {
                    String param = args[i+1];
                    ctx = ctx.addSettings(setting, param);
                    i++;
                }
                else
                {
                    ctx = ctx.addSettings(setting, null);
                }
            }
        }
    }

    private static void displayHelp()
    {
        System.out.printf("%s --\n", Const.STR);
        System.out.printf("(Author: %s)\n\n", Const.AUTHOR);
        System.out.printf("Usage: %s [--operation] <input> [settings]\n", Const.ID);
        System.out.print("Operations:\n");
        System.out.print("  --help:     This Screen\n");
        System.out.print("  --test:     Test Routines [params: (input file)]\n");
        System.out.print("  --merge:    Merge Two CSV Files [requires: (input) (output) (key_field)]\n");
        System.out.print("  --diff:     Diff Two CSV Files  [requires: (input) (output) (key_field)]\n");
        System.out.print("  --reformat: Reformat A CSV File [requires: (input) (output) (headers)]\n");
        System.out.print("Settings (Cannot be listed before the operation, but it is accepted anywhere afterwards):\n");
        System.out.print("  --utf8:\nSets the CSV Input for UTF-8 Format\n");
        System.out.print("  --input (file):\nSets the CSV Input File #2\n");
        System.out.print("  --output (file):\nSets the CSV Input for UTF-8 Format\n");
        System.out.print("  --headers (file):\nSets the CSV Input for UTF-8 Format\n");
        System.out.print("  --key (key):\nSets the CSV Input for UTF-8 Format\n");
    }

    private static void displayContext()
    {
        System.out.printf("Operation: %s\n",  ctx.getOp().getName());
        System.out.print("Settings:");
        if (ctx.getSettings() == null)
        {
            System.out.print(" <EMPTY>\n");
        }
        else
        {
            ctx.getSettings().forEach((s, v) ->
                    {
                        if (v.isEmpty())
                        {
                            System.out.printf(" %s", s.toString());
                        }
                        else
                        {
                            System.out.printf(" %s [p: %s]", s.toString(), v);
                        }
                    });
            System.out.print("\n");
        }
        System.out.printf("InputFile: %s\n", ctx.getInputFile());
    }

    private static boolean isSettingMissing(Settings entry)
    {
        if (ctx.getSettings() != null)
        {
            return !ctx.getSettings().containsKey(entry);
        }

        return true;
    }

    private static void verifyOperation()
    {
        if (ctx.getOp().needsInput() && isSettingMissing(Settings.INPUT2))
        {
            exit(ExitCode.MISSING_INPUT2);
        }
        else if (ctx.getOp().needsOutput() && isSettingMissing(Settings.OUTPUT))
        {
            exit(ExitCode.MISSING_OUTPUT);
        }
        else if (ctx.getOp().needsKey() && isSettingMissing(Settings.KEY))
        {
            exit(ExitCode.MISSING_KEY);
        }
        else if (ctx.getOp().needsHeaders() && isSettingMissing(Settings.HEADERS))
        {
            exit(ExitCode.MISSING_HEADERS);
        }
    }

    private static void processSettings()
    {
        OptSettings opt = new OptSettings();

        if (ctx.getSettings() != null)
        {
            for (Settings entry : ctx.getSettings().keySet())
            {
                switch (entry)
                {
                    case INPUT2 -> opt.setInput2(entry.getSetting());
                    case OUTPUT -> opt.setOutput(entry.getSetting());
                    case KEY -> opt.setKey(entry.getSetting());
                    case HEADERS -> opt.setHeadersConfig(entry.getSetting());
                    case UTF8 -> opt.setUtf8(true);
                    case TEST -> {}
                }
            }
        }

        ctx = ctx.setOptSettings(opt);
    }
}