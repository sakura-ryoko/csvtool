package csvtool.operation;

import csvtool.data.Context;
import csvtool.enums.Operations;
import csvtool.utils.LogWrapper;

public class OperationEmpty extends Operation
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());

    public OperationEmpty(Operations op)
    {
        super(op);
    }

    @Override
    public boolean runOperation(Context ctx)
    {
        LOGGER.debug("runOperation(): Empty()");
        return true;
    }
}
