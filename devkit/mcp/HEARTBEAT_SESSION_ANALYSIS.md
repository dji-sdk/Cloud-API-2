# DJI CloudAPI2 MCP Server: Session & SSE Client State Management Analysis

## Executive Summary

This report analyzes the current session lifecycle and SSE client management in the DJI CloudAPI2 MCP codebase. **Critical finding**: There is **NO bidirectional mapping between SSE clients and MCP sessions**, creating a significant gap in reconnection/heartbeat handling. When an SSE connection drops, the associated MCP session persists orphaned until timeout.

---

## 1. Session Creation and Lifecycle

### 1.1 When Sessions Are Created

**Entry Point**: `FhConnectTool.execute()` (explicit call by client)

```
Client → MCP initialize → tools/list → tools/call("fh_connect") 
  → FhConnectTool.execute()
    → sdkService.initialize()
    → sessionManager.createSession(workspaceUuid, workspaceName)
    → Returns session_id
```

**Session ID Format**: `sess_` + 12 hex chars (e.g., `sess_a1b2c3d4e5f6`)

**Flow**:
- `sessionManager.createSession()` generates unique ID
- Creates `SessionContext` instance (workspace-bound, immutable after creation)
- Stores in `ConcurrentHashMap<String, SessionContext>` (thread-safe)
- Session is NOT automatically associated with SSE client ID

### 1.2 Session State Structure

**SessionContext** holds:
- **Identification**: `sessionId`, `workspaceUuid`, `workspaceName`, `createdAt`
- **Security**: `maxSecurityLevel`
- **Control State**: 
  - `activeControlDeviceSn` (device currently controlled, null = no control)
  - `controlAcquiredAt` (timestamp of last control acquisition)
- **DRC Link**: 
  - `activeDrcDeviceSn` (device with active DRC link, null = no link)
  - `drcConnectedAt` (timestamp of connection)
- **Flight State**:
  - `flightPhase` (IDLE, IN_FLIGHT, etc.)
  - `activeFlightId`, `activeFlyToId`, `activeWaylineTaskUuid`
- **Activity Tracking**:
  - `lastActivityAt` (updated on every tool call via `touch()`)
  - `activeListenIds` (subscription handles)

**Key Properties**:
- All mutable fields are `volatile` (safe for concurrent reads)
- `activeListenIds` is a `ConcurrentHashMap.newKeySet()`
- **No client association field** (e.g., no `sseClientId`)

---

## 2. Session Timeout & Cleanup Mechanism

### 2.1 Scheduled Session Cleanup

**Location**: `SessionManager.cleanupExpiredSessions()` (lines 110-123)

```java
@Scheduled(fixedDelayString = "${flighthub.security.session-check-interval:30000}")
public void cleanupExpiredSessions() {
    int timeout = properties.getSecurity().getSessionTimeout();  // default: 1800s = 30 min
    sessions.entrySet().removeIf(entry -> {
        SessionContext session = entry.getValue();
        if (session.isExpired(timeout)) {
            log.warn("Session expired (idle {}s > {}s): id={}",
                    timeout, timeout, session.getSessionId());
            releaseSessionResources(session);
            return true;
        }
        return false;
    });
}
```

**Configuration** (application.yml):
```yaml
flighthub:
  security:
    session-timeout: 1800          # seconds (default)
    session-check-interval: 30000  # milliseconds (check every 30s)
```

**Expiration Check**:
- `session.isExpired(timeoutSeconds)` → `Instant.now().isAfter(lastActivityAt.plusSeconds(timeoutSeconds))`
- Purely based on idle time, **not on SSE connection status**

### 2.2 Activity Touch Points

Sessions are marked "active" via `session.touch()` in:
1. Session creation (constructor initializes `lastActivityAt = Instant.now()`)
2. `setActiveWaylineTaskUuid()` (line 220) — only this setter explicitly touches
3. Every tool execution (implicit, via `RequestContext` and request handling)

**Gap**: `touch()` is called from tool execution context, but SSE disconnection does NOT touch or expire the session immediately.

### 2.3 Resource Release on Timeout

When session is expired, `releaseSessionResources(session)` is called:

```java
private void releaseSessionResources(SessionContext session) {
    // 1. Release flight control authority
    if (session.hasControlAuthority()) {
        releaseControlViaSdk(droneSn);
        session.clearControlDevice();
    }
    // 2. Disconnect DRC link
    if (session.hasDrcLink()) {
        disconnectDrcViaSdk(droneSn);
        session.clearDrcDevice();
    }
    // 3. Cancel subscriptions
    for (Integer listenId : session.getActiveListenIds()) {
        // (Placeholder: actual cancellation not shown)
    }
}
```

