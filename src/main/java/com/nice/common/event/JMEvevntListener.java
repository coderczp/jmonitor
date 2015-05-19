package com.nice.common.event;

import com.alibaba.fastjson.JSONObject;

public interface JMEvevntListener {

    void handle(JSONObject event);

}
