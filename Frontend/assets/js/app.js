// === UTIL: GET AUTH HEADERS ===
function getAuthHeaders() {
  const token = localStorage.getItem("jwtToken");
  return {
    "Content-Type": "application/json",
    "Authorization": `Bearer ${token}`
  };
}

// === LOGIN ===
document.getElementById("loginForm").addEventListener("submit", function (e) {
  e.preventDefault();
  const username = document.getElementById("username").value.trim();
  const password = document.getElementById("password").value.trim();
  const resultDiv = document.getElementById("loginResult");

  fetch("http://localhost:8080/api/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  })
    .then(res => {
      if (!res.ok) throw new Error("Invalid credentials.");
      return res.json();
    })
    .then(data => {
      localStorage.setItem("jwtToken", data.token);
      resultDiv.innerHTML = `<p style="color: green;"><strong>Login successful!</strong></p>`;
    })
    .catch(() => {
      resultDiv.innerHTML = `<p class="error">Login failed. Please try again.</p>`;
    });
});

// === SMART PRICING ===
function applyAnimation(element) {
    // Fade + zoom reset
    element.classList.remove("show-animation");
    void element.offsetWidth;
    element.classList.add("show-animation");

    // Table column-by-column animation
    const table = element.querySelector("table");
    if (table) {
        table.classList.add("animate-columns");
        const cells = table.querySelectorAll("th, td");
        cells.forEach((cell, index) => {
            cell.style.animationDelay = `${index * 0.05}s`;
        });
    }
}


// === SMART PRICING - GET PRICE ===
document.getElementById("smartPricingForm")?.addEventListener("submit", function (e) {
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

// === SMART PRICING - AUTO ADJUST ===
document.getElementById("autoAdjustForm")?.addEventListener("submit", function (e) {
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
                <p><strong>Adjustment Reason:</strong> ${data.adjustmentReason || "N/A"}</p>
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

// === SMART PRICING - LOAD ALL HISTORY ===
document.getElementById("loadHistoryAll")?.addEventListener("click", function () {
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
                applyAnimation(historyDiv);
                return;
            }

            let html = "<table><tr><th>Product</th><th>Old Price</th><th>New Price</th><th>Updated By</th><th>Date</th></tr>";
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
            applyAnimation(historyDiv);
        })
        .catch(err => {
            historyDiv.innerHTML = `<p class='error'>${err.message}</p>`;
            applyAnimation(historyDiv);
        });
});

// === SMART PRICING - LOAD PRODUCT HISTORY ===
document.getElementById("historyByProductForm")?.addEventListener("submit", function (e) {
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
                applyAnimation(historyDiv);
                return;
            }

            let html = "<table><tr><th>Old Price</th><th>New Price</th><th>Updated By</th><th>Date</th></tr>";
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
            applyAnimation(historyDiv);
        })
        .catch(err => {
            historyDiv.innerHTML = `<p class='error'>${err.message}</p>`;
            applyAnimation(historyDiv);
        });
});


// === INVENTORY REFILL ===
window.loadInventory = function () {
  const inventoryDiv = document.getElementById("inventoryList");
  inventoryDiv.innerHTML = "Loading...";

  fetch("http://localhost:8080/api/inventory", {
    headers: getAuthHeaders()
  })
    .then(res => {
      if (res.status === 403) throw new Error("Access denied. Please login again.");
      return res.json();
    })
    .then(data => {
      if (!Array.isArray(data) || data.length === 0) {
        inventoryDiv.innerHTML = "<p>No products found.</p>";
        return;
      }
      let html = "<ul>";
      data.forEach(item => {
        const warning = item.quantity < item.minQuantity
          ? " <span class='low-stock'>⚠️ Low Stock!</span>"
          : "";
        html += `<li>${item.productName} - Qty: ${item.quantity}${warning}</li>`;
      });
      html += "</ul>";
      inventoryDiv.innerHTML = html;
    })
    .catch(err => {
      inventoryDiv.innerHTML = `<p class='error'>${err.message}</p>`;
    });
};

// === SALES HEATMAP ===
let salesChart;
function renderSalesChart(heatmapData) {
  const ctx = document.getElementById("salesChart").getContext("2d");
  const labels = heatmapData.map(entry => entry.areaCode);
  const values = heatmapData.map(entry => entry.totalSales);

  if (salesChart) salesChart.destroy();

  salesChart = new Chart(ctx, {
    type: "bar",
    data: {
      labels: labels,
      datasets: [{
        label: "Total Sales (€)",
        data: values,
        backgroundColor: "#006699"
      }]
    },
    options: {
      responsive: true,
      plugins: {
        legend: { display: false },
        title: {
          display: true,
          text: "Weekly Sales by Area"
        }
      }
    }
  });
}

