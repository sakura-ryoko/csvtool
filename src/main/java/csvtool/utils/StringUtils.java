package csvtool.utils;

public class StringUtils
{
    public static String format(final String format, final Object... args)
    {
        String result = format;

        for (Object arg : args)
        {
            result = result.replaceFirst("\\{\\}", arg.toString());
        }

        return result;
    }
}
