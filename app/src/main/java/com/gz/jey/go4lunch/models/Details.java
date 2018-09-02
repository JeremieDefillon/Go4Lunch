package com.gz.jey.go4lunch.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Details {

    @SerializedName("result")
    @Expose
    private DetailsResult result;
    @SerializedName("status")
    @Expose
    private String status;

    public DetailsResult getResult() {
        return result;
    }

    public void setResult(DetailsResult result) {
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
