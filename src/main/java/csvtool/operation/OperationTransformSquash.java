package csvtool.operation;

import csvtool.data.Context;
import csvtool.data.FileCache;
import csvtool.enums.Operations;
import csvtool.transform.HeaderTransformParser;
import csvtool.utils.LogWrapper;

public class OperationTransformSquash extends Operation implements AutoCloseable
{
    private final LogWrapper LOGGER = new LogWrapper(getClass());

    private final HeaderTransformParser PARSER;
    private final FileCache FILE;
    private final FileCache OUT;

    public OperationTransformSquash(Operations op)
    {
        super(op);
        this.PARSER = new HeaderTransformParser();
        this.FILE = new FileCache();
        this.OUT = new FileCache();
    }

    public boolean runOperation(Context ctx)
    {
        LOGGER.debug("runOperation() [SQUASH]");
        return false;
    }

    @Override
    public void displayHelp()
    {
        System.out.print("Header Squash Operation:\n");
        System.out.printf("\tAliases: %s\n\n", Operations.TRANSFORM_SQUASH.getAlias().toString());

//        System.out.print("It accepts one input file (--input), and an output (--output).\n");
//        System.out.print("This operation simply copies the input to the output file to test the inner-workings of this program.\n");
    }

    @Override
    public void clear()
    {
        if (this.FILE != null && !this.FILE.isEmpty())
        {
            this.FILE.clear();
        }

        if (this.OUT != null && !this.OUT.isEmpty())
        {
            this.OUT.clear();
        }

        if (this.PARSER != null && !this.PARSER.isEmpty())
        {
            this.PARSER.clear();
        }
    }

    @Override
    public void close() throws Exception
    {
        if (this.FILE != null)
        {
            this.FILE.close();
        }

        if (this.OUT != null)
        {
            this.OUT.close();
        }

        if (this.PARSER != null)
        {
            this.PARSER.close();
        }
    }
}
