package com.james.concurrency.dataobject;

public class Doctor {

    private Long id;

    /**
     * 排班id
     */
    private Long shiftId;

    private String name;

    /**
     * 是否值班
     */
    private Boolean onCall;

    public Doctor(Long shiftId, String name, Boolean onCall) {
        this.shiftId = shiftId;
        this.name = name;
        this.onCall = onCall;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getShiftId() {
        return shiftId;
    }

    public void setShiftId(Long shiftId) {
        this.shiftId = shiftId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getOnCall() {
        return onCall;
    }

    public void setOnCall(Boolean onCall) {
        this.onCall = onCall;
    }
}
