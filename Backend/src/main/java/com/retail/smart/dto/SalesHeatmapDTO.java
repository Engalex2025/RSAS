package com.retail.smart.dto;

import java.util.List;

public class SalesHeatmapDTO {

    private List<HeatmapEntry> heatmap;
    private List<RelocationSuggestionEntry> relocationSuggestions;

    public SalesHeatmapDTO(List<HeatmapEntry> heatmap, List<RelocationSuggestionEntry> relocationSuggestions) {
        this.heatmap = heatmap;
        this.relocationSuggestions = relocationSuggestions;
    }

    public List<HeatmapEntry> getHeatmap() {
        return heatmap;
    }

    public List<RelocationSuggestionEntry> getRelocationSuggestions() {
        return relocationSuggestions;
    }

    public static class HeatmapEntry {
        private String areaCode;
        private int totalSales;
        private List<String> topCategories;
        private String suggestion;

        public HeatmapEntry(String areaCode, int totalSales, List<String> topCategories, String suggestion) {
            this.areaCode = areaCode;
            this.totalSales = totalSales;
            this.topCategories = topCategories;
            this.suggestion = suggestion;
        }

        public String getAreaCode() {
            return areaCode;
        }

        public int getTotalSales() {
            return totalSales;
        }

        public List<String> getTopCategories() {
            return topCategories;
        }

        public String getSuggestion() {
            return suggestion;
        }
    }

    public static class RelocationSuggestionEntry {
        private String productId;
        private String productName;
        private String category;
        private String fromArea;
        private String toArea;
        private String reason;

        public RelocationSuggestionEntry(String productId, String productName, String category,
                                         String fromArea, String toArea, String reason) {
            this.productId = productId;
            this.productName = productName;
            this.category = category;
            this.fromArea = fromArea;
            this.toArea = toArea;
            this.reason = reason;
        }

        public String getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public String getCategory() {
            return category;
        }

        public String getFromArea() {
            return fromArea;
        }

        public String getToArea() {
            return toArea;
        }

        public String getReason() {
            return reason;
        }
    }
}
