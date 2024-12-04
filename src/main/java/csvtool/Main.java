package csvtool;

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
            } else
            {
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
                    } else
                    {
                        ctx = ctx.addSettings(setting, null);
                    }
                }
            }
        }

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
            LOGGER.fatal("Operation FAILED.");
            exit(ExitCode.OPERATION_FAILURE);
        }
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