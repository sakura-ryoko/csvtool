package csvtool;

import csvtool.enums.ExitCode;
import csvtool.enums.Operation;

public class Main
{
    private static Operation operation;

    public static void main(String[] args)
    {
        processArgs(args);
    }

    private static void processArgs(String[] args)
    {
        if (args.length < 1)
        {
            System.out.print("Invalid Syntax [Not enough Args]\n");
            System.exit(ExitCode.INVALID_SYNTAX.get());
        }

        operation = Operation.fromArgStatic(args[0]);

        if (operation == null)
        {
            System.out.printf("Invalid Operation [\"%s\"]\n", args[0]);
            System.exit(ExitCode.INVALID_OPERATION.get());
        }

        for (int i = 1; i < args.length; i++)
        {
            System.out.printf("ARG[%d]: %s\n", i, args[i]);
        }

        System.out.printf("Operation: %s\n", operation.getName());
        System.out.print("All Args Processed.\n");
        System.exit(ExitCode.SUCCESS.get());
    }
}