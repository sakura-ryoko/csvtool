package csvtool.header;

import com.google.gson.annotations.SerializedName;
import csvtool.remap.RemapExamples;
import csvtool.remap.RemapList;

import java.util.List;

public class HeaderConfig
{
    @SerializedName("__header_config_description")
    public String config_comment = "CSV Remap Headers config";

    @SerializedName("__input_file")
    public String inputFile;

    @SerializedName("input")
    public CSVHeader input;

    @SerializedName("__output_file")
    public String outputFile;

    @SerializedName("output")
    public CSVHeader output;

    @SerializedName("__remap_example_list")
    public List<String> remap_example_list = RemapExamples.EXAMPLE_LIST;

    @SerializedName("remap_examples")
    public RemapList remap_examples = RemapExamples.EXAMPLES;

    @SerializedName("remap_list")
    public RemapList remapList = new RemapList();
}
