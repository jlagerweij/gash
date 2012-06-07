package net.lagerwey.gash;

import com.j_spaces.core.IJSpace;
import com.j_spaces.jdbc.driver.GConnection;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class PrettyPrintUtils {
    public static int prettyPrintResultSet(CurrentWorkingLocation currentWorkingLocation, IJSpace space, GConnection conn, ResultSet rs) throws SQLException {
        List<List<String>> data = new ArrayList<List<String>>();
        List<Integer> lengthPerColumn = new ArrayList<Integer>();
        int nrOfObjects = 0;
        List<String> row = new ArrayList<String>();
        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
            String columnName = rs.getMetaData().getColumnName(i);
            row.add(columnName);
            lengthPerColumn.add(columnName.length());
        }
        if (!StringUtils.hasText(currentWorkingLocation.getObjectType())) {
            row.add("COUNT");
        }

        data.add(row);

        while (rs.next()) {
            row = new ArrayList<String>();
            nrOfObjects++;
            String fieldValue = null;
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                fieldValue = rs.getString(i);
                row.add(fieldValue);
                if (lengthPerColumn.get(i - 1) < fieldValue.length()) {
                    lengthPerColumn.set(i - 1, fieldValue.length());
                }
            }
            if (!StringUtils.hasText(currentWorkingLocation.getObjectType())) {
                lengthPerColumn.add(6);
                if (!space.getFinderURL().getSchema().equals("mirror")) {
                    Statement countSt = conn.createStatement();
                    ResultSet countRs = countSt.executeQuery("SELECT COUNT(*) FROM " + fieldValue);
                    while (countRs.next()) {
                        String countValue = countRs.getString(1);
                        row.add(countValue);
                    }
                    countRs.close();
                    countSt.close();
                }
            }
            data.add(row);
        }

        // Data gathered, now lets display it.
        int rowNr = 0;
        for (List<String> dataRow : data) {
            if (rowNr == 0) {
                printColumnLine(lengthPerColumn, dataRow, "_");
            }
            int columnIndex = 0;
            for (String fieldValue : dataRow) {
                Utils.print("%-" + lengthPerColumn.get(columnIndex) + "s\t", fieldValue);
                columnIndex++;
            }

            Utils.println("");
            if (rowNr == 0) {
                printColumnLine(lengthPerColumn, dataRow, "_");
            }
            rowNr++;
        }
        return nrOfObjects;
    }

    private static void printColumnLine(List<Integer> lengthPerColumn, List<String> dataRow, String lineCharacter) {
        StringBuilder s = new StringBuilder();
        int columnIndex = 0;
        for (String fieldValue : dataRow) {
            for (int i = 0; i < lengthPerColumn.get(columnIndex); i++) {
                s.append(lineCharacter);
            }
            s.append("\t");
            columnIndex++;
        }
        Utils.println(s.toString());
    }

}
