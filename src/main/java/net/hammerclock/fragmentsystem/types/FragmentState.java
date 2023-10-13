package net.hammerclock.fragmentsystem.types;

public class FragmentState {
    private String uuid;
    private String name;
    private double x;
    private double y;
    private double z;
    private FragmentStateEnum state;
    private String reason;
    private Integer fragmentNr;

    public String getName() {
        return name;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public FragmentStateEnum getState() {
        return state;
    }

    public String getReason() {
        return reason;
    }

    public Integer getFragmentNumber() {
        return fragmentNr;
    }

    public String getUUID() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setState(FragmentStateEnum state) {
        this.state = state;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setFragmentNr(Integer fragmentNr) {
        this.fragmentNr = fragmentNr;
    }
}