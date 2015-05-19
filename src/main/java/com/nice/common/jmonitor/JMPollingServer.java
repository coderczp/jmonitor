package com.nice.common.jmonitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.nice.common.event.JMEevntCenter;
import com.nice.common.event.JMEvevntListener;
import com.nice.common.util.JMSyncHelper;

public class JMPollingServer extends HttpServlet implements JMEvevntListener {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(JMPollingServer.class);
    private CopyOnWriteArrayList<HttpServletResponse> conn = new CopyOnWriteArrayList<HttpServletResponse>();

    @Override
    public void destroy() {
        JMEevntCenter.getInstance().close();
    }

    @Override
    public void init() throws ServletException {
        JMEevntCenter.getInstance().addListener(this);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse rep) throws ServletException, IOException {
        conn.add(rep);
        JMSyncHelper.syncObj(rep, TimeUnit.SECONDS.toMillis(120));
        JSONObject timeout = new JSONObject();
        timeout.put("type", "timeout");
        writeMessage(timeout, rep);
        conn.remove(rep);
    }

    @Override
    public void handle(JSONObject event) {
        for (int i = 0; i < conn.size(); i++) {
            try {
                HttpServletResponse remove = conn.remove(i);
                HttpServletResponse rep = writeMessage(event, remove);
                JMSyncHelper.notice(rep);
            } catch (Exception e) {
                log.error("handle event error", e);
            }
        }

    }

    private HttpServletResponse writeMessage(JSONObject event, HttpServletResponse rep) throws IOException {
        PrintWriter writer = rep.getWriter();
        writer.print(event);
        writer.flush();
        writer.close();
        return rep;
    }

}
