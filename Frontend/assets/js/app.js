// assets/js/app.js
// Central UI logic for all pages. Safe-guards for missing elements.
// All API calls include Authorization using authHeaders() from auth.js.

const API_BASE = "http://localhost:8080";

const API = {
  pricing: {
    getPrice: (productId) => `${API_BASE}/api/pricing/price?productId=${encodeURIComponent(productId)}`,
    autoAdjust: `${API_BASE}/api/pricing/auto-adjust`,
    historyAll: `${API_BASE}/api/pricing/history`,
    // If your backend uses query param instead of path, swap the line below accordingly.
    historyByProduct: (productId) => `${API_BASE}/api/pricing/history/${encodeURIComponent(productId)}`
  },
  inventory: {
    // Backend expects @RequestParam productId (no JSON body)
    manualRefill: `${API_BASE}/api/inventory/manual-refill`,
    requestReplenishment: (productId, quantity) =>
      `${API_BASE}/api/inventory/request-replenishment?productId=${encodeURIComponent(productId)}&quantity=${encodeURIComponent(quantity)}`,
    restockHistory: (productId) =>
      `${API_BASE}/api/inventory/restock-history?productId=${encodeURIComponent(productId)}`,
    recentRestocks: `${API_BASE}/api/inventory/recent-restocks`,
    notifyPurchasing: `${API_BASE}/api/inventory/notify-purchasing`,
    scheduledAlerts: `${API_BASE}/api/inventory/scheduled-alerts`
  },
  sales: {
    heatmap: (week) => `${API_BASE}/api/sales/heatmap?week=${encodeURIComponent(week)}`,
    report4WeekAverage: `${API_BASE}/api/sales/heatmap/report/4-week-average`
  },
  security: {
    alerts: `${API_BASE}/api/security/alerts`,
    summary: `${API_BASE}/api/security/summary`
  },
  products: {
    add: `${API_BASE}/api/products`
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
  el.innerHTML = "";
  const pre = document.createElement("pre");
  pre.textContent = json;
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

      const url = `${API.inventory.manualRefill}?productId=${encodeURIComponent(pid)}`;

      const resp = await fetch(url, {
        method: "POST",
        headers: authHeaders()
      });
      const text = await resp.text();
      if (!resp.ok) {
        return setPreJSON(manualResult, text || "Request failed.");
      }
      setPreJSON(manualResult, text);
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
})();

// -------------------------------
// SALES HEATMAP PAGE
// -------------------------------
// -------------------------------
// SALES HEATMAP PAGE (robust)
// -------------------------------
// -------------------------------
// SALES HEATMAP PAGE (robust + rAF + auto submit)
// -------------------------------
(function initSalesHeatmap() {
  const form = $("#heatmapForm");
  const weekSel = $("#weekSelector");
  const outHeatmap = $("#heatmapOutput");
  const outReloc = $("#relocationOutput");
  const chartCanvas = $("#salesChart");
  const btnReport = $("#downloadReportBtn");

  let chartRef = null;

  // Report download
  if (btnReport) {
    btnReport.addEventListener("click", async () => {
      btnReport.textContent = "Preparing report...";
      try {
        const resp = await fetch(API.sales.report4WeekAverage, { headers: authHeaders() });
        btnReport.textContent = "Download 4-Week Average Report (.docx)";
        if (!resp.ok) {
          const err = await resp.text();
          return setPreJSON(outHeatmap || outReloc, err || "Report download failed.");
        }
        const blob = await resp.blob();
        downloadBlob(blob, "Sales_Heatmap_Report_4_Week_Average.docx");
      } catch {
        btnReport.textContent = "Download 4-Week Average Report (.docx)";
        setPreJSON(outHeatmap || outReloc, "Network error while downloading report.");
      }
    });
  }

  if (!form || !weekSel) return;

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const week = weekSel.value;

    if (outHeatmap) setHTML(outHeatmap, "Loading heatmap...");
    if (outReloc) setHTML(outReloc, "Loading relocation suggestions...");

    try {
      const r = await fetch(API.sales.heatmap(week), { headers: authHeaders() });
      const txt = await r.text();
      let data = null;
      try { data = JSON.parse(txt); } catch {}

      if (!r.ok) {
        if (outHeatmap) setPreJSON(outHeatmap, txt || "Heatmap endpoint failed.");
        return;
      }

      const heat = Array.isArray(data) ? data : (data?.heatmap ?? []);
      const reloc = Array.isArray(data?.relocationSuggestions) ? data.relocationSuggestions : [];

      if (outHeatmap) setPreJSON(outHeatmap, heat.length ? heat : (data ?? txt));
      if (outReloc) {
        if (reloc.length) setPreJSON(outReloc, reloc);
        else setHTML(outReloc, "<em>No relocation suggestions for this week.</em>");
      }

      // Draw chart (wait one frame so CSS/layout settle)
      if (!chartCanvas) return;
      if (typeof Chart === "undefined") { console.warn("Chart.js not found on page."); return; }
      if (!Array.isArray(heat) || !heat.length) { console.warn("No heatmap array to plot."); return; }

      await new Promise(requestAnimationFrame); // <-- key trick
      const rect = chartCanvas.getBoundingClientRect();
      console.log("Canvas size before draw:", rect.width, rect.height);

      if (chartRef && typeof chartRef.destroy === "function") chartRef.destroy();

      const ctx = chartCanvas.getContext("2d");
      chartRef = new Chart(ctx, {
        type: "bar",
        data: {
          labels: heat.map(x => x.areaCode || x.area || "Area"),
          datasets: [{ label: "Total Sales", data: heat.map(x => Number(x.totalSales ?? x.sales ?? 0)) }]
        },
        options: { responsive: true, maintainAspectRatio: false }
      });

      console.log("Heatmap chart drawn with", heat.length, "bars.");
    } catch (e2) {
      if (outHeatmap) setPreJSON(outHeatmap, "Network error.");
      console.error(e2);
    }
  });

  // Auto-submit on load so you see the chart without clicking
  document.addEventListener("DOMContentLoaded", () => {
    form.dispatchEvent(new Event("submit", { cancelable: true, bubbles: true }));
  });
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

// -------------------------------
// Active nav based on current page
// -------------------------------
(function setActiveNav() {
  const links = document.querySelectorAll('#nav a');
  if (!links.length) return;

  const path = (location.pathname.split('/').pop() || '').toLowerCase();
  const current = (!path || path === '/' || path === 'index.html') ? 'home.html' : path;

  links.forEach(a => {
    const href = (a.getAttribute('href') || '').split('/').pop().toLowerCase();
    if (href === current) {
      a.classList.add('active');
      a.classList.add('active-locked');
    } else {
      a.classList.remove('active', 'active-locked');
    }
  });
})();
