// assets/js/app.js
// Central UI logic for all pages. Safe-guards for missing elements.
// All API calls include Authorization using authHeaders() from auth.js.

// -------------------------------
// API endpoints (adjust if needed)
// -------------------------------
const API = {
  pricing: {
    getPrice: (productId) => `/api/pricing/price?productId=${encodeURIComponent(productId)}`,
    autoAdjust: `/api/pricing/auto-adjust`,
    historyAll: `/api/pricing/history`,
    historyByProduct: (productId) => `/api/pricing/history/${encodeURIComponent(productId)}`
    // If your backend uses query param instead of path:
    // historyByProduct: (productId) => `/api/pricing/history?productId=${encodeURIComponent(productId)}`
  },
  inventory: {
    manualRefill: `/api/inventory/refill`, // expects { productId }
    restockHistory: (productId) => `/api/inventory/restocks?productId=${encodeURIComponent(productId)}`,
    recentRestocks: `/api/inventory/restocks/recent`,
    notifyPurchasing: `/api/inventory/purchasing/notifications`,
    exportCsv: `/api/inventory/export/csv`
  },
  sales: {
    heatmap: (week) => `/api/sales/heatmap?week=${encodeURIComponent(week)}`,
    report4WeekAverage: `/api/sales/heatmap/report/4-week-average` // returns .docx
  },
  security: {
    alerts: `/api/security/alerts`,
    summary: `/api/security/summary`
  },
  products: {
    add: `/api/products`
  }
};

// -------------------------------
// Small DOM helpers
// -------------------------------
const $ = (sel, root = document) => root.querySelector(sel);
const $$ = (sel, root = document) => Array.from(root.querySelectorAll(sel));

function setHTML(el, html) {
  if (!el) return;
  el.innerHTML = html;
}

function setPreJSON(el, data) {
  if (!el) return;
  const json = (typeof data === "string") ? data : JSON.stringify(data, null, 2);
  el.innerHTML = ""; // clear
  const pre = document.createElement("pre");
  pre.textContent = json; // safe
  el.appendChild(pre);
}

function downloadBlob(blob, filename) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  setTimeout(() => {
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }, 0);
}

// -------------------------------
// SMART PRICING PAGE
// -------------------------------
(function initSmartPricing() {
  const formGet = $("#smartPricingForm");
  const productIdInput = $("#productId");
  const priceResult = $("#priceResult");

  if (formGet && productIdInput && priceResult) {
    formGet.addEventListener("submit", async (e) => {
      e.preventDefault();
      const pid = productIdInput.value.trim();
      if (!pid) return setHTML(priceResult, "Please provide a Product ID.");

      setHTML(priceResult, "Loading...");
      const resp = await fetch(API.pricing.getPrice(pid), { headers: authHeaders() });
      if (!resp.ok) {
        const err = await resp.text();
        return setPreJSON(priceResult, err || "Request failed.");
      }
      const data = await resp.json();
      setPreJSON(priceResult, data);
    });
  }

  const autoForm = $("#autoAdjustForm");
  const autoPid = $("#autoAdjustProductId");
  const autoResult = $("#autoAdjustResult");

  if (autoForm && autoPid && autoResult) {
    autoForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const pid = autoPid.value.trim();
      if (!pid) return setHTML(autoResult, "Please provide a Product ID.");

      setHTML(autoResult, "Adjusting...");
      const resp = await fetch(API.pricing.autoAdjust, {
        method: "POST",
        headers: authHeaders(),
        body: JSON.stringify({ productId: pid })
      });
      if (!resp.ok) {
        const err = await resp.text();
        return setPreJSON(autoResult, err || "Request failed.");
      }
      const data = await resp.json();
      setPreJSON(autoResult, data);
    });
  }

  const btnHistAll = $("#loadHistoryAll");
  const histAllResult = $("#historyAllResult");
  if (btnHistAll && histAllResult) {
    btnHistAll.addEventListener("click", async () => {
      setHTML(histAllResult, "Loading history...");
      const resp = await fetch(API.pricing.historyAll, { headers: authHeaders() });
      if (!resp.ok) {
        const err = await resp.text();
        return setPreJSON(histAllResult, err || "Request failed.");
      }
      const data = await resp.json();
      setPreJSON(histAllResult, data);
    });
  }

  const histByForm = $("#historyByProductForm");
  const histPid = $("#historyProductId");
  const histProductResult = $("#historyProductResult");
  if (histByForm && histPid && histProductResult) {
    histByForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const pid = histPid.value.trim();
      if (!pid) return setHTML(histProductResult, "Please provide a Product ID.");

      setHTML(histProductResult, "Loading product history...");
      const resp = await fetch(API.pricing.historyByProduct(pid), { headers: authHeaders() });
      if (!resp.ok) {
        const err = await resp.text();
        return setPreJSON(histProductResult, err || "Request failed.");
      }
      const data = await resp.json();
      setPreJSON(histProductResult, data);
    });
  }
})();

