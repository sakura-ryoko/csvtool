package csvtool.operation;

import csvtool.enums.Operations;

import javax.annotation.Nullable;

public class OperationType<T extends Operation>
{
    //private final LogWrapper LOGGER = new LogWrapper(getClass());

    public static final OperationType<OperationHelp>  HELP;
    public static final OperationType<OperationTest> TEST;
    public static final OperationType<OperationMerge> MERGE;
    public static final OperationType<OperationDiff> DIFF;
    public static final OperationType<OperationEmpty> GEN;
    public static final OperationType<OperationEmpty> REFORMAT;
    private final OperationFactory<? extends T> factory;
    private final Operations ops;

    @SuppressWarnings("unchecked")
    private static <T extends Operation> OperationType<T> create(OperationFactory<? extends T> factory, Operations ops)
    {
        return (OperationType) new OperationType(factory, ops);
    }

    private OperationType(OperationFactory<? extends T> factory, Operations ops)
    {
        this.ops = ops;
        this.factory = factory;
    }

    @Nullable
    public T init(Operations op)
    {
        return this.factory.create(op);
    }

    public Operations getOps()
    {
        return this.ops;
    }

    static
    {
        HELP = create(OperationHelp::new, Operations.HELP);
        TEST = create(OperationTest::new, Operations.TEST);
        MERGE = create(OperationMerge::new, Operations.MERGE);
        DIFF = create(OperationDiff::new, Operations.DIFF);
        GEN = create(OperationEmpty::new, Operations.SAVE_HEADER);
        REFORMAT = create(OperationEmpty::new, Operations.REFORMAT);
    }

    @FunctionalInterface
    interface OperationFactory<T extends Operation>
    {
        T create(Operations ops);
    }
}
