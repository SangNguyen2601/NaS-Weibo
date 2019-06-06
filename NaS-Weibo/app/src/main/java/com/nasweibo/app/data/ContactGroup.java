package com.nasweibo.app.data;

import java.util.List;


public class ContactGroup {
    private String groupName;
    private int size;
    private List<People> listPeople;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<People> getListPeople() {
        return listPeople;
    }

    public void setListPeople(List<People> listPeople) {
        this.listPeople = listPeople;
    }

    public boolean equals(Object contactGroup){
      if (contactGroup instanceof ContactGroup){
        if (this.groupName.equals(((ContactGroup) contactGroup).getGroupName())) return true;
        return false;
      }
      return false;
    }
}
