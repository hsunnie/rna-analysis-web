from fastapi import FastAPI, UploadFile, File, Form
import pandas as pd
import io


app = FastAPI()


@app.get("/")
def home():
    return {
        "status": "success",
        "message": "RNA analysis server is running"
    }


@app.post("/analyze")
async def analyze(
    file: UploadFile = File(...),
    geneColumn: str = Form(...),
    log2fcColumn: str = Form(...),
    pvalueColumn: str = Form(...),
    padjColumn: str = Form(...)
):

    # 파일 읽기
    file_bytes = await file.read()

    df = pd.read_csv(
        io.BytesIO(file_bytes)
    )

    # 사용자가 선택한 컬럼만 사용
    result = df[
        [
            geneColumn,
            log2fcColumn,
            pvalueColumn,
            padjColumn
        ]
    ].copy()

    # 표준 컬럼명으로 변경
    result.columns = [
        "gene",
        "log2fc",
        "pvalue",
        "padj"
    ]

    # 숫자형으로 변환
    result["log2fc"] = pd.to_numeric(
        result["log2fc"],
        errors="coerce"
    )

    result["pvalue"] = pd.to_numeric(
        result["pvalue"],
        errors="coerce"
    )

    result["padj"] = pd.to_numeric(
        result["padj"],
        errors="coerce"
    )

    # 기본적인 데이터 정리
    result = result.dropna(
        subset=[
            "gene",
            "log2fc",
            "pvalue",
            "padj"
        ]
    )

    # 통계 요약
    total_genes = len(result)

    significant = result[
        result["padj"] < 0.05
    ]

    upregulated = significant[
        significant["log2fc"] > 0
    ]

    downregulated = significant[
        significant["log2fc"] < 0
    ]

    return {
        "status": "success",

        "totalGenes": total_genes,

        "significantGenes": len(significant),

        "upregulatedGenes": len(upregulated),

        "downregulatedGenes": len(downregulated)
    }