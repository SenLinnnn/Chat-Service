package edu.stevens.cs522.chat.twoway.entities;
import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import edu.stevens.cs522.chat.twoway.contracts.PeerContract;

public class Messages implements Parcelable{
    public long id;
    public String messageText;
    public String sender;
    public int describeContents() {
        return 0;
    }

    public Messages(int i, String m, String s) {
        this.id=i;
        this.messageText  = m;
        this.sender = s;
    }
    public void writeToParcel(Parcel out, int arg1) {
        out.writeLong(id);
        out.writeStringArray(new String[] {this.sender,
                this.messageText});
    }
    public Messages(Parcel in) {
        readFromParcel(in) ;
    }
    public void readFromParcel(Parcel in) {
        this.id=in.readLong();
        String[] data = new String[2];
        in.readStringArray(data);
        this.messageText  = data[0];
        this.sender = data[1];
    }
    public static final Parcelable.Creator<Messages> CREATOR = new Parcelable.Creator<Messages>() {

        public Messages createFromParcel(Parcel in) {
            return new Messages(in);
        }

        public Messages[] newArray(int size) {
            return new Messages[size];
        }

    };
    public void writeToProvider(ContentValues values) {
        PeerContract.putId(values, id);
        PeerContract.putSender(values, sender);
        PeerContract.putText(values, messageText);
    }

}
