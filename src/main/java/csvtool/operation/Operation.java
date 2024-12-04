package csvtool.operation;

import csvtool.data.Context;
import csvtool.enums.Operations;
import csvtool.utils.LogUtils;

public abstract class Operation
{
    private final LogUtils LOGGER = new LogUtils(this.getClass());

    public Operation(Operations op)
    {
        LOGGER.debug("new Operation for op [{}]", op.getName());
    }

    public boolean runOperation(Context ctx) { return false; }
}
