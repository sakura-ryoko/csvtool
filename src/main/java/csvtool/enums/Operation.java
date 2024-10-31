package csvtool.enums;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public enum Operation
{
    HELP        ("help",     "--help",     false, false, false, List.of("-h", "-help")),
    TEST        ("test",     "--test",     false, false, false, List.of()),
    MERGE       ("merge",    "--merge",     true,  true, false, List.of()),
    DIFF        ("diff",     "--diff",      true,  true, false, List.of()),
    REFORMAT    ("reformat", "--reformat", false,  true,  true, List.of());

    public static final ImmutableList<Operation> VALUES = ImmutableList.copyOf(values());

    private final String name;          // Operation Name
    private final String op;            // Operation argument
    private final boolean input2;       // Operation requires a second input file
    private final boolean output;       // Operation requires an output file
    private final boolean headers;      // Operation requires a CSV header mapping
    private final List<String> alias = new ArrayList<>();

    Operation(String name, String op, boolean input2, boolean output, boolean headers, List<String> alias)
    {
        this.name = name;
        this.op = op;
        this.input2 = input2;
        this.output = output;
        this.headers = headers;
        this.alias.addAll(alias);
    }

    public String getName() { return this.name; }

    public String getOp() { return this.op; }

    public List<String> getAlias() { return this.alias; }

    public boolean needsInput() { return this.input2; }

    public boolean needsOutput() { return this.output; }

    public boolean needsHeaders() { return this.headers; }

    @Nullable
    public static Operation fromArg(String name)
    {
        for (Operation val : VALUES)
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
