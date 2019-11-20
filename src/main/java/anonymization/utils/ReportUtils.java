package anonymization.utils;

import anonymization.AnonymizationReport;

import java.util.ArrayList;
import java.util.List;

public class ReportUtils {

    public static List<String> getPrintableReport(AnonymizationReport report) {
        List<String> printableReport = new ArrayList<>();

        printableReport.add("Solution: " + report.getLevelOfAnonymization().toString());
        printableReport.add("K-Value: " + report.getkValue());
        printableReport.add("K-Value with suppression: " + report.getkValueWithSuppression());
        printableReport.add("LOG: " + report.getLogMetric());
        printableReport.add("Percentage of supression: " + report.getPercentageOfSuppression());
        printableReport.add("Rows to delete: " + report.getRowToDelete().toString());

        return printableReport;
    }

    public static void printReport (AnonymizationReport report) {
        List<String> printableReport = getPrintableReport(report);
        for (String s : printableReport) {
            System.out.println(s);
        }
    }


}
