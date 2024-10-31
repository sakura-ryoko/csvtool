package csvtool;

import csvtool.data.Const;
import csvtool.data.Context;
import csvtool.enums.ExitCode;
import csvtool.enums.Operation;
import csvtool.enums.Settings;
import csvtool.utils.FileUtils;

public class Main
{
    private static Context ctx;

    public static void main(String[] args)
    {
        processArgs(args);
        displayContext();
        verifyOperation();

        exit(ExitCode.SUCCESS);
    }

    private static void exit(ExitCode code)
    {
        System.out.printf("%s\n", code.getMessage());
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
        /*
        for (int i = 0; i < args.length; i++)
        {
            System.out.printf("ARG[%d]: %s\n", i, args[i]);
        }
         */

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
        String input1Str = args[argIndex];
        argIndex++;

        if (FileUtils.fileExists(input1Str))
        {
            System.out.print("input1 file exists.\n");
        }
        else
        {
            exit(ExitCode.FILE_NOT_FOUND);
        }

        ctx = ctx.setInputFile1(input1Str);

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
        // Get Secondary Input
        if (ctx.getOp().needsInput() && args.length > argIndex)
        {
            String input2Str = args[argIndex];
            argIndex++;

            if (FileUtils.fileExists(input2Str))
            {
                System.out.print("input2 file exists.\n");
            }
            else
            {
                exit(ExitCode.FILE_NOT_FOUND);
            }

            ctx = ctx.setInputFile2(input2Str);
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
        // Get Output File
        if (ctx.getOp().needsOutput() && args.length > argIndex)
        {
            String outputStr = args[argIndex];
            argIndex++;

            if (FileUtils.fileExists(outputStr))
            {
                System.out.print("output file exists.\n");
            }
            else
            {
                System.out.print("output file does not exist.\n");
            }

            ctx = ctx.setOutputFile(outputStr);
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
        // Get Key
        if (ctx.getOp().needsKey() && args.length > argIndex)
        {
            String key = args[argIndex];
            argIndex++;
            ctx = ctx.setKey(key);
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
        // Get Headers
        if (ctx.getOp().needsHeaders() && args.length > argIndex)
        {
            String headers = args[argIndex];
            argIndex++;
            ctx = ctx.setHeaders(headers);
        }

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
        System.out.printf("Usage: %s [--operation] <input> [input2] [output] [headers]\n", Const.ID);
        System.out.print("Operations:\n");
        System.out.print("  --help:     This Screen\n");
        System.out.print("  --test:     Test Routines [params: (input file)]\n");
        System.out.print("  --merge:    Merge Two CSV Files [params: (input file) (input file 2) (output file) (key_field)]\n");
        System.out.print("  --diff:     Diff Two CSV Files  [params: (input file) (input file 2) (output file) (key_field)]\n");
        System.out.print("  --reformat: Reformat A CSV File [params: (input file) (output file) (headers)]\n");
        System.out.print("Optional Settings (Cannot be listed before the operation, but it is accepted anywhere afterwards):\n");
        System.out.print("  --utf8:     Sets the CSV Input for UTF-8 Format\n");
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
        System.out.printf("InputFile1: %s\n", ctx.getInputFile1());
        System.out.printf("InputFile2: %s\n", ctx.getInputFile2());
        System.out.printf("OutputFile: %s\n", ctx.getOutputFile());
        System.out.printf("Key Field: %s\n",  ctx.getKey());
        System.out.printf("Headers: %s\n",    ctx.getHeaders());
    }

    private static void verifyOperation()
    {
        if (ctx.getOp().needsInput() && ctx.getInputFile2().isEmpty())
        {
            exit(ExitCode.MISSING_INPUT2);
        }
        else if (ctx.getOp().needsOutput() && ctx.getOutputFile().isEmpty())
        {
            exit(ExitCode.MISSING_OUTPUT);
        }
        else if (ctx.getOp().needsKey() && ctx.getKey().isEmpty())
        {
            exit(ExitCode.MISSING_KEY);
        }
        else if (ctx.getOp().needsHeaders() && ctx.getHeaders().isEmpty())
        {
            exit(ExitCode.MISSING_HEADERS);
        }
    }
}