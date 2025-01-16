package csvtool.header;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CSVRemap implements AutoCloseable
{
    private int id;
    private RemapType type;
    @Nullable
    private List<String> params;
    @Nullable
    private final CSVRemap subRemap;

    public CSVRemap(int id, @Nonnull RemapType type)
    {
        this(id, type, null, null);
    }

    public CSVRemap(int id, @Nonnull RemapType type, @Nullable List<String> params)
    {
        this(id, type, params, null);
    }

    public CSVRemap(int id, @Nonnull RemapType type, @Nullable List<String> params, @Nullable CSVRemap subRemap)
    {
        this.id = id;
        this.type = type;

        if (params == null || params.isEmpty())
        {
            this.params = null;
        }
        else
        {
            this.params = new ArrayList<>(params);
        }

        if (subRemap != null)
        {
            // Should never use a SWAP or DROP sub remap
            if (subRemap.getType() == RemapType.SWAP || subRemap.getType() == RemapType.DROP)
            {
                this.subRemap = null;
            }
            else
            {
                this.subRemap = subRemap;
            }
        }
        else
        {
            this.subRemap = null;
        }
    }

    public int getId()
    {
        return this.id;
    }

    public RemapType getType()
    {
        return this.type;
    }

    public @Nullable List<String> getParams()
    {
        return this.params;
    }

    public CSVRemap setId(int id)
    {
        this.id = id;
        return this;
    }

    public CSVRemap setType(@Nonnull RemapType type)
    {
        this.type = type;
        return this;
    }

    public CSVRemap setParams(@Nullable List<String> params)
    {
        if (params == null || params.isEmpty())
        {
            this.params = null;
        }
        else
        {
            this.params = new ArrayList<>(params);
        }

        return this;
    }

    public @Nullable CSVRemap getSubRemap()
    {
        return this.subRemap;
    }

    public void clear()
    {
        if (this.params != null && !this.params.isEmpty())
        {
            this.params.clear();
        }

        if (this.subRemap != null)
        {
            this.subRemap.clear();
        }
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("CSVRemap[");
        builder.append("id=").append(this.getId());
        builder.append("type={").append(this.getType().toString()).append("}");

        if (this.getType().needsParam() && this.getParams() != null)
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

        if (this.subRemap != null)
        {
            builder.append("subRemap={").append(this.subRemap).append("}");
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
