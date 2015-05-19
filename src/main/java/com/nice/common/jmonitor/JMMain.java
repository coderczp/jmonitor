package com.nice.common.jmonitor;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/***
 * 
 * @author coder_czp@126.com
 *
 */
public class JMMain {

    public static void main(String[] args) throws Exception {
        int port = 8088;
        if (args.length > 0) {
            port = Integer.valueOf(args[0]);
        }
        WebAppContext context = new WebAppContext();
        context.setDescriptor("./webapp/WEB-INF/web.xml");
        context.setParentLoaderPriority(true);
        context.setResourceBase("./webapp");
        context.setContextPath("/");

        Server server = new Server(port);
        server.setHandler(context);
        server.start();
        System.out.println("jmontior running at:"+port);
    }
}
