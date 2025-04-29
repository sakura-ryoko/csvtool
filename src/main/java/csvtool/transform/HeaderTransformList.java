package csvtool.transform;

import csvtool.utils.LogWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class HeaderTransformList
{
    private static final LogWrapper LOGGER = new LogWrapper(HeaderTransformList.class);
    private final AbstractList<Entry> entryList = new ArrayList<>();

    public HeaderTransformList() {}

    public HeaderTransformList(List<Entry> list)
    {
        this.entryList.addAll(list);
    }

    public HeaderTransformList addEntry(Entry entry)
    {
        this.entryList.add(entry);
        return this;
    }

    public @Nullable Entry getEntry(int index)
    {
        if (this.size() > index)
        {
            return this.entryList.get(index);
        }

        return null;
    }

    public HeaderTransformList setEntry(int index, Entry entry)
    {
        if (index > this.entryList.size())
        {
            LOGGER.error("setEntry(): Error; Entry: [{}] > size [{}]", index, this.entryList.size());
            return this;
        }

        this.entryList.set(index, entry);
        return this;
    }

    public Stream<Entry> stream()
    {
        return this.entryList.stream();
    }

    public Iterator<Entry> iterator()
    {
        return this.entryList.iterator();
    }

    public int size()
    {
        return this.entryList.size();
    }

    public boolean isEmpty()
    {
        return this.entryList.isEmpty();
    }

    public void clear()
    {
        if (!this.entryList.isEmpty())
        {
            this.entryList.clear();
        }
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("HeaderTransformList[");

        if (!this.entryList.isEmpty())
        {
            for (int i = 0; i < this.entryList.size(); i++)
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

                builder.append(this.entryList.get(i).toString()).append("}");
            }
        }

        builder.append("]");
        return builder.toString();
    }

    public record Entry(int id, String format, List<String> args)
    {
        public String reformat()
        {
            String[] split1 = this.format.split("\\{");

            if (split1.length > 1)
            {
                StringBuilder result = new StringBuilder(split1[0]);
                int p = 0;

                for (int i = 0; i < split1.length; i++)
                {
                    String substring = split1[i];
                    String[] split2 = substring.split("}", 1);
                    String token = "{" + split2[0] + "}";
                    TransformType type = TransformType.matchFormatter(token);

                    LOGGER.debug("Token[{}]: token [{}]", i, token);

                    if (type != null)
                    {
                        String fmt;

                        if (type.needsParam() && this.args.size() > p)
                        {
                            fmt = type.apply(this.args.get(p));
                            p++;
                        }
                        else
                        {
                            fmt = type.apply();
                        }

                        if (fmt != null)
                        {
                            LOGGER.debug("Append[{}]: fmt [{}]", i, fmt);
                            result.append(fmt);
                        }
                        else
                        {
                            LOGGER.error("Entry.reformat(): Error applying token for TransformType '{}'", type.toString());
                        }
                    }
                    else
                    {
                        LOGGER.error("Entry.reformat(): Error parsing token for TransformType '{}'", token);
                    }

                    if (split2.length > 1)
                    {
                        result.append(split2[1]);
                    }
                }

                return result.toString();
            }

            return split1[0];
        }

        @Override
        public @Nonnull String toString()
        {
            return "Entry["+this.id()+",["+ this.format() + "],{" + this.args().toString()+"}]";
        }
    }
}
