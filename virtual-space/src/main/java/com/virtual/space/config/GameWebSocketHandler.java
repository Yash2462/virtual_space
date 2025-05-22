package com.virtual.space.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.virtual.space.entity.User;
import com.virtual.space.service.UserService;
import com.virtual.space.util.JwtUtil;
import com.virtual.space.websocketdto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    // Store active user sessions: sessionId -> user info
    private final Map<String, UserInfo> activeSessions = new ConcurrentHashMap<>();
    // Store space users: spaceId -> Set of sessionIds
    private final Map<String, Set<String>> spaceUsers = new ConcurrentHashMap<>();
    // We need to track WebSocket sessions too
    private final Map<String, WebSocketSession> webSocketSessions = new ConcurrentHashMap<>();

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("WebSocket connection established: " + session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            ObjectMapper mapper = new ObjectMapper();
            WebSocketMessage wsMessage = mapper.readValue(message.getPayload(), WebSocketMessage.class);

            switch (wsMessage.getType()) {
                case "join":
                    handleJoinSpace(session, wsMessage);
                    break;
                case "move":
                    handleMovement(session, wsMessage);
                    break;
                default:
                    System.out.println("Unknown message type: " + wsMessage.getType());
            }
        } catch (Exception e) {
            System.err.println("Error handling WebSocket message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleJoinSpace(WebSocketSession session, WebSocketMessage message) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JoinPayload payload = mapper.convertValue(message.getPayload(), JoinPayload.class);

        try {
            // Validate JWT token
            String username = jwtUtil.getUsernameFromToken(payload.getToken());
            User user = userService.findByUsername(username);

            // Create user info for this session
            UserInfo userInfo = new UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(username);
            userInfo.setX(getRandomSpawnX());
            userInfo.setY(getRandomSpawnY());

            // Store session
            activeSessions.put(session.getId(), userInfo);

            // Add to space
            spaceUsers.computeIfAbsent(payload.getSpaceId(), k -> ConcurrentHashMap.newKeySet())
                    .add(session.getId());

            // Send space-joined response
            SpaceJoinedResponse response = new SpaceJoinedResponse();

            SpawnPoint spawn = new SpawnPoint();
            spawn.setX(userInfo.getX());
            spawn.setY(userInfo.getY());
            response.getPayload().setSpawn(spawn);

            // Get other users in the same space
            List<UserInfo> usersInSpace = getUsersInSpace(payload.getSpaceId(), session.getId());
            response.getPayload().setUsers(usersInSpace);

            session.sendMessage(new TextMessage(mapper.writeValueAsString(response)));

            // Notify other users about new user
            broadcastUserJoined(userInfo, payload.getSpaceId(), session.getId());

        } catch (Exception e) {
            System.err.println("Failed to join space: " + e.getMessage());
            // Send error response or close connection
            session.close();
        }
    }

    private void handleMovement(WebSocketSession session, WebSocketMessage message) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MovePayload payload = mapper.convertValue(message.getPayload(), MovePayload.class);
        UserInfo userInfo = activeSessions.get(session.getId());

        if (userInfo != null) {
            String spaceId = findUserSpace(session.getId());

            if (spaceId != null && isValidMovement(payload.getX(), payload.getY(), spaceId)) {
                // Update user position
                userInfo.setX(payload.getX());
                userInfo.setY(payload.getY());

                // Broadcast movement to other users in the same space
                broadcastMovement(userInfo, spaceId, session.getId());
            } else {
                // Send movement rejected
                sendMovementRejected(session, userInfo.getX(), userInfo.getY());
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        UserInfo userInfo = activeSessions.remove(session.getId());

        if (userInfo != null) {
            // Find and remove from space
            String spaceId = findUserSpace(session.getId());
            if (spaceId != null) {
                spaceUsers.get(spaceId).remove(session.getId());
                if (spaceUsers.get(spaceId).isEmpty()) {
                    spaceUsers.remove(spaceId);
                }

                // Notify other users
                broadcastUserLeft(userInfo, spaceId);
            }
        }

        System.out.println("WebSocket connection closed: " + session.getId());
    }

    private List<UserInfo> getUsersInSpace(String spaceId, String excludeSessionId) {
        Set<String> sessionIds = spaceUsers.get(spaceId);
        if (sessionIds == null) return new ArrayList<>();

        return sessionIds.stream()
                .filter(sessionId -> !sessionId.equals(excludeSessionId))
                .map(activeSessions::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private String findUserSpace(String sessionId) {
        return spaceUsers.entrySet().stream()
                .filter(entry -> entry.getValue().contains(sessionId))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private void broadcastUserJoined(UserInfo userInfo, String spaceId, String excludeSessionId) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "user-join");

        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userInfo.getId());
        payload.put("x", userInfo.getX());
        payload.put("y", userInfo.getY());
        message.put("payload", payload);

        broadcastToSpace(spaceId, message, excludeSessionId);
    }

    private void broadcastMovement(UserInfo userInfo, String spaceId, String excludeSessionId) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "movement");

        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userInfo.getId().toString());
        payload.put("x", userInfo.getX());
        payload.put("y", userInfo.getY());
        message.put("payload", payload);

        broadcastToSpace(spaceId, message, excludeSessionId);
    }

    private void broadcastUserLeft(UserInfo userInfo, String spaceId) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "user-left");

        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userInfo.getId());
        message.put("payload", payload);

        broadcastToSpace(spaceId, message, null);
    }

    private void sendMovementRejected(WebSocketSession session, Integer x, Integer y) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "movement-rejected");

            Map<String, Object> payload = new HashMap<>();
            payload.put("x", x);
            payload.put("y", y);
            message.put("payload", payload);

            ObjectMapper mapper = new ObjectMapper();
            session.sendMessage(new TextMessage(mapper.writeValueAsString(message)));
        } catch (Exception e) {
            System.err.println("Failed to send movement rejected: " + e.getMessage());
        }
    }

    private void broadcastToSpace(String spaceId, Object message, String excludeSessionId) {
        Set<String> sessionIds = spaceUsers.get(spaceId);
        if (sessionIds == null) return;

        ObjectMapper mapper = new ObjectMapper();

        sessionIds.stream()
                .filter(sessionId -> !sessionId.equals(excludeSessionId))
                .forEach(sessionId -> {
                    UserInfo userInfo = activeSessions.get(sessionId);
                    if (userInfo != null) {
                        try {
                            // Find the WebSocketSession - you'll need to maintain a map for this
                            // For now, we'll need to add session tracking
                            WebSocketSession session = findSessionById(sessionId);
                            if (session != null && session.isOpen()) {
                                session.sendMessage(new TextMessage(mapper.writeValueAsString(message)));
                            }
                        } catch (Exception e) {
                            System.err.println("Failed to broadcast message: " + e.getMessage());
                        }
                    }
                });
    }

    private WebSocketSession findSessionById(String sessionId) {
        return webSocketSessions.get(sessionId);
    }

    private Integer getRandomSpawnX() {
        return (int) (Math.random() * 50) + 10; // Random spawn between 10-60
    }

    private Integer getRandomSpawnY() {
        return (int) (Math.random() * 50) + 10; // Random spawn between 10-60
    }

    private boolean isValidMovement(Integer x, Integer y, String spaceId) {
        // Add your movement validation logic here
        // Check boundaries, collisions with static elements, other users, etc.

        // Basic boundary check (assuming 100x200 space)
        if (x < 0 || x > 100 || y < 0 || y > 200) {
            return false;
        }

        // Check collision with other users
        Set<String> sessionIds = spaceUsers.get(spaceId);
        if (sessionIds != null) {
            for (String sessionId : sessionIds) {
                UserInfo otherUser = activeSessions.get(sessionId);
                if (otherUser != null && otherUser.getX().equals(x) && otherUser.getY().equals(y)) {
                    return false; // Position occupied
                }
            }
        }

        return true;
    }
}
