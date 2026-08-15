package com.sporekart.modules.shipment.domain;

import java.math.BigDecimal;
import java.util.Objects;

public final class PackageDetails {
    private final Integer weightGrams;
    private final Integer lengthMm;
    private final Integer widthMm;
    private final Integer heightMm;
    private final BigDecimal declaredValue;

    public PackageDetails(
            Integer weightGrams,
            Integer lengthMm,
            Integer widthMm,
            Integer heightMm,
            BigDecimal declaredValue
    ) {
        this.weightGrams = weightGrams != null ? weightGrams : 500;
        this.lengthMm = lengthMm != null ? lengthMm : 100;
        this.widthMm = widthMm != null ? widthMm : 100;
        this.heightMm = heightMm != null ? heightMm : 100;
        this.declaredValue = declaredValue != null ? declaredValue : BigDecimal.ZERO;
    }

    public Integer getWeightGrams() { return weightGrams; }
    public Integer getLengthMm() { return lengthMm; }
    public Integer getWidthMm() { return widthMm; }
    public Integer getHeightMm() { return heightMm; }
    public BigDecimal getDeclaredValue() { return declaredValue; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PackageDetails that = (PackageDetails) o;
        return Objects.equals(weightGrams, that.weightGrams) &&
                Objects.equals(lengthMm, that.lengthMm) &&
                Objects.equals(widthMm, that.widthMm) &&
                Objects.equals(heightMm, that.heightMm) &&
                Objects.equals(declaredValue, that.declaredValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(weightGrams, lengthMm, widthMm, heightMm, declaredValue);
    }
}
