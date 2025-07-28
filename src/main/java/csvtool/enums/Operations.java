package csvtool.enums;

import csvtool.operation.Operation;
import csvtool.operation.OperationType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public enum Operations
{
    HELP                ("help",             "--help",              false, false, false, false, OperationType.HELP,               List.of("-h", "-help")),
    TEST                ("test",             "--test",              false, true , false, false, OperationType.TEST,               List.of("-test", "-t", "--t")),
    MERGE               ("merge",            "--merge",             true,  true,  false, false, OperationType.MERGE,              List.of("-merge", "-m", "--m")),
    DEDUPE              ("dedupe",           "--dedupe",            false, true,  true,  false, OperationType.DEDUPE,             List.of("-dedupe", "-ded", "--ded")),
//    SPLIT               ("split",            "--split",             false, true, false,false, OperationType.SPLIT,              List.of("-split", "-sp", "--sp", "-s", "--s")),
    DIFF                ("diff",             "--diff",              true,  true,  true,  false, OperationType.DIFF,               List.of("-diff", "-d", "--d")),
    HEADER_ADD          ("header-add",       "--header-add",        false, true,  false, true,  OperationType.HEADER_ADD,         List.of("-header-add", "--add", "-add")),
    HEADER_SAVE         ("header-save",      "--header-save",       false, false, false, true,  OperationType.HEADER_SAVE,        List.of("-header-save", "--save", "-save", "--sv", "-sv")),
    JOIN                ("join",             "--join",              true,  true,  true,  false, OperationType.JOIN,               List.of("-join", "--vl", "-vl")),
    REFORMAT            ("reformat",         "--reformat",          false, true,  false, true,  OperationType.REFORMAT,           List.of("-reformat", "--ref", "-ref", "--rf", "-rf")),
    SERIALIZE           ("serialize",        "--serialize",         false, true,  true,  false, OperationType.SERIALIZE,          List.of("-serialize", "--ser", "-ser")),
    TRANSFORM_SAVE      ("transform-save",   "--transform-save",    false, false, false, true,  OperationType.TRANSFORM_SAVE,     List.of("-transform-save", "--tfs", "-tfs")),
    TRANSFORM_EXPAND    ("transform-expand", "--transform-expand",  false, true,  true,  true,  OperationType.TRANSFORM_EXPAND,   List.of("-transform-expand", "--expand", "--exp", "-exp")),
    FILE_PIVOT          ("file-pivot",       "--file-pivot",        true,  true,  false, true,  OperationType.FILE_PIVOT,         List.of("-file-pivot", "--fp", "-fp")),
    FILE_PIVOT_SAVE     ("file-pivot-save",  "--file-pivot-save",   false, false, false, true,  OperationType.FILE_PIVOT_SAVE,    List.of("-file-pivot-save", "--fps", "-fps"));

    public static final List<Operations> VALUES = List.of(values());

    private final String name;          // Operation Name
    private final String op;            // Operation argument
    private final boolean input2;       // Operation requires a second input file
    private final boolean output;       // Operation requires an output file
    private final boolean key;          // Operation requires a key field
    private final boolean headers;      // Operation requires a CSV header mapping

    private final OperationType<?> type;
    private final List<String> alias = new ArrayList<>();

    Operations(String name, String op, boolean input2, boolean output, boolean key, boolean headers, OperationType<?> type, List<String> alias)
    {
        this.name = name;
        this.op = op;
        this.input2 = input2;
        this.output = output;
        this.key = key;
        this.headers = headers;
        this.type = type;
        this.alias.addAll(alias);
    }

    public String getName() { return this.name; }

    public String getOp() { return this.op; }

    public List<String> getAlias() { return this.alias; }

    public boolean needsInput() { return this.input2; }

    public boolean needsOutput() { return this.output; }

    public boolean needsKey() { return this.key; }

    public boolean needsHeaders() { return this.headers; }

    public OperationType<?> getType()
    {
        return this.type;
    }

    public @Nullable Operation init()
    {
        return this.type.init(this);
    }

    @Nullable
    public static Operations fromArg(String name)
    {
        for (Operations val : VALUES)
        {
            if (val.getOp().equalsIgnoreCase(name))
            {
                return val;
            }
            else if (val.getAlias().contains(name))
            {
                return val;
            }
        }

        return null;
    }
}
