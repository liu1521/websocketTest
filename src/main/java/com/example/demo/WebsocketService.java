package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: liu
 * @Date: 2020/12/6
 * @Description:
 */

@Slf4j
@Component
@ServerEndpoint("/websocket/{employeeId}")
public class WebsocketService {

    /**
     * 当前在线用户 employee,expireTime
     */
    private static ConcurrentHashMap<Long, Long> onLineUser = new ConcurrentHashMap<>();

    /**
     * 当前在线用户所对应的 socket session信息
     */
    private static ConcurrentHashMap<Long, Session> webSocketSession = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("employeeId") Long employeeId) {
        if (employeeId == null) {
            return;
        }
        webSocketSession.put(employeeId, session);
        log.info("连接打开");
    }

    /**
     * 不做处理如果 前台可以监听到浏览器关闭 此处处理在线人数也可
     *
     * @param session
     */
    @OnClose
    public void onClose(Session session) {

        log.info("连接关闭");
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("socket error,{}", error);
        error.printStackTrace();
    }

    /**
     * 此方法接收 前台信息
     *
     * @param message
     * @param session
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        if (StringUtils.isEmpty(message)) {
            return;
        }
    }

    /**
     * 更新用户过期时间
     *
     * @param json
     */
    private void heartBeatHandle(String json) {
        Long currentDate = System.currentTimeMillis();
        Long expireTime = currentDate + 5 * 1000;
    }

    /**
     * 移除过期用户,如果用户超过5s未获取到心跳列表则清除在线用户信息
     */
    @Scheduled(cron = "0/5 * * * * ?")
    private void removeOnLineUser() {
        Long currentDate = System.currentTimeMillis();
        Iterator<Map.Entry<Long, Long>> it = onLineUser.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, Long> entry = it.next();
            Long key = entry.getKey();
            Long value = entry.getValue();
            Long userExpireTime = value + 5 * 1000;
            if (currentDate > userExpireTime) {
                onLineUser.remove(key);
                webSocketSession.remove(key);
            }
        }
    }
}
