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
                new CSVRemap(2, RemapType.EMPTY),
                new CSVRemap(3, RemapType.STATIC, List.of("apple", "orange")),
                new CSVRemap(4, RemapType.DATE, List.of("yyyyMMdd", "yyyy-MM-dd")),
                new CSVRemap(5, RemapType.DATE_NOW, List.of("yyyy-MM-dd")),
                new CSVRemap(6, RemapType.DATE_YEARS, List.of("yyyy-MM-dd")),
                new CSVRemap(7, RemapType.DATE_MONTHS, List.of("yyyy-MM-dd")),
                new CSVRemap(8, RemapType.DATE_DAYS, List.of("yyyy-MM-dd")),
                new CSVRemap(9, RemapType.INCLUDE, List.of("01", "02", "03")),
                new CSVRemap(10, RemapType.EXCLUDE, List.of("04", "05", "06")),
                new CSVRemap(11, RemapType.INCLUDE_REGEX, List.of("(?:^|\\W)included(?:$|\\W)")),
                new CSVRemap(12, RemapType.EXCLUDE_REGEX, List.of("(?:^|\\W)excluded(?:$|\\W)")),
                new CSVRemap(13, RemapType.PAD, List.of("3", "0")),
                new CSVRemap(14, RemapType.TRUNCATE, List.of("1")),
                new CSVRemap(15, RemapType.IF_STATIC, List.of("2", "apple", "orange")),
                new CSVRemap(16, RemapType.IF_EMPTY, List.of("was_empty")),
                new CSVRemap(17, RemapType.IF_EMPTY_FIELD, List.of("2", "its_empty")),
                new CSVRemap(18, RemapType.IF_EMPTY_COPY, List.of("3")),
                new CSVRemap(19, RemapType.IF_EQUAL, List.of("13", "equal", "not equal")),
                new CSVRemap(20, RemapType.IF_EQUAL_COPY, List.of("13", "14")),
                new CSVRemap(21, RemapType.IF_FIELDS_EQUAL, List.of("13", "14", "equal", "not equal")),
                new CSVRemap(22, RemapType.IF_RANGE, List.of("0", "15", "Less than 16", "16", "32", "Between 16 and 32", "Above 32")),
                new CSVRemap(23, RemapType.IF_DATE_RANGE, List.of("MM/dd/yyyy", "4", "MM/dd/yyyy", "5", "MM/dd/yyyy", "Before TimeDate", "During TimeDate", "After TimeDate", "Out of Range")),
                new CSVRemap(24, RemapType.NOT_EMPTY, List.of("not_empty")),
                new CSVRemap(25, RemapType.NOT_EMPTY_FIELD, List.of("2", "not_empty")),
                new CSVRemap(26, RemapType.NOT_EMPTY_COPY, List.of("3")),
                new CSVRemap(27, RemapType.COPY, List.of("3")),
                new CSVRemap(28, RemapType.MERGE, List.of("3")),
                new CSVRemap(29, RemapType.APPEND, List.of("Extra")),
                new CSVRemap(30, RemapType.SWAP, List.of("2"),
                        new CSVRemap(3, RemapType.STATIC, List.of("orange", "banana"))
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
