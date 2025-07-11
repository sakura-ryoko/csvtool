package csvtool.enums;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public enum Settings
{
    INPUT2      ("input2",      "--input",        true,  List.of("--in", "-input", "-i")),
    OUTPUT      ("output",      "--output",       true,  List.of("--out", "-output", "-out")),
    HEADERS     ("headers",     "--headers",      true,  List.of("--header", "--head", "-head", "-header", "-headers", "--hd", "-hd")),
    KEY         ("key",         "--key",          true,  List.of("--k", "-key", "-k")),
    KEY2        ("key2",        "--key2",         true,  List.of("--k2", "-key2", "-k2")),
    KEY3        ("key3",        "--key3",         true,  List.of("--k3", "-key3", "-k3")),
    KEY4        ("key4",        "--key4",         true,  List.of("--k4", "-key4", "-k4")),
    KEY5        ("key5",        "--key5",         true,  List.of("--k5", "-key5", "-k5")),
    SERIAL_KEY  ("serial-key",  "--serial-key",   true,  List.of("--sk", "-serial-key", "-sk")),
    SERIAL_START("serial-start","--serial-start", true,  List.of("--sks", "-serial-start", "-sks")),
    SERIAL_END  ("serial-ebd",  "--serial-end",   true,  List.of("--ske", "-serial-end", "-ske")),
    SIDE        ("side",        "--side",         true,  List.of("--sides", "-side", "-sides", "--s", "-s")),
    DE_DUPE     ("de-dupe",     "--de-dupe",      false, List.of("-de-dupe", "--dedupe", "-dedupe", "--dd", "-dd")),
    SQUASH_DUPE ("squash-dupe", "--squash-dupe",  false, List.of("-squash-dupe", "--squashdupe", "-squashdupe", "--sqdd", "-sqdd", "--sqd", "-sqd")),
    QUOTES      ("quotes",      "--quotes",       false, List.of("-quotes")),
    APPEND      ("append",      "--append",       false, List.of("--a", "-append", "-a")),
    QUIET       ("quiet",       "--quiet",        false, List.of("-quiet", "--q", "-q")),
    DEBUG       ("debug",       "--debug",        false, List.of("--db", "-db", "-debug", "--d", "-d")),
    ANSI_COLORS ("ansi-colors", "--ansi-colors",  false, List.of("--colors", "--color", "--ansi", "-colors", "-color", "-ansi")),
    ;

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
