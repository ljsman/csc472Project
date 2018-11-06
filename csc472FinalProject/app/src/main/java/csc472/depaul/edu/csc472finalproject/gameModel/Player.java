package csc472.depaul.edu.csc472finalproject.gameModel;

import android.os.Parcel;
import android.os.Parcelable;

public class Player extends Role implements Parcelable {
    private String playerName = "";
    private String avatorName = "";
    private int playerId;


    public Player(String playerName, String avatorName, int playerId){
        this.playerName = playerName;
        this.avatorName = avatorName;
        this.playerId = playerId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(playerName);
        dest.writeString(avatorName);
        dest.writeInt(playerId);
    }

    public static final Parcelable.Creator<Player> CREATOR = new Parcelable.Creator<Player>(){
        public Player[] newArray(int size){return new Player[size];}

        @Override
        public Player createFromParcel(Parcel source) {
            return new Player(source);
        }
    };

    private Player(Parcel source){
        playerName = source.readString();
        avatorName = source.readString();
        playerId = source.readInt();
    }
}
