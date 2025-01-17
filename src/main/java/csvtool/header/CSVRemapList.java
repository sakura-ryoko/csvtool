package csvtool.header;

import csvtool.utils.LogWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CSVRemapList
{
    private static final LogWrapper LOGGER = new LogWrapper(CSVRemapList.class);
    public static final CSVRemapList EXAMPLES = new CSVRemapList(
        List.of(
                new CSVRemap(0, RemapType.NONE),
                new CSVRemap(1, RemapType.DROP),
                new CSVRemap(2, RemapType.STATIC, List.of("apple", "orange")),
                new CSVRemap(3, RemapType.DATE, List.of("yyyyMMdd", "yyyy-MM-dd")),
                new CSVRemap(4, RemapType.INCLUDE, List.of("01", "02", "03")),
                new CSVRemap(5, RemapType.EXCLUDE, List.of("04", "05", "06")),
                new CSVRemap(6, RemapType.INCLUDE_REGEX, List.of("(?:^|\\W)included(?:$|\\W)")),
                new CSVRemap(7, RemapType.EXCLUDE_REGEX, List.of("(?:^|\\W)excluded(?:$|\\W)")),
                new CSVRemap(8, RemapType.PAD, List.of("3", "0")),
                new CSVRemap(9, RemapType.TRUNCATE, List.of("1")),
                new CSVRemap(10, RemapType.SWAP, List.of("2"),
                        new CSVRemap(2, RemapType.STATIC, List.of("orange", "banana"))
                )
        )
    );

    private final List<CSVRemap> list;

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
        if (remap.getParams() == null)
        {
            remap = remap.setParams(List.of());
        }

        this.list.add(remap);
        return this;
    }

    public boolean hasRemap(int entry)
    {
        if (entry > this.list.size())
        {
            return false;
        }

        return this.list.get(entry) != null;
    }

    public @Nullable CSVRemap getRemap(int entry)
    {
        if (entry > this.list.size())
        {
            LOGGER.error("setRemap(): Error; Entry: [{}] > size [{}]", entry, this.list.size());
            return null;
        }

        return this.list.get(entry);
    }

    public void setRemap(int entry, @Nonnull CSVRemap newRemap)
    {
        if (entry > this.list.size())
        {
            LOGGER.error("setRemap(): Error; Entry: [{}] > size [{}]", entry, this.list.size());
            return;
        }

        this.list.set(entry, newRemap);
    }

    public List<CSVRemap> getList()
    {
        return this.list;
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
            for (int i = 0; i < this.list.size(); i++)
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

                builder.append(this.list.get(i).toString()).append("}");
            }
        }

        builder.append("]");
        return builder.toString();
    }
}
