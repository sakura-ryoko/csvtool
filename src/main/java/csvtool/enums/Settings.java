package csvtool.enums;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public enum Settings
{
    TEST        ("test",     "--t",     true,  List.of("-t")),
    UTF8        ("utf8",     "--utf8",  false, List.of("--utf", "-utf", "-utf8"));

    public static final ImmutableList<Settings> VALUES = ImmutableList.copyOf(values());

    private final String name;          // Setting Name
    private final String setting;       // Setting argument
    private final boolean param;        // Setting requires a parameter
    private final List<String> alias = new ArrayList<>();

    Settings(String name, String setting, boolean param, List<String> alias)
    {
        this.name = name;
        this.setting = setting;
        this.param = param;
        this.alias.addAll(alias);
    }

    public String getName() { return this.name; }

    public String getSetting() { return this.setting; }

    public List<String> getAlias() { return this.alias; }

    public boolean needsParam() { return this.param; }

    @Nullable
    public static Settings fromArg(String name)
    {
        for (Settings val : VALUES)
        {
            if (val.getSetting().equalsIgnoreCase(name))
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

    @Override
    public String toString()
    {
        return this.getName();
    }
}
