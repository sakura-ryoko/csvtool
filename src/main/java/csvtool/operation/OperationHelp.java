package csvtool.operation;

import csvtool.data.Const;
import csvtool.data.Context;
import csvtool.enums.Operations;
import csvtool.utils.LogUtils;

public class OperationHelp extends Operation
{
    private final LogUtils LOGGER = new LogUtils(this.getClass());

    public OperationHelp(Operations op)
    {
        super(op);
    }

    @Override
    public boolean runOperation(Context ctx)
    {
        LOGGER.debug("displayHelp()");
        this.displayHelp();
        return true;
    }

    private void displayHelp()
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
}
