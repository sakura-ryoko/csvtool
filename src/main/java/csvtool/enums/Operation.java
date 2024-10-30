package csvtool.enums;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;

public enum Operation
{
    MERGE("--merge"),             // --merge
    DIFF("--diff"),               // --diff
    REFORMAT("--reformat");       // --reformat

    public static final ImmutableList<Operation> VALUES = ImmutableList.copyOf(values());

    private final String op;

    Operation(String op)
    {
        this.op = op;
    }

    public String get()
    {
        return this.op;
    }

    public Operation fromString(String name)
    {
        return fromStringStatic(name);
    }

    @Nullable
    public static Operation fromStringStatic(String name)
    {
        for (Operation val : VALUES)
        {
            if (val.get().equalsIgnoreCase(name))
            {
                return val;
            }
        }

        return null;
    }
}