// -------------------------------
// INVENTORY PAGE
// -------------------------------
(function initInventory() {
  const btnManual = $("#btnManualRefill");
  const manualInput = $("#manualProductId");
  const manualResult = $("#manualRefillResult");

  if (btnManual && manualInput && manualResult) {
    btnManual.addEventListener("click", async () => {
      const pid = manualInput.value.trim();
      if (!pid) return setHTML(manualResult, "Please provide a Product ID.");
      setHTML(manualResult, "Processing...");
      const resp = await fetch(API.inventory.manualRefill, {
        method: "POST",
        headers: authHeaders(),
        body: JSON.stringify({ productId: pid })
      });
      if (!resp.ok) {
        const err = await resp.text();
        return setPreJSON(manualResult, err || "Request failed.");
      }
      const data = await resp.json();
      setPreJSON(manualResult, data);
    });
  }

  const btnHist = $("#btnRestockHist");
  const histInput = $("#histProductId");
  const histResult = $("#restockHistoryResult");
  if (btnHist && histInput && histResult) {
    btnHist.addEventListener("click", async () => {
      const pid = histInput.value.trim();
      if (!pid) return setHTML(histResult, "Please provide a Product ID.");
      setHTML(histResult, "Loading history...");
      const resp = await fetch(API.inventory.restockHistory(pid), { headers: authHeaders() });
      if (!resp.ok) {
        const err = await resp.text();
        return setPreJSON(histResult, err || "Request failed.");
      }
      const data = await resp.json();
      setPreJSON(histResult, data);
    });
  }

  const btnRecent = $("#btnRecentRestocks");
  const recentResult = $("#recentRestocksResult");
  if (btnRecent && recentResult) {
    btnRecent.addEventListener("click", async () => {
      setHTML(recentResult, "Loading recent restocks...");
      const resp = await fetch(API.inventory.recentRestocks, { headers: authHeaders() });
      if (!resp.ok) {
        const err = await resp.text();
        return setPreJSON(recentResult, err || "Request failed.");
      }
      const data = await resp.json();
      setPreJSON(recentResult, data);
    });
  }

  const notifyResult = $("#notifyPurchasingResult");
  if (notifyResult) {
    // Auto-load list when inventory page opens
    (async () => {
      setHTML(notifyResult, "Loading notifications...");
      const resp = await fetch(API.inventory.notifyPurchasing, { headers: authHeaders() });
      if (!resp.ok) {
        const err = await resp.text();
        return setPreJSON(notifyResult, err || "Request failed.");
      }
      const data = await resp.json();
      setPreJSON(notifyResult, data);
    })();
  }

  // Export CSV function (called by onclick="exportInventoryCSV()")
  if (typeof window !== "undefined") {
    window.exportInventoryCSV = async function () {
      const resp = await fetch(API.inventory.exportCsv, { headers: authHeaders() });
      if (!resp.ok) {
        const msg = await resp.text();
        return alert(msg || "Export failed."); // ok to use alert here as a quick feedback
      }
      const blob = await resp.blob();
      downloadBlob(blob, "inventory.csv");
    };
  }
})();