**Fail-safe**: SDK calls wrapped with `SHUTDOWN_SDK_TIMEOUT_SECONDS = 3` timeout.

---

## 3. SSE Transport & Client Management

### 3.1 SSE Client Lifecycle

**Location**: `SseTransport.java` (lines 50-69)

```java
@GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<ServerSentEvent<String>> connect() {
    String clientId = UUID.randomUUID().toString().substring(0, 8);  // e.g., "a1b2c3d4"
    Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().unicast().onBackpressureBuffer();
    clientSinks.put(clientId, sink);  // Store in ConcurrentHashMap
    
    log.info("SSE client connected: {}", clientId);
    
    // Send endpoint info so client knows where to POST
    sink.tryEmitNext(ServerSentEvent.<String>builder()
            .event("endpoint")
            .data("{\"endpoint\":\"/message?clientId=" + clientId + "\"}")
            .build());
    
    return sink.asFlux()
            .doFinally(signalType -> {
                clientSinks.remove(clientId);  // ← Only cleanup: remove from map
                log.info("SSE client disconnected: {} ({})", clientId, signalType);
            });
}
```

**Client Sink Storage**:
- `clientSinks: ConcurrentHashMap<String, Sinks.Many<ServerSentEvent<String>>>`
- Each SSE connection gets a unique 8-char ID
- Sink is removed on disconnect (in `doFinally`)

**Message Flow**:
1. Client connects to `/sse` → gets `clientId` and endpoint URL
2. Client POSTs JSON-RPC to `/message?clientId={clientId}`
3. Server responds via sink, sends back to that client via SSE

### 3.2 Disconnection Handling

**Only Detection**: `Flux.doFinally(signalType)`
- Triggered on: completion (COMPLETE), error (ERROR), cancellation (CANCEL)
- **Action**: Remove from `clientSinks` map, log disconnect

**No Cross-Reference Cleanup**:
- SSE disconnection does NOT:
  - Expire associated session
  - Release flight control
  - Disconnect DRC
  - Update session `lastActivityAt`

**Result**: If SSE connection drops:
- Client cannot send new requests (sink removed)
- But session remains alive for `1800s` (default timeout)
- Any in-flight drones/DRC connections remain held
- Only timeout/crash recovery releases resources

---

## 4. Session ↔ SSE Client Mapping

### 4.1 Current Mapping

**NONE IMPLEMENTED**

**What Exists**:
1. `ClientId` (SSE only, 8 chars) — ephemeral, per connection
2. `SessionId` (MCP, 12 chars) — persistent, per workspace
3. `RequestContext` (ThreadLocal, per request) — includes both IDs

**RequestContext Usage** (lines 21-161 in RequestContext.java):
```java
public class RequestContext {
    private final Object requestId;      // JSON-RPC id
    private final String toolName;       // Tool name
    private final String clientId;       // SSE clientId or "stdio"
    private String sessionId;            // Filled via enrichWithSession()
    
    public static void set(Object requestId, String toolName, String clientId) {
        HOLDER.set(new RequestContext(requestId, toolName, clientId));
    }
}
```

**Flow**:
1. `SseTransport.message()` sets `RequestContext.set(id, toolName, clientId)` with SSE clientId
2. Tool executes, can call `enrichWithSession(session)` to populate sessionId
3. Used for audit logging and idempotency key construction

**Key Gap**: RequestContext is ThreadLocal per request, NOT a persistent mapping.

### 4.2 Multi-Session SSE Scenario

Currently unsupported because:
- `SessionManager.getActiveSession()` returns only the first session
- No session ID routing in SSE transport
- No way to associate SSE clientId with specific session

(Future multi-session design would need: `SessionManager.getSession(sessionId)` routing logic in SSE handlers)

---

## 5. Heartbeat & Reconnection Mechanisms

### 5.1 Existing Heartbeat

**Minimal Implementation**:
- `SseTransport` responds to `"ping"` method with empty success response (line 116)
- `StdioTransport` also supports `"ping"`
- **No server-initiated heartbeat**
- **No disconnect detection by server**

```java
case "ping" -> createSuccessResponse(id, Map.of());
```

### 5.2 Health Endpoints

**Location**: `HealthEndpoint.java` (lines 53-116)

Three endpoints (no auth required):
- `GET /health` — liveness probe (always 200 if process alive)
- `GET /ready` — readiness probe (checks tool registry, session manager)
- `GET /status` — detailed metrics (version, uptime, tool count, session active, JVM stats)

**None detect SSE connection drops or session validity.**

### 5.3 What's Missing

