package csvtool.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
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

    public static String removeFileSuffix(String file)
    {
        String[] split = file.split("\\.(?=[^\\.]+$)", 1);

//        LOGGER.debug("addFileSuffix({}, {})", file, suffix);

        if (split.length > 1)
        {
            return split[0];
        }

        return file;
    }

    public static String sanitizeString(String input)
    {
        String replace = Matcher.quoteReplacement(input);

        replace = replace.replaceAll("\\R", "; ");
//        replace = replace.replaceAll("#", "\\\\#");
        replace = replace.replaceAll("\\\\$", "(Dollars)");

        return replace;
    }

	public static String truncateByTokenAndLength(final String in, final String token, final int maxLength)
	{
		if (in == null || token == null || maxLength <= 0)
		{
			return "";
		}

		final String[] tokens = in.split(Pattern.quote(token), -1);
		StringBuilder sb = new StringBuilder();

		for (String entry : tokens)
		{
			int addedLength = sb.isEmpty() ? entry.length() : token.length() + entry.length();

			if (sb.length() + addedLength <= maxLength)
			{
				if (!sb.isEmpty())
				{
					sb.append(token);
				}

				sb.append(entry);
			}
			else
			{
				break;
			}
		}

		return sb.toString();
	}
}
