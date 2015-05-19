# jmonitor
专业java监控平台,提供以下特性:
1 JVM/OS cpu消耗监控
2 堆内存细分监控
3 非堆内存细分监控
4 GC耗时和次数监控
5 线程dump
6 堆dump
7 VM类加载及系统参数查看
8 实现了mini版本的long-polling，提供消息推送，可以应用到mycat sql消息推送等
仅依赖jetty，可以嵌入到任何应用，远程本地JVM均可以监控
