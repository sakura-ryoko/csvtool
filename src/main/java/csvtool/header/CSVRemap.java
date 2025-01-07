package csvtool.header;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class CSVRemap implements AutoCloseable
{
    private RemapType type;
    private List<String> params;

    public CSVRemap(@Nonnull RemapType type, List<String> params)
    {
        this.type = type;

        if (params == null || params.isEmpty())
        {
            this.params = new ArrayList<>();
        }
        else
        {
            this.params = new ArrayList<>(params);
        }
    }

    public RemapType getType()
    {
        return this.type;
    }

    public List<String> getParams()
    {
        return this.params;
    }

    public CSVRemap setType(@Nonnull RemapType type)
    {
        this.type = type;
        return this;
    }

    public CSVRemap setParams(List<String> params)
    {
        if (params == null || params.isEmpty())
        {
            this.params = new ArrayList<>();
        }
        else
        {
            this.params = new ArrayList<>(params);
        }

        return this;
    }

    public void clear()
    {
        if (!this.params.isEmpty())
        {
            this.params.clear();
        }
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("CSVRemap[");
        builder.append("type={").append(this.getType().toString()).append("}");

        if (this.getType().needsParam())
        {
            for (int i = 0; i < this.getParams().size(); i++)
            {
                if (i == 0)
                {
                    builder.append("params=[").append("{");
                }
                else
                {
                    builder.append(",{");
                }

                builder.append(this.getParams().get(i)).append("}");
            }

            builder.append("]");
        }

        builder.append("]");
        return builder.toString();
    }

    @Override
    public void close() throws Exception
    {
        this.clear();
    }
}
