package csvtool.operation;

import csvtool.data.Context;
import csvtool.enums.Operations;
import csvtool.utils.LogWrapper;

public class OperationReformat extends Operation
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());

    public OperationReformat(Operations op)
    {
        super(op);
    }

    @Override
    public boolean runOperation(Context ctx)
    {
        LOGGER.debug("runOperation(): Reformat");
        return true;
    }

    @Override
    public void displayHelp()
    {
        System.out.print("Reformat Operation:\n");
        System.out.printf("\tAliases: %s\n\n", Operations.REFORMAT.getAlias().toString());
    }

    @Override
    public void clear()
    {
        // NO-OP
    }
}
