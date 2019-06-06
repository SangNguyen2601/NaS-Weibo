package com.nasweibo.app.contact.DataBase;

import com.nasweibo.app.data.ContactGroup;
import com.nasweibo.app.data.People;

import java.util.ArrayList;
import java.util.List;


public class MapUtils {
  public static List<PeopleEntity> map1(ContactGroup contactGroup) {
    List<PeopleEntity> peopleEntities = new ArrayList<>();
    for (People people : contactGroup.getListPeople()) {
      PeopleEntity peopleEntity = new PeopleEntity();
      peopleEntity.setAvatar(people.getAvatar());
      peopleEntity.setContactCategory(contactGroup.getGroupName());
      peopleEntity.setEmail(people.getEmail());
      peopleEntity.setName(people.getName());
      peopleEntity.setId(people.getUid());
      peopleEntities.add(peopleEntity);
    }
    return peopleEntities;
  }

  public static List<People> map2(List<PeopleEntity> peopleEntities) {
    List<People> people = new ArrayList<>();
    for (PeopleEntity peopleEntity : peopleEntities) {
      People people1 = new People();
      people1.setName(peopleEntity.getName());
      people1.setAvatar(peopleEntity.getAvatar());
      people1.setEmail(peopleEntity.getEmail());
      people1.setUid(peopleEntity.getId());
      people.add(
          people1
      );
    }
    return people;
  }

  public static List<PeopleEntity> map3(List<ContactGroup> contactGroups) {
    List<PeopleEntity> peopleEntities = new ArrayList<>();
    for (ContactGroup contactGroup : contactGroups) {
      List<PeopleEntity> peopleEntities1 = map1(contactGroup);
      peopleEntities.addAll(peopleEntities1);
    }
    return peopleEntities;
  }

  public static List<ContactGroup> map4(List<PeopleEntity> people) {
    List<ContactGroup> temp = new ArrayList<>();
    for (PeopleEntity peopleEntity : people) {
      ContactGroup contact = new ContactGroup();
      contact.setGroupName(peopleEntity.getContactCategory());
      if (!temp.contains(contact)) temp.add(contact);
    }
    for (PeopleEntity peopleEntity : people) {
      for (ContactGroup contactGroup : temp) {
        if (peopleEntity.getContactCategory().equals(contactGroup.getGroupName())) {
          if (contactGroup.getListPeople() == null)
            contactGroup.setListPeople(new ArrayList<People>());
          contactGroup.getListPeople().add(map5(peopleEntity));
          break;
        }
      }
    }
    return temp;
  }

  public static People map5(PeopleEntity peopleEntity){
    People people = new People();
    people.setUid(peopleEntity.getId());
    people.setEmail(peopleEntity.getEmail());
    people.setAvatar(peopleEntity.getAvatar());
    people.setName(peopleEntity.getName());
    return people;
  }

  public static PeopleEntity map6(People people){
    PeopleEntity peopleEntity = new PeopleEntity();
    peopleEntity.setId(people.getUid());
    peopleEntity.setName(people.getName());
    peopleEntity.setEmail(people.getEmail());
    peopleEntity.setAvatar(people.getAvatar());
    return peopleEntity;
  }
}
