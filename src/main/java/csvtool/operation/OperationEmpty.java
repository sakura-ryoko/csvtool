package csvtool.operation;

import csvtool.data.Context;
import csvtool.enums.Operations;
import csvtool.utils.LogUtils;

public class OperationEmpty extends Operation
{
    private final LogUtils LOGGER = new LogUtils(this.getClass());

    public OperationEmpty(Operations op)
    {
        super(op);
    }

    @Override
    public boolean runOperation(Context ctx)
    {
        LOGGER.debug("Empty()");
        return true;
    }
}
