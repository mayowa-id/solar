package com.example.solar.common.util;

import java.math.BigDecimal;

public class GeoUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /*
      Distance btw two points using Haversine formula:

      @param lat1 Latitude of first point
      @param lon1 Longitude of first point
      @param lat2 Latitude of second point
      @param lon2 Longitude of second point
      @return Distance in kilometers
     */
    public static double calculateDistance(BigDecimal lat1, BigDecimal lon1,
                                           BigDecimal lat2, BigDecimal lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return Double.MAX_VALUE;
        }

        double lat1Rad = Math.toRadians(lat1.doubleValue());
        double lon1Rad = Math.toRadians(lon1.doubleValue());
        double lat2Rad = Math.toRadians(lat2.doubleValue());
        double lon2Rad = Math.toRadians(lon2.doubleValue());

        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /*
       dist. score (0-100) based on distance and service radius
      100 if distance <= 10km, decreasing to 0 at service radius
     */
    public static double calculateDistanceScore(double distanceKm, int serviceRadiusKm) {
        if (distanceKm > serviceRadiusKm) {
            return 0.0;
        }

        if (distanceKm <= 10.0) {
            return 100.0;
        }

        // Linear decrease from 100 at 10km to 0 at service radius
        double score = 100.0 - ((distanceKm - 10.0) / (serviceRadiusKm - 10.0) * 100.0);
        return Math.max(0.0, Math.min(100.0, score));
    }

    public static boolean isValidLatitude(BigDecimal latitude) {
        if (latitude == null) return false;
        double lat = latitude.doubleValue();
        return lat >= -90.0 && lat <= 90.0;
    }


    public static boolean isValidLongitude(BigDecimal longitude) {
        if (longitude == null) return false;
        double lon = longitude.doubleValue();
        return lon >= -180.0 && lon <= 180.0;
    }
}