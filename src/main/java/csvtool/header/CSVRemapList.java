package csvtool.header;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record CSVRemapList(List<CSVRemap> list) implements AutoCloseable
{
    public CSVRemapList()
    {
        this(null);
    }

    public CSVRemapList(@Nullable List<CSVRemap> list)
    {
        if (list == null || list.isEmpty())
        {
            this.list = new ArrayList<>();
        }
        else
        {
            this.list = new ArrayList<>(list);
        }
    }

    public CSVRemapList addToList(CSVRemap remap)
    {
        if (remap != null && remap.getType() != null)
        {
            this.list.add(remap);
        }

        return this;
    }

    public @Nullable CSVRemap buildRemap(int id, String type, String... params)
    {
        RemapType remapType = RemapType.fromArg(type);

        if (remapType != null)
        {
            List<String> list;

            if (remapType.needsParam())
            {
                list = new ArrayList<>(Arrays.asList(params));
            }
            else
            {
                list = new ArrayList<>();
            }

            return new CSVRemap(id, remapType, list);
        }

        return null;
    }

    public int size()
    {
        return this.list.size();
    }

    public boolean isEmpty()
    {
        return this.list.isEmpty();
    }

    public CSVRemapList addRemap(@Nonnull CSVRemap remap)
    {
        this.list.add(remap);
        return this;
    }

    public void clear()
    {
        if (!this.list.isEmpty())
        {
            this.list.forEach(CSVRemap::clear);
        }
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("CSVRemapList[");

        if (!this.list.isEmpty())
        {
            for (int i = 0; i < this.list().size(); i++)
            {
                builder.append(i);

                if (i == 0)
                {
                    builder.append("{");
                }
                else
                {
                    builder.append(",{");
                }

                builder.append(this.list().get(i).toString()).append("}");
            }
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
