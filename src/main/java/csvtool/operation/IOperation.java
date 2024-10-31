package csvtool.operation;

import org.apache.commons.csv.CSVRecord;

import java.util.List;

public interface IOperation
{
    boolean readCSVFile(String file, List<CSVRecord> records);
    boolean writeCSVFile(String file, List<CSVRecord> records);
}
