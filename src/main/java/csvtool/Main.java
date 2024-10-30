package csvtool;

import csvtool.enums.ExitCode;

public class Main
{
    public static void main(String[] args)
    {
        processArgs(args);
    }

    private static void processArgs(String[] args)
    {
        if (args.length < 1)
        {
            System.exit(ExitCode.INVALID_SYNTAX.get());
        }
        for (int i = 1; i < args.length; i++)
        {
            System.out.printf("ARG[%d]: %s\n", i, args[i]);
        }

        System.out.print("All Args Processed.\n");
        System.exit(ExitCode.SUCCESS.get());
    }
}