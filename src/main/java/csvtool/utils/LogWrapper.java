package csvtool.utils;

import csvtool.enums.Colors;

public class LogWrapper
{
    private final String log;

    public LogWrapper(Class<?> clazz)
    {
        this.log = clazz.getName();
    }

    public void info(String fmt, Object... args)
    {
        if (this.log != null)
        {
            String msg = StringUtils.format(fmt, args);
            System.out.printf(Colors.WHITE + "[INFO/"+this.log+"]: %s" + Colors.RESET+"\n", msg);
        }
    }

    public void debug(String fmt, Object... args)
    {
        if (this.log != null)
        {
            String msg = StringUtils.format(fmt, args);
            System.out.printf(Colors.PURPLE_BOLD + "[DEBUG/"+this.log+"]: %s" + Colors.RESET+"\n", msg);
        }
    }

    public void warn(String fmt, Object... args)
    {
        if (this.log != null)
        {
            String msg = StringUtils.format(fmt, args);
            System.out.printf(Colors.YELLOW_BOLD + "[WARN/"+this.log+"]: %s" + Colors.RESET+"\n", msg);
        }
    }

    public void error(String fmt, Object... args)
    {
        if (this.log != null)
        {
            String msg = StringUtils.format(fmt, args);
            System.out.printf(Colors.RED_BOLD + "[ERROR/"+this.log+"]: %s" + Colors.RESET+"\n", msg);
        }
    }

    public void fatal(String fmt, Object... args)
    {
        if (this.log != null)
        {
            String msg = StringUtils.format(fmt, args);
            System.out.printf(Colors.RED_BOLD_BRIGHT + "[FATAL/"+this.log+"]: %s" + Colors.RESET+"\n", msg);
        }
    }
}
