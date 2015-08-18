package com.cmcc.hyapps.andyou.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2015/7/14 0014.
 */
public class Recommand implements Parcelable {
    public int id;
    public String image_url;
    public int stype;
    public String action;
    public String name;
    public String content;
    public class RecommandList extends ResultList<Recommand> {}
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "Recommand{" +
                "id=" + id +
                ", image_url='" + image_url + '\'' +
                ", stype=" + stype +
                ", action='" + action + '\'' +
                ", name='" + name + '\'' +
                ", content='" + content + '\'' +
                '}';
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.image_url);
        dest.writeInt(this.stype);
        dest.writeString(this.action);
        dest.writeString(this.name);
        dest.writeString(this.content);
    }

    public Recommand() {
    }


    protected Recommand(Parcel in) {
        this.id = in.readInt();
        this.image_url = in.readString();
        this.stype = in.readInt();
        this.action = in.readString();
        this.name = in.readString();
        this.name = in.readString();
    }

    public static final Parcelable.Creator<Recommand> CREATOR = new Parcelable.Creator<Recommand>() {
        public Recommand createFromParcel(Parcel source) {
            return new Recommand(source);
        }

        public Recommand[] newArray(int size) {
            return new Recommand[size];
        }
    };
}
