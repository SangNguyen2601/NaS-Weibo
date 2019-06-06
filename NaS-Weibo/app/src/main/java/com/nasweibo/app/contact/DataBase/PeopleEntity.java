package com.nasweibo.app.contact.DataBase;

import android.os.Parcel;
import android.os.Parcelable;

import com.litesuits.orm.db.annotation.Table;

import java.io.Serializable;


@Table("contact")
public class PeopleEntity implements Parcelable, Serializable {
  public static final String ID_FRIEND = "";

  String id;
  String contactCategory;
  String ownerId;
  String avatar;
  private String name;
  private String email;

  protected PeopleEntity(Parcel in) {
    id = in.readString();
    contactCategory = in.readString();
    ownerId = in.readString();
    avatar = in.readString();
    name = in.readString();
    email = in.readString();
  }

  public static final Creator<PeopleEntity> CREATOR = new Creator<PeopleEntity>() {
    @Override
    public PeopleEntity createFromParcel(Parcel in) {
      return new PeopleEntity(in);
    }

    @Override
    public PeopleEntity[] newArray(int size) {
      return new PeopleEntity[size];
    }
  };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeString(id);
    parcel.writeString(contactCategory);
    parcel.writeString(ownerId);
    parcel.writeString(avatar);
    parcel.writeString(name);
    parcel.writeString(email);
  }

  public PeopleEntity() {
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getContactCategory() {
    return contactCategory;
  }

  public void setContactCategory(String contactCategory) {
    this.contactCategory = contactCategory;
  }

  public String getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  public String getAvatar() {
    return avatar;
  }

  public void setAvatar(String avatar) {
    this.avatar = avatar;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}
