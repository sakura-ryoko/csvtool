package csvtool.transform;

import com.google.gson.annotations.SerializedName;
import csvtool.header.CSVHeader;

import java.util.List;

public class HeaderTransformConfig
{
    @SerializedName("__header_transform_config_description")
    public String comment = "CSV Header Transform config";

    @SerializedName("__input_file")
    public String inputFile;

    @SerializedName("__transform_example_list")
    public List<String> transform_example_list = List.of(
            "This configuration defines a list of column headers to generate using a pre-defined pattern.",
            "The (subKey) is used to define which key header is responsible for the {k} value",
            "Each TransformType is enclosed with '{}', and can be provided with arguments using the 'args' array.",
            "{k} - Returns the subkey value in the table",
            "{d} (column) - Returns the data from the CSV row element using number (column)",
            "{u} - Returns an Underscore character '_'",
            "{h} - Returns a Hypen character '-'",
            "All other text is inserted into the resulting Column name.",
            "An example Formatter pattern might be '{k}{u}{d}{u}Name'",
            "Which would result in a column named '<subkey_value>_<data_value>_Name'"
    );

    @SerializedName("input")
    public CSVHeader input;

    @SerializedName("subkey")
    public String subkey;

    @SerializedName("transforms")
    public HeaderTransformList transformList;
}
