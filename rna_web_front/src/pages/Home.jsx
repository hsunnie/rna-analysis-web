import React, { useState } from "react";

function Home() {
  const [file, setFile] = useState(null);
  const [columns, setColumns] = useState([]);
  const [previewData, setPreviewData] = useState([]);
  const [mapping, setMapping] = useState({gene: "", log2fc: "", pvalue: "", padj: ""});
  const [message, setMessage] = useState("");
  const [showMapping, setShowMapping] = useState(false);

  // 파일 선택
  const handleFileChange = (event) => {
    const selectedFile = event.target.files[0];
    if (selectedFile) {
      setFile(selectedFile);
      setMessage("");
      setColumns([]);
      setShowMapping(false);
    }
  };

  // 컬럼 확인
  const handleCheckColumns = async () => {
    if (!file) {
      setMessage("먼저 DEG 파일을 선택해주세요.");
      return;
    }
    const formData = new FormData();
    formData.append("file", file);
    try {
      const response = await fetch(
        "http://localhost:8080/api/analysis/columns",
        {method: "POST", body: formData}
      );
      const result = await response.json();
      if (!response.ok) {
        setMessage(result.error || "파일을 읽을 수 없습니다.");
        return;
      }
      
      // 컬럼 목록 저장
      setColumns(result.columns);

      // 자동 인식
      const detectedMapping = detectColumns(result.columns);
      setMapping(detectedMapping);
      setShowMapping(true);
      setMessage("컬럼을 확인했습니다.");
    } catch (error) {
      console.error(error);
      setMessage("Spring Boot 서버에 연결할 수 없습니다.");
    }
  };


  // 컬럼 자동 인식
  const detectColumns = (columns) => {
    const normalizedColumns =
      columns.map(column => ({original: column, normalized: column.toLowerCase().replace(/[_\-\s.]/g, "")}));
    const findColumn = (aliases) => {
      const found = normalizedColumns.find(column => aliases.includes(column.normalized));
      return found
        ? found.original
        : "";
    };
    return {
      gene: findColumn(["gene", "geneid", "genename", "genesymbol", "symbol"]),
      log2fc: findColumn(["log2foldchange", "log2fc", "logfc", "log2foldchange"]),
      pvalue: findColumn(["pvalue", "pval", "pvaluevalue"]),
      padj: findColumn(["padj", "fdr", "adjpval", "adjustedpvalue", "qvalue"])
    };
  };

  // dropdown 변경
  const handleMappingChange = (type, value) => {setMapping({...mapping, [type]: value});};

  // 최종 확인 -> Spring boot로 전송
  const handleStartAnalysis = async () => {
    if (!mapping.gene || !mapping.log2fc || !mapping.pvalue || !mapping.padj) {
      setMessage("모든 컬럼을 선택해주세요.");
      return;
    }

    const formData = new FormData();

    formData.append("file", file);
    formData.append("geneColumn", mapping.gene);
    formData.append("log2fcColumn", mapping.log2fc);
    formData.append("pvalueColumn", mapping.pvalue);
    formData.append("padjColumn", mapping.padj);

    try {
      const response = await fetch(
        "http://localhost:8080/api/analysis/preview",
        {method: "POST", body: formData}
      );
      const result = await response.json();
      
      if (!response.ok) {
        setMessage(result.error || "데이터를 불러올 수 없습니다.");
        return;
      }

      console.log("미리보기:", result.preview);
      setPreviewData(result.preview);
      setMessage("데이터를 정상적으로 읽었습니다.");
    } catch (error) {

      console.error(error);
      setMessage("Spring Boot 서버에 연결할 수 없습니다.");
    }
  };

  const handlePreview = async () => {
    if (!file) {alert("파일을 먼저 선택해주세요.");
      return;
    }

    if (!mapping.gene || !mapping.log2fc || !mapping.pvalue || !mapping.padj) {
      alert("Gene, Log2FC, P-value, FDR 컬럼을 모두 선택해주세요.");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);
    formData.append("geneColumn", mapping.gene);
    formData.append("log2fcColumn", mapping.log2fc);
    formData.append("pvalueColumn", mapping.pvalue);
    formData.append("padjColumn", mapping.padj);

    try {
      const response = await fetch("http://localhost:8080/api/analysis/preview", {method: "POST", body: formData});
      const result = await response.json();
      if (!response.ok) {
        alert(result.error || "미리보기 데이터를 가져오지 못했습니다.");
        return;
      }
      console.log("Preview result:", result);
      setPreviewData(result.preview);
    } catch (error) {
      console.error(error);
      alert("Spring Boot 서버에 연결할 수 없습니다.");
    }
  };

  return (
    <div className="home-container">
      <h1>🧬 RNA Web</h1>
      <h2>RNA-seq Analysis Platform</h2>
      <p>DEG 데이터를 업로드하고<br />다양한 분석 결과를 확인하세요.</p>  
      <div className="upload-box"> {/* 파일 업로드 */}
        <label htmlFor="file-upload" className="upload-button">📁 파일 선택</label>
        <input id="file-upload" type="file" accept=".csv,.tsv,.txt,.xlsx,.xls" onChange={handleFileChange}/>
        <p>{file ? `📄 ${file.name}` : "선택된 파일 없음"}</p>
      </div>
    
      <button className="analyze-button" onClick={handleCheckColumns}>데이터 확인</button> {/* 컬럼 확인 버튼 */}


      {/* 컬럼 설정 */}
      {showMapping && (
        <div className="mapping-box">
          <h3>📊 데이터 컬럼 확인</h3>
          <p>자동으로 인식된 컬럼을 확인해주세요.</p>

          {/* Gene */}
          <ColumnSelector label="Gene" value={mapping.gene} columns={columns} onChange={(value) => handleMappingChange("gene", value)} />

          {/* Log2FC */}
          <ColumnSelector label="Log2 Fold Change" value={mapping.log2fc} columns={columns} onChange={(value) => handleMappingChange("log2fc", value)}/>

          {/* P-value */}
          <ColumnSelector label="P-value" value={mapping.pvalue} columns={columns} onChange={(value) => handleMappingChange("pvalue", value)}/>

          {/* FDR */}
          <ColumnSelector label="Adjusted P-value / FDR" value={mapping.padj} columns={columns} onChange={(value) => handleMappingChange("padj", value)}/>

          <button className="analyze-button" onClick={handlePreview}>📊 데이터 미리보기</button>
        </div>
      )}

      {previewData.length > 0 && (
        <div className="preview-box">

          <h3>📊 데이터 미리보기</h3>
          <p>선택한 컬럼의 처음 10개 데이터를 확인해주세요.</p>
          <table>
            <thead>
              <tr>
                <th>Gene</th>
                <th>Log2FC</th>
                <th>P-value</th>
                <th>FDR</th>
              </tr>
            </thead>
            <tbody>
              {previewData.map(
                (row, index) => (
                  <tr key={index}>
                    <td>{row.gene}</td>
                    <td>{row.log2fc}</td>
                    <td>{row.pvalue}</td>
                    <td>{row.padj}</td>
                  </tr>
                )
              )}
            </tbody>
          </table>
          <button className="analyze-button" onClick={() => {setMessage("데이터가 확인되었습니다. 다음 단계에서 분석을 시작합니다.");}}>
            🧬 이 데이터로 분석 시작
          </button>
        </div>
      )}

      {/* 메시지 */}
      {message && (<p>{message}</p>)}
    </div>
  );
}


// Dropdown 컴포넌트
function ColumnSelector({label, value, columns, onChange}) {
  return (
    <div className="column-selector">
      <label>{label}</label>
      <select value={value} onChange={(e) => onChange(e.target.value)}>
        <option value="">-- 컬럼 선택 --</option>
        {columns.map((column) => (<option key={column} value={column}>{column}</option>))}
      </select>
    </div>
  );
}

export default Home;