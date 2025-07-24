package csvtool.operation;

import csvtool.enums.Operations;

import javax.annotation.Nullable;

public class OperationType<T extends Operation>
{
    //private final LogWrapper LOGGER = new LogWrapper(getClass());

    public static final OperationType<OperationHelp>  HELP;
    public static final OperationType<OperationTest> TEST;
    public static final OperationType<OperationMerge> MERGE;
    public static final OperationType<OperationDedupe> DEDUPE;
//    public static final OperationType<OperationSplit> SPLIT;
    public static final OperationType<OperationDiff> DIFF;
    public static final OperationType<OperationHeaderAdd> HEADER_ADD;
    public static final OperationType<OperationHeaderSave> HEADER_SAVE;
    public static final OperationType<OperationTransformSave> TRANSFORM_SAVE;
    public static final OperationType<OperationReformat> REFORMAT;
    public static final OperationType<OperationSerialize> SERIALIZE;
    public static final OperationType<OperationTransformExpand> TRANSFORM_EXPAND;
//    public static final OperationType<OperationTransformSquash> TRANSFORM_SQUASH;
    public static final OperationType<OperationFilePivot> FILE_PIVOT;
    public static final OperationType<OperationFilePivotSave> FILE_PIVOT_SAVE;
    private final OperationFactory<? extends T> factory;
    private final Operations ops;

    private static <T extends Operation> OperationType<T> create(OperationFactory<? extends T> factory, Operations ops)
    {
        return new OperationType<>(factory, ops);
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
        DEDUPE = create(OperationDedupe::new, Operations.DEDUPE);
//        SPLIT = create(OperationSplit::new, Operations.SPLIT);
        DIFF = create(OperationDiff::new, Operations.DIFF);
        HEADER_ADD = create(OperationHeaderAdd::new, Operations.HEADER_SAVE);
        HEADER_SAVE = create(OperationHeaderSave::new, Operations.HEADER_SAVE);
        TRANSFORM_SAVE = create(OperationTransformSave::new, Operations.TRANSFORM_SAVE);
        REFORMAT = create(OperationReformat::new, Operations.REFORMAT);
        SERIALIZE = create(OperationSerialize::new, Operations.SERIALIZE);
        TRANSFORM_EXPAND = create(OperationTransformExpand::new, Operations.TRANSFORM_EXPAND);
//        TRANSFORM_SQUASH = create(OperationTransformSquash::new, Operations.TRANSFORM_SQUASH);
        FILE_PIVOT = create(OperationFilePivot::new, Operations.FILE_PIVOT);
        FILE_PIVOT_SAVE = create(OperationFilePivotSave::new, Operations.FILE_PIVOT_SAVE);
    }

    @FunctionalInterface
    interface OperationFactory<T extends Operation>
    {
        T create(Operations ops);
    }
}
