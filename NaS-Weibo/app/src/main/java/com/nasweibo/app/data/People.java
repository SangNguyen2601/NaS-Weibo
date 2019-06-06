package com.nasweibo.app.data;


public class People extends User{

    private String onlineStamp;

    public People(){}

    public People(User user){
        this.setName(user.getName());
        this.setOnlineStamp("Active now");
        this.setAvatar(user.getAvatar());
        this.setOnline(user.isOnline());
        this.setUid(user.getUid());
        this.setEmail(user.getEmail());
        this.setStatus(user.getStatus());
    }

    public String getOnlineStamp() {
        return onlineStamp;
    }

    public void setOnlineStamp(String onlineStamp) {
        this.onlineStamp = onlineStamp;
    }

    @Override
    public int hashCode(){
        return this.getUid().hashCode();
    }

    @Override
    public boolean equals(Object obj){
        if(obj == null){
            return false;
        }

        if(!People.class.isAssignableFrom(obj.getClass())){
            return false;
        }

        final People other = (People)obj;

        if(!getUid().equals(other.getUid())){
            return false;
        }

//        if(!this.getEmail().equals(other.getEmail())){
//            return false;
//        }

        return true;
    }
}
