package csvtool.operation;

import csvtool.data.Context;
import csvtool.data.FileCache;
import csvtool.enums.Operations;
import csvtool.enums.Settings;
import csvtool.pivot.FilePivotDirectoryBuilder;
import csvtool.pivot.FilePivotParser;
import csvtool.pivot.FilePivotTransform;
import csvtool.utils.FileUtils;
import csvtool.utils.LogWrapper;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OperationFilePivot extends Operation implements AutoCloseable
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());
    private FileCache FILE;
    private final FilePivotParser PARSER;
    private FilePivotDirectoryBuilder builder;
    private Path input;
    private Path output;
    private final Set<Path> inputDirSet;
    private int totalCount;

    public OperationFilePivot(Operations op)
    {
        super(op);
        this.FILE = new FileCache();
        this.PARSER = new FilePivotParser();
        this.inputDirSet = new HashSet<>();
        this.totalCount = 0;
    }

    public boolean runOperation(Context ctx)
    {
        if (ctx.getOpt().isQuiet())
        {
            super.toggleQuiet(true);
            LOGGER.toggleQuiet(true);
        }

        if (ctx.getOpt().isDebug())
        {
            super.toggleDebug(true);
            LOGGER.toggleDebug(true);
        }

        if (ctx.getOpt().isAnsiColors())
        {
            super.toggleAnsiColor(true);
            LOGGER.toggleAnsiColor(true);
        }

        if (!ctx.getOpt().hasInput2())
        {
            LOGGER.error("runOperation(): File Pivot FAILED, an input 2 is required.");
            return false;
        }

        if (!ctx.getOpt().hasOutput())
        {
            LOGGER.error("runOperation(): File Pivot FAILED, an output is required.");
            return false;
        }

        LOGGER.debug("runOperation(): --> FILE_PIVOT [{}], to output [{}], using header file [{}]", ctx.getInputFile(),
                     ctx.getSettingValue(Settings.OUTPUT), ctx.getSettingValue(Settings.HEADERS));
        if (this.readFiles(ctx.getInputFile(), false, ctx.getOpt().isDebug()))
        {
            LOGGER.debug("runOperation(): --> File [{}] read successfully.", ctx.getInputFile());

            if (this.PARSER.init(ctx, false))
            {
                LOGGER.debug("runOperation(): --> File Pivot Config Parser initialized.");

                if (this.PARSER.loadConfig())
                {
                    LOGGER.debug("runOperation(): --> File Pivot loaded config from [{}].",
                                 this.PARSER.getHeaderConfigFile());

                    try
                    {
                        this.input = Path.of(ctx.getSettingValue(Settings.INPUT2));
                        this.output = Path.of(ctx.getSettingValue(Settings.OUTPUT));

                        if (!FileUtils.checkIfDirectoryExists(this.input))
                        {
                            LOGGER.error("runOperation(): --> Failed to read Input 2 directory [{}]!",
                                         this.input.toAbsolutePath().toString());
                            return false;
                        }

                        if (!FileUtils.checkIfDirectoryExists(this.output))
                        {
                            LOGGER.error("runOperation(): --> Failed to read Output directory [{}]!",
                                         this.output.toAbsolutePath().toString());
                            return false;
                        }

                        this.builder = new FilePivotDirectoryBuilder(this.output);
                    }
                    catch (RuntimeException err)
                    {
                        LOGGER.error(
                                "runOperation(): --> Failed to initialize File Pivot Dir Builder from output [{}]!",
                                this.output.toAbsolutePath().toString());
                        return false;
                    }

                    if (this.runFilePivotOperation())
                    {
                        LOGGER.info("runOperation(): --> File Pivot complete, total files moved: [{}]",
                                    this.totalCount);
                        return true;
                    }
                    else
                    {
                        LOGGER.error("runOperation(): --> Failed to run File Pivot!");
                        return false;
                    }
                }
                else
                {
                    LOGGER.error("runOperation(): --> Failed to load File Pivot config [{}]!",
                                 this.PARSER.getHeaderConfigFile());
                    return false;
                }
            }
        }

        LOGGER.error("runOperation(): General Operation failure.");
        return false;
    }

    @Override
    public void displayHelp()
    {
        System.out.print("File Pivot Operation:\n");
        System.out.printf("\tAliases: %s\n\n", Operations.FILE_PIVOT.getAlias().toString());
    }

    private boolean readFiles(String file1, boolean ignoreQuotes, boolean debug)
    {
        LOGGER.debug("readFiles(): Reading file [{}] ...", file1);

        this.FILE = this.readFile(file1, ignoreQuotes, debug);

        if (this.FILE == null || this.FILE.isEmpty())
        {
            LOGGER.error("readFiles(): Input File Cache is Empty!");
            return false;
        }

        return true;
    }

    private boolean runFilePivotOperation()
    {
        if (Files.exists(this.input) && Files.isDirectory(this.input) && Files.isReadable(this.input))
        {
            if (!this.cacheInputDirectory())
            {
                LOGGER.error("runFilePivotOperation(): Exception caching input directory.");
                return false;
            }

            FilePivotTransform fromTransform = this.PARSER.getFileTransformFrom();
            FilePivotTransform toTransform = this.PARSER.getFileTransformTo();
            List<FilePivotDirectoryBuilder.Entry> entries = this.PARSER.getDirectoryEntries();

            if (fromTransform == null || fromTransform.data() < 0 || fromTransform.format().length() <= 3)
            {
                LOGGER.error("runFilePivotOperation(): fromTransform has not been configured.");
                return false;
            }
            else if (toTransform == null || toTransform.data() < 0 || toTransform.format().length() <= 3)
            {
                LOGGER.error("runFilePivotOperation(): toTransform has not been configured.");
                return false;
            }
            else if (entries == null)
            {
                LOGGER.error("runFilePivotOperation(): Directory Builder entries has not been configured.");
                return false;
            }

            for (int i = 0; i < this.FILE.getFile().size(); i++)
            {
                List<String> entry = this.FILE.getFile().get(i);

                // Ignore header
                if (i != 0 && !entry.isEmpty())
                {
                    if (!this.runTransformEachLine(i, fromTransform, toTransform, entries, entry))
                    {
                        LOGGER.error("runFilePivotOperation(): File Pivot failure on line [{}]", i);
                        return false;
                    }
                }

            }

            return true;
        }

        LOGGER.error("runFilePivotOperation(): General Operation failure.");
        return false;
    }

    private boolean cacheInputDirectory()
    {
        this.inputDirSet.clear();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(this.input))
        {
            for (Path file : stream)
            {
                if (Files.isRegularFile(file) && Files.isReadable(file))
                {
                    this.inputDirSet.add(file);
                }
            }
        }
        catch (Exception err)
        {
            LOGGER.error("runTransformEachLine(): Directory Builder has failed to construct a new sub dir!");
            return false;
        }

        return true;
    }

    private boolean runTransformEachLine(int index, FilePivotTransform fromTransform, FilePivotTransform toTransform,
                                         List<FilePivotDirectoryBuilder.Entry> entries, List<String> row)
    {
        String nameIn = fromTransform.transformFileName("", index, row);

        if (nameIn.isEmpty())
        {
            LOGGER.error("runTransformEachLine(): Failed to transform fromFile!");
            return false;
        }

        for (Path file : this.inputDirSet)
        {
            if (file.getFileName().toString().equalsIgnoreCase(nameIn))
            {
                if (this.transformAndPivotFile(file, toTransform, index, entries, row))
                {
                    this.inputDirSet.remove(file);
                    LOGGER.debug("runTransformEachLine(): File pivot successful for file '{}'",
                                 file.getFileName().toString());
                    break;
                }
                else
                {
                    LOGGER.error("runTransformEachLine(): Exception pivoting file '{}'", file.getFileName().toString());
                    return false;
                }
            }
        }

        return true;
    }

    private boolean transformAndPivotFile(Path file, FilePivotTransform toTransform, int index,
                                          List<FilePivotDirectoryBuilder.Entry> entries, List<String> row)
    {
        String toFile = toTransform.transformFileName("", index, row);

        if (toFile.isEmpty())
        {
            LOGGER.error("transformAndPivotFile(): Failed to transform toFile!");
            return false;
        }

        Path currentDir = this.builder.build(entries, row);

        if (currentDir == null)
        {
            LOGGER.error("runTransformEachLine(): Directory Builder has failed to construct a new sub dir!");
            return false;
        }

        try
        {
            Path destFile = currentDir.resolve(FileUtils.sanitizeFileName(toFile));

            if (Files.exists(destFile))
            {
                Files.delete(destFile);
                LOGGER.warn("transformAndPivotFile(): Deleted existing file '{}'",
                            destFile.toAbsolutePath().toString());
            }

            Files.move(file, destFile);
            LOGGER.debug("transformAndPivotFile(): File pivot: '{}' -> '{}'", file.getFileName().toString(),
                         destFile.toAbsolutePath().toString());
            this.totalCount++;
        }
        catch (Exception err)
        {
            LOGGER.error("transformAndPivotFile(): Exception moving file '{}'; {}", file.getFileName().toString(),
                         err.getLocalizedMessage());
            return false;
        }

        return true;
    }

    @Override
    public void clear()
    {
        if (this.FILE != null && !this.FILE.isEmpty())
        {
            this.FILE.clear();
        }

        if (this.PARSER != null && !this.PARSER.isEmpty())
        {
            this.PARSER.clear();
        }

        this.inputDirSet.clear();
    }

    @Override
    public void close() throws Exception
    {
        if (this.FILE != null)
        {
            this.FILE.close();
        }
        if (this.PARSER != null)
        {
            this.PARSER.close();
        }

        this.inputDirSet.clear();
    }
}
