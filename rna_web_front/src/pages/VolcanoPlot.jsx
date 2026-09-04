import React, { useMemo, useState } from "react";
import { ScatterChart, Scatter, XAxis, YAxis, CartesianGrid, Tooltip, ReferenceLine } from "recharts";

function VolcanoPlot({ data }) {
    const [fcCutoff, setFcCutoff] = useState(1);
    const [fdrCutoff, setFdrCutoff] = useState(0.05);
    const [searchGene, setSearchGene] = useState("");

    const processedData = useMemo(() => {
        if (!data) {return [];}

        return data.map((item) => {
            let status = "NS";

            if (item.padj < fdrCutoff && item.log2fc >= fcCutoff) {status = "UP";}
            if (item.padj < fdrCutoff && item.log2fc <= -fcCutoff) {status = "DOWN";}

            return {...item, status};
        });
    }, [data, fcCutoff, fdrCutoff]);

    const stats = useMemo(() => {
        const significant = processedData.filter((item) => item.padj < fdrCutoff);
        const up = processedData.filter((item) => item.status === "UP");
        const down = processedData.filter((item) => item.status === "DOWN");

        return {
            significant: significant.length,
            up: up.length,
            down: down.length
        };
    }, [processedData, fdrCutoff]);

    const selectedGeneData = useMemo(() => {
        if (!searchGene.trim()) {return [];}

        const found = processedData.find((item) => item.gene?.trim().toLowerCase() === searchGene.trim().toLowerCase());

        return found ? [found] : [];
    }, [processedData, searchGene]);

    if (processedData.length === 0) {return <p>Volcano Plot 데이터가 없습니다.</p>;}

    return (
        <div>
            <h3>🌋 Volcano Plot</h3>

            <div>
                <label>Log2FC cutoff: <input type="number" step="0.1" value={fcCutoff} onChange={(e) => setFcCutoff(Number(e.target.value))}/></label>
                <label>FDR cutoff: <input type="number" step="0.01" value={fdrCutoff} onChange={(e) => setFdrCutoff(Number(e.target.value))}/></label>
            </div>

            <div>
                <label>Gene 검색: <input type="text" value={searchGene} placeholder="예: ISG15" onChange={(e) => setSearchGene(e.target.value)}/></label>
            </div>

            <div>
                <span>Significant: {stats.significant}</span>
                <span> | UP: {stats.up}</span>
                <span> | DOWN: {stats.down}</span>
            </div>

            <ScatterChart width={800} height={500} margin={{top: 20, right: 20, bottom: 20, left: 20}}>
                <CartesianGrid />
                <XAxis type="number" dataKey="log2fc" name="Log2 Fold Change"/>
                <YAxis type="number" dataKey="minusLog10Pvalue" name="-log10(p-value)"/>

                <ReferenceLine x={fcCutoff} strokeDasharray="3 3" />
                <ReferenceLine x={-fcCutoff} strokeDasharray="3 3" />

                <Tooltip content={({ active, payload }) => {
                    if (active && payload && payload.length > 0) {
                        const point = payload[0].payload;

                        return (
                            <div style={{background: "white", border: "1px solid #ccc", padding: "8px"}}>
                                <strong>{point.gene}</strong>
                                <div>log2FC: {point.log2fc}</div>
                                <div>p-value: {point.pvalue}</div>
                                <div>FDR: {point.padj}</div>
                                <div>status: {point.status}</div>
                            </div>
                        );
                    }
                    return null;
                }}/>

                <Scatter data={processedData} shape={(props) => {
                    const { cx, cy, payload } = props;

                    let color = "#aaaaaa";
                    if (payload.status === "UP") {color = "red";}
                    if (payload.status === "DOWN") {color = "blue";}

                    return <circle cx={cx} cy={cy} r={2} fill={color} />;
                }}/>

                <Scatter data={selectedGeneData} shape={(props) => {
                    const { cx, cy, payload } = props;

                    return (
                        <g>
                            <circle cx={cx} cy={cy} r={7} stroke="black" strokeWidth={2} />
                            <text x={cx + 10} y={cy - 10} fontSize={14} fontWeight="bold">{payload.gene}</text>
                        </g>
                    );
                }}/>

            </ScatterChart>
        </div>
    );
}

export default VolcanoPlot;