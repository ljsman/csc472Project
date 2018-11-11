package test.depaul.edu.test.Models;

import org.json.JSONException;
import org.json.JSONObject;

public class Player {
    public String name = null;
    public int position = -1;
    public final class RoleType {
        public static final int Unknown = 0b0;
        public static final int Werewolf = 0b1;
        public static final int Villager = 0b10;
        public static final int Seer = 0b100;
    }
    public static String getRoleString(int role) {
        switch (role) {
            case RoleType.Werewolf:
                return "Werewolf";
            case RoleType.Villager:
                return "Villager";
            case RoleType.Seer:
                return "Seer";
            default:
                return "Unknown";
        }
    }

    public int role = RoleType.Unknown;
    public enum Status {
        Alive,
        Died,
    }
    public Status status = Status.Alive;

    public Player(int pos) {
        position = pos;
    }

    public void updateInfo(JSONObject json) {
        try {
            name = json.getString("name");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
