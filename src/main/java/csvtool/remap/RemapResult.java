package csvtool.remap;

import java.util.List;

public record RemapResult(boolean exclude, String result, List<String> row)
{
}
