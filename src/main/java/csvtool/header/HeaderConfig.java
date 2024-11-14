package csvtool.header;

import com.google.gson.annotations.SerializedName;

public class HeaderConfig
{
    @SerializedName("__header_config")
    public String comment = "Headers config";

    @SerializedName("input")
    public CSVHeader input;

    @SerializedName("output")
    public CSVHeader output;
}