For heartbeat/reconnect:
- ❌ Server-initiated heartbeat (no periodic `PING` events sent to clients)
- ❌ Client detection of server-side session loss (no session expiration notification)
- ❌ Automatic session re-binding on reconnect (client must re-run `fh_connect`)
- ❌ Graceful degradation on disconnect (no queuing/retry logic)
- ❌ Session-to-SSE-client tracking (no way to know which client owns which session)
- ❌ Heartbeat interval configuration (not in FlightHubProperties)
- ❌ Activity tracking on SSE events (only on tool calls via RequestContext)

---

## 6. Timeout Configurations

**In FlightHubProperties.java**:

| Config Key | Default | Type | Meaning |
|------------|---------|------|---------|
| `flighthub.security.session-timeout` | 1800 | seconds | MCP session inactivity timeout |
| `flighthub.security.session-check-interval` | 30000 | ms | Cleanup scheduler interval |
| `flighthub.failsafe.disconnect-timeout` | 60 | seconds | SDK failsafe: time before RTH on loss-of-signal |

**No SSE-specific timeouts**:
- No connection idle timeout
- No heartbeat interval
- No client ping frequency

---

## 7. Activity Tracking & Idle Detection

### 7.1 Session Activity Touch Points

| Location | Method | When Touched |
|----------|--------|--------------|
| SessionContext constructor | `touch()` | Session created |
| `setActiveWaylineTaskUuid()` | explicit touch | Wayline task updated |
| (Implicit) | via RequestContext | Every tool invocation |

### 7.2 Activity NOT Tracked For

- ✗ SSE client messages (only request context set during tool execution)
- ✗ SSE keepalive pings
- ✗ Subscribe/listen operations
- ✗ View-only queries

---

## 8. Shutdown & Resource Cleanup

### 8.1 Graceful Shutdown

**Shutdown Hook** (lines 130-140):
```java
private void registerShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        log.warn("JVM ShutdownHook triggered — releasing flight resources...");
        try {
            closeAllSessions();  // Calls releaseSessionResources for each
        } catch (Exception e) {
            log.error("ShutdownHook: error releasing resources", e);
        }
    }, "flighthub-shutdown-hook"));
}
```

