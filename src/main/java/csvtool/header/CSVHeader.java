package csvtool.header;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class CSVHeader
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

    public void add(String header)
    {
        this.headers.add(header);
    }

    public Stream<String> stream()
    {
        return this.headers.stream();
    }

    public Iterator<String> iterator()
    {
        return this.headers.iterator();
    }

    public ImmutableList<String> toImmutable()
    {
        if (this.headers.isEmpty())
        {
            return ImmutableList.of();
        }

        ImmutableList.Builder<String> builder = ImmutableList.builder();
        this.stream().forEach(builder::add);
        return builder.build();
    }
}
