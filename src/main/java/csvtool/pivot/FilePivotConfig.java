package csvtool.pivot;

import com.google.gson.annotations.SerializedName;
import csvtool.header.CSVHeader;
import csvtool.transform.HeaderTransformConfig;

import java.util.List;

public class FilePivotConfig
{
    @SerializedName("__file_pivot_config_description")
    public String config_comment = "CSV File Pivot config";

    @SerializedName("__input_file")
    public String inputFile;

    @SerializedName("input")
    public CSVHeader input;

    @SerializedName("__transform_example_list")
    public List<String> transform_example_list = new HeaderTransformConfig().transform_example_list;

    @SerializedName("__file_transform_description")
    public List<String> file_comment = List.of(
            "This Transform is to reformat a filename based on fields that exist in the CSV.",
            "It uses the from to match the files using fields, and then transforms the file using to to a new name.",
            "And then moves it to a new directory based on CSV fields using: directory_field_list"
    );

    @SerializedName("directory_field_list")
    public List<FilePivotDirectoryBuilder.Entry> directoryEntryList = List.of(new FilePivotDirectoryBuilder.Entry(0));

    @SerializedName("file_transform_from")
    public FilePivotTransform fileTransformFrom = new FilePivotTransform("{k}", "pdf", -1, List.of());

    @SerializedName("file_transform_to")
    public FilePivotTransform fileTransformTo = new FilePivotTransform("{k}", "pdf", -1, List.of());
}
