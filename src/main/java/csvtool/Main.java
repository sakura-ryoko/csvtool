package csvtool;

import csvtool.data.Const;
import csvtool.data.Context;
import csvtool.data.OptSettings;
import csvtool.enums.ExitCode;
import csvtool.enums.Operations;
import csvtool.enums.Settings;
import csvtool.operation.Operation;
import csvtool.utils.FileUtils;
import csvtool.utils.LogWrapper;

public class Main
{
    private static final LogWrapper LOGGER = new LogWrapper(Main.class);
    private static Context ctx;

    public static void main(String[] args)
    {
        processArgs(args);
        verifyOperation();
        processSettings();
        displayContext();
        displayOptSettings();
        executeOperations();
        exit(ExitCode.EOF);
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
        ctx = new Context(Operations.fromArg(args[argIndex]));
        Settings setting;
        argIndex++;

        if (ctx.getOp() == null)
        {
            exit(ExitCode.INVALID_OPERATION);
        }

        if (args.length > 1)
        {
            setting = Settings.fromArg(args[argIndex]);

            if (setting != null)
            {
                if (setting.needsParam())
                {
                    String param = args[argIndex];
                    argIndex++;
                    ctx = ctx.addSettings(setting, param);
                } else
                {
                    ctx = ctx.addSettings(setting, null);
                }
                argIndex++;
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
                LOGGER.error("input file [{}] does not exist!", inputStr);
                exit(ExitCode.FILE_NOT_FOUND);
            }

            ctx = ctx.setInputFile(inputStr);
        }

        if (argIndex < args.length)
        {
            for (int i = argIndex; i < args.length; i++)
            {
                setting = Settings.fromArg(args[i]);

                if (setting != null)
                {
                    if (setting.needsParam() && args.length > (i + 1))
                    {
                        String param = args[i + 1];
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
    }

   private static void displayContext()
    {
        if (!Const.DEBUG)
        {
            return;
        }

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

    private static void displayOptSettings()
    {
        if (!Const.DEBUG)
        {
            return;
        }

        OptSettings opt = ctx.getOpt();
        System.out.print("Optional Settings:\n");

        if (opt.hasInput2())
        {
            System.out.printf(" Input2: [%s] // applied [%s]\n", opt.getInput2(), ctx.getSettingValue(Settings.INPUT2));
        }
        else
        {
            System.out.print(" Input2: [NOT_SET]\n");
        }

        if (opt.hasOutput())
        {
            System.out.printf(" Output: [%s] // applied [%s]\n", opt.getOutput(), ctx.getSettingValue(Settings.OUTPUT));
        }
        else
        {
            System.out.print(" Output: [NOT_SET]\n");
        }

        if (opt.hasHeaders())
        {
            System.out.printf(" Headers: [%s] // applied [%s]\n", opt.getHeadersConfig(), ctx.getSettingValue(Settings.HEADERS));
        }
        else
        {
            System.out.print(" Headers: [NOT_SET]\n");
        }

        if (opt.hasKey())
        {
            System.out.printf(" Key: [%s] // applied [%s]\n", opt.getKey(), ctx.getSettingValue(Settings.KEY));
        }
        else
        {
            System.out.print(" Key: [NOT_SET]\n");
        }

        //System.out.printf(" UTF8: [%s]\n", opt.isUtf8());
        System.out.printf(" Apply Quotes: [%s]\n", opt.isApplyQuotes());
        System.out.printf(" Append Output: [%s]\n", opt.isAppendOutput());
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
            exit(ExitCode.MISSING_INPUT);
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
                LOGGER.debug("processSettings(): entry [{}], value [{}]", entry.getName(), ctx.getSettingValue(entry));

                switch (entry)
                {
                    case INPUT2 -> opt.setInput2(ctx.getSettingValue(entry));
                    case OUTPUT -> opt.setOutput(ctx.getSettingValue(entry));
                    case KEY -> opt.setKey(ctx.getSettingValue(entry));
                    case HEADERS -> opt.setHeadersConfig(ctx.getSettingValue(entry));
                    //case UTF8 -> opt.setUtf8(true);
                    case QUOTES -> opt.setApplyQuotes(false);
                    case APPEND -> opt.setAppendOutput(false);
                    case TEST -> {}
                }
            }
        }

        ctx = ctx.setOptSettings(opt);
    }

    private static void executeOperations()
    {
        if (ctx.getOp() != Operations.HELP
                && (ctx.getInputFile() == null || ctx.getInputFile().isEmpty()))
        {
            exit(ExitCode.MISSING_INPUT);
        }

        Operation type = ctx.getOp().init();

        if (type != null && type.runOperation(ctx))
        {
            LOGGER.info("Operation Successful.");
            exit(ExitCode.SUCCESS);
        }
        else
        {
            LOGGER.error("Operation FAILED.");
            exit(ExitCode.OPERATION_FAILURE);
        }
    }
}