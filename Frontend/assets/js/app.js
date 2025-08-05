// ==============================
// AUTH HELPERS
// ==============================
function getAuthHeaders() {
  const token = localStorage.getItem("jwtToken");
  return {
    "Content-Type": "application/json",
    "Authorization": `Bearer ${token}`
  };
}

// ==============================
// ANIMATION HELPERS
// ==============================
function applyAnimation(element) {
  element.classList.remove("show-animation");
  void element.offsetWidth;
  element.classList.add("show-animation");

  const table = element.querySelector("table");
  if (table) {
    table.classList.add("animate-columns");
    table.querySelectorAll("th, td").forEach((cell, i) => {
      cell.style.animationDelay = `${i * 0.05}s`;
    });
  }
}

// ==============================
// DOM LOADED EVENT
// ==============================
document.addEventListener("DOMContentLoaded", () => {

  // ==============================
  // LOGIN
  // ==============================
  document.getElementById("loginForm")?.addEventListener("submit", (e) => {
    e.preventDefault();
    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value.trim();
    const resultDiv = document.getElementById("loginResult");
    resultDiv.innerHTML = "Processing...";

    fetch("http://localhost:8080/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password }),
    })
      .then(res => {
        if (!res.ok) {
          localStorage.removeItem("jwtToken");
          throw new Error("Invalid credentials.");
        }
        return res.json();
      })
      .then(data => {
        localStorage.setItem("jwtToken", data.token);
        resultDiv.innerHTML = `<p style="color: green;"><strong>Login successful!</strong></p>`;
      })
      .catch(err => {
        resultDiv.innerHTML = `<p class="error">${err.message}</p>`;
      });
  });

  // ==============================
  // SMART PRICING - GET PRICE
  // ==============================
  document.getElementById("smartPricingForm")?.addEventListener("submit", (e) => {
    e.preventDefault();
    const productId = document.getElementById("productId").value.trim();
    const resultDiv = document.getElementById("priceResult");
    resultDiv.innerHTML = "Loading...";

    fetch(`http://localhost:8080/api/pricing/price?productId=${productId}`, {
      headers: getAuthHeaders()
    })
      .then(res => {
        if (res.status === 403) throw new Error("Access denied. Please login again.");
        if (!res.ok) throw new Error("Product not found.");
        return res.json();
      })
      .then(data => {
        resultDiv.innerHTML = `
          <h3>Product Information</h3>
          <p><strong>Product:</strong> ${data.productName || "N/A"} (${data.productId || productId})</p>
          <p><strong>Current Price:</strong> €${typeof data.currentPrice === "number" ? data.currentPrice.toFixed(2) : "N/A"}</p>
          <p><strong>Area:</strong> ${data.area || "N/A"}</p>
        `;
        applyAnimation(resultDiv);
      })
      .catch(err => {
        resultDiv.innerHTML = `<p class='error'>${err.message}</p>`;
        applyAnimation(resultDiv);
      });
  });

  // ==============================
  // SMART PRICING - AUTO ADJUST
  // ==============================
  document.getElementById("autoAdjustForm")?.addEventListener("submit", (e) => {
    e.preventDefault();
    const productId = document.getElementById("autoAdjustProductId").value.trim();
    const resultDiv = document.getElementById("autoAdjustResult");
    resultDiv.innerHTML = "Processing automatic adjustment...";

    fetch(`http://localhost:8080/api/pricing/auto-adjust?productId=${productId}`, {
      method: "POST",
      headers: getAuthHeaders()
    })
      .then(res => {
        if (res.status === 403) throw new Error("Access denied. Please login again.");
        if (!res.ok) throw new Error("Product not found or adjustment failed.");
        return res.json();
      })
      .then(data => {
        resultDiv.innerHTML = `
          <h3>Automatic Price Adjustment</h3>
          <p><strong>Product:</strong> ${data.productName || "N/A"} (${data.productId || productId})</p>
          <p><strong>Original Price:</strong> €${Number(data.originalPrice ?? 0).toFixed(2)}</p>
          <p><strong>New Price:</strong> €${Number(data.adjustedPrice ?? 0).toFixed(2)}</p>
          <p><strong>Reason:</strong> ${data.adjustmentReason || "N/A"}</p>
          <p><strong>Area:</strong> ${data.area || "N/A"}</p>
          <p><strong>Recommendation:</strong> ${data.recommendation || "N/A"}</p>
        `;
        applyAnimation(resultDiv);
      })
      .catch(err => {
        resultDiv.innerHTML = `<p class='error'>${err.message}</p>`;
        applyAnimation(resultDiv);
      });
  });
  // ==============================
  // SMART PRICING - HISTORY ALL
  // ==============================
  document.getElementById("loadHistoryAll")?.addEventListener("click", () => {
    const historyDiv = document.getElementById("historyAllResult");
    historyDiv.innerHTML = "Loading...";

    fetch("http://localhost:8080/api/pricing/history", {
      headers: getAuthHeaders()
    })
      .then(res => {
        if (res.status === 403) throw new Error("Access denied. Please login again.");
        if (!res.ok) throw new Error("Could not fetch price history.");
        return res.json();
      })
      .then(data => {
        if (!Array.isArray(data) || data.length === 0) {
          historyDiv.innerHTML = "<p>No price history found.</p>";
        } else {
          let html = "<table><tr><th>Product</th><th>Old Price</th><th>New Price</th><th>User</th><th>Date</th></tr>";
          data.forEach(entry => {
            html += `
              <tr>
                <td>${entry.product_name} (${entry.product_id})</td>
                <td>€${Number(entry.old_price ?? 0).toFixed(2)}</td>
                <td>€${Number(entry.new_price ?? 0).toFixed(2)}</td>
                <td>${entry.updated_by}</td>
                <td>${entry.update_time}</td>
              </tr>
            `;
          });
          html += "</table>";
          historyDiv.innerHTML = html;
        }
        applyAnimation(historyDiv);
      })
      .catch(err => {
        historyDiv.innerHTML = `<p class='error'>${err.message}</p>`;
        applyAnimation(historyDiv);
      });
  });

  // ==============================
  // SMART PRICING - HISTORY BY PRODUCT
  // ==============================
  document.getElementById("historyByProductForm")?.addEventListener("submit", (e) => {
    e.preventDefault();
    const productId = document.getElementById("historyProductId").value.trim();
    const historyDiv = document.getElementById("historyProductResult");
    historyDiv.innerHTML = "Loading...";

    fetch(`http://localhost:8080/api/pricing/history?productId=${productId}`, {
      headers: getAuthHeaders()
    })
      .then(res => {
        if (res.status === 403) throw new Error("Access denied. Please login again.");
        if (!res.ok) throw new Error("Could not fetch product history.");
        return res.json();
      })
      .then(data => {
        if (!Array.isArray(data) || data.length === 0) {
          historyDiv.innerHTML = `<p>No price history found for ${productId}.</p>`;
        } else {
          let html = "<table><tr><th>Old Price</th><th>New Price</th><th>User</th><th>Date</th></tr>";
          data.forEach(entry => {
            html += `
              <tr>
                <td>€${Number(entry.old_price ?? 0).toFixed(2)}</td>
                <td>€${Number(entry.new_price ?? 0).toFixed(2)}</td>
                <td>${entry.updated_by}</td>
                <td>${entry.update_time}</td>
              </tr>
            `;
          });
          html += "</table>";
          historyDiv.innerHTML = html;
        }
        applyAnimation(historyDiv);
      })
      .catch(err => {
        historyDiv.innerHTML = `<p class='error'>${err.message}</p>`;
        applyAnimation(historyDiv);
      });
  });

  // ==============================
  // INVENTORY - MANUAL REFILL
  // ==============================
  document.getElementById("btnManualRefill")?.addEventListener("click", () => {
    const productId = document.getElementById("manualProductId").value.trim();
    const result = document.getElementById("manualRefillResult");
    result.innerHTML = "Processing...";

    fetch(`http://localhost:8080/api/inventory/manual-refill?productId=${productId}`, {
      method: "POST",
      headers: getAuthHeaders()
    })
      .then(res => {
        if (!res.ok) throw new Error("Manual refill failed.");
        return res.text();
      })
      .then(msg => result.innerHTML = `<p style="color:green;">${msg}</p>`)
      .catch(err => result.innerHTML = `<p class="error">${err.message}</p>`);
  });

  // ==============================
  // INVENTORY - REQUEST REPLENISHMENT
  // ==============================
  document.getElementById("btnRequestRepl")?.addEventListener("click", () => {
    const productId = document.getElementById("reqProductId").value.trim();
    const quantity = Number(document.getElementById("reqQuantity").value);
    const result = document.getElementById("requestReplResult");
    result.innerHTML = "Processing...";

    fetch(`http://localhost:8080/api/inventory/request-replenishment?productId=${productId}&quantity=${quantity}`, {
      method: "POST",
      headers: getAuthHeaders()
    })
      .then(res => {
        if (!res.ok) throw new Error("Request replenishment failed.");
        return res.text();
      })
      .then(msg => result.innerHTML = `<p style="color:green;">${msg}</p>`)
      .catch(err => result.innerHTML = `<p class="error">${err.message}</p>`);
  });

  // ==============================
  // INVENTORY - RESTOCK HISTORY
  // ==============================
  document.getElementById("btnRestockHist")?.addEventListener("click", () => {
    const productId = document.getElementById("histProductId").value.trim();
    const resultDiv = document.getElementById("restockHistoryResult");
    resultDiv.innerHTML = "Loading history…";

    fetch(`http://localhost:8080/api/inventory/restock-history?productId=${productId}`, {
      headers: getAuthHeaders()
    })
      .then(res => {
        if (!res.ok) throw new Error("Could not fetch history.");
        return res.json();
      })
      .then(data => {
        renderRestockHistory(data);
      })
      .catch(err => {
        resultDiv.innerHTML = `<p class="error">${err.message}</p>`;
      });

    function renderRestockHistory(data) {
      const result = document.getElementById("restockHistoryResult");

      if (!Array.isArray(data) || data.length === 0) {
        result.innerHTML = "<p>No history found.</p>";
        return;
      }

      let html = "<ul>";
      data.forEach(log => {
        const pid = log.product.productId || log.product.id;
        const qty = log.quantityAdded;
        const ts = new Date(log.timestamp).toLocaleString();
        html += `<li>${pid} – ${qty} units on ${ts}</li>`;
      });
      html += "</ul>";
      result.innerHTML = html;
    }
  });
  // ==============================
  // INVENTORY - RECENT RESTOCKS
  // ==============================
  document.getElementById("btnRecentRestocks")?.addEventListener("click", () => {
    const resultDiv = document.getElementById("recentRestocksResult");
    resultDiv.innerHTML = "Loading recent restocks…";

    fetch("http://localhost:8080/api/inventory/recent-restocks", {
      headers: getAuthHeaders()
    })
      .then(res => {
        if (!res.ok) throw new Error("Could not load recent restocks.");
        return res.json();
      })
      .then(data => {
        renderRecentRestocks(data);
      })
      .catch(err => {
        resultDiv.innerHTML = `<p class="error">${err.message}</p>`;
      });

    function renderRecentRestocks(data) {
      const result = document.getElementById("recentRestocksResult");
      if (!Array.isArray(data) || data.length === 0) {
        result.innerHTML = "<p>No recent restocks.</p>";
        return;
      }

      let html = "<ul>";
      data.forEach(item => {
        const pid = item.productId || (item.product && item.product.productId);
        const qty = item.quantity || item.quantityAdded;
        const ts = new Date(item.timestamp).toLocaleString();
        html += `<li>${pid} – Restocked ${qty} units on ${ts}</li>`;
      });
      html += "</ul>";
      result.innerHTML = html;
    }
  });

  // ==============================
  // INVENTORY - PURCHASING NOTIFICATIONS
  // ==============================
  function loadPurchasingNotifications() {
    const container = document.getElementById("notifyPurchasingResult");
    container.innerHTML = "Loading inventory alerts…";

    fetch("http://localhost:8080/api/inventory/scheduled-alerts", {
      headers: getAuthHeaders()
    })
      .then(res => {
        if (!res.ok) throw new Error("Failed to load scheduled alerts.");
        return res.json();
      })
      .then(alerts => {
        container.innerHTML = "";
        if (!Array.isArray(alerts) || alerts.length === 0) {
          container.innerHTML = "<p>No inventory alerts at this time.</p>";
          return;
        }
        let html = '<ul class="notifications">';
        alerts.forEach(msg => {
          html += `<li>${msg}</li>`;
        });
        html += "</ul>";
        container.innerHTML = html;
        container.scrollTop = container.scrollHeight;
      })
      .catch(err => {
        container.innerHTML = `<p class="error">${err.message}</p>`;
      });
  }

  // ==============================
  // SALES HEATMAP
  // ==============================
  document.getElementById("heatmapForm")?.addEventListener("submit", async (e) => {
    e.preventDefault();
    const week = document.getElementById("weekSelector").value;
    const url = `http://localhost:8080/api/sales/heatmap?week=${week}`;
    const output = document.getElementById("heatmapOutput");
    const suggestions = document.getElementById("relocationOutput");
    const ctx = document.getElementById("salesChart").getContext("2d");
    output.innerHTML = "Loading...";
    suggestions.innerHTML = "";

    try {
      const res = await fetch(url, { headers: getAuthHeaders() });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const dto = await res.json();

      let table = `
        <table class="styled-table">
          <thead>
            <tr><th>Area</th><th>Sales (€)</th><th>Top Categories</th></tr>
          </thead>
          <tbody>
      `;
      dto.heatmap.forEach(h => {
        table += `
          <tr>
            <td>${h.areaCode}</td>
            <td>€${h.totalSales}</td>
            <td>${h.topCategories.join(", ")}</td>
          </tr>
        `;
      });
      table += "</tbody></table>";
      output.innerHTML = table;
      applyAnimation(output);

      if (dto.relocationSuggestions.length) {
        let ul = "<h3>Relocation Suggestions</h3><ul>";
        dto.relocationSuggestions.forEach(s => {
          ul += `
            <li>
              Move <strong>${s.productName}</strong> (${s.productId})
              from <em>${s.fromArea}</em> to <em>${s.toArea}</em>: ${s.reason}
            </li>
          `;
        });
        ul += "</ul>";
        suggestions.innerHTML = ul;
        applyAnimation(suggestions);
      }

      if (window.salesChartInstance) window.salesChartInstance.destroy();
      const labels = dto.heatmap.map(h => h.areaCode);
      const values = dto.heatmap.map(h => h.totalSales);
      window.salesChartInstance = new Chart(ctx, {
        type: 'bar',
        data: { labels, datasets: [{ label: `Week ${week} Sales`, data: values }] },
        options: { responsive: true, scales: { y: { beginAtZero: true } } }
      });

      window.lastHeatmapData = dto.heatmap;
    } catch (err) {
      output.innerHTML = `<p class="error">Error loading heatmap: ${err.message}</p>`;
    }
  });

  // ==============================
  // EXPORT HEATMAP CSV
  // ==============================
  function exportHeatmapCSV() {
    if (!window.lastHeatmapData) {
      alert('Please generate the heatmap first.');
      return;
    }
    let csv = 'Area,Sales,TopCategories\n';
    window.lastHeatmapData.forEach(entry => {
      const cats = '"' + entry.topCategories.join(';') + '"';
      csv += `${entry.areaCode},${entry.totalSales},${cats}\n`;
    });
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `heatmap_week_${document.getElementById('weekSelector').value}.csv`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }

  // ==============================
  // SECURITY MONITOR
  // ==============================
  async function fetchSecurityAlerts() {
    const out = document.getElementById("securityOutput");
    out.innerHTML = "Loading alerts…";
    try {
      const res = await fetch("http://localhost:8080/api/security/alerts", { headers: getAuthHeaders() });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const alerts = await res.json();
      if (!alerts.length) {
        out.innerHTML = "<p>No alerts at this time.</p>";
        return;
      }
      let html = "<ul class='notifications'>";
      alerts.forEach(a => {
        const lvl = a.level ?? "UNKNOWN";
        const msg = a.description ?? "–";
        const area = a.area ?? "–";
        const time = a.timestamp
          ? new Date(a.timestamp).toLocaleString()
          : "Invalid Date";
        html += `
        <li>
          <strong>[${lvl}]</strong> ${msg}
          <br/><small>${time} — ${area}</small>
        </li>`;
      });
      html += "</ul>";
      out.innerHTML = html;
      applyAnimation(out);
    } catch (err) {
      out.innerHTML = `<p class="error">Error loading alerts: ${err.message}</p>`;
      applyAnimation(out);
    }
  }

  async function fetchSecuritySummary() {
    const out = document.getElementById("securitySummary");
    out.innerHTML = "Loading summary…";
    try {
      const res = await fetch("http://localhost:8080/api/security/summary", {
        headers: getAuthHeaders()
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const s = await res.json();
      let html = `
      <h3>Security Summary</h3>
      <p><strong>Total Alerts:</strong> ${s.totalAlerts}</p>
      <p><strong>Average per Day:</strong> ${s.averagePerDay.toFixed(2)}</p>
      <p><strong>Top Locations:</strong> ${s.topLocations.join(", ")}</p>
      <h4>Counts by Level:</h4>
      <ul>
    `;
      for (const [level, count] of Object.entries(s.countsByLevel)) {
        html += `<li>${level}: ${count}</li>`;
      }
      html += "</ul>";
      out.innerHTML = html;
      applyAnimation(out);
    } catch (err) {
      out.innerHTML = `<p class="error">Error loading summary: ${err.message}</p>`;
      applyAnimation(out);
    }
  }

  document.getElementById("btnLoadSecurity").addEventListener("click", fetchSecurityAlerts);
  document.getElementById("btnLoadSummary").addEventListener("click", fetchSecuritySummary);

  setInterval(() => {
    if (window.location.hash === "#security") {
      fetchSecurityAlerts();
      fetchSecuritySummary();
    }
  }, 60000);
});
