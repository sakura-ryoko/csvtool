package csvtool.operation;

import csvtool.enums.Operations;

import javax.annotation.Nullable;

public class OperationType<T extends Operation>
{
    //private final LogUtils LOGGER = new LogUtils(getClass());

    public static final OperationType<OperationHelp>  HELP;
    public static final OperationType<OperationEmpty> TEST;
    public static final OperationType<OperationEmpty> MERGE;
    public static final OperationType<OperationEmpty> DIFF;
    public static final OperationType<OperationEmpty> GEN;
    public static final OperationType<OperationEmpty> REFORMAT;
    private final OperationFactory<? extends T> factory;
    private final Operations op;

    @SuppressWarnings("unchecked")
    private static <T extends Operation> OperationType<T> create(OperationFactory<? extends T> factory, Operations op)
    {
        return (OperationType) new OperationType(factory, op);
    }

    private OperationType(OperationFactory<? extends T> factory, Operations op)
    {
        this.op = op;
        this.factory = factory;
    }

    @Nullable
    public T init(Operations op)
    {
        return this.factory.create(op);
    }

    public Operations getOp()
    {
        return this.op;
    }

    static
    {
        HELP = create(OperationHelp::new, Operations.HELP);
        TEST = create(OperationEmpty::new, Operations.TEST);
        MERGE = create(OperationEmpty::new, Operations.MERGE);
        DIFF = create(OperationEmpty::new, Operations.DIFF);
        GEN = create(OperationEmpty::new, Operations.SAVE_HEADER);
        REFORMAT = create(OperationEmpty::new, Operations.REFORMAT);
    }

    @FunctionalInterface
    interface OperationFactory<T extends Operation>
    {
        T create(Operations op);
    }
}
