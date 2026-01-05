# Architecture Documentation
# @kirenpaul/rn-foreground-service

**Version:** 2.2.0
**Platform:** Android
**Last Updated:** January 2026

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture Diagram](#architecture-diagram)
3. [System Components](#system-components)
4. [Architectural Layers](#architectural-layers)
5. [Data Flow](#data-flow)
6. [Key Design Patterns](#key-design-patterns)
7. [Component Details](#component-details)
8. [Task Management System](#task-management-system)
9. [Notification System](#notification-system)
10. [Security & Permissions](#security--permissions)
11. [Performance Considerations](#performance-considerations)
12. [Extension Points](#extension-points)

---

## Overview

This library provides a React Native bridge for Android foreground services, enabling persistent background task execution with customizable notifications. The architecture follows a layered approach with clear separation between JavaScript/TypeScript interface and native Android implementation.

### Core Capabilities

- **Foreground Service Management**: Start, stop, and monitor Android foreground services
- **Task Scheduling**: Execute JavaScript functions in the background with configurable intervals
- **Notification Management**: Display and update persistent notifications with action buttons
- **Event Handling**: Respond to user interactions with notifications

### Design Goals

1. **Simplicity**: Easy-to-use API with sensible defaults
2. **Flexibility**: Extensive configuration options for notifications and tasks
3. **Reliability**: Robust error handling and service lifecycle management
4. **Performance**: Efficient task scheduling with minimal overhead

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                     React Native Application                     │
│                      (JavaScript/TypeScript)                     │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            │ ReactNativeForegroundService API
                            │
┌───────────────────────────▼─────────────────────────────────────┐
│                   JavaScript Bridge Layer                        │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  index.js - High-level API & Task Management             │   │
│  │  - Service control (start/stop/update)                   │   │
│  │  - Task scheduler with interval management               │   │
│  │  - Event listener for notification interactions          │   │
│  │  - State management (tasks object, serviceRunning flag)  │   │
│  └──────────────────────────────────────────────────────────┘   │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            │ React Native Bridge
                            │
┌───────────────────────────▼─────────────────────────────────────┐
│                    Native Module Layer (Java)                    │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  ForegroundServiceModule.java                            │   │
│  │  - Bridge methods (@ReactMethod)                         │   │
│  │  - Input validation & error handling                     │   │
│  │  - Intent creation & service communication               │   │
│  └──────────────────────────────────────────────────────────┘   │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            │ Android Intents
                            │
┌───────────────────────────▼─────────────────────────────────────┐
│                  Android Service Layer (Java)                    │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  ForegroundService.java (Singleton)                      │   │
│  │  - Service lifecycle management                          │   │
│  │  - Action handling (START/STOP/UPDATE/RUN_TASK)          │   │
│  │  - Task execution coordination                           │   │
│  │  - Handler for looped tasks                              │   │
│  │  - Reference counting for multiple start/stop calls      │   │
│  └──────────────────────────────────────────────────────────┘   │
│                            │                                     │
│  ┌─────────────────────────┴────────────────────────────────┐   │
│  │  ForegroundServiceTask.java (HeadlessJsTaskService)      │   │
│  │  - Executes JS tasks in headless context                 │   │
│  │  - Manages wake locks                                    │   │
│  │  - Task timeout configuration (60s)                      │   │
│  └──────────────────────────────────────────────────────────┘   │
└───────────────────────────┬─────────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────────┐
│                  Notification & Config Layer                     │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  NotificationHelper.java (Singleton)                     │   │
│  │  - Notification channel creation                         │   │
│  │  - Notification builder with full customization          │   │
│  │  - Action button configuration                           │   │
│  │  - Progress bar management                               │   │
│  │  - PendingIntent creation for interactions               │   │
│  └──────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  NotificationConfig.java                                 │   │
│  │  - Configuration model for notifications                 │   │
│  └──────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Constants.java                                          │   │
│  │  - Action constants (START/STOP/UPDATE/RUN_TASK)         │   │
│  │  - Error codes                                           │   │
│  │  - Configuration keys                                    │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                            │
                            │
┌───────────────────────────▼─────────────────────────────────────┐
│                    Android System Services                       │
│  - NotificationManager                                           │
│  - ActivityManager                                               │
│  - PowerManager (WakeLock)                                       │
└─────────────────────────────────────────────────────────────────┘
```

---

## System Components

### 1. JavaScript/TypeScript Layer

#### **index.js** (Main API)
Primary interface exposing high-level methods for service and task management.

**Key Responsibilities:**
- Wraps native module calls with JavaScript-friendly API
- Implements client-side task scheduling with 500ms sampling interval
- Manages task state and execution timing
- Provides event listener for notification interactions

#### **index.d.ts** (Type Definitions)
TypeScript type declarations providing IDE autocomplete and type safety.

### 2. Native Bridge Layer

#### **ForegroundServiceModule.java**
React Native module that bridges JavaScript and native Android code.

**Exposed Methods:**
- `startService(notificationConfig)`: Start foreground service
- `updateNotification(notificationConfig)`: Update notification
- `cancelNotification(id)`: Dismiss specific notification
- `stopService()`: Stop service (decrements counter)
- `stopServiceAll()`: Force stop service
- `runTask(taskConfig)`: Execute headless task
- `isRunning()`: Check service status

**Features:**
- Input validation for all methods
- Promise-based async API
- Error handling with descriptive error codes
- Intent-based communication with service

### 3. Android Service Layer

#### **ForegroundService.java** (Core Service)
Main Android service running in the foreground with singleton pattern.

**Features:**
- Singleton pattern for global access
- Reference counting for multiple start/stop calls
- Action-based intent handling
- Handler-based task looping
- Automatic service restart on kill
- Last notification config caching for recovery

**State Management:**
- `running`: Counter tracking start/stop calls
- `lastNotificationConfig`: Cached for service recovery
- `mInstance`: Singleton instance reference
- `taskConfig`: Current task configuration
- `handler`: For scheduled task execution

#### **ForegroundServiceTask.java**
Extends React Native's HeadlessJsTaskService to execute JavaScript tasks in background.

**Configuration:**
- Task timeout: 60 seconds (for long-running tasks)
- Foreground execution allowed
- Automatic wake lock management

### 4. Notification Layer

#### **NotificationHelper.java**
Singleton helper for building and managing notifications.

**Capabilities:**
- Notification channel creation (Android 8.0+)
- Custom notification styling
- Up to 2 action buttons
- Progress bar support
- Icon customization
- Color theming
- PendingIntent creation for button clicks

#### **NotificationConfig.java**
Data model for notification configuration.

#### **Constants.java**
Centralized constants for actions, errors, and configuration keys.

---

## Architectural Layers

### Layer 1: Public API (JavaScript)

**Location**: `index.js`, `index.d.ts`

**Purpose**: Developer-facing API with high-level abstractions

**Key Features**:
- Simple, promise-based methods
- Task scheduler with internal state management
- Event-driven notification interaction handling
- Automatic task ID generation
- Task lifecycle management (success/error callbacks)

**Example**:
```javascript
await ReactNativeForegroundService.start({
  id: 144,
  title: 'Service Running'
});

ReactNativeForegroundService.add_task(
  () => console.log('Task running'),
  { delay: 5000, onLoop: true }
);
```

### Layer 2: Native Bridge (React Native Module)

**Location**: `ForegroundServiceModule.java`

**Purpose**: Translate between JavaScript and native Android

**Key Features**:
- `@ReactMethod` annotations for JS exposure
- ReadableMap for configuration objects
- Promise resolution/rejection for async operations
- Intent-based service communication

**Example**:
```java
@ReactMethod
public void startService(ReadableMap notificationConfig, Promise promise) {
    Intent intent = new Intent(context, ForegroundService.class);
    intent.setAction(Constants.ACTION_FOREGROUND_SERVICE_START);
    intent.putExtra(NOTIFICATION_CONFIG, Arguments.toBundle(notificationConfig));
    context.startService(intent);
    promise.resolve(null);
}
```

### Layer 3: Service Implementation (Android Service)

**Location**: `ForegroundService.java`, `ForegroundServiceTask.java`

**Purpose**: Core service lifecycle and task execution

**Key Features**:
- Singleton service instance
- Reference counting for graceful shutdown
- Handler-based task scheduling
- Headless JS task execution
- Service restart capability

**Service Lifecycle**:
```
onCreate() → mInstance = this, running = 0
onStartCommand() → Handle action, increment/decrement running
onDestroy() → Clean up, mInstance = null, running = 0
```

### Layer 4: Notification Management (Android Notifications)

**Location**: `NotificationHelper.java`, `NotificationConfig.java`

**Purpose**: Build and manage Android notifications

**Key Features**:
- Notification channel management
- Rich notification styling
- Action button configuration
- Progress bar integration
- Intent handling for user interactions

---

## Data Flow

### 1. Starting a Foreground Service

```
User calls start()
    ↓
index.js validates and wraps config
    ↓
ForegroundServiceModule.startService()
    ↓
Creates Intent with ACTION_FOREGROUND_SERVICE_START
    ↓
ForegroundService.onStartCommand()
    ↓
NotificationHelper.buildNotification()
    ↓
Service.startForeground(id, notification)
    ↓
running counter incremented
    ↓
Promise resolved to JavaScript
```

### 2. Adding and Executing Tasks

```
User calls add_task(fn, config)
    ↓
Task stored in tasks object with nextExecutionTime
    ↓
taskRunner() runs every 500ms (samplingInterval)
    ↓
Check each task's nextExecutionTime vs Date.now()
    ↓
If time arrived, execute task function
    ↓
Call onSuccess or onError callback
    ↓
If onLoop=true, update nextExecutionTime
    ↓
If onLoop=false, delete task from tasks object
```

### 3. Handling Notification Interactions

```
User taps notification button
    ↓
Android triggers PendingIntent
    ↓
MainActivity receives Intent
    ↓
ForegroundEmitter() extracts button identifier
    ↓
Emits 'notificationClickHandle' event via DeviceEventEmitter
    ↓
eventListener callback in JavaScript receives data
    ↓
User code handles button press
```

### 4. Updating Notifications

```
User calls update()
    ↓
ForegroundServiceModule.updateNotification()
    ↓
Creates Intent with ACTION_UPDATE_NOTIFICATION
    ↓
ForegroundService.onStartCommand()
    ↓
If service running, NotificationManager.notify()
    ↓
If service not running, restart service with new config
    ↓
Promise resolved
```

---

## Key Design Patterns

### 1. Singleton Pattern

**Used in**: `ForegroundService.java`, `NotificationHelper.java`

**Purpose**:
- Single service instance across application lifecycle
- Easy access to service state
- Prevent multiple service instances

**Implementation**:
```java
private static ForegroundService mInstance = null;

public static ForegroundService getInstance() {
    if(isServiceCreated()) {
        return mInstance;
    }
    return null;
}
```

### 2. Reference Counting

**Used in**: `ForegroundService.java`

**Purpose**:
- Handle multiple start/stop calls gracefully
- Only stop service when all references released
- Prevent premature service termination

**Implementation**:
```java
// Start increments
running += 1;

// Stop decrements
running -= 1;
if (running == 0) {
    stopSelf();
}
```

### 3. Bridge Pattern

**Used in**: `ForegroundServiceModule.java`

**Purpose**:
- Decouple JavaScript API from native implementation
- Enable platform-specific implementations
- Provide consistent API across platforms

### 4. Observer Pattern (Event Emitter)

**Used in**: `eventListener()` in `index.js`

**Purpose**:
- Notify application of notification interactions
- Loose coupling between service and app components
- Subscribe/unsubscribe pattern

**Implementation**:
```javascript
const eventListener = (callback) => {
  let subscription = DeviceEventEmitter.addListener(
    "notificationClickHandle",
    callback
  );
  return function cleanup() {
    subscription.remove();
  };
};
```

### 5. Builder Pattern

**Used in**: `NotificationHelper.java`

**Purpose**:
- Complex notification object construction
- Flexible configuration
- Readable notification setup

### 6. Task Scheduler Pattern

**Used in**: Task management in `index.js`

**Purpose**:
- Efficient task scheduling
- Minimal CPU usage with sampling interval
- Parallel task execution
- Task lifecycle management

**Key Components**:
- `tasks` object: Task registry
- `taskRunner`: Interval-based execution loop
- `nextExecutionTime`: Scheduling mechanism
- `samplingInterval`: 500ms check frequency

---

## Component Details

### JavaScript Task Manager

**Location**: `index.js:137-373`

**Architecture**:
```javascript
// State
let tasks = {};           // Task registry
const samplingInterval = 500;  // Execution check frequency
let serviceRunning = false;    // Service state

// Task Structure
{
  taskId: {
    task: Function,              // The function to execute
    nextExecutionTime: Number,   // Timestamp when to execute
    delay: Number,               // Interval between executions
    onLoop: Boolean,             // Repeat or one-time
    onSuccess: Function,         // Success callback
    onError: Function            // Error callback
  }
}
```

**Task Execution Loop**:
1. `taskRunner()` registered as headless task
2. Runs every 500ms via native task scheduling
3. Iterates through all tasks
4. Checks if `Date.now() >= task.nextExecutionTime`
5. Executes matching tasks in parallel via `Promise.all()`
6. Updates `nextExecutionTime` for looped tasks
7. Removes one-time tasks after execution

**Advantages**:
- Efficient: Single native task checks multiple JS tasks
- Flexible: Tasks can have different intervals
- Parallel: Multiple tasks execute concurrently
- Resilient: Individual task errors don't affect others

### Native Service State Machine

**Location**: `ForegroundService.java`

**States**:
```
NOT_CREATED: mInstance = null
    ↓ startService()
CREATED: mInstance != null, running = 0
    ↓ ACTION_FOREGROUND_SERVICE_START
RUNNING: running > 0
    ↓ ACTION_FOREGROUND_SERVICE_STOP
RUNNING: running decremented
    ↓ running == 0
STOPPED: stopSelf() called
    ↓
NOT_CREATED: mInstance = null
```

**Actions**:
- `ACTION_FOREGROUND_SERVICE_START`: Start service, increment running
- `ACTION_UPDATE_NOTIFICATION`: Update notification, start if needed
- `ACTION_FOREGROUND_RUN_TASK`: Execute headless task
- `ACTION_FOREGROUND_SERVICE_STOP`: Decrement running, stop if 0
- `ACTION_FOREGROUND_SERVICE_STOP_ALL`: Force stop immediately

### Notification Builder

**Location**: `NotificationHelper.java`

**Build Process**:
1. Create/retrieve notification channel
2. Configure NotificationCompat.Builder
3. Set content (title, message, icon)
4. Add action buttons (up to 2)
5. Configure progress bar if needed
6. Set colors and styling
7. Create PendingIntent for main tap
8. Create PendingIntents for button taps
9. Build and return Notification object

**Customization Options**:
- Title, message, ticker text
- Small icon, large icon
- Importance/priority level
- Visibility (public/private/secret)
- Color theming
- Progress bar (determinate)
- Action buttons with custom text
- Badge number (for supported devices)
- Alert once flag

---

## Task Management System

### Task Lifecycle

```
add_task()
    ↓
Task added to tasks object
    ↓
nextExecutionTime = Date.now()
    ↓
taskRunner() detects execution time
    ↓
Task function executed
    ↓
Success → onSuccess() callback
    ↓
Error → onError() callback
    ↓
onLoop=true → Update nextExecutionTime, repeat
    ↓
onLoop=false → Remove from tasks object
```

### Task Scheduling Algorithm

**Key Innovation**: Client-side scheduling with single native task

**How it works**:
```javascript
// Sampling interval for task checks
const samplingInterval = 500;  // ms

// Task delay rounded to nearest sampling interval
const effectiveDelay = Math.ceil(delay / samplingInterval) * samplingInterval;

// Next execution time
task.nextExecutionTime = Date.now() + effectiveDelay;

// Check every 500ms
if (Date.now() >= task.nextExecutionTime) {
  executeTask();
  if (onLoop) {
    task.nextExecutionTime = Date.now() + task.delay;
  }
}
```

**Benefits**:
- Minimal native overhead (one native task)
- Supports multiple independent tasks
- Flexible task intervals
- Easy to add/remove tasks dynamically

**Tradeoffs**:
- Minimum effective interval is 500ms
- Tasks checked in JavaScript (not guaranteed)
- Tasks share JS execution context

### Task API

**add_task(task, config)**:
```javascript
ReactNativeForegroundService.add_task(
  async () => {
    // Your task logic
  },
  {
    delay: 5000,           // Interval in ms
    onLoop: true,          // Repeat or one-time
    taskId: 'my-task',     // Optional unique ID
    onSuccess: () => {},   // Success callback
    onError: (e) => {}     // Error callback
  }
);
```

**update_task(task, config)**:
Replace existing task by ID.

**remove_task(taskId)**:
Stop and remove a task.

**is_task_running(taskId)**:
Check if task exists.

**get_task(taskId)**:
Retrieve task object.

**get_all_tasks()**:
Get all registered tasks.

**remove_all_tasks()**:
Clear all tasks.

---

## Notification System

### Notification Architecture

```
NotificationConfig (Input)
    ↓
NotificationHelper.buildNotification()
    ↓
NotificationChannel (Android 8.0+)
    ↓
NotificationCompat.Builder
    ↓
Add actions, progress, styling
    ↓
Notification object
    ↓
startForeground() or notify()
```

### Action Buttons

**Configuration**:
```javascript
{
  button: true,
  buttonText: 'Pause',
  buttonOnPress: 'pause_action',
  button2: true,
  button2Text: 'Cancel',
  button2OnPress: 'cancel_action',
  mainOnPress: 'main_action'
}
```

**Event Flow**:
```
User taps button
    ↓
PendingIntent triggered
    ↓
MainActivity.onNewIntent()
    ↓
Extract action identifier from Intent extras
    ↓
Emit 'notificationClickHandle' event
    ↓
eventListener callback receives:
{
  main: 'main_action',      // If main notification tapped
  button: 'pause_action',   // If button 1 tapped
  button2: 'cancel_action'  // If button 2 tapped
}
```

### Progress Bar

**Configuration**:
```javascript
{
  progress: {
    max: 100,
    curr: 45
  }
}
```

**Features**:
- Determinate progress bar
- Update in real-time via `update()` method
- Useful for downloads, uploads, processing

---

## Security & Permissions

### Required Permissions

**AndroidManifest.xml**:
```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

### Permission Justification

- **FOREGROUND_SERVICE**: Required for running foreground services on Android 9.0+
- **WAKE_LOCK**: Automatic wake lock management by HeadlessJsTaskService

### Security Considerations

1. **No Data Storage**: Library doesn't persist sensitive data
2. **Intent Validation**: All intents validated before processing
3. **Error Handling**: Exceptions caught and returned as promise rejections
4. **Notification Privacy**: Visibility level configurable (private/public/secret)

---

## Performance Considerations

### Memory Management

1. **Task Registry**: In-memory JavaScript object
   - Consider impact if hundreds of tasks added
   - No automatic cleanup except on task completion

2. **Service Singleton**: Single instance reduces memory footprint

3. **Notification Caching**: Last config cached for service recovery

### CPU Usage

1. **Task Sampling**: 500ms interval minimizes CPU usage
   - Tradeoff: Tasks won't execute at exact millisecond
   - Acceptable for most background task use cases

2. **Handler-based Looping**: Efficient task looping mechanism

3. **Headless Task Timeout**: 60 second timeout prevents runaway tasks

### Battery Optimization

1. **Wake Locks**: Automatically managed by Android
2. **Foreground Service**: Required notification keeps service alive
3. **Task Frequency**: Consider battery impact of frequent tasks

### Best Practices

1. **Task Intervals**: Use longest acceptable interval (reduce CPU/battery)
2. **Task Cleanup**: Remove tasks when no longer needed
3. **Service Lifecycle**: Stop service when not needed
4. **Notification Updates**: Batch updates to reduce overhead

---

## Extension Points

### 1. Custom Notification Layouts

**Current**: Standard notification with up to 2 buttons

**Extension**: Implement custom RemoteViews in `NotificationHelper.java`

**Use Cases**: Media players, custom progress layouts

### 2. Additional Action Buttons

**Current**: Limited to 2 action buttons

**Extension**: Modify `NotificationHelper` to support more buttons

**Consideration**: UI space constraints on small screens

### 3. Task Priority System

**Current**: All tasks treated equally

**Extension**: Implement priority queue in task scheduler

**Use Cases**: Critical vs background tasks

### 4. Task Persistence

**Current**: Tasks lost on app restart

**Extension**: Store tasks in SharedPreferences or database

**Use Cases**: Long-running background operations

### 5. Multi-Service Support

**Current**: Single foreground service

**Extension**: Support multiple concurrent services with different notifications

**Use Cases**: Separate download, upload, sync services

### 6. iOS Support

**Current**: Android only

**Extension**: Implement iOS background modes

**Challenges**: iOS has different background execution model

### 7. Task Dependencies

**Current**: Tasks execute independently

**Extension**: Implement task dependency graph

**Use Cases**: Sequential workflows, task chains

### 8. Event Hooks

**Current**: Limited to notification interactions

**Extension**: Lifecycle events (onServiceStart, onServiceStop, onTaskStart, onTaskComplete)

**Use Cases**: Logging, analytics, debugging

---

## Troubleshooting Guide

### Common Issues

#### 1. Service Not Starting
**Symptom**: `startService()` promise rejects
**Causes**:
- `register()` not called
- Missing AndroidManifest.xml permissions
- Invalid notification config

**Debug**:
```javascript
try {
  await ReactNativeForegroundService.start(config);
} catch (error) {
  console.log('Error:', error);
  // Check error code: ERROR_INVALID_CONFIG or ERROR_SERVICE_ERROR
}
```

#### 2. Tasks Not Executing
**Symptom**: Task functions never called
**Causes**:
- Service not running
- Task delay too large
- Task function throws unhandled error

**Debug**:
```javascript
const isRunning = ReactNativeForegroundService.is_running();
const task = ReactNativeForegroundService.get_task('my-task');
console.log({ isRunning, task });
```

#### 3. Notification Not Appearing
**Symptom**: Service starts but no notification
**Causes**:
- Notification channel not configured
- Icon resource missing
- User disabled notifications for app

**Solution**: Check notification settings and verify icon resources exist

---

## Version History

### 2.2.0 (Current)
- Android 14 compatibility updates
- Improved stability and error handling
- Updated peer dependencies

### Previous Versions
- Based on react-native-foreground-service
- Adapted from Voximplant and zo0r implementations

---

## Contributing

### Adding New Features

1. **JavaScript API**: Add method in `index.js` and type in `index.d.ts`
2. **Native Bridge**: Add `@ReactMethod` in `ForegroundServiceModule.java`
3. **Service Logic**: Implement in `ForegroundService.java`
4. **Constants**: Add action/error codes in `Constants.java`
5. **Documentation**: Update README.md and ARCHITECTURE.md

### Code Style

- **JavaScript**: Modern ES6+ syntax, async/await for promises
- **Java**: Android standard conventions
- **Comments**: Explain "why" not "what"

---

## References

- [Android Foreground Services](https://developer.android.com/guide/components/foreground-services)
- [React Native Headless JS](https://reactnative.dev/docs/headless-js-android)
- [Android Notifications](https://developer.android.com/develop/ui/views/notifications)
- [Original Implementation](https://github.com/raja0sama)

---

## License

MIT © Raja Osama (supersami)
Modified by Kiren Paul

---

**Document Maintenance**
This architecture document should be updated whenever:
- Major features are added
- Architecture patterns change
- New components are introduced
- Breaking changes occur
