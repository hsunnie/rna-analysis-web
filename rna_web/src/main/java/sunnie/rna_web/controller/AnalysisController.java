package sunnie.rna_web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

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
    private final RestTemplate restTemplate;

    public AnalysisController(FileColumnService fileColumnService) {
        this.fileColumnService = fileColumnService;
        this.restTemplate = new RestTemplate();
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

                System.out.println("업로드된 파일명: " + file.getOriginalFilename());

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
        try {System.out.println("===== Python 분석 요청 =====");

            System.out.println("파일: " + file.getOriginalFilename());

            // =====================================
            // Python으로 보낼 Multipart 데이터
            // =====================================
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            body.add("file", new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));
            body.add("geneColumn", geneColumn);
            body.add("log2fcColumn", log2fcColumn);
            body.add("pvalueColumn", pvalueColumn);
            body.add("padjColumn", padjColumn);


            // =====================================
            // HTTP Header
            // =====================================
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);


            // =====================================
            // Python 서버 호출
            // =====================================
            ResponseEntity<Map> pythonResponse =
                    restTemplate.postForEntity("http://127.0.0.1:8000/analyze", request, Map.class);
            System.out.println("Python 응답: " + pythonResponse.getBody());
            return ResponseEntity.ok(pythonResponse.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message",e.getMessage()));
        }
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
                    fileColumnService.getPreview(file, geneColumn, log2fcColumn, pvalueColumn, padjColumn);
            return ResponseEntity.ok(Map.of("preview", preview));
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