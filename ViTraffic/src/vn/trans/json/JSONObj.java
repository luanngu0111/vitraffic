package vn.trans.json;

import org.json.simple.JSONObject;

public abstract class JSONObj extends Object {
	public abstract JSONObject conv2JsonObj();
	public abstract Object conv2Obj(String json);
}
