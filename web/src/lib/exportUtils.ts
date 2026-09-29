import { Subscriber, ThreatEvent } from '@/types';

/**
 * Escapes fields for CSV according to RFC 4180
 */
function escapeCSV(val: any): string {
  if (val === null || val === undefined) return '';
  const str = String(val);
  if (str.includes(',') || str.includes('"') || str.includes('\n') || str.includes('\r')) {
    return `"${str.replace(/"/g, '""')}"`;
  }
  return str;
}

/**
 * High-performance CSV export with UTF-8 BOM for Microsoft Excel compatibility
 */
export function exportToCSV(
  filename: string,
  headers: { label: string; key: string }[],
  data: Record<string, any>[]
) {
  const headerRow = headers.map((h) => escapeCSV(h.label)).join(',');
  const rows = data.map((item) =>
    headers.map((h) => escapeCSV(item[h.key] ?? '')).join(',')
  );

  // \uFEFF ensures Excel interprets UTF-8 characters cleanly
  const csvContent = '\uFEFF' + [headerRow, ...rows].join('\r\n');
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);

  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', `${filename}.csv`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}

/**
 * Generates official Microsoft Excel XML Spreadsheet (.xls) with custom styling
 */
export function exportToExcel(
  filename: string,
  sheetName: string,
  headers: { label: string; key: string }[],
  data: Record<string, any>[]
) {
  const headerCells = headers
    .map(
      (h) =>
        `<Cell ss:StyleID="HeaderStyle"><Data ss:Type="String">${escapeXml(h.label)}</Data></Cell>`
    )
    .join('');

  const dataRows = data
    .map((row) => {
      const cells = headers
        .map((h) => {
          const val = row[h.key] ?? '';
          return `<Cell ss:StyleID="DataStyle"><Data ss:Type="String">${escapeXml(String(val))}</Data></Cell>`;
        })
        .join('');
      return `<Row ss:AutoFitHeight="1">${cells}</Row>`;
    })
    .join('');

  const xmlContent = `<?xml version="1.0" encoding="UTF-8"?>
<?mso-application progid="Excel.Sheet"?>
<Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet"
 xmlns:o="urn:schemas-microsoft-com:office:office"
 xmlns:x="urn:schemas-microsoft-com:office:excel"
 xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet">
 <Styles>
  <Style ss:ID="Default" ss:Name="Normal">
   <Alignment ss:Vertical="Center"/>
   <Font ss:FontName="Calibri" ss:Size="11" ss:Color="#000000"/>
  </Style>
  <Style ss:ID="HeaderStyle">
   <Alignment ss:Vertical="Center" ss:Horizontal="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#BE001C"/>
   </Borders>
   <Font ss:FontName="Calibri" ss:Size="11" ss:Bold="1" ss:Color="#FFFFFF"/>
   <Interior ss:Color="#BE001C" ss:Pattern="Solid"/>
  </Style>
  <Style ss:ID="DataStyle">
   <Alignment ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
   </Borders>
   <Font ss:FontName="Calibri" ss:Size="10" ss:Color="#1E293B"/>
  </Style>
 </Styles>
 <Worksheet ss:Name="${escapeXml(sheetName)}">
  <Table ss:DefaultColumnWidth="120" ss:DefaultRowHeight="20">
   <Row ss:Height="26">
    ${headerCells}
   </Row>
   ${dataRows}
  </Table>
 </Worksheet>
</Workbook>`;

  const blob = new Blob([xmlContent], {
    type: 'application/vnd.ms-excel;charset=utf-8;',
  });
  const url = URL.createObjectURL(blob);

  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', `${filename}.xls`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}

function escapeXml(str: string): string {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&apos;');
}

/**
 * Triggers a high-resolution, print-optimized Security Audit Executive Report (PDF)
 */
export function exportToPDFReport(options: {
  title: string;
  generatedBy: string;
  stats: {
    totalSubscribers: number;
    activeSubscribers: number;
    totalThreats: number;
    criticalThreats: number;
    mitigationRate: string;
  };
  threats: ThreatEvent[];
  subscribers: Subscriber[];
}) {
  const printWindow = window.open('', '_blank');
  if (!printWindow) {
    alert('Pop-up window diblokir oleh browser. Izinkan pop-up untuk mencetak PDF.');
    return;
  }

  const nowStr = new Date().toLocaleString('id-ID', {
    dateStyle: 'full',
    timeStyle: 'medium',
  });

  const threatRows = options.threats
    .slice(0, 50)
    .map(
      (t) => `
    <tr>
      <td style="font-family: monospace; font-size: 11px;">${t.id}</td>
      <td style="font-weight: 600; color: ${
        t.severity === 'CRITICAL' ? '#be001c' : t.severity === 'HIGH' ? '#c2410c' : '#0369a1'
      };">${t.severity}</td>
      <td><strong>${t.threat_type}</strong></td>
      <td style="font-family: monospace; font-size: 11px;">${t.msisdn}</td>
      <td style="font-size: 11px; max-width: 200px; word-break: break-all;">${t.target}</td>
      <td><span style="background: #e8f5e9; color: #065f46; padding: 2px 6px; border-radius: 4px; font-size: 10px; font-weight: bold;">${t.action_taken}</span></td>
      <td style="font-size: 11px; color: #64748b;">${new Date(t.timestamp).toLocaleTimeString('id-ID')}</td>
    </tr>
  `
    )
    .join('');

  const html = `<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>${options.title} - Telkomsel Secure</title>
  <style>
    @page { size: A4 landscape; margin: 15mm; }
    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #0f172a; margin: 0; padding: 20px; font-size: 12px; }
    .header { display: flex; justify-content: space-between; align-items: center; border-bottom: 3px solid #be001c; padding-bottom: 12px; margin-bottom: 20px; }
    .brand { font-size: 20px; font-weight: 800; color: #0b132b; letter-spacing: -0.5px; }
    .brand span { color: #ed0226; }
    .meta { text-align: right; color: #64748b; font-size: 11px; }
    .kpi-grid { display: flex; gap: 15px; margin-bottom: 25px; }
    .kpi-card { flex: 1; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 12px; text-align: center; }
    .kpi-card .num { font-size: 22px; font-weight: 800; color: #0b132b; margin-top: 4px; }
    .kpi-card .lbl { font-size: 10px; font-weight: 700; color: #64748b; text-transform: uppercase; }
    table { width: 100%; border-collapse: collapse; margin-top: 15px; }
    th { background: #be001c; color: #ffffff; text-align: left; padding: 8px 10px; font-size: 10px; text-transform: uppercase; letter-spacing: 0.5px; }
    td { padding: 8px 10px; border-bottom: 1px solid #e2e8f0; }
    tr:nth-child(even) { background: #f8fafc; }
    .footer { margin-top: 30px; text-align: center; font-size: 10px; color: #94a3b8; border-top: 1px solid #e2e8f0; padding-top: 10px; }
    @media print {
      body { padding: 0; }
      .no-print { display: none; }
    }
  </style>
</head>
<body>
  <div class="header">
    <div>
      <div class="brand">TELKOMSEL <span>SECURE</span></div>
      <div style="font-weight: 600; color: #475569; margin-top: 2px;">Security Operations Center (SOC) • Executive Incident Report</div>
    </div>
    <div class="meta">
      <div><strong>Tanggal Laporan:</strong> ${nowStr}</div>
      <div><strong>Dicetak Oleh:</strong> ${options.generatedBy}</div>
      <div><strong>Klasifikasi:</strong> Confidential / Telkomsel Cyber Security</div>
    </div>
  </div>

  <div class="kpi-grid">
    <div class="kpi-card">
      <div class="lbl">Total Terdaftar</div>
      <div class="num">${options.stats.totalSubscribers}</div>
    </div>
    <div class="kpi-card">
      <div class="lbl">Subscriber Aktif</div>
      <div class="num" style="color: #059669;">${options.stats.activeSubscribers}</div>
    </div>
    <div class="kpi-card">
      <div class="lbl">Total Ancaman Dicegah</div>
      <div class="num" style="color: #be001c;">${options.stats.totalThreats}</div>
    </div>
    <div class="kpi-card">
      <div class="lbl">Ancaman Kritis</div>
      <div class="num" style="color: #dc2626;">${options.stats.criticalThreats}</div>
    </div>
    <div class="kpi-card">
      <div class="lbl">Tingkat Mitigasi</div>
      <div class="num" style="color: #0284c7;">${options.stats.mitigationRate}</div>
    </div>
  </div>

  <h3 style="margin-bottom: 6px; color: #0f172a; font-size: 13px;">Log Ancaman & Telemetri Siber Terbaru (Sample Eksekutif)</h3>
  <table>
    <thead>
      <tr>
        <th>Incident ID</th>
        <th>Severity</th>
        <th>Kategori</th>
        <th>MSISDN Pelanggan</th>
        <th>Target / URL / File</th>
        <th>Tindakan Mitigasi</th>
        <th>Waktu Insiden</th>
      </tr>
    </thead>
    <tbody>
      ${threatRows || '<tr><td colspan="7" style="text-align: center; color: #94a3b8;">Tidak ada insiden tercatat</td></tr>'}
    </tbody>
  </table>

  <div class="footer">
    Dokumen ini dihasilkan secara otomatis oleh Platform Telkomsel Secure SOC v2.4 (Kaspersky SDK & BlackWall RASP Engine).
  </div>

  <script>
    window.onload = function() {
      setTimeout(function() {
        window.print();
      }, 500);
    };
  </script>
</body>
</html>`;

  printWindow.document.write(html);
  printWindow.document.close();
}
