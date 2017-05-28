package com.sanwell.sw_4.model.database.cores;

import io.realm.RealmObject;

/*
 * Created by Roman Kyrylenko on 18/05/16.
 */
public class RItemPlanInfo extends RealmObject {

    private String clientId, id, groupId, goodId;
    private double planned, planSum, fact, factSum;
//    private int isCalculated;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGoodId() {
        return goodId;
    }

    public void setGoodId(String goodId) {
        this.goodId = goodId;
    }

    public double getPlanned() {
        return planned;
    }

    public void setPlanned(double planned) {
        this.planned = planned;
    }

    public double getPlanSum() {
        return planSum;
    }

    public void setPlanSum(double planSum) {
        this.planSum = planSum;
    }

    public double getFact() {
        return fact;
    }

    public void setFact(double fact) {
        this.fact = fact;
    }

    public double getFactSum() {
        return factSum;
    }

    public void setFactSum(double factSum) {
        this.factSum = factSum;
    }

//    public int getIsCalculated() {
//        return isCalculated;
//    }
//
//    public void setIsCalculated(int isCalculated) {
//        this.isCalculated = isCalculated;
//    }
}
