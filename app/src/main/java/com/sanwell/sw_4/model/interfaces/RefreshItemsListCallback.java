package com.sanwell.sw_4.model.interfaces;

import com.sanwell.sw_4.model.database.objects.Item;

import java.util.ArrayList;

public interface RefreshItemsListCallback {

    void refresh(ArrayList<Item> list);

}
