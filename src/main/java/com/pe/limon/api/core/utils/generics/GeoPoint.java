package com.pe.limon.api.core.utils.generics;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeoPoint {
        @NotNull
        private Double latitude;
        @NotNull
        private Double longitude;

        public GeoPoint(double latitude, double longitude) {
                this.latitude = latitude;
                this.longitude = longitude;
        }
        @Override
        public String toString() {
                return String.format("POINT(%f %f)", longitude, latitude);
        }

        public GeoPoint (String location) {
                String[] coordinates = location.replace("POINT(", "").replace(")", "").split(" ");
                this.longitude = Double.parseDouble(coordinates[0]);
                this.latitude = Double.parseDouble(coordinates[1]);
        }
}

