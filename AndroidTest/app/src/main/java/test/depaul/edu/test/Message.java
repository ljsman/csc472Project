package test.depaul.edu.test;

import org.json.JSONException;
import org.json.JSONObject;

public class Message {
    private static int requestIdx = 1;

    public final int requestId;
    public final int msgType;
    public JSONObject jsonObj;

    public Message(int type) {
        requestId = requestIdx++;
        msgType = type;
        jsonObj = new JSONObject();
        this.addParam("request_id", requestId);
        this.addParam("type", type);
    }

    public Message(String str) {
        int rid = -1, type = -1;
        try {
            jsonObj = new JSONObject(str);
            if(jsonObj.has("request_id")) rid = jsonObj.getInt("request_id");
            type = jsonObj.getInt("type");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        requestId = rid;
        msgType = type;
    }

    public void addParam(String key, int val) {
        try {
            jsonObj.put(key, val);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addParam(String key, String val) {
        try {
            jsonObj.put(key, val);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getJSONString() {
        return jsonObj.toString();
    }
}
