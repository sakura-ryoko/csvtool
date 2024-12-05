package csvtool.operation;

import csvtool.data.Context;
import csvtool.enums.Operations;
import csvtool.utils.LogWrapper;

public abstract class Operation
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());

    public Operation(Operations op)
    {
        LOGGER.debug("new abstract Operation for op [{}]", op.getName());
    }

    public boolean runOperation(Context ctx)
    {
        LOGGER.debug("runOperation() [SUPER]");
        return false;
    }
}
