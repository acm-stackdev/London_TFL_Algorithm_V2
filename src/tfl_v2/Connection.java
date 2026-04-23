package tfl_v2;

/**
 * Represents a single track between two stations.
 * V2: Used as an Edge for Graph structure.
 */
public class Connection {
    private Station destination;
    private String lineName;
    private String direction;
    private double normalTime;
    private double delayTime;
    private boolean isOpen;

    public Connection(Station destination, String lineName, String direction, double normalTime) {
        this.destination = destination;
        this.lineName = lineName;
        this.direction = direction;
        this.normalTime = normalTime;
        this.delayTime = 0.0;
        this.isOpen = true;
    }

    public double getTotalTime() {
        return normalTime + delayTime;
    }

    public Station getDestination() {
        return destination;
    }
    public String getLineName() {
        return lineName;
    }
    public String getDirection() {
        return direction;
    }
    public double getDelayTime() {
        return delayTime;
    }
    public void setDelayTime(double delayTime) {
        this.delayTime = delayTime;
    }
    public boolean isOpen() {
        return isOpen;
    }
    public void setOpenStatus(boolean open) {
        isOpen = open;
    }
    public double getNormalTime() {
        return normalTime;
    }
}
