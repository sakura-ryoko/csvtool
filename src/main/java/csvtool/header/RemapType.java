package csvtool.header;

import javax.annotation.Nullable;
import java.util.List;

public enum RemapType
{
    NONE        ("none",   false),
    DROP        ("drop",   false),
    STATIC      ("static", true),
    SWAP        ("swap",   true),
    EXCLUDE     ("exclude",true),
    PAD         ("pad",    true),
    DATE        ("date",   true);

    public static final List<RemapType> VALUES = List.of(values());

    private final String name;
    private final boolean param;

    RemapType(String name, boolean param)
    {
        this.name = name;
        this.param = param;
    }

    public String getName() { return this.name; }

    public boolean needsParam() { return this.param; }

    @Nullable
    public static RemapType fromArg(String name)
    {
        for (RemapType val : VALUES)
        {
            if (val.getName().equalsIgnoreCase(name))
            {
                return val;
            }
        }

        return null;
    }

    @Override
    public String toString()
    {
        return this.getName();
    }
}
