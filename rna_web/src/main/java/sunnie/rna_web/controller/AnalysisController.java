package sunnie.rna_web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sunnie.rna_web.service.FileColumnService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
@CrossOrigin(origins = {
        "http://localhost:3000",
        "http://127.0.0.1:3000"
})
public class AnalysisController {

    private final FileColumnService fileColumnService;


    public AnalysisController(
            FileColumnService fileColumnService
    ) {

        this.fileColumnService =
                fileColumnService;
    }


    // ==========================================
    // 1. 파일 컬럼 확인
    // ==========================================

    @PostMapping("/columns")
    public ResponseEntity<?> getColumns(

            @RequestParam("file")
            MultipartFile file

    ) {

        try {

            List<String> columns =
                    fileColumnService
                            .getColumns(file);


            return ResponseEntity.ok(
                    Map.of(
                            "filename",
                            file.getOriginalFilename(),

                            "columns",
                            columns
                    )
            );


        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }


    // ==========================================
    // 2. 분석 시작
    // ==========================================

    @PostMapping("/start")
    public ResponseEntity<?> startAnalysis(

            @RequestParam("file")
            MultipartFile file,

            @RequestParam("geneColumn")
            String geneColumn,

            @RequestParam("log2fcColumn")
            String log2fcColumn,

            @RequestParam("pvalueColumn")
            String pvalueColumn,

            @RequestParam("padjColumn")
            String padjColumn

    ) {

        System.out.println(
                "===== 분석 시작 ====="
        );


        System.out.println(
                "파일: "
                        + file.getOriginalFilename()
        );


        System.out.println(
                "Gene: "
                        + geneColumn
        );


        System.out.println(
                "Log2FC: "
                        + log2fcColumn
        );


        System.out.println(
                "P-value: "
                        + pvalueColumn
        );


        System.out.println(
                "Padj: "
                        + padjColumn
        );


        return ResponseEntity.ok(
                Map.of(
                        "status",
                        "success",

                        "message",
                        "분석 요청을 받았습니다."
                )
        );
    }


    // ==========================================
    // 3. 데이터 미리보기
    // ==========================================

    @PostMapping("/preview")
    public ResponseEntity<?> previewData(

            @RequestParam("file")
            MultipartFile file,

            @RequestParam("geneColumn")
            String geneColumn,

            @RequestParam("log2fcColumn")
            String log2fcColumn,

            @RequestParam("pvalueColumn")
            String pvalueColumn,

            @RequestParam("padjColumn")
            String padjColumn

    ) {

        try {

            List<Map<String, String>> preview =
                    fileColumnService.getPreview(

                            file,

                            geneColumn,

                            log2fcColumn,

                            pvalueColumn,

                            padjColumn
                    );


            return ResponseEntity.ok(
                    Map.of(
                            "preview",
                            preview
                    )
            );


        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }
}