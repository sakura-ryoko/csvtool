package csvtool.utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class LogUtils
{
    private final Logger log;

    public LogUtils(Class<?> clazz)
    {
        this.log = LogManager.getLogger(clazz);
    }

    public void info(String msg, Object... args)
    {
        this.log.info(String.format(msg, args));
    }

    public void debug(String msg, Object... args)
    {
        this.log.debug(String.format(msg, args));
    }

    public void warn(String msg, Object... args)
    {
        this.log.warn(String.format(msg, args));
    }

    public void error(String msg, Object... args)
    {
        this.log.error(String.format(msg, args));
    }

    public void fatal(String msg, Object... args)
    {
        this.log.fatal(String.format(msg, args));
    }

    public void trace(String msg, Object... args)
    {
        this.log.trace(String.format(msg, args));
    }
}
