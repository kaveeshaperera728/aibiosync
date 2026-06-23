package com.fingerprint.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fingerprint.entity.AttendanceLog;
import com.fingerprint.entity.Device;
import com.fingerprint.entity.DeviceCommand;
import com.fingerprint.entity.Employee;
import com.fingerprint.entity.BiometricTemplate;
import com.fingerprint.repository.AttendanceLogRepository;
import com.fingerprint.repository.DeviceCommandRepository;
import com.fingerprint.repository.DeviceRepository;
import com.fingerprint.repository.EmployeeRepository;
import com.fingerprint.repository.BiometricTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Component
public class DeviceWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(DeviceWebSocketHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DeviceRepository deviceRepository;
    private final AttendanceLogRepository attendanceLogRepository;
    private final EmployeeRepository employeeRepository;
    private final DeviceCommandRepository deviceCommandRepository;
    private final BiometricTemplateRepository biometricTemplateRepository;
    
    // Store active connections by device serial number or IP for reference
    private final Map<String, WebSocketSession> activeSessions = new HashMap<>();
    
    // Store recent raw JSON payloads for debugging via API/Postman
    private final ConcurrentLinkedQueue<String> recentPayloads = new ConcurrentLinkedQueue<>();

    public DeviceWebSocketHandler(DeviceCommandRepository deviceCommandRepository, 
                                  DeviceRepository deviceRepository,
                                  EmployeeRepository employeeRepository,
                                  AttendanceLogRepository attendanceLogRepository,
                                  BiometricTemplateRepository biometricTemplateRepository) {
        this.deviceCommandRepository = deviceCommandRepository;
        this.deviceRepository = deviceRepository;
        this.employeeRepository = employeeRepository;
        this.attendanceLogRepository = attendanceLogRepository;
        this.biometricTemplateRepository = biometricTemplateRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("New WebSocket connection established from {}", session.getRemoteAddress());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        logger.info("RAW JSON RECEIVED FROM DEVICE: {}", payload);
        
        recentPayloads.add(payload);
        if (recentPayloads.size() > 50) {
            recentPayloads.poll(); // Keep only the last 50 payloads
        }

        try {
            JsonNode msgNode = objectMapper.readTree(payload);
            String cmd = msgNode.has("cmd") ? msgNode.get("cmd").asText() : "";
            String ret = msgNode.has("ret") ? msgNode.get("ret").asText() : "";

            Map<String, Object> reply = new HashMap<>();
            String currentCloudTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // Get device SN from JSON payload if present (Added in protocol 1.8)
            String jsonSn = msgNode.has("sn") ? msgNode.get("sn").asText() : null;
            if (jsonSn != null && !jsonSn.isEmpty() && !"UNKNOWN".equals(jsonSn)) {
                activeSessions.put(jsonSn, session);
            }

            // Fallback to active sessions map
            String sessionSn = activeSessions.entrySet().stream()
                    .filter(e -> e.getValue().equals(session))
                    .map(Map.Entry::getKey)
                    .findFirst().orElse(null);

            if (!cmd.isEmpty()) {
                switch (cmd) {
                    case "reg":
                        String sn = sessionSn != null ? sessionSn : "UNKNOWN";
                        logger.info("Device registered: {}", sn);
                        activeSessions.put(sn, session);
                        sessionSn = sn;

                        Device device = deviceRepository.findBySerialNumber(sn).orElseGet(() -> {
                            logger.info("Auto-registering new device with SN: {}", sn);
                            Device newDevice = new Device();
                            newDevice.setSerialNumber(sn);
                            newDevice.setName("Auto-Registered Device (" + sn + ")");
                            newDevice.setDeviceType("ZKTeco (Unknown Model)");
                            return newDevice;
                        });
                        
                        device.setStatus("online");
                        device.setLastSyncTime(LocalDateTime.now());
                        device.setIpAddress(session.getRemoteAddress() != null ? session.getRemoteAddress().getAddress().getHostAddress() : "");

                        if (msgNode.has("devinfo")) {
                            JsonNode devinfo = msgNode.get("devinfo");
                            if (devinfo.has("modelname")) device.setDeviceType("ZKTeco " + devinfo.get("modelname").asText());
                            if (devinfo.has("firmware")) device.setFirmwareVersion(devinfo.get("firmware").asText());
                            if (devinfo.has("mac")) device.setMacAddress(devinfo.get("mac").asText());
                            if (devinfo.has("useduser")) device.setUserCount(devinfo.get("useduser").asInt());
                            if (devinfo.has("usedfp")) device.setFingerprintCount(devinfo.get("usedfp").asInt());
                            if (devinfo.has("usedface")) device.setFaceCount(devinfo.get("usedface").asInt());
                            if (devinfo.has("usedlog")) device.setLogCount(devinfo.get("usedlog").asInt());
                        }

                        deviceRepository.save(device);

                        reply.put("ret", "reg");
                        reply.put("result", true);
                        reply.put("cloudtime", currentCloudTime);
                        break;

                    case "sendlog":
                        int count = msgNode.has("count") ? msgNode.get("count").asInt() : 0;
                        int logindex = msgNode.has("logindex") ? msgNode.get("logindex").asInt() : 0;
                        logger.info("Received {} attendance logs (index {})", count, logindex);

                        if (msgNode.has("record") && msgNode.get("record").isArray()) {
                            for (JsonNode record : msgNode.get("record")) {
                                try {
                                    String userEnrollNumber = record.has("enrollid") ? record.get("enrollid").asText() : "UNKNOWN";
                                    String timeStr = record.has("time") ? record.get("time").asText() : "";
                                    int verifyMode = record.has("mode") ? record.get("mode").asInt() : 0;
                                    int inOutMode = record.has("inout") ? record.get("inout").asInt() : 0;

                                    LocalDateTime logTime;
                                    try {
                                        logTime = LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                                    } catch (Exception e) {
                                        logTime = LocalDateTime.now();
                                    }

                                    AttendanceLog attLog = new AttendanceLog();
                                    attLog.setPunchTime(logTime);
                                    attLog.setVerificationType(verifyMode);
                                    attLog.setDirection(inOutMode);
                                    
                                    if (!"UNKNOWN".equals(userEnrollNumber)) {
                                        Employee employee = employeeRepository.findByEmployeeNumber(userEnrollNumber).orElseGet(() -> {
                                            Employee newEmp = new Employee();
                                            newEmp.setEmployeeNumber(userEnrollNumber);
                                            newEmp.setFirstName("User " + userEnrollNumber);
                                            return employeeRepository.save(newEmp);
                                        });
                                        attLog.setEmployee(employee);
                                    }

                                    if (sessionSn != null) {
                                        deviceRepository.findBySerialNumber(sessionSn).ifPresent(dev -> {
                                            attLog.setDevice(dev);
                                            dev.setLastSyncTime(LocalDateTime.now());
                                            deviceRepository.save(dev);
                                        });
                                    }

                                    attendanceLogRepository.save(attLog);
                                    logger.info("Saved log: user={} time={} mode={}", userEnrollNumber, logTime, verifyMode);
                                } catch (Exception ex) {
                                    logger.error("Failed to parse individual log record", ex);
                                }
                            }
                        }

                        reply.put("ret", "sendlog");
                        reply.put("result", true);
                        if (count > 0) {
                            reply.put("count", count);
                        }
                        if (logindex >= 0) {
                            reply.put("logindex", logindex);
                        }
                        reply.put("cloudtime", currentCloudTime);
                        reply.put("access", 1); // CRITICAL: Required by ZKTeco protocol to acknowledge log
                        break;

                    case "senduser":
                        // Process user data actively pushed from device (e.g., when enrolled on keypad)
                        String enrollId = msgNode.has("enrollid") ? msgNode.get("enrollid").asText() : "";
                        String name = msgNode.has("name") ? msgNode.get("name").asText() : "";
                        logger.info("Received user data sync from device: enrollid={}, name={}", enrollId, name);
                        
                        if (!enrollId.isEmpty()) {
                            Employee employee = employeeRepository.findByEmployeeNumber(enrollId).orElseGet(() -> {
                                Employee newEmp = new Employee();
                                newEmp.setEmployeeNumber(enrollId);
                                return newEmp;
                            });
                            
                            // Only overwrite name if device actually sent a valid name
                            if (!name.isEmpty()) {
                                employee.setFirstName(name);
                            } else if (employee.getFirstName() == null) {
                                employee.setFirstName("User " + enrollId);
                            }
                            
                            employeeRepository.save(employee);
                        }

                        reply.put("ret", "senduser");
                        reply.put("result", true);
                        reply.put("cloudtime", currentCloudTime);
                        break;

                    default:
                        logger.warn("Unknown command received: {}", cmd);
                        reply.put("ret", "unknown");
                        reply.put("result", false);
                        break;
                }

                // Send acknowledgment back to device for commands
                String replyJson = objectMapper.writeValueAsString(reply);
                logger.debug("Replying to device: {}", replyJson);
                session.sendMessage(new TextMessage(replyJson));

            } else if (!ret.isEmpty()) {
                // Handle Device Responses to our Server Commands
                if ("getuserlist".equals(ret)) {
                    boolean result = msgNode.has("result") && msgNode.get("result").asBoolean();
                    if (result && msgNode.has("record") && msgNode.get("record").isArray()) {
                        logger.info("Processing getuserlist response with {} records", msgNode.get("count").asInt());
                        for (JsonNode record : msgNode.get("record")) {
                            String userEnrollNumber = record.has("enrollid") ? record.get("enrollid").asText() : "";
                            int backupnum = record.has("backupnum") ? record.get("backupnum").asInt() : 0;
                            String name = record.has("name") ? record.get("name").asText() : "";
                            
                            if (!userEnrollNumber.isEmpty()) {
                                Employee empToSave = employeeRepository.findByEmployeeNumber(userEnrollNumber).orElseGet(() -> {
                                    logger.info("Auto-registering employee from getuserlist: {}", userEnrollNumber);
                                    Employee newEmp = new Employee();
                                    newEmp.setEmployeeNumber(userEnrollNumber);
                                    return newEmp;
                                });
                                
                                if (!name.isEmpty()) {
                                    empToSave.setFirstName(name);
                                } else if (empToSave.getFirstName() == null || empToSave.getFirstName().isEmpty()) {
                                    empToSave.setFirstName("User " + userEnrollNumber);
                                }
                                
                                employeeRepository.save(empToSave);
                            }
                        }
                    }

                    // Read pagination info from getuserlist response
                    int count = msgNode.has("count") ? msgNode.get("count").asInt() : 0;
                    boolean hasMore = count > 0;

                    // If sessionSn is null (device didn't send SN), fallback to the first device that had a SENT getuserlist
                    String effectiveSn = sessionSn;
                    if (effectiveSn == null) {
                        List<DeviceCommand> sentCmds = deviceCommandRepository.findByStatus("SENT");
                        for (DeviceCommand c : sentCmds) {
                            if ("getuserlist".equals(c.getCommandType()) && c.getDevice() != null) {
                                effectiveSn = c.getDevice().getSerialNumber();
                                break;
                            }
                        }
                    }

                    if (effectiveSn != null) {
                        // Queue getuserinfo for each user found in this package
                        if (msgNode.has("record") && msgNode.get("record").isArray()) {
                            // Track which users we've already requested backupnum=50 for during this chunk
                            java.util.Set<String> requestedNameUsers = new java.util.HashSet<>();
                            
                            for (JsonNode record : msgNode.get("record")) {
                                try {
                                    String userEnrollNumber = record.has("enrollid") ? record.get("enrollid").asText() : "";
                                    int backupnum = record.has("backupnum") ? record.get("backupnum").asInt() : 0;
                                    
                                    if (!userEnrollNumber.isEmpty()) {
                                        Device device = deviceRepository.findBySerialNumber(effectiveSn).orElse(null);
                                        if (device != null) {
                                            ObjectNode commandPayload = objectMapper.createObjectNode();
                                            commandPayload.put("cmd", "getuserinfo");
                                            commandPayload.put("enrollid", Integer.parseInt(userEnrollNumber));
                                            commandPayload.put("backupnum", backupnum);
                                            
                                            DeviceCommand cmdObj = new DeviceCommand();
                                            cmdObj.setDevice(device);
                                            cmdObj.setCommandType("getuserinfo");
                                            cmdObj.setCommandPayload(commandPayload.toString());
                                            cmdObj.setStatus("PENDING");
                                            deviceCommandRepository.save(cmdObj);
                                            
                                            // CRITICAL: Request backupnum=50 explicitly to force the device to return the user's name
                                            // Ensure we only queue this ONCE per user per chunk
                                            if (!requestedNameUsers.contains(userEnrollNumber)) {
                                                requestedNameUsers.add(userEnrollNumber);
                                                
                                                ObjectNode nameCommandPayload = objectMapper.createObjectNode();
                                                nameCommandPayload.put("cmd", "getuserinfo");
                                                nameCommandPayload.put("enrollid", Integer.parseInt(userEnrollNumber));
                                                nameCommandPayload.put("backupnum", 50);
                                                
                                                DeviceCommand nameCmdObj = new DeviceCommand();
                                                nameCmdObj.setDevice(device);
                                                nameCmdObj.setCommandType("getuserinfo");
                                                nameCmdObj.setCommandPayload(nameCommandPayload.toString());
                                                nameCmdObj.setStatus("PENDING");
                                                deviceCommandRepository.save(nameCmdObj);
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    logger.error("Error processing record in getuserlist", e);
                                }
                            }
                        }
                        
                        if (hasMore) {
                            // Request the next paginated chunk
                            ObjectNode nextChunkReq = objectMapper.createObjectNode();
                            nextChunkReq.put("cmd", "getuserlist");
                            nextChunkReq.put("stn", false);
                            logger.info("Requesting next chunk of users from device {}", effectiveSn);
                            session.sendMessage(new TextMessage(nextChunkReq.toString()));
                        } else {
                            // No more records, finally mark the overall getuserlist command as SUCCESS
                            logger.info("Finished receiving all user list chunks from device {}", effectiveSn);
                            List<DeviceCommand> cmds = deviceCommandRepository.findByDeviceSerialNumberAndStatus(effectiveSn, "SENT");
                            for (DeviceCommand c : cmds) {
                                if ("getuserlist".equals(c.getCommandType())) {
                                    c.setStatus("SUCCESS");
                                    deviceCommandRepository.save(c);
                                }
                            }
                        }
                    }
                } else if ("getuserinfo".equals(ret)) {
                    boolean result = msgNode.has("result") && msgNode.get("result").asBoolean();
                    if (result) {
                        String enrollId = msgNode.has("enrollid") ? msgNode.get("enrollid").asText() : "";
                        String name = msgNode.has("name") ? msgNode.get("name").asText() : "";
                        int backupNum = msgNode.has("backupnum") ? msgNode.get("backupnum").asInt() : 0;
                        String recordString = msgNode.has("record") ? msgNode.get("record").asText() : "";
                        
                        // Some firmwares embed the name inside the 'record' payload as a nested JSON string
                        if (name.isEmpty() && !recordString.isEmpty()) {
                            try {
                                JsonNode nestedRecord = objectMapper.readTree(recordString);
                                if (nestedRecord.has("name")) {
                                    name = nestedRecord.get("name").asText();
                                }
                            } catch (Exception ignored) {
                                // Not JSON, probably a raw base64 template
                            }
                        }
                        
                        logger.info("Processed getuserinfo response: enrollid={}, backupnum={}, name={}, hasRecord={}", enrollId, backupNum, name, !recordString.isEmpty());
                        
                        if (!enrollId.isEmpty()) {
                            java.util.Optional<Employee> optEmp = employeeRepository.findByEmployeeNumber(enrollId);
                            if (optEmp.isPresent()) {
                                Employee emp = optEmp.get();
                                if (!name.isEmpty()) {
                                    emp.setFirstName(name);
                                }
                                
                                // Extract comprehensive flags from backupnum=50 payload if available
                                if (msgNode.has("faceflag") && msgNode.get("faceflag").asInt() == 1) {
                                    emp.setHasFace(true);
                                }
                                if (msgNode.has("fpflag") && msgNode.get("fpflag").asInt() == 1) {
                                    emp.setHasFingerprint(true);
                                }
                                
                                // Also update flags based on backupNum in case the flags are missing
                                if (backupNum >= 0 && backupNum <= 9) emp.setHasFingerprint(true);
                                else if (backupNum == 10) emp.setHasPassword(true);
                                else if (backupNum == 11) emp.setHasCard(true);
                                else if (backupNum >= 20 && backupNum <= 27) emp.setHasFace(true);

                                if (msgNode.has("photourl")) {
                                    String photoUrl = msgNode.get("photourl").asText();
                                    if (!photoUrl.isEmpty()) {
                                        emp.setPhotoUrl(photoUrl);
                                    }
                                }

                                // Link device
                                if (sessionSn != null) {
                                    Device sessionDev = deviceRepository.findBySerialNumber(sessionSn).orElse(null);
                                    if (sessionDev != null) {
                                        emp.getRegisteredDevices().add(sessionDev);
                                    }
                                }

                                employeeRepository.save(emp);
                                
                                // Save actual template string only if it's a valid biometric template (0-9 for fingers, 10-11 for card/pw, 20-50 for face/user record)
                                if (!recordString.isEmpty() && backupNum >= 0 && backupNum <= 50) {
                                    com.fingerprint.entity.BiometricTemplate template = biometricTemplateRepository.findByEmployeeAndBackupNum(emp, backupNum).orElse(new com.fingerprint.entity.BiometricTemplate());
                                    template.setEmployee(emp);
                                    template.setBackupNum(backupNum);
                                    template.setTemplateData(recordString);
                                    biometricTemplateRepository.save(template);
                                    logger.info("Saved biometric template for user {}, backupNum {}", enrollId, backupNum);
                                }
                            }
                        }
                    }
                    
                    String effectiveSn = sessionSn;
                    if (effectiveSn == null) {
                        List<DeviceCommand> sentCmds = deviceCommandRepository.findByStatus("SENT");
                        for (DeviceCommand c : sentCmds) {
                            if ("getuserinfo".equals(c.getCommandType()) && c.getDevice() != null) {
                                effectiveSn = c.getDevice().getSerialNumber();
                                break;
                            }
                        }
                    }
                    
                    if (effectiveSn != null) {
                        List<DeviceCommand> cmds = deviceCommandRepository.findByDeviceSerialNumberAndStatus(effectiveSn, "SENT");
                        for (DeviceCommand c : cmds) {
                            if ("getuserinfo".equals(c.getCommandType())) {
                                try {
                                    JsonNode payloadNode = objectMapper.readTree(c.getCommandPayload());
                                    // Identify the exact command that just finished
                                    if (msgNode.has("enrollid") && msgNode.has("backupnum")) {
                                        String responseEnrollId = msgNode.get("enrollid").asText();
                                        int responseBackupNum = msgNode.get("backupnum").asInt();
                                        
                                        if (payloadNode.has("enrollid") && payloadNode.get("enrollid").asText().equals(responseEnrollId) &&
                                            payloadNode.has("backupnum") && payloadNode.get("backupnum").asInt() == responseBackupNum) {
                                            c.setStatus("SUCCESS");
                                            deviceCommandRepository.save(c);
                                            break; // Only mark the specific command as success to maintain the 5-command throttle
                                        }
                                    } else {
                                        c.setStatus("SUCCESS");
                                        deviceCommandRepository.save(c);
                                        break;
                                    }
                                } catch (Exception e) {
                                    logger.error("Error verifying sent command payload", e);
                                }
                            }
                        }
                    }
                }
            }

            // Dispatch any pending commands if we know the device
            if (sessionSn != null) {
                dispatchPendingCommands(session, sessionSn);
            }

        } catch (Exception e) {
            logger.error("Failed to parse or process WebSocket message", e);
        }
    }

    public void triggerCommandDispatch(String sn) {
        WebSocketSession session = activeSessions.get(sn);
        if (session == null || !session.isOpen()) {
            logger.warn("Cannot dispatch commands: Device {} is not currently connected via WebSocket.", sn);
            return;
        }

        // Priority: send any settime or high-priority commands IMMEDIATELY, bypassing the normal 5-cmd throttle
        List<DeviceCommand> pendingCmds = deviceCommandRepository.findByDeviceSerialNumberAndStatus(sn, "PENDING");
        for (DeviceCommand cmd : pendingCmds) {
            if ("settime".equals(cmd.getCommandType())) {
                try {
                    logger.info("Sending PRIORITY settime command to device {}", sn);
                    session.sendMessage(new TextMessage(cmd.getCommandPayload()));
                    cmd.setStatus("SUCCESS"); // settime has no meaningful response to wait for
                    deviceCommandRepository.save(cmd);
                    logger.info("Time sync sent successfully to device {}", sn);
                } catch (Exception e) {
                    logger.error("Failed to send settime command to device {}", sn, e);
                    cmd.setStatus("FAILED");
                    deviceCommandRepository.save(cmd);
                }
                return; // Done — don't queue more commands alongside a time sync
            }
        }

        // Normal throttled dispatch for other commands
        dispatchPendingCommands(session, sn);
    }

    /**
     * Directly sends a settime command to the device via its active WebSocket session.
     * Bypasses the command queue completely for instant delivery.
     */
    public boolean sendTimeSyncToDevice(String sn) {
        WebSocketSession session = activeSessions.get(sn);
        if (session == null || !session.isOpen()) {
            logger.warn("Cannot send time sync: Device {} has no active WebSocket session. Active sessions: {}", sn, activeSessions.keySet());
            return false;
        }
        try {
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            ObjectNode payload = new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode();
            payload.put("cmd", "settime");
            payload.put("cloudtime", currentTime);
            String msg = payload.toString();
            logger.info("Sending direct settime to device {}: {}", sn, msg);
            session.sendMessage(new org.springframework.web.socket.TextMessage(msg));
            logger.info("Time sync sent successfully to device {}", sn);
            return true;
        } catch (Exception e) {
            logger.error("Failed to send settime directly to device {}", sn, e);
            return false;
        }
    }

    private void dispatchPendingCommands(WebSocketSession session, String sn) {
        List<DeviceCommand> pendingCmds = deviceCommandRepository.findByDeviceSerialNumberAndStatus(sn, "PENDING");
        int dispatchedCount = 0;
        for (DeviceCommand cmd : pendingCmds) {
            if (dispatchedCount >= 5) {
                break; // Only send max 5 commands per cycle to prevent overwhelming the device buffer
            }
            try {
                logger.info("Dispatching pending command {} to device {}", cmd.getCommandType(), sn);
                session.sendMessage(new TextMessage(cmd.getCommandPayload()));
                cmd.setStatus("SENT");
                deviceCommandRepository.save(cmd);
                dispatchedCount++;
            } catch (Exception e) {
                logger.error("Failed to send pending command", e);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("WebSocket connection closed from {}, status: {}", session.getRemoteAddress(), status);
        activeSessions.entrySet().removeIf(entry -> entry.getValue().equals(session));
    }
    
    public List<String> getRecentPayloads() {
        return recentPayloads.stream().collect(Collectors.toList());
    }
}
