package csc472.depaul.edu.csc472finalproject.gameModel;

import android.os.Parcel;
import android.os.Parcelable;

//this class has all the default roles as well as the roles abilities.
public class Role implements Parcelable {

    private String roleName = null;


    static {
        /*
        private final String WEREWOLF;
        private final String VILLAGER;
        private final String PROPHET;
        */
    }

    //TODO complete the set method
    public void setRole(String name, String action){

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    public static final Creator<Role> CREATOR = new Creator<Role>() {
        @Override
        public Role createFromParcel(Parcel source) {
            return new Role(source);
        }

        @Override
        public Role[] newArray(int size) {
            return new Role[size];
        }
    };

    private Role(Parcel source) {

    }

    public Role(){

    }


}