document.getElementById("heatmapForm").addEventListener("submit", function (e) {
  e.preventDefault();
  const week = document.getElementById("weekSelector").value;
  const output = document.getElementById("heatmapOutput");
  const suggestions = document.getElementById("relocationOutput");
  output.innerHTML = "Loading...";
  suggestions.innerHTML = "";

  fetch(`http://localhost:8080/api/salesheatmap?week=${week}`, {
    headers: getAuthHeaders()
  })
    .then(res => {
      if (res.status === 403) throw new Error("Access denied. Please login again.");
      return res.json();
    })
    .then(data => {
      let heatmapHtml = "<h3>Heatmap</h3><ul>";
      data.heatmap?.forEach(entry => {
        heatmapHtml += `<li>${entry.areaCode} - €${entry.totalSales} - Top: ${entry.topCategories?.join(", ")}</li>`;
      });
      heatmapHtml += "</ul>";

      let suggestionHtml = "<h3>Relocation Suggestions</h3><ul>";
      data.relocationSuggestions?.forEach(sug => {
        suggestionHtml += `<li>Move ${sug.productName} from ${sug.fromArea} to ${sug.toArea} - ${sug.reason}</li>`;
      });
      suggestionHtml += "</ul>";

      output.innerHTML = heatmapHtml;
      suggestions.innerHTML = suggestionHtml;

      renderSalesChart(data.heatmap || []);
    })
    .catch(err => {
      output.innerHTML = `<p class='error'>${err.message}</p>`;
    });
});

// === SECURITY MONITOR ===
window.loadSecurityEvents = function () {
  const secOut = document.getElementById("securityOutput");
  secOut.innerHTML = "Loading...";

  fetch("http://localhost:8080/api/security/summary", {
    headers: getAuthHeaders()
  })
    .then(res => {
      if (res.status === 403) throw new Error("Access denied. Please login again.");
      return res.json();
    })
    .then(data => {
      secOut.innerHTML = `
        <p><strong>Total Events:</strong> ${data.totalEvents || 0}</p>
        <p><strong>Most Affected Area:</strong> ${data.areaWithMostAlerts || "N/A"}</p>
        <p><strong>Recommendation:</strong> ${data.recommendation || "N/A"}</p>
      `;
    })
    .catch(err => {
      secOut.innerHTML = `<p class='error'>${err.message}</p>`;
    });
};

// === CSV EXPORT ===
function downloadCSV(filename, csvContent) {
  const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" });
  const link = document.createElement("a");
  link.href = URL.createObjectURL(blob);
  link.download = filename;
  link.click();
}

function exportInventoryCSV() {
  fetch("http://localhost:8080/api/inventory", {
    headers: getAuthHeaders()
  })
    .then(res => {
      if (res.status === 403) throw new Error("Access denied.");
      return res.json();
    })
    .then(data => {
      let csv = "Product Name,Quantity,Min Quantity\n";
      data.forEach(item => {
        csv += `${item.productName},${item.quantity},${item.minQuantity}\n`;
      });
      downloadCSV("inventory.csv", csv);
    })
    .catch(err => alert(err.message));
}

function exportHeatmapCSV() {
  const week = document.getElementById("weekSelector").value;
  fetch(`http://localhost:8080/api/salesheatmap?week=${week}`, {
    headers: getAuthHeaders()
  })
    .then(res => {
      if (res.status === 403) throw new Error("Access denied.");
      return res.json();
    })
    .then(data => {
      let csv = "Area Code,Total Sales,Top Categories\n";
      data.heatmap.forEach(entry => {
        csv += `${entry.areaCode},${entry.totalSales},"${entry.topCategories.join(" | ")}"\n`;
      });
      downloadCSV("sales_heatmap.csv", csv);
    })
    .catch(err => alert(err.message));
}

// === ADD PRODUCT ===
document.getElementById("addProductForm").addEventListener("submit", function (e) {
  e.preventDefault();

  const product = {
    productId: document.getElementById("newProductId").value.trim(),
    name: document.getElementById("newProductName").value.trim(),
    quantity: parseInt(document.getElementById("newQuantity").value),
    minimumQuantity: parseInt(document.getElementById("newMinQuantity").value),
    price: parseFloat(document.getElementById("newPrice").value),
    area: document.getElementById("newArea").value.trim()
  };

  const resultDiv = document.getElementById("addProductResult");
  resultDiv.innerHTML = "Submitting...";

  fetch("http://localhost:8080/api/products", {
    method: "POST",
    headers: getAuthHeaders(),
    body: JSON.stringify(product)
  })
    .then(res => res.text())
    .then(msg => {
      resultDiv.innerHTML = `<p style="color: green;">${msg}</p>`;
    })
    .catch(err => {
      resultDiv.innerHTML = `<p class="error">Error: ${err.message}</p>`;
    });
});
