package org.example;

public class SensorReading {
    private String id;
    private String sensorId;
    private long timestampEpochMs;
    private double value;
    private String unit;

    public SensorReading() {
    }

    public SensorReading(String id, String sensorId, long timestampEpochMs, double value, String unit) {
        this.id = id;
        this.sensorId = sensorId;
        this.timestampEpochMs = timestampEpochMs;
        this.value = value;
        this.unit = unit;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public long getTimestampEpochMs() {
        return timestampEpochMs;
    }

    public void setTimestampEpochMs(long timestampEpochMs) {
        this.timestampEpochMs = timestampEpochMs;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}

