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

    public static String addFileSuffix(String file, String suffix)
    {
        StringBuilder result = new StringBuilder();
        String[] split = file.split("\\.(?=[^\\.]+$)");
        result.append(split[0]).append(suffix).append(".").append(split[1]);
        return result.toString();
    }
}
