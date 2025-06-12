package csvtool.pivot;

import csvtool.transform.TransformType;
import csvtool.utils.LogWrapper;

import java.util.List;

public record FilePivotTransform(String format, String defaultExt, int data, List<String> args)
{
    private static final LogWrapper LOGGER = new LogWrapper(FilePivotTransform.class);

    public String transformFileName(String key, int index, List<String> data)
    {
        String[] split1 = this.format.split("\\{");
        String fileOut;

        if (split1.length > 1)
        {
            StringBuilder result = new StringBuilder(split1[0]);
            int dataIndex = 0;

            for (int i = 0; i < split1.length; i++)
            {
                String subString = split1[i];

                if (subString.isEmpty())
                {
                    continue;
                }

                String[] split2 = subString.split("}");
                LOGGER.debug("transformFileName(): token[{}]: substring [{}], split2 0: [{}] 1: [{}]", i, subString,
                             split2[0], split2.length > 1 ? split2[1] : "<>");

                String token = "{" + split2[0] + "}";
                LOGGER.debug("transformFileName(): token[{}]: match [{}]", i, token);
                TransformType type = TransformType.matchFormatter(token);

                if (type != null)
                {
                    String fmt;
                    LOGGER.debug("transformFileName(): token[{}]: type [{}]", i, type.getName());

                    switch (type)
                    {
                        case KEY -> fmt = key;
                        case DATA ->
                        {
                            if (this.args.size() > dataIndex && data.size() > dataIndex)
                            {
                                int f = Integer.parseInt(this.args.get(dataIndex));
                                fmt = data.get(f);
                            }
                            else
                            {
                                fmt = "";
                            }

                            dataIndex++;
                        }
                        case INDEX -> fmt = String.valueOf(index);
                        case HYPHEN -> fmt = "-";
                        case UNDERSCORE -> fmt = "_";
                        default -> fmt = "";
                    }

                    result.append(fmt);
                }

                if (split2.length > 1)
                {
                    LOGGER.debug("Append[{}]: split2 [{}]", i, split2[1]);
                    result.append(split2[1]);
                }
                else
                {
                    LOGGER.debug("Skip[{}] (No Data)", i);
                }
            }

            fileOut = result.toString();
        }
        else
        {
            fileOut = split1[0];
        }

        // Ensure a FileExt exists
        final int pos = fileOut.lastIndexOf(".");

        if (pos < 0)
        {
            LOGGER.debug("AppendFileExt: [{}]", this.defaultExt());
            return fileOut+"."+this.defaultExt();
        }

        return fileOut;
    }
}
