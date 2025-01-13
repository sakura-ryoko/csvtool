package csvtool.header;

import com.google.gson.annotations.SerializedName;

public class HeaderConfig
{
    @SerializedName("__header_config")
    public String comment = "CSV Headers config";

    @SerializedName("__input_file")
    public String inputFile;

    @SerializedName("input")
    public CSVHeader input;

    @SerializedName("__output_file")
    public String outputFile;

    @SerializedName("output")
    public CSVHeader output;

    @SerializedName("__remap_example_NONE")
    public String remap_ex_none = "NONE (Performs no remap)";

    @SerializedName("__remap_example_SWAP")
    public String remap_ex_swap = "SWAP (field-id) (Performs a field swap with field-id, and also copies the remap)";

    @SerializedName("__remap_example_PAD")
    public String remap_ex_pad = "PAD { (count), [data] } (Pads this value with (count) and optional [data])";

    @SerializedName("__remap_example_STATIC")
    public String remap_ex_static = "STATIC { (old), [new] } (Performs a static 1-to-1 swap if (old) exists, swap it with [new], or if only 1 parameter, set data to (old) regardless)";

    @SerializedName("__remap_example_DATE")
    public String remap_ex_date = "DATE { (old-fmt), (new-fmt) } (Performs a date reformat based on (old-fmt) into (new-fmt); this uses SimpleDateFormat patterns)";

    @SerializedName("remaps")
    public CSVRemapList remapList;
}
