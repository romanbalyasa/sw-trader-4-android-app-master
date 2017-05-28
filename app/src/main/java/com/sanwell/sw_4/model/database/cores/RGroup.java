package com.sanwell.sw_4.model.database.cores;

import io.realm.RealmObject;

/**
 * Created by Roman Kyrylenko on 2/9/16.
 */
public class RGroup extends RealmObject {

    private String name;
    private String id;
    private String parent;
//    private String childrenCount;
    private String planOrder;
    private String isPlanGroup = "0";

    public String getPlanOrder() {
        return planOrder;
    }

    public void setPlanOrder(String planOrder) {
        this.planOrder = planOrder;
    }

    public String getIsPlanGroup() {
        return isPlanGroup;
    }

    public void setIsPlanGroup(String isPlanGroup) {
        this.isPlanGroup = isPlanGroup;
    }

//    public String getChildrenCount() {
//        return childrenCount;
//    }

//    public void setChildrenCount(String childrenCount) {
//        this.childrenCount = childrenCount;
//    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
