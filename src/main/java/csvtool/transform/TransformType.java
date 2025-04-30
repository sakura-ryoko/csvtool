package csvtool.transform;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public enum TransformType
{
    KEY             ("key",         "{k}",   List.of("{key}")),
    UNDERSCORE      ("underscore",  "{u}",   List.of("{underscore}", "{us}")),
    HYPHEN          ("hyphen",      "{h}",   List.of("{hyphen}", "{hy}")),
    INDEX           ("index",       "{i}",   List.of("{index}", "{idx}")),
    FIELD           ("field",       "{f}",   List.of("{field}")),
    DATA            ("data",        "{d}",   List.of("{data}", "{str}", "{string}")),
    ;

    public static final List<TransformType> VALUES = List.of(values());

    private final String name;
    private final String formatter;
    private final List<String> alias;

    TransformType(String name, String formatter, List<String> alias)
    {
        this.name = name;
        this.formatter = formatter;
        this.alias = alias;
    }

    public String getName() { return this.name; }

    public String getFormatter() { return this.formatter; }

    public List<String> getFormatterAlias() { return this.alias; }

    @Nullable
    public static TransformType fromName(String name)
    {
        for (TransformType val : VALUES)
        {
            if (val.getName().equalsIgnoreCase(name))
            {
                return val;
            }
        }

        return null;
    }

    @Nullable
    public static TransformType matchFormatter(String input)
    {
        for (TransformType val : VALUES)
        {
            if (val.getFormatter().equalsIgnoreCase(input))
            {
                return val;
            }
            else if (!val.getFormatterAlias().isEmpty())
            {
                AtomicBoolean matched = new AtomicBoolean(false);

                val.getFormatterAlias().forEach(
                        (e) ->
                        {
                            if (e.equalsIgnoreCase(input))
                            {
                                matched.set(true);
                            }
                        }
                );

                if (matched.get())
                {
                    return val;
                }
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
