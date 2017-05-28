package com.sanwell.sw_4.controller.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sanwell.sw_4.R;
import com.sanwell.sw_4.controller.adapters.GroupAdapter;
import com.sanwell.sw_4.model.database.DataModel;
import com.sanwell.sw_4.model.database.objects.Group;
import com.sanwell.sw_4.model.database.objects.Item;
import com.sanwell.sw_4.model.interfaces.RefreshItemsListCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class CatalogueFragment extends Fragment {

    public GroupAdapter groupAdapter;
    private ListView listView;
    private Stack<Group> historyStack;
    private String defaultChain;
    private Map<String, ArrayList<Group>> cache;
    private RefreshItemsListCallback refreshItemsListCallback;
    private String searchRequest;
    private Boolean isCommonCatalogueSelected = false;

    public Boolean getIsCommonCatalogueSelected() {
        return isCommonCatalogueSelected;
    }

    public void setIsCommonCatalogueSelected(Boolean isCommonCatalogueSelected) {
        if (isCommonCatalogueSelected != this.isCommonCatalogueSelected) {
            this.isCommonCatalogueSelected = isCommonCatalogueSelected;
            cache = new HashMap<>();
            refresh(-1);
        }
    }

    public void refreshListView() {
        groupAdapter.setDefaultChain(defaultChain);
        groupAdapter.notifyDataSetChanged();
    }

    public Integer getCount() {
        if (groupAdapter != null) {
            return groupAdapter.getCount() - 1; // 1 for title
        }
        return 0;
    }

    public void setRefreshItemsListCallback(RefreshItemsListCallback refreshItemsListCallback) {
        this.refreshItemsListCallback = refreshItemsListCallback;
    }

    public void setDefaultDefaultChain() {
        String chain = isCommonCatalogueSelected ? getResources().getString(R.string.all_goods) : getResources().getString(R.string.recommended_goods);
        setDefaultChain(chain);
    }

    public void setDefaultChain(String defaultChain) {
        this.defaultChain = defaultChain;
        groupAdapter.setDefaultChain(defaultChain);
        groupAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_catalogue, container, false);
        listView = (ListView) rootView.findViewById(R.id.catalogue_fragment_list_view);
        cache = new HashMap<>();
        ArrayList<Group> groupArrayList = DataModel.getInstance().getCatalogue("0",
                isCommonCatalogueSelected);
        cache.put("0", groupArrayList);
        groupAdapter = new GroupAdapter(getActivity(), groupArrayList);
        groupAdapter.setDefaultChain(defaultChain);
        setDefaultDefaultChain();
        listView.setAdapter(groupAdapter);
        refresh(0);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handleGroupSelection(position);
            }
        });

        addSwipe(listView);

        return rootView;
    }

    private void addSwipe(View rootView) {
        final GestureDetector gesture = new GestureDetector(getActivity(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2,
                                           float velocityX, float velocityY) {
                        final int SWIPE_MIN_DISTANCE = 120;
                        final int SWIPE_MAX_OFF_PATH = 250;
                        final int SWIPE_THRESHOLD_VELOCITY = 200;
                        try {
                            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                                return false;
                            if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE &&
                                    Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                handleHistoryBack();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return super.onFling(e1, e2, velocityX, velocityY);
                    }
                });
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gesture.onTouchEvent(event);
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        historyStack = new Stack<>();
    }

    public void backToRoot() {
        while (!historyStack.isEmpty()) {
            historyStack.pop();
        }
        refresh(-1);
        if (refreshItemsListCallback != null) {
            refreshItemsListCallback.refresh(groupAdapter.getSelectedGroupItems());
        }
    }

    // move backward
    public Boolean handleHistoryBack() {
        if (historyStack.isEmpty()) {
            return false;
        }
        historyStack.pop();
        refresh(-1);
        return true;
    }

    // move forward
    private void handleGroupSelection(int selectedIndex) {
        if (selectedIndex == 0) { // header
            refresh(0);
            return;
        }
        Group selectedGroup = groupAdapter.getItem(selectedIndex);
        Boolean hasSubs = selectedGroup.hasSubCategories();
        if (hasSubs) {
            historyStack.push(selectedGroup);
        }
        refresh(hasSubs ? -1 : selectedIndex);
    }

    private void refresh(Integer index) {
        String currentID = "0";
        ArrayList<Item> selectedItems = null;
        if (!historyStack.isEmpty()) {
            Group selectedGroup = historyStack.peek();
            selectedItems = selectedGroup.getChildren();
            currentID = selectedGroup.getID();
        }
        ArrayList<Group> groupArrayList;
        if (cache.containsKey(currentID)) {
            groupArrayList = cache.get(currentID);
        } else {
            groupArrayList = DataModel.getInstance().getCatalogue(currentID,
                    isCommonCatalogueSelected);
            cache.put(currentID, groupArrayList);
        }

        int ind = listView.getFirstVisiblePosition();
        View v = listView.getChildAt(0);
        int top = (v == null) ? 0 : (v.getTop() - listView.getPaddingTop());

        groupAdapter = new GroupAdapter(getActivity(), groupArrayList);
        String groupChain = (searchRequest == null || searchRequest.isEmpty()) ?
                (isCommonCatalogueSelected ? getResources().getString(R.string.all_goods) : getResources().getString(R.string.recommended_goods))
                : getResources().getString(R.string.search_results);
        for (Group group : historyStack) {
            if (groupChain.isEmpty()) {
                groupChain = group.getTitle();
            } else {
                if (group.equals(historyStack.peek())) {
                    groupChain += " / " + Html.fromHtml("<p>" + group.getTitle() + "</p>");
                } else {
                    groupChain += " / " + group.getTitle();
                }
            }
        }
        groupAdapter.setChain(groupChain);
        groupAdapter.setDefaultChain(defaultChain);
        groupAdapter.setSelectedCategory(index);
        groupAdapter.setSearchRequest(searchRequest);
        listView.setAdapter(groupAdapter);
        if (refreshItemsListCallback != null) {
            refreshItemsListCallback.refresh((index == -1 || index == 0) ?
                    selectedItems :
                    groupAdapter.getSelectedGroupItems());
        }
        listView.setSelectionFromTop(ind, top);
    }

    public String getSearchRequest() {
        return searchRequest;
    }

    public void setSearchRequest(String searchRequest) {
        this.searchRequest = searchRequest;
        refresh(-1);
    }
}
