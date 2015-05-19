package com.nice.common.jmonitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @author code_czp@126.com-2015年5月12日
 */
public class JMDispatcher extends HttpServlet {

    public static final ConcurrentHashMap<String, JSONObject> map = new ConcurrentHashMap<String, JSONObject>();
    private static final Logger log = LoggerFactory.getLogger(JMDispatcher.class);
    private static final long serialVersionUID = 1L;
    public static final String REQ = "REQ";
    public static final String CONN = "CONN";
    public static final String RESP = "RESP";

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.METHOD, ElementType.TYPE })
    public static @interface HttpMapping {

        String url();
    }

    public JMDispatcher() {
        initHttpMapping();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            JSONObject obj = map.get(req.getRequestURI());
            if (obj == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            resp.setCharacterEncoding("utf-8");
            Map<String, Object> param = new HashMap<String, Object>(2);
            param.put(RESP, resp);
            param.put(REQ, req);
            Object ins = obj.get("ins");
            Method method = (Method) obj.get("method");
            Object res = method.invoke(ins, param);
            if (res == null)
                return;
            if (res instanceof JSON)
                writeJson(req, resp, res);
            else
                writeHtml(resp, res);

        } catch (Exception e) {
            log.error("dispatch url error", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            String app = req.getParameter("app");
            writeHtml(resp, app + " has disconnect");
        }
    }

    @Override
    public void destroy() {
        JMConnManager.close();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        String configFile = config.getInitParameter("config");
        JMConnManager.init(configFile);
    }

    private void writeHtml(HttpServletResponse resp, Object data) throws IOException {
        PrintWriter out = resp.getWriter();
        out.print(data);
        out.flush();
    }

    private void writeJson(HttpServletRequest request, HttpServletResponse response, Object data) throws IOException {
        if (isLowerIE(request)) {
            response.setContentType("text/plain;charset=UTF-8");
        } else {
            response.setContentType("application/json");
        }
        writeHtml(response, data);
    }

    private static boolean isLowerIE(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        if (ua == null)
            return false;
        if (ua.contains("MSIE")) {
            return ua.contains("MSIE 9.0") || ua.contains("MSIE 8.0") || ua.contains("MSIE 7.0")
                    || ua.contains("MSIE 6.0");
        }
        return false;
    }

    private void initHttpMapping() {
        try {
            String baseUrl = "";
            JMServer ins = new JMServer();
            Class<JMServer> cls = JMServer.class;
            HttpMapping clsAno = cls.getAnnotation(HttpMapping.class);
            if (clsAno != null)
                baseUrl = clsAno.url();

            Method[] methods = cls.getDeclaredMethods();
            for (Method method : methods) {
                HttpMapping ant = method.getAnnotation(HttpMapping.class);
                if (ant == null)
                    continue;
                String mUrl = ant.url();
                if (!mUrl.startsWith(baseUrl))
                    mUrl = baseUrl + mUrl;
                mUrl = mUrl.replaceAll("//", "/");
                method.setAccessible(true);

                JSONObject obj = new JSONObject();
                obj.put("method", method);
                obj.put("ins", ins);
                map.putIfAbsent(mUrl, obj);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
