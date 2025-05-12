package utils;

import java.io.*;
import java.util.*;

public class CSVUtils {
    public static List<String[]> readCSV(File file) throws IOException {
        List<String[]> data = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            data.add(line.split(","));
        }
        br.close();
        return data;
    }
}