**Covers**:
- ✓ Normal shutdown (SIGTERM)
- ✓ Uncaught exceptions
- ✗ kill -9 (relies on SDK's `outOfControlAction` failsafe)

### 8.2 Explicit Session Close

```java
public void closeSession(String sessionId) {
    SessionContext session = sessions.remove(sessionId);
    if (session != null) {
        releaseSessionResources(session);
        log.info("Session closed: id={}", sessionId);
    }
}
```

(No tool exposes this; only internal use)

---

## 9. Key Findings & Implications

### ✅ What Works

1. **Persistent Session Tracking**: Sessions stored in thread-safe ConcurrentHashMap, survive across requests
2. **Scheduled Cleanup**: Background scheduler periodically reaps expired sessions
3. **Resource Release Chain**: On timeout/close, SDK calls are made to release control & DRC links
4. **Cross-Layer Context**: RequestContext bridges transport and tool layers for audit/idempotency

### ⚠️ Critical Gaps

1. **No SSE ↔ Session Binding**
   - When SSE client connects, no association with MCP session created
   - When SSE client disconnects, session doesn't know
   - Orphaned session remains allocated until timeout

2. **No Active Disconnect Detection**
   - Only `doFinally` in SSE handler removes from `clientSinks`
   - Session is NOT expired/cleaned up
   - Flight resources held until timeout or crash

3. **No Server-Initiated Heartbeat**
   - Clients must periodically ping to refresh `lastActivityAt`
   - But pings are client-driven, not mandatory
   - Idle client → idle session → timeout after 30 minutes

4. **No Reconnection Flow**
   - Client disconnects and reconnects → gets new SSE clientId
   - Old session still held
   - Must call `fh_connect` again (creates new session, but old one remains until timeout)

5. **No Activity on SSE Events**
   - Only tool execution updates `lastActivityAt`
   - Ping messages don't reset idle timer
   - SSE connection can be "alive" but session marked "expired" if no tool calls

### ⛔ Failure Scenario

```
t=0s     Client connects to /sse (no session yet)
t=10s    Client calls fh_connect → session created (lastActivityAt=t10s)
t=15s    Client calls fh_takeoff → session.touch() at t15s, drone flying
t=16s    Network fails, SSE connection drops → clientSinks.remove(clientId)
         But SessionManager has no way to know; session still holds control
t=20s    Client reconnects to /sse (new clientId), but no awareness of old session
t=21s    Client calls fh_connect again → ERROR "Already connected"
         (Or if it succeeds, now 2 sessions exist, old one still holding control)
t=50s    (Multiple reconnects, accumulate sessions)
t=1800s  First session finally expires → releaseControlViaSdk() called
         But drone may have been executing random commands from stale session
```

---

## 10. Recommendations for Heartbeat/Reconnect Feature

### Phase 1: Immediate (Session-to-Client Mapping)

```java
// In SessionManager
private final Map<String, String> sessionToClient = new ConcurrentHashMap<>();
private final Map<String, String> clientToSession = new ConcurrentHashMap<>();

public void bindSessionToClient(String sessionId, String clientId) {
    sessionToClient.put(sessionId, clientId);
    clientToSession.put(clientId, sessionId);
}

public String getSessionIdForClient(String clientId) {
    return clientToSession.get(clientId);
}

public void unbindClient(String clientId) {
    String sessionId = clientToSession.remove(clientId);
    if (sessionId != null) {
        sessionToClient.remove(sessionId);
    }
}
```

### Phase 2: Active Disconnect Detection

```java
// In SseTransport.connect()
.doFinally(signalType -> {
    clientSinks.remove(clientId);
    String sessionId = sessionManager.getSessionIdForClient(clientId);
    if (sessionId != null) {
        // Option A: Expire session immediately
        sessionManager.expireSession(sessionId);
        // Option B: Mark for expedited cleanup (check in 30s instead of 30m)
        sessionManager.markSessionForExpiry(sessionId);
    }
    log.info("SSE client disconnected: {} (session={})", clientId, sessionId);
});
```

### Phase 3: Server-Initiated Heartbeat

```java
// Add to FlightHubProperties
private Heartbeat heartbeat = new Heartbeat();

public static class Heartbeat {
    private int intervalSeconds = 30;      // Send ping every 30s
    private int timeoutSeconds = 90;       // Expect pong within 90s
    
    // getters/setters
}
```

```java
// New component: HeartbeatManager
@Component
@EnableScheduling
public class HeartbeatManager {
    
    @Scheduled(fixedRateString = "${flighthub.heartbeat.interval-seconds:30}")
    public void sendHeartbeats() {
        clientSinks.forEach((clientId, sink) -> {
            sink.tryEmitNext(ServerSentEvent.<String>builder()
                .event("heartbeat")
                .data("{\"ts\":" + System.currentTimeMillis() + "}")
                .build());
        });
    }
}
```

### Phase 4: Reconnection Grace Period

```java
// Track recent disconnects
private final Map<String, Instant> recentDisconnects = new ConcurrentHashMap<>();

public void onClientDisconnect(String clientId) {
    String sessionId = getSessionIdForClient(clientId);
    recentDisconnects.put(clientId, Instant.now());
    
    // Don't expire immediately; grace period
    // If client reconnects within grace period, can rebind
}

public boolean tryRebindSession(String clientId, String sessionId) {
    Instant disconnectTime = recentDisconnects.get(clientId);
    if (disconnectTime != null && 
        Duration.between(disconnectTime, Instant.now()).getSeconds() < GRACE_PERIOD_SECONDS) {
        
        // Rebind session to new clientId
        bindSessionToClient(sessionId, clientId);
        recentDisconnects.remove(clientId);
        return true;
    }
    return false;
}
```

---

## 11. Appendix: Code Locations

| Component | File | Lines |
|-----------|------|-------|
| SessionManager | `session/SessionManager.java` | 1-216 |
| SessionContext | `session/SessionContext.java` | 1-239 |
| SseTransport | `transport/SseTransport.java` | 1-199 |
| StdioTransport | `transport/StdioTransport.java` | 1-300+ |
| RequestContext | `context/RequestContext.java` | 1-161 |
| FlightHubProperties | `config/FlightHubProperties.java` | 1-123 |
| FhConnectTool | `tools/connect/FhConnectTool.java` | 1-150+ |
| HealthEndpoint | `transport/HealthEndpoint.java` | 1-122 |

---

## 12. Summary Table

| Aspect | Current State | Limitation |
|--------|---------------|-----------|
| **Session Creation** | On explicit `fh_connect` | Manual trigger; no auto-session per client |
| **Session Storage** | ConcurrentHashMap (in-memory) | Not persistent; lost on restart |
| **Session Timeout** | 1800s idle | Purely time-based; doesn't detect network drops |
| **Timeout Detection** | Scheduled every 30s | Not real-time |
| **SSE Client Tracking** | Separate ConcurrentHashMap | No linkage to sessions |
| **Connection Drop Detection** | Via Flux.doFinally | Only removes from clientSinks; no session action |
| **Heartbeat** | Client-initiated ping only | No server-initiated keep-alive |
| **Session Re-bind** | Not supported | Disconnect = permanent session loss (until timeout) |
| **Resource Release** | On timeout or explicit close | Delayed (up to 30 min) on network drop |
| **Activity Tracking** | `lastActivityAt` per session | Only updated on tool calls |
| **Failsafe** | Shutdown hook + SDK timeout | Only for graceful shutdown; not network-aware |