// -------------------------------
// SALES HEATMAP PAGE
// -------------------------------
(function initSalesHeatmap() {
  const form = $("#heatmapForm");
  const weekSel = $("#weekSelector");
  const outHeatmap = $("#heatmapOutput");
  const outReloc = $("#relocationOutput");
  const chartCanvas = $("#salesChart");
  const btnReport = $("#downloadReportBtn");

  let chartRef = null;

  if (btnReport) {
    btnReport.addEventListener("click", async () => {
      btnReport.textContent = "Preparing report...";
      const resp = await fetch(API.sales.report4WeekAverage, { headers: authHeaders() });
      btnReport.textContent = "Download 4-Week Average Report (.docx)";
      if (!resp.ok) {
        const err = await resp.text();
        return setPreJSON(outHeatmap || outReloc, err || "Report download failed.");
      }
      const blob = await resp.blob();
      downloadBlob(blob, "Sales_Heatmap_Report_4_Week_Average.docx");
    });
  }

  if (form && weekSel) {
    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      const week = weekSel.value;
      if (outHeatmap) setHTML(outHeatmap, "Loading heatmap...");
      if (outReloc) setHTML(outReloc, "");

      const resp = await fetch(API.sales.heatmap(week), { headers: authHeaders() });
      if (!resp.ok) {
        const err = await resp.text();
        if (outHeatmap) setPreJSON(outHeatmap, err || "Request failed.");
        return;
      }
      const data = await resp.json();

      // Expecting structure like:
      // { heatmap: [{ areaCode, totalSales, topCategories }...],
      //   relocationSuggestions: [{ productId, productName, fromArea, toArea, reason }...] }
      if (outHeatmap) setPreJSON(outHeatmap, data.heatmap || data);
      if (outReloc) setPreJSON(outReloc, data.relocationSuggestions || []);

      // Draw chart if Chart is available and canvas exists
      if (chartCanvas && typeof Chart !== "undefined" && data.heatmap && Array.isArray(data.heatmap)) {
        const labels = data.heatmap.map(x => x.areaCode || x.area || "Area");
        const values = data.heatmap.map(x => Number(x.totalSales || 0));

        // Destroy previous chart if needed
        if (chartRef && typeof chartRef.destroy === "function") chartRef.destroy();

        // One chart per page load
        chartRef = new Chart(chartCanvas.getContext("2d"), {
          type: "bar",
          data: {
            labels,
            datasets: [{
              label: "Total Sales",
              data: values
              // No specific colors or styles as requested
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false
          }
        });
      }
    });
  }
})();

// -------------------------------
// SECURITY PAGE
// -------------------------------
(function initSecurity() {
  const btnLoad = $("#btnLoadSecurity");
  const btnSummary = $("#btnLoadSummary");
  const outAlerts = $("#securityOutput");
  const outSummary = $("#securitySummary");

  if (btnLoad && outAlerts) {
    btnLoad.addEventListener("click", async () => {
      setHTML(outAlerts, "Loading alerts...");
      const resp = await fetch(API.security.alerts, { headers: authHeaders() });
      if (!resp.ok) {
        const err = await resp.text();
        return setPreJSON(outAlerts, err || "Request failed.");
      }
      const data = await resp.json();
      setPreJSON(outAlerts, data);
    });
  }

  if (btnSummary && outSummary) {
    btnSummary.addEventListener("click", async () => {
      setHTML(outSummary, "Loading summary...");
      const resp = await fetch(API.security.summary, { headers: authHeaders() });
      if (!resp.ok) {
        const err = await resp.text();
        return setPreJSON(outSummary, err || "Request failed.");
      }
      const data = await resp.json();
      setPreJSON(outSummary, data);
    });
  }
})();

// -------------------------------
// ADD PRODUCT PAGE
// -------------------------------
(function initAddProduct() {
  const form = $("#addProductForm");
  const resultBox = $("#addProductResult");

  if (form && resultBox) {
    form.addEventListener("submit", async (e) => {
      e.preventDefault();

      const payload = {
        productId: $("#newProductId")?.value?.trim(),
        productName: $("#newProductName")?.value?.trim(),
        quantity: Number($("#newQuantity")?.value || 0),
        minQuantity: Number($("#newMinQuantity")?.value || 0),
        price: Number($("#newPrice")?.value || 0),
        area: $("#newArea")?.value?.trim() || null
      };

      // Basic validation
      if (!payload.productId || !payload.productName) {
        return setHTML(resultBox, "Product ID and Product Name are required.");
      }

      setHTML(resultBox, "Saving...");
      const resp = await fetch(API.products.add, {
        method: "POST",
        headers: authHeaders(),
        body: JSON.stringify(payload)
      });
      if (!resp.ok) {
        const err = await resp.text();
        return setPreJSON(resultBox, err || "Request failed.");
      }
      const data = await resp.json();
      setPreJSON(resultBox, data);
      form.reset();
    });
  }
})();
