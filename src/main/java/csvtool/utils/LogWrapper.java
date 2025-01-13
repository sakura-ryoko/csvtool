package csvtool.utils;

import csvtool.data.Const;
import csvtool.enums.Colors;

import javax.annotation.concurrent.Immutable;

@Immutable
public class LogWrapper
{
    private final String log;
    private boolean debug;
    private boolean quiet;

    public LogWrapper(Class<?> clazz)
    {
        this(clazz, Const.DEBUG, Const.QUIET);
    }

    public LogWrapper(Class<?> clazz, boolean debug)
    {
        this(clazz, debug, Const.QUIET);
    }

    public LogWrapper(Class<?> clazz, boolean debug, boolean quiet)
    {
        this.log = clazz.getName();
        this.debug = debug;
        this.quiet = quiet;
    }

    public void toggleDebug(boolean toggle)
    {
        this.debug = toggle;
    }

    public void toggleQuiet(boolean toggle)
    {
        this.quiet = toggle;
    }

    public void info(String fmt, Object... args)
    {
        if (this.log != null && !this.quiet)
        {
            String msg = StringUtils.format(fmt, args);

            if (Const.ANSI_COLOR)
            {
                System.out.printf(Colors.WHITE + "[INFO/" + this.log + "]: %s" + Colors.RESET + "\n", msg);
            }
            else
            {
                System.out.printf("[INFO/" + this.log + "]: %s\n", msg);
            }
        }
    }

    public void debug(String fmt, Object... args)
    {
        if (this.log != null && this.debug)
        {
            String msg = StringUtils.format(fmt, args);

            if (Const.ANSI_COLOR)
            {
                System.out.printf(Colors.PURPLE_BOLD + "[DEBUG/" + this.log + "]: %s" + Colors.RESET + "\n", msg);
            }
            else
            {
                System.out.printf("[DEBUG/" + this.log + "]: %s\n", msg);
            }
        }
    }

    public void warn(String fmt, Object... args)
    {
        if (this.log != null && !this.quiet)
        {
            String msg = StringUtils.format(fmt, args);

            if (Const.ANSI_COLOR)
            {
                System.out.printf(Colors.YELLOW_BOLD + "[WARN/" + this.log + "]: %s" + Colors.RESET + "\n", msg);
            }
            else
            {
                System.out.printf("[WARN/" + this.log + "]: %s\n", msg);
            }
        }
    }

    public void error(String fmt, Object... args)
    {
        if (this.log != null && !this.quiet)
        {
            String msg = StringUtils.format(fmt, args);

            if (Const.ANSI_COLOR)
            {
                System.out.printf(Colors.RED_BOLD + "[ERROR/" + this.log + "]: %s" + Colors.RESET + "\n", msg);
            }
            else
            {
                System.out.printf("[ERROR/" + this.log + "]: %s\n", msg);
            }
        }
    }

    public void fatal(String fmt, Object... args)
    {
        if (this.log != null && !this.quiet)
        {
            String msg = StringUtils.format(fmt, args);

            if (Const.ANSI_COLOR)
            {
                System.out.printf(Colors.RED_BOLD_BRIGHT + "[FATAL/" + this.log + "]: %s" + Colors.RESET + "\n", msg);
            }
            else
            {
                System.out.printf("[FATAL/" + this.log + "]: %s\n", msg);
            }
        }
    }
}
