package csvtool.header;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class CSVHeader implements AutoCloseable
{
    private final AbstractList<String> headers;

    public CSVHeader()
    {
        this(null);
    }

    public CSVHeader(@Nullable List<String> headers)
    {
        if (headers != null) {
            this.headers = new ArrayList<>(headers);
        }
        else
        {
            this.headers = new ArrayList<>();
        }
    }

    public void setHeaders(@Nonnull List<String> headers)
    {
        this.headers.clear();
        this.headers.addAll(headers);
    }

    public boolean matches(@Nonnull CSVHeader otherHeaders)
    {
        if (this.size() != otherHeaders.size())
        {
            return false;
        }

        Iterator<String> iter = this.iterator();
        Iterator<String> otherIter = otherHeaders.iterator();

        while (iter.hasNext() && otherIter.hasNext())
        {
            String left = iter.next();
            String right = otherIter.next();

            if (!left.matches(right))
            {
                return false;
            }
        }

        return true;
    }

    public CSVHeader add(String header)
    {
        this.headers.add(header);
        return this;
    }

    public int getId(String header)
    {
        for (int i = 0; i < this.headers.size(); i++)
        {
            if (this.headers.get(i).equals(header))
            {
                return i;
            }
        }

        return -1;
    }

    public @Nullable String getFromId(int id)
    {
        if (id >= this.size())
        {
            return null;
        }

        return this.headers.get(id);
    }

    public Stream<String> stream()
    {
        return this.headers.stream();
    }

    public Iterator<String> iterator()
    {
        return this.headers.iterator();
    }

    public int size()
    {
        return this.headers.size();
    }

    public boolean isEmpty()
    {
        return this.headers.isEmpty();
    }

    public void clear()
    {
        if (!this.headers.isEmpty())
        {
            this.headers.clear();
        }
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("CSVHeader[");

        this.headers.forEach((entry) ->
        {
            builder.append(entry);
            builder.append(",");
        });
        builder.append("]");
        String result = builder.toString();
        return result.replace(",]", "]");
    }

    @Override
    public void close() throws Exception
    {
        this.clear();
    }
}
