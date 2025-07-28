package csvtool.header;

import javax.annotation.Nullable;
import java.util.List;

public enum RemapType
{
    NONE            ("none",           false),
    DROP            ("drop",           false),
    EMPTY           ("empty",          false),
    STATIC          ("static",         true),
    SWAP            ("swap",           true),
    COPY            ("copy",           true),
    APPEND          ("append",         true),
    MERGE           ("merge",          true),
    PREFIX          ("prefix",         true),
    IF_STATIC       ("if-static",      true),
    IF_EMPTY        ("if-empty",       true),
    IF_EMPTY_FIELD  ("if-empty-field", true),
    IF_EMPTY_COPY   ("if-empty-copy",  true),
    IF_EQUAL        ("if-equal",       true),
    IF_EQUAL_COPY   ("if-equal-copy",  true),
    IF_EQUAL_APPEND ("if-equal-append",true),
    IF_FIELDS_EQUAL ("if-fields-equal",true),
    IF_RANGE        ("if-range",       true),
    IF_DATE_RANGE   ("if-date-range",  true),
    NOT_EMPTY       ("not-empty",      true),
    NOT_EMPTY_FIELD ("not-empty-field",true),
    NOT_EMPTY_APPEND("not-empty-append",true),
    NOT_EMPTY_COPY  ("not-empty-copy", true),
    NOT_EMPTY_MERGE ("not-empty-merge",true),
    NOT_EMPTY_PREFIX("not-empty-prefix",true),
    INCLUDE         ("include",        true),
    EXCLUDE         ("exclude",        true),
    INCLUDE_REGEX   ("include-regex",  true),
    EXCLUDE_REGEX   ("exclude-regex",  true),
    PAD             ("pad",            true),
    TRUNCATE        ("truncate",       true),
    REPLACE         ("replace",        true),
    SANITIZE        ("sanitize",       false),
    PHONE_NUMBER    ("phone_number",   false),
    DATE            ("date",           true),
    DATE_NOW        ("date-now",       true),
    DATE_YEARS      ("date-years",     true),
    DATE_MONTHS     ("date-months",    true),
    DATE_DAYS       ("date-days",      true),
    ;

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
