package csvtool.transform;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public enum TransformType
{
    KEY             ("key",         "{k}",   true,   List.of("{key}")),
    UNDERSCORE      ("underscore",  "{us}",  false,  List.of("{underscore}")),
    HYPHEN          ("hyphen",      "{h}",   false,  List.of("{hyphen}")),
    INDEX           ("index",       "{i}",   true,   List.of("{index}")),
    STRING          ("string",      "{s}",   true,   List.of("{string}")),
    ;

    public static final List<TransformType> VALUES = List.of(values());

    private final String name;
    private final String formatter;
    private final boolean param;
    private final List<String> alias;

    TransformType(String name, String formatter, boolean param, List<String> alias)
    {
        this.name = name;
        this.formatter = formatter;
        this.param = param;
        this.alias = alias;
    }

    public String getName() { return this.name; }

    public String getFormatter() { return this.formatter; }

    public boolean needsParam() { return this.param; }

    public List<String> getFormatterAlias() { return this.alias; }

    public @Nullable String apply()
    {
        if (this.needsParam())
        {
            return null;
        }

        return this.apply(null);
    }

    public @Nullable String apply(@Nullable String param)
    {
        if (this.needsParam() && (param == null || param.isEmpty()))
        {
            return null;
        }

        switch (this)
        {
            case KEY, STRING -> { return param; }
            case UNDERSCORE -> { return "_"; }
            case HYPHEN -> { return "-"; }
            case INDEX ->
            {
                if (param == null) return null;

                try
                {
                    int prev = Integer.getInteger(param);
                    prev++;
                    return String.format("%d", prev);
                }
                catch (Exception e)
                {
                    return null;
                }
            }
            default -> { return null; }
        }
    }

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
