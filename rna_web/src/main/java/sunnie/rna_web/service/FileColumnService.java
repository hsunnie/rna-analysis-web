package sunnie.rna_web.service;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FileColumnService {

    // ==========================================
    // 1. 파일의 컬럼 읽기
    // ==========================================

    public List<String> getColumns(MultipartFile file)
            throws Exception {

        String filename = file.getOriginalFilename();

        if (filename == null) {throw new IllegalArgumentException("파일 이름을 확인할 수 없습니다.");}

        String lowerName = filename.toLowerCase();

        // Excel
        if (lowerName.endsWith(".xlsx") || lowerName.endsWith(".xls")) {return readExcel(file);}
        // CSV / TSV / TXT
        return readTextFile(file);
    }


    // ==========================================
    // 2. CSV / TSV / TXT 컬럼 읽기
    // ==========================================

    private List<String> readTextFile(
            MultipartFile file
    ) throws Exception {

        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));

        String firstLine = reader.readLine();
        System.out.println("첫 번째 줄: " + firstLine);

        if (firstLine == null) {throw new IllegalArgumentException("빈 파일입니다.");}

        String delimiter;

        if (firstLine.contains("\t")) {
            delimiter = "\t";
        } else if (firstLine.contains(",")) {
            delimiter = ",";
        } else {
            throw new IllegalArgumentException("CSV 또는 TSV 형식을 확인할 수 없습니다.");
        }

        String[] columns = firstLine.split(delimiter);

        List<String> result = new ArrayList<>();

        for (String column : columns) {
            String cleaned = column.trim().replace("\"", "");
            result.add(cleaned);
        }

        return result;
    }


    // ==========================================
    // 3. Excel 컬럼 읽기
    // ==========================================

    private List<String> readExcel(
            MultipartFile file
    ) throws Exception {

        Workbook workbook =
                WorkbookFactory.create(
                        file.getInputStream()
                );

        Sheet sheet =
                workbook.getSheetAt(0);

        Row headerRow =
                sheet.getRow(0);

        if (headerRow == null) {

            workbook.close();

            throw new IllegalArgumentException(
                    "Excel 파일의 첫 번째 행을 찾을 수 없습니다."
            );
        }

        List<String> columns =
                new ArrayList<>();

        for (Cell cell : headerRow) {

            String value =
                    cell.toString().trim();

            if (!value.isEmpty()) {
                columns.add(value);
            }
        }

        workbook.close();

        return columns;
    }


    // ==========================================
    // 4. 데이터 미리보기
    // ==========================================

    public List<Map<String, String>> getPreview(

            MultipartFile file,

            String geneColumn,

            String log2fcColumn,

            String pvalueColumn,

            String padjColumn

    ) throws Exception {

        String filename =
                file.getOriginalFilename();

        if (filename == null) {
            throw new IllegalArgumentException(
                    "파일 이름을 확인할 수 없습니다."
            );
        }

        String lowerName =
                filename.toLowerCase();


        // Excel
        if (lowerName.endsWith(".xlsx")
                || lowerName.endsWith(".xls")) {

            return readExcelPreview(
                    file,
                    geneColumn,
                    log2fcColumn,
                    pvalueColumn,
                    padjColumn
            );
        }


        // CSV / TSV / TXT
        return readTextPreview(
                file,
                geneColumn,
                log2fcColumn,
                pvalueColumn,
                padjColumn
        );
    }


    // ==========================================
    // 5. CSV / TSV / TXT 미리보기
    // ==========================================

    private List<Map<String, String>> readTextPreview(

            MultipartFile file,

            String geneColumn,

            String log2fcColumn,

            String pvalueColumn,

            String padjColumn

    ) throws Exception {

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                file.getInputStream(),
                                StandardCharsets.UTF_8
                        )
                );


        String firstLine =
                reader.readLine();

        if (firstLine == null) {
            throw new IllegalArgumentException(
                    "빈 파일입니다."
            );
        }


        String delimiter;

        if (firstLine.contains("\t")) {
            delimiter = "\t";
        } else if (firstLine.contains(",")) {
            delimiter = ",";
        } else {
            throw new IllegalArgumentException(
                    "CSV 또는 TSV 형식을 확인할 수 없습니다."
            );
        }


        String[] headers =
                firstLine.split(
                        delimiter,
                        -1
                );


        int geneIndex =
                findColumnIndex(
                        headers,
                        geneColumn
                );

        int log2fcIndex =
                findColumnIndex(
                        headers,
                        log2fcColumn
                );

        int pvalueIndex =
                findColumnIndex(
                        headers,
                        pvalueColumn
                );

        int padjIndex =
                findColumnIndex(
                        headers,
                        padjColumn
                );


        List<Map<String, String>> result =
                new ArrayList<>();


        String line;

        int rowCount = 0;


        while (
                (line = reader.readLine()) != null
                        && rowCount < 10
        ) {

            String[] values =
                    line.split(
                            delimiter,
                            -1
                    );


            Map<String, String> row =
                    new HashMap<>();


            row.put(
                    "gene",
                    getValue(
                            values,
                            geneIndex
                    )
            );


            row.put(
                    "log2fc",
                    getValue(
                            values,
                            log2fcIndex
                    )
            );


            row.put(
                    "pvalue",
                    getValue(
                            values,
                            pvalueIndex
                    )
            );


            row.put(
                    "padj",
                    getValue(
                            values,
                            padjIndex
                    )
            );


            result.add(row);

            rowCount++;
        }


        return result;
    }


    // ==========================================
    // 6. Excel 미리보기
    // ==========================================

    private List<Map<String, String>> readExcelPreview(

            MultipartFile file,

            String geneColumn,

            String log2fcColumn,

            String pvalueColumn,

            String padjColumn

    ) throws Exception {


        Workbook workbook =
                WorkbookFactory.create(
                        file.getInputStream()
                );


        Sheet sheet =
                workbook.getSheetAt(0);


        Row headerRow =
                sheet.getRow(0);


        if (headerRow == null) {

            workbook.close();

            throw new IllegalArgumentException(
                    "Excel 파일의 첫 번째 행을 찾을 수 없습니다."
            );
        }


        List<String> headers =
                new ArrayList<>();


        for (Cell cell : headerRow) {

            headers.add(
                    cell.toString().trim()
            );
        }


        int geneIndex =
                findColumnIndex(
                        headers.toArray(
                                new String[0]
                        ),
                        geneColumn
                );


        int log2fcIndex =
                findColumnIndex(
                        headers.toArray(
                                new String[0]
                        ),
                        log2fcColumn
                );


        int pvalueIndex =
                findColumnIndex(
                        headers.toArray(
                                new String[0]
                        ),
                        pvalueColumn
                );


        int padjIndex =
                findColumnIndex(
                        headers.toArray(
                                new String[0]
                        ),
                        padjColumn
                );


        List<Map<String, String>> result =
                new ArrayList<>();


        int rowCount = 0;


        for (
                int rowIndex = 1;
                rowIndex <= sheet.getLastRowNum()
                        && rowCount < 10;
                rowIndex++
        ) {

            Row row =
                    sheet.getRow(rowIndex);


            if (row == null) {
                continue;
            }


            Map<String, String> data =
                    new HashMap<>();


            data.put(
                    "gene",
                    getExcelValue(
                            row,
                            geneIndex
                    )
            );


            data.put(
                    "log2fc",
                    getExcelValue(
                            row,
                            log2fcIndex
                    )
            );


            data.put(
                    "pvalue",
                    getExcelValue(
                            row,
                            pvalueIndex
                    )
            );


            data.put(
                    "padj",
                    getExcelValue(
                            row,
                            padjIndex
                    )
            );


            result.add(data);

            rowCount++;
        }


        workbook.close();

        return result;
    }


    // ==========================================
    // 7. 컬럼 위치 찾기
    // ==========================================

    private int findColumnIndex(

            String[] headers,

            String target

    ) {

        for (
                int i = 0;
                i < headers.length;
                i++
        ) {

            String header =
                    headers[i]
                            .trim()
                            .replace("\"", "");


            if (header.equals(target)) {
                return i;
            }
        }


        throw new IllegalArgumentException(
                "컬럼을 찾을 수 없습니다: "
                        + target
        );
    }


    // ==========================================
    // 8. CSV/TSV 값 가져오기
    // ==========================================

    private String getValue(

            String[] values,

            int index

    ) {

        if (
                index < 0
                        || index >= values.length
        ) {
            return "";
        }


        return values[index]
                .trim()
                .replace("\"", "");
    }


    // ==========================================
    // 9. Excel 값 가져오기
    // ==========================================

    private String getExcelValue(

            Row row,

            int index

    ) {

        Cell cell =
                row.getCell(index);


        if (cell == null) {
            return "";
        }


        return cell
                .toString()
                .trim();
    }
}