package vn.trans.json;

import org.json.JSONObject;

public abstract class JSONObj extends Object {
	public abstract JSONObject conv2JsonObj();
	public abstract Object conv2Obj(String json);
}
