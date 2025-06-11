package csvtool.pivot;

import csvtool.utils.LogWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FilePivotDirectoryBuilder
{
    private static final LogWrapper LOGGER = new LogWrapper(FilePivotDirectoryBuilder.class);
    private Path base;
    private Path workingDir = null;

    public FilePivotDirectoryBuilder(Path base) throws RuntimeException
    {
        this.base = base.normalize();
        this.ensurePath(this.base);
    }

    public FilePivotDirectoryBuilder setBaseDir(@Nonnull Path dir) throws RuntimeException
    {
        if (this.ensurePath(dir))
        {
            this.base = dir.normalize();
            return this;
        }

        LOGGER.error("setBaseDir(): Exception ensuring directory exists");
        throw new RuntimeException("Ensure directory exists, and is writable.");
    }

    public boolean hasWorkingDir()
    {
        return this.workingDir != null;
    }

    public @Nullable Path getWorkingDir()
    {
        return this.workingDir;
    }

    private boolean ensurePath(@Nonnull Path dir) throws RuntimeException
    {
        boolean create = false;

        if (Files.exists(dir))
        {
            if (Files.isDirectory(dir))
            {
                if (Files.isWritable(dir))
                {
                    return true;
                }
            }
            else
            {
                try
                {
                    Files.delete(dir);
                    create = true;
                }
                catch (Exception err)
                {
                    LOGGER.error("ensurePath(): Exception deleting file '{}'; {}", dir.toAbsolutePath().toString(), err.getLocalizedMessage());
                    throw new RuntimeException(err);
                }
            }
        }
        else
        {
            create = true;
        }

        if (create)
        {
            try
            {
                Files.createDirectories(dir);
                return true;
            }
            catch (Exception err)
            {
                LOGGER.error("ensurePath(): Exception creating directory '{}'; {}", dir.toAbsolutePath().toString(), err.getLocalizedMessage());
                throw new RuntimeException(err);
            }
        }

        return false;
    }

    public @Nullable Path build(List<Entry> fields, List<String> row)
    {
        Path newDir = this.base;

        for (Entry entry : fields)
        {

            if (row.size() > entry.field())
            {
                try
                {
                    newDir = newDir.resolve(row.get(entry.field())).normalize();
                }
                catch (Exception err)
                {
                    LOGGER.error("build(): Exception building directory; {}", err.getLocalizedMessage());
                    return null;
                }
            }
            else
            {
                LOGGER.error("build(): Row size Exception; [{} > {}]", entry, row.size());
                return null;
            }
        }

        try
        {
            if (this.ensurePath(newDir))
            {
                this.workingDir = newDir;
                return newDir;
            }
        }
        catch (RuntimeException err)
        {
            LOGGER.error("build(): Exception ensuring directory exists; {}", err.getLocalizedMessage());
        }

        return null;
    }

    public record Entry(int field) {}
}
