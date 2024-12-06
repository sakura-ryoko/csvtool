package csvtool.enums;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public enum Settings
{
    TEST        ("test",     "--t",       true,  List.of("-t")),
    INPUT2      ("input2",   "--input",   true,  List.of("--in", "-input", "-i")),
    OUTPUT      ("output",   "--output",  true,  List.of("--out", "-output", "-out")),
    HEADERS     ("headers",  "--headers", true,  List.of("--header", "--head", "-head", "-header", "-headers")),
    KEY         ("key",      "--key",     true,  List.of("--k", "-key", "-k")),
    DE_DUPE     ("de-dupe",  "--de-dupe", false, List.of("-de-dupe", "--dd", "-dd")),
    QUOTES      ("quotes",   "--quotes",  false, List.of("--q", "-quotes", "-q")),
    APPEND      ("append",   "--append",  false, List.of("--a", "-append", "-a"));

    public static final List<Settings> VALUES = List.of(values());

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
