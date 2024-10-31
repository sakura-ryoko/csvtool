package csvtool.enums;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;

public enum Operation
{
    TEST    ("test",     "--test"),                 // --test
    MERGE   ("merge",    "--merge"),                // --merge
    DIFF    ("diff",     "--diff"),                 // --diff
    REFORMAT("reformat", "--reformat");             // --reformat

    public static final ImmutableList<Operation> VALUES = ImmutableList.copyOf(values());

    private final String name;
    private final String op;

    Operation(String name, String op)
    {
        this.name = name;
        this.op = op;
    }

    public String getName()
    {
        return this.name;
    }

    public String getOp()
    {
        return this.op;
    }

    public Operation fromArg(String name)
    {
        return fromArgStatic(name);
    }

    @Nullable
    public static Operation fromArgStatic(String name)
    {
        for (Operation val : VALUES)
        {
            if (val.getOp().equalsIgnoreCase(name))
            {
                return val;
            }
        }

        return null;
    }
}
