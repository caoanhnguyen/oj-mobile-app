package com.kma.oj_app_mobile.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserHeatMap {
    @SerializedName("totalSubmissions")
    private int totalSubmissions;
    @SerializedName("heatmapItems")
    private List<HeatMapItem> heatmapItems;

    public int getTotalSubmissions() { return totalSubmissions; }
    public List<HeatMapItem> getHeatmapItems() { return heatmapItems; }

    public static class HeatMapItem {
        // Backend serializes LocalDateTime as array: [year, month, day, hour, min, sec, nano]
        // OR as a string - we handle both via a custom deserializer approach using raw JsonElement
        @SerializedName("date")
        private Object date; // can be String or array of ints from Jackson/Retrofit
        @SerializedName("count")
        private Integer count;

        public Object getRawDate() { return date; }
        public Integer getCount() { return count; }

        /**
         * Returns the "yyyy-MM-dd" key regardless of whether backend sends a String or int[].
         */
        public String getDateKey() {
            if (date == null) return null;

            if (date instanceof String) {
                String s = (String) date;
                // Could be "2026-03-14T11:24:26.029692" or "2026-03-14"
                if (s.length() >= 10) return s.substring(0, 10);
            }

            if (date instanceof List) {
                // Gson deserializes JSON array into List<Double> by default when type is Object
                List<?> arr = (List<?>) date;
                if (arr.size() >= 3) {
                    int year  = ((Number) arr.get(0)).intValue();
                    int month = ((Number) arr.get(1)).intValue();
                    int day   = ((Number) arr.get(2)).intValue();
                    return String.format("%04d-%02d-%02d", year, month, day);
                }
            }

            // Fallback: try toString and take first 10 chars
            String s = date.toString();
            return s.length() >= 10 ? s.substring(0, 10) : null;
        }
    }
}
