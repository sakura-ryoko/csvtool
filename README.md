# csvtool
A basic utlity meant to perform operations on multiple CSV files, such as merging, diff, and reformatting.

Most functions use a particular 'Operation' (aka mode of execution) per a task; and this needs to always be the first run paremeter.
See the `--help` menu for more information on use; and refer to the example / documentation under the Reformatting JSON config file generated via the `--header-save` Operation.  There are several 'CSVRemap' functions available that can be used.

Valid Operations:
- `--help` - Help system.  Use `--help --operation` for more details, optional config settings, and typical syntax used for each operation.
- `--test` - A simple test routine to make a copy of an existing CSV file using it's internal mechanisms.
- `--merge` - Merge two CSV files' data assuming that the headers match.
- `--diff` - Compare two CSV files' data and output a DIFF CSV, assuming that the headers match.
- `--header-save` - Saves the Headers of a pair of CSV files (Input, Output) to a JSON Headers config file.
- `--reformat` - Performs a CSV reformat (Input / Output) based on the saved JSON Headers configuration; utilizing the CSV Remap system as outlined in the JSON Config examples.  This method can only match or Shrink the size of the Input CSV Headers.
