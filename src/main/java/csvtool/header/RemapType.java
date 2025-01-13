package csvtool.header;

import javax.annotation.Nullable;
import java.util.List;

public enum RemapType
{
    NONE        ("none",   "--none",   false),
    STATIC      ("static", "--static", true),
    SWAP        ("swap",   "--swap",   true),
    PAD         ("pad",    "--pad",    true),
    DATE        ("date",   "--date",   true);

    public static final List<RemapType> VALUES = List.of(values());

    private final String name;
    private final String setting;
    private final boolean param;

    RemapType(String name, String setting, boolean param)
    {
        this.name = name;
        this.setting = setting;
        this.param = param;
    }

    public String getName() { return this.name; }

    public String getSetting() { return this.setting; }

    public boolean needsParam() { return this.param; }

    @Nullable
    public static RemapType fromArg(String name)
    {
        for (RemapType val : VALUES)
        {
            if (val.getSetting().equalsIgnoreCase(name))
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
