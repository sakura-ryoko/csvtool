package csvtool.utils;

public class StringUtils extends org.apache.commons.lang3.StringUtils
{
//    private static final LogWrapper LOGGER = new LogWrapper(StringUtils.class);

    public static String format(final String format, final Object... args)
    {
        String result = format;

        for (Object arg : args)
        {
            result = result.replaceFirst("\\{\\}", formatSafe(arg.toString()));
        }

        return result;
    }

    public static String formatSafe(String str)
    {
        str = str.replace("\\", "\\\\");
        return str.replace("\"", "\\\"");
    }

    public static String addFileSuffix(String file, String suffix)
    {
        StringBuilder result = new StringBuilder();
        String[] split = file.split("\\.(?=[^\\.]+$)");

//        LOGGER.debug("addFileSuffix({}, {})", file, suffix);

        if (split.length > 1)
        {
            result.append(split[0]).append(suffix).append(".").append(split[1]);
        }
        else
        {
            result.append(file).append(suffix);
        }

        return result.toString();
    }
}
