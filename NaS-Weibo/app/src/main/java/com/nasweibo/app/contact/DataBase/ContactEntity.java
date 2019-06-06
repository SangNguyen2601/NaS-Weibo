package com.nasweibo.app.contact.DataBase;

import android.os.Parcel;
import android.os.Parcelable;

import com.litesuits.orm.db.annotation.MapCollection;
import com.litesuits.orm.db.annotation.Mapping;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;
import com.litesuits.orm.db.enums.Relation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Table("contact")
public class ContactEntity implements Parcelable, Serializable {

  public static final String ID = "id";
  @PrimaryKey(AssignType.AUTO_INCREMENT)
  int _id;

  @MapCollection(ArrayList.class)
  @Mapping(Relation.OneToMany)
  List<PeopleEntity> friends;
  @MapCollection(ArrayList.class)
  @Mapping(Relation.OneToMany)
  List<PeopleEntity> family;
  @MapCollection(ArrayList.class)
  @Mapping(Relation.OneToMany)
  List<PeopleEntity> coworker;
  @MapCollection(ArrayList.class)
  @Mapping(Relation.OneToMany)
  List<PeopleEntity> Acquaintances;

  public ContactEntity(){}

  protected ContactEntity(Parcel in) {
    _id = in.readInt();
    friends = in.createTypedArrayList(PeopleEntity.CREATOR);
    family = in.createTypedArrayList(PeopleEntity.CREATOR);
    coworker = in.createTypedArrayList(PeopleEntity.CREATOR);
    Acquaintances = in.createTypedArrayList(PeopleEntity.CREATOR);
  }

  public static final Creator<ContactEntity> CREATOR = new Creator<ContactEntity>() {
    @Override
    public ContactEntity createFromParcel(Parcel in) {
      return new ContactEntity(in);
    }

    @Override
    public ContactEntity[] newArray(int size) {
      return new ContactEntity[size];
    }
  };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeInt(_id);
    parcel.writeTypedList(friends);
    parcel.writeTypedList(family);
    parcel.writeTypedList(coworker);
    parcel.writeTypedList(Acquaintances);
  }

  public int get_id() {
    return _id;
  }

  public void set_id(int _id) {
    this._id = _id;
  }

  public List<PeopleEntity> getFriends() {
    return friends;
  }

  public void setFriends(List<PeopleEntity> friends) {
    this.friends = friends;
  }

  public List<PeopleEntity> getFamily() {
    return family;
  }

  public void setFamily(List<PeopleEntity> family) {
    this.family = family;
  }

  public List<PeopleEntity> getCoworker() {
    return coworker;
  }

  public void setCoworker(List<PeopleEntity> coworker) {
    this.coworker = coworker;
  }
}
