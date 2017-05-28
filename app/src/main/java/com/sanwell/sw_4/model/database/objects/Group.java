package com.sanwell.sw_4.model.database.objects;

import com.sanwell.sw_4.model.Helpers;
import com.sanwell.sw_4.model.database.DataModel;
import com.sanwell.sw_4.model.database.cores.RGroup;
import com.sanwell.sw_4.model.database.cores.RItem;

import java.util.ArrayList;

import io.realm.RealmResults;

public class Group {

    private String ID;
    private String title;
    private String isPlan = "0";
    private ArrayList<Item> children = new ArrayList<>();
    private ArrayList<Item> filteredChildren = new ArrayList<>();
    private String searchRequest;

    private String cacheRequest;
    private Boolean cacheResponse;

    public Group(RGroup group) {
        this(group, false);
    }

    public Group(RGroup group, boolean isInPlan) {
        if (group != null) {
            ID = group.getId();
            title = group.getName();
            isPlan = group.getIsPlanGroup();
        }
        children = new ArrayList<>(DataModel.getInstance()
                .getItems(ID, Helpers.currentClient.getId(), isInPlan));
        filteredChildren = new ArrayList<>(children);
    }

    // BRG
    public void setSearchRequest(String searchRequest) {
        this.searchRequest = searchRequest;
        if (searchRequest == null || searchRequest.isEmpty()) {
            filteredChildren = children;
        }
        else
        {
            String srch = searchRequest.toLowerCase();
            filteredChildren = new ArrayList<>();
            for (Item child : children)
                {
                    // BRG - доработал фильтр по нескольким фрагментам поиска через пробел
                    String[] arrcontent = srch.split(" ");
                    boolean added = true;
                    for (int i = 0; i < arrcontent.length; i++)
                    {
                        String sub_srch = arrcontent[i];
                        if (child.getTitle().toLowerCase().contains(sub_srch))
                        {
                        }
                        else
                        {
                            added = false;
                        }

                    }
                    if (added) {
                        filteredChildren.add(child);
                    }
                }

        }
    }
    /*
    public void setSearchRequest(String searchRequest) {
        this.searchRequest = searchRequest;
        if (searchRequest == null || searchRequest.isEmpty()) {
            filteredChildren = children;
        } else {
            String srch = searchRequest.toLowerCase();
            filteredChildren = new ArrayList<>();
            for (Item child : children) {
                if (child.getTitle().toLowerCase().contains(srch)) {
                    filteredChildren.add(child);
                }
            }
        }
    }
    */

    public ArrayList<Item> getChildren() {
        return filteredChildren;
    }

    public String getID() {
        return ID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getNumberOfFilteredChildren() {
        if (searchRequest != null && !searchRequest.isEmpty()) {
            return filteredChildren.size();
        }
        return 0;
    }

    public boolean hasSubCategories() {
        if (cacheRequest == null && searchRequest == null && cacheResponse != null) {
            return cacheResponse;
        }
        if (cacheRequest != null && searchRequest != null) {
            if (cacheRequest.equals(searchRequest)) {
                return cacheResponse;
            }
        }
        cacheRequest = searchRequest;
        cacheResponse = _hasSubcategories();
        return cacheResponse;
    }

    private boolean _hasSubcategories() {
        if (searchRequest == null || searchRequest.isEmpty()) {
            return DataModel.getInstance().
                    realm.where(RGroup.class)
                    .beginGroup()
                    .equalTo("isPlanGroup", isPlan)
                    .equalTo("parent", ID)
                    .endGroup()
                    .findAll()
                    .size() != 0;
        } else {
            String req = searchRequest.toUpperCase();
            RealmResults<RGroup> groups = DataModel.getInstance().
                    realm.where(RGroup.class)
                    .beginGroup()
                    .equalTo("isPlanGroup", isPlan)
                    .equalTo("parent", ID)
                    .endGroup()
                    .findAll();
            for (int i = 0; i < groups.size(); i++) {
                try {
                    if (checkGroupHasSubItems(groups.get(i), req)) {
                        return true;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return false;
    }

    private boolean checkGroupHasSubItems(RGroup group, String req) {
        RItem item = DataModel.getInstance().
                realm.where(RItem.class)
                .beginGroup()
                .equalTo("parent", group.getId())
                .contains("title", req)
                .endGroup()
                .findFirst();
        if (item != null) {
            return true;
        }
        RealmResults<RGroup> groups = DataModel.getInstance()
                .realm.where(RGroup.class)
                .beginGroup()
                .equalTo("isPlanGroup", isPlan)
                .equalTo("parent", group.getId())
                .endGroup()
                .findAll();
        for (int i = 0; i < groups.size(); i++) {
            if (checkGroupHasSubItems(groups.get(i), req)) {
                return true;
            }
        }
        return false;
    }

}
