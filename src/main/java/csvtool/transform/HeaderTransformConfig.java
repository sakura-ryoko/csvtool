package csvtool.transform;

import com.google.gson.annotations.SerializedName;
import csvtool.header.CSVHeader;

public class HeaderTransformConfig
{
    @SerializedName("__header_transform_config_description")
    public String comment = "CSV Header Transform config";

    @SerializedName("__input_file")
    public String inputFile;

    @SerializedName("input")
    public CSVHeader input;

    @SerializedName("transforms")
    public HeaderTransformList transformList;
}
