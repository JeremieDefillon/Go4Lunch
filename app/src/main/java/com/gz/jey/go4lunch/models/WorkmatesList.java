package com.gz.jey.go4lunch.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WorkmatesList {

    @SerializedName("results")
    @Expose
    private List<Workmates> workmates = null;

    public List<Workmates> getWorkmates() {
        return workmates;
    }

    public void setWorkmates(List<Workmates> workmates) {
        this.workmates = workmates;
    }
}
