package csvtool.header;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HeaderConfig
{
    @SerializedName("__header_config_description")
    public String comment = "CSV Headers config";

    @SerializedName("__input_file")
    public String inputFile;

    @SerializedName("input")
    public CSVHeader input;

    @SerializedName("__output_file")
    public String outputFile;

    @SerializedName("output")
    public CSVHeader output;

    @SerializedName("__remap_example_list")
    public List<String> remap_example_list = List.of(
            "This file defines the --reformat functionality's behavior.",
        "It is organized by column index, and a list of 'CSVRemap' functions; which are listed below.",
        "There is also a 'remap_examples' to show how this configuration file can be used.",
        "The resulting output gets then remapped using the 'Input' Headers, being transformed to the 'Output' Headers.",
        "This process requires the correct number of DROP remaps to be performed so that the column count is equal to the output column size.",
        "--------------------------------------------------------------------------------------------------------------------------------------",
        "NONE (Performs no remap)",
        "DROP (Performs a Column Removal, and advances to the next)",
        "EMPTY (Performs a field empty which deletes any existing data)",
        "STATIC { (old), [new], ... } (Performs a static 1-to-1 swap if (old) exists, swap it with [new], or if only 1 parameter, set data to (old) regardless, and optionally an extended list)",
        "DATE { (old-fmt), (new-fmt) } (Performs a date reformat based on (old-fmt) into (new-fmt); this uses SimpleDateFormat patterns)",
        "DATE_NOW { (fmt) } (Performs a date format based on (fmt) of now; using SimpleDateFormat patterns)",
        "DATE_YEARS { (fmt) } (Performs a date format based on (fmt) of now; using SimpleDateFormat patterns, and then returns the duration in Years since.)",
        "DATE_MONTHS { (fmt) } (Performs a date format based on (fmt) of now; using SimpleDateFormat patterns, and then returns the duration in Months since.)",
        "DATE_DAYS { (fmt) } (Performs a date format based on (fmt) of now; using SimpleDateFormat patterns, and then returns the duration in Days since.)",
        "INCLUDE (...) (Performs a row exclusion if this field does not match items on a list)",
        "EXCLUDE (...) (Performs a row exclusion if this field matches items in a list)",
        "INCLUDE_REGEX (RegEx) (Performs a row exclusion if this field does not match the RegEx)",
        "EXCLUDE_REGEX (RegEx) (Performs a row exclusion if this field matches the RegEx)",
        "PAD { (count), [data] } (Pads this value with (count) and optional [data])",
        "TRUNCATE { (length) } (Truncates the value to a length of (length))",
        "IF_STATIC { (field-id), (condition), (value) } (Performs a value static if the value in (field-id) matches (condition), then this field becomes (value))",
        "IF_EMPTY { (value), [subRemap] } (Performs a value static if the value is empty, then this field becomes (value), or applies the optional [subRemap])",
        "IF_EMPTY_FIELD { (other-field), (value), [subRemap] } (Performs a value static if the value in (other-field) is empty, then this field becomes (value), or applies the optional [subRemap])",
        "IF_EMPTY_COPY { (other-field), [subRemap] } (Performs a value copy if the value is empty, or preserve the data and apply the optional [subRemap])",
        "IF_FIELDS_EQUAL { (field1), (field2), (value-true), (value-false) } (Performs a field compare of (field1) with (field2), and if they match, return (value-true), or (value-false))",
        "IF_RANGE { (min-value), (max-value), (result), [else], {...} }",
            "* (Performs an integer comparison of existing data using >= (min-value) and <= (max-value), then return (result), or return [else]; can be chained for multiple range tests)",
        "IF_DATE_RANGE { (data-fmt), (min-field), (min-fmt), (max-field), (max-fmt), (before), (between), (after), (out-of-range) } ",
            "* (Performs a date (data-fmt) comparison of existing data using (min-field) with (min-fmt) and <= (max-field) with (max-fmt), then return (before), (between), (after), or (out-of-range); using SimpleDateFormat patterns)",
        "NOT_EMPTY { (value), [subRemap] } (Performs a value static if the value is not empty, then this field becomes (value), or applies the optional [subRemap])",
        "NOT_EMPTY_FIELD { (other-field), (value), [subRemap] } (Performs a value static if the value in (other-field) is not empty, then this field becomes (value), or applies the optional [subRemap])",
        "NOT_EMPTY_COPY { (other-field), [subRemap] } (Performs a value copy if the value is not empty, or preserve the data and apply the optional [subRemap])",
        "COPY { (field-id) } (Performs a value copy of the value in (field-id))",
        "MERGE { (field-id) } (Performs a value merge from the value in (field-id), with a single ' ' space in between)",
        "APPEND { (value) } (Performs a value append of the value in (value), with a space in between)",
        "SWAP (field-id) + subRemap (Performs a field swap with field-id, and also copies the subRemap and executes it)",
            "* subRemap {}: (Any Remap except for a SWAP and DROP; can be nested with a subRemap type)"
    );

    @SerializedName("remap_examples")
    public CSVRemapList remap_examples = CSVRemapList.EXAMPLES;

    @SerializedName("remap_list")
    public CSVRemapList remapList;
}
