package csvtool.operation;

import csvtool.data.Context;
import csvtool.enums.Operations;
import csvtool.utils.LogWrapper;

public class OperationHeaderSave extends Operation
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());

    public OperationHeaderSave(Operations op)
    {
        super(op);
    }

    @Override
    public boolean runOperation(Context ctx)
    {
        LOGGER.debug("runOperation(): HeaderSave");
        return true;
    }

    @Override
    public void displayHelp()
    {
        System.out.print("HeaderSave Operation:\n");
        System.out.printf("\tAliases: %s\n\n", Operations.HEADER_SAVE.getAlias().toString());
    }
}
