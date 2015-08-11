package com.cmcc.hyapps.andyou.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/5/26.
 */
public class User implements Parcelable {
    public int id;
    public String url;
    public String username;
    public String email;
    public List<String> groups = new ArrayList();
    public QHUserInfo user_info;



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.url);
        dest.writeString(this.username);
        dest.writeString(this.email);
        dest.writeList(this.groups);
        dest.writeParcelable(this.user_info, flags);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };
    public String toString() {
        return "User [id=" + id + ", url=" + url + ", username=" + username
                + ", email=" + email + ", user_info=" + user_info +"]";
    }

    public User(Parcel in) {
        this.id = in.readInt();
        this.url = in.readString();
        this.username = in.readString();
        this.email = in.readString();
//        this.groups = in.readArrayList();
//        in.readList(groups,User.class.getClassLoader());
        this.groups = in.createStringArrayList();
        this.user_info = in.readParcelable(User.class.getClassLoader());
    }

    public class QHUserInfo implements Parcelable{

        public String nickname ="";
        public int gender;
        public String avatar_url="";

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.nickname);
            dest.writeInt(this.gender);
            dest.writeString(this.avatar_url);
            if(null==this.avatar_url)this.avatar_url= "";
        }
        public final Parcelable.Creator<QHUserInfo> CREATOR = new Parcelable.Creator<QHUserInfo>() {
            public QHUserInfo createFromParcel(Parcel source) {
                return new QHUserInfo(source);
            }

            public QHUserInfo[] newArray(int size) {
                return new QHUserInfo[size];
            }
        };
        public QHUserInfo(Parcel in) {
            this.nickname = in.readString();
            this.gender = in.readInt();
            this.avatar_url = in.readString();
            if(null==this.avatar_url)this.avatar_url= "";
        }
    }
}
