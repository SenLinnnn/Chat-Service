package edu.stevens.cs522.chat.twoway.entities;

import java.net.InetAddress;
import java.net.UnknownHostException;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import edu.stevens.cs522.chat.twoway.contracts.PeerContract;

public class Peer implements Parcelable {

	private static final String TAG = Peer.class.getCanonicalName();
	public long id;
	public String name;
	public InetAddress address;
	public int port;

	public Peer(Cursor cursor){
		try {
			this.id = PeerContract.getId(cursor);
			this.name = PeerContract.getName(cursor);
			this.address = InetAddress.getByName(PeerContract.getAddress(cursor));
			this.port = Integer.parseInt(PeerContract.getPort(cursor));
		}catch(UnknownHostException e){
			Log.i(TAG, "UnknownHostException in Peer entity: ");
			e.printStackTrace();
		}

	}

	public Peer(String n,InetAddress a,int p) {
		name=n;
		address=a;
		port=p;
	}
	public Peer(Parcel in) {
		readFromParcel(in);
	}
	public void readFromParcel(Parcel in) { 
		this.id=in.readLong();
        this.name  = in.readString();
        this.address = (InetAddress)in.readParcelable(InetAddress.class.getClassLoader());
        this.port = in.readInt();
        
	 } 
	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(name);
		dest.writeValue(address);
		dest.writeInt(port);
	}
	public static final Parcelable.Creator<Peer> CREATOR = new Parcelable.Creator<Peer>() {  
	    
        public Peer createFromParcel(Parcel in) {  
            return new Peer(in);  
        }  
   
        public Peer[] newArray(int size) {  
            return new Peer[size];  
        }  
          
    };
    public void writeToProvider(ContentValues values) {
		PeerContract.putId(values, id);
		PeerContract.putName(values, name);
		PeerContract.putAddress(values, address.getHostAddress());
		PeerContract.putPort(values, port);
    }
}
