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
    private boolean ansiColor;

    public LogWrapper(Class<?> clazz)
    {
        this(clazz, Const.DEBUG, Const.QUIET, Const.ANSI_COLOR);
    }

    public LogWrapper(Class<?> clazz, boolean debug)
    {
        this(clazz, debug, Const.QUIET, Const.ANSI_COLOR);
    }

    public LogWrapper(Class<?> clazz, boolean debug, boolean quiet, boolean ansiColor)
    {
        this.log = clazz.getName();
        this.debug = debug;
        this.quiet = quiet;
        this.ansiColor = ansiColor;
    }

    public void toggleDebug(boolean toggle)
    {
        this.debug = toggle;
    }

    public void toggleQuiet(boolean toggle)
    {
        this.quiet = toggle;
    }

    public void toggleAnsiColor(boolean toggle)
    {
        this.ansiColor = toggle;
    }

    public void info(String fmt, Object... args)
    {
        if (this.log != null && !this.quiet)
        {
            String msg = StringUtils.format(fmt, args);

            if (this.ansiColor)
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
        if (this.log != null && this.debug && !this.quiet)
        {
            String msg = StringUtils.format(fmt, args);

            if (this.ansiColor)
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

            if (this.ansiColor)
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

            if (this.ansiColor)
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

            if (this.ansiColor)
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
