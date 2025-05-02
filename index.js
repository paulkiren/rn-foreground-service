import { NativeModules, AppRegistry, DeviceEventEmitter, Platform } from "react-native";

const ForegroundServiceModule = NativeModules.ForegroundService;

class ForegroundService {
  static registerForegroundTask(taskName, task) {
    AppRegistry.registerHeadlessTask(taskName, () => task);
  }

  static async startService(config) {
    if (Platform.OS !== 'android') {
      console.warn("Foreground service is only available on Android");
      return false;
    }
    return await ForegroundServiceModule.startService(config);
  }

  static async updateNotification(config) {
    if (Platform.OS !== 'android') {
      console.warn("Foreground service is only available on Android");
      return false;
    }
    return await ForegroundServiceModule.updateNotification(config);
  }

  static async cancelNotification(id) {
    if (Platform.OS !== 'android') {
      console.warn("Foreground service is only available on Android");
      return false;
    }
    return await ForegroundServiceModule.cancelNotification({ id });
  }

  static async stopService() {
    if (Platform.OS !== 'android') {
      console.warn("Foreground service is only available on Android");
      return false;
    }
    return await ForegroundServiceModule.stopService();
  }

  static async stopServiceAll() {
    if (Platform.OS !== 'android') {
      console.warn("Foreground service is only available on Android");
      return false;
    }
    return await ForegroundServiceModule.stopServiceAll();
  }

  static async runTask(config) {
    if (Platform.OS !== 'android') {
      console.warn("Foreground service is only available on Android");
      return false;
    }
    return await ForegroundServiceModule.runTask(config);
  }

  static async isRunning() {
    if (Platform.OS !== 'android') {
      return false;
    }
    return await ForegroundServiceModule.isRunning();
  }
  
  static async checkNotificationPermission() {
    if (Platform.OS !== 'android') {
      return true;
    }
    return await ForegroundServiceModule.checkNotificationPermission();
  }
}

const tasks = {};
const samplingInterval = 500; // ms
let serviceRunning = false;

const taskRunner = async () => {
  if (!serviceRunning) return;

  const now = Date.now();
  const promises = Object.entries(tasks).map(([taskId, task]) => {
    if (now >= task.nextExecutionTime) {
      if (task.onLoop) task.nextExecutionTime = now + task.delay;
      else delete tasks[taskId];
      return Promise.resolve(task.task()).then(task.onSuccess, task.onError);
    }
  });

  await Promise.all(promises.filter(Boolean));
};

/**
 * Registers the foreground service task runner
 */
const register = () => {
  if (!serviceRunning) {
    ForegroundService.registerForegroundTask("taskRunner", taskRunner);
  }
};

/**
 * Starts a foreground service with the given notification config
 * 
 * @param {import('./index').NotificationConfig} config Notification configuration
 * @returns {Promise<boolean>} True if service started successfully
 */
const start = async (config) => {
  try {
    // Check for notification permission on Android 13+
    if (Platform.OS === 'android') {
      const hasPermission = await ForegroundService.checkNotificationPermission();
      if (!hasPermission) {
        console.error("Notification permission is required for foreground service on Android 13+");
        return false;
      }
    }
    
    if (!serviceRunning) {
      await ForegroundService.startService(config);
      serviceRunning = true;
      await ForegroundService.runTask({
        taskName: "taskRunner",
        delay: samplingInterval,
        loopDelay: samplingInterval,
        onLoop: true,
      });
      return true;
    } else {
      console.log("Foreground service is already running.");
      return true;
    }
  } catch (error) {
    console.error("Error starting foreground service:", error);
    return false;
  }
};

/**
 * Updates an existing foreground service notification
 * 
 * @param {import('./index').NotificationConfig} config Notification configuration
 * @returns {Promise<boolean>} True if notification updated successfully
 */
const update = async (config) => {
  try {
    await ForegroundService.updateNotification(config);
    if (!serviceRunning) {
      serviceRunning = true;
      await ForegroundService.runTask({
        taskName: "taskRunner",
        delay: samplingInterval,
        loopDelay: samplingInterval,
        onLoop: true,
      });
    }
    return true;
  } catch (error) {
    console.error("Error updating notification:", error);
    return false;
  }
};

/**
 * Stops the foreground service
 * 
 * @returns {Promise<boolean>} True if service stopped successfully
 */
const stop = async () => {
  try {
    serviceRunning = false;
    await ForegroundService.stopService();
    return true;
  } catch (error) {
    console.error("Error stopping service:", error);
    return false;
  }
};

/**
 * Stops all foreground services
 * 
 * @returns {Promise<boolean>} True if services stopped successfully
 */
const stopAll = async () => {
  try {
    serviceRunning = false;
    await ForegroundService.stopServiceAll();
    return true;
  } catch (error) {
    console.error("Error stopping all services:", error);
    return false;
  }
};

/**
 * Adds a task to be executed in the background
 * 
 * @param {Function} task Function to execute
 * @param {import('./index').TaskOptions} options Task configuration options
 * @returns {string} Task ID
 */
const addTask = (task, options) => {
  const taskId = options.taskId || Math.random().toString(36).substring(2, 15);
  tasks[taskId] = {
    task,
    nextExecutionTime: Date.now(),
    delay: options.delay || 5000,
    onLoop: options.onLoop || false,
    onSuccess: options.onSuccess || (() => {}),
    onError: options.onError || ((e) => console.error("Task error:", e)),
  };
  return taskId;
};

/**
 * Updates an existing task
 * 
 * @param {Function} task New function to execute
 * @param {import('./index').TaskOptions} options Task configuration options including taskId of existing task
 * @returns {string} Task ID
 */
const updateTask = (task, options) => {
  const taskId = options.taskId || Math.random().toString(36).substring(2, 15);
  tasks[taskId] = {
    task,
    nextExecutionTime: Date.now(),
    delay: options.delay || 5000,
    onLoop: options.onLoop || false,
    onSuccess: options.onSuccess || (() => {}),
    onError: options.onError || ((e) => console.error("Task error:", e)),
  };
  return taskId;
};

/**
 * Removes a task by ID
 * 
 * @param {string} taskId ID of the task to remove
 */
const removeTask = (taskId) => {
  delete tasks[taskId];
};

/**
 * Checks if a task is currently running
 * 
 * @param {string} taskId ID of the task to check
 * @returns {boolean} boolean indicating if task is running
 */
const isTaskRunning = (taskId) => !!tasks[taskId];

/**
 * Removes all background tasks
 */
const removeAllTasks = () => {
  Object.keys(tasks).forEach((taskId) => delete tasks[taskId]);
};

/**
 * Gets a task by ID
 * 
 * @param {string} taskId ID of the task to get
 * @returns {import('./index').Task | undefined} Task object
 */
const getTask = (taskId) => tasks[taskId];

/**
 * Gets all running tasks
 * 
 * @returns {Record<string, import('./index').Task>} Record of all tasks
 */
const getAllTasks = () => tasks;

/**
 * Cancels a notification by ID
 * 
 * @param {number} id Notification ID to cancel
 * @returns {Promise<boolean>} True if notification cancelled successfully
 */
const cancelNotification = async (id) => {
  try {
    await ForegroundService.cancelNotification(id);
    return true;
  } catch (error) {
    console.error("Error cancelling notification:", error);
    return false;
  }
};

/**
 * Adds an event listener for notification clicks
 * 
 * @param {Function} callback Function to call when a notification is clicked
 * @returns {Function} Function to remove the listener
 */
const eventListener = (callback) => {
  const subscription = DeviceEventEmitter.addListener(
    "notificationClickHandle",
    callback
  );
  return () => subscription.remove();
};

/**
 * Checks if the app has notification permission (Android 13+ requirement)
 * 
 * @returns {Promise<boolean>} True if notification permission is granted
 */
const checkNotificationPermission = async () => {
  if (Platform.OS !== 'android') {
    return true;
  }
  return await ForegroundService.checkNotificationPermission();
};

const ReactNativeForegroundService = {
  register,
  start,
  update,
  stop,
  stopAll,
  addTask,
  updateTask,
  removeTask,
  isTaskRunning,
  removeAllTasks,
  getTask,
  getAllTasks,
  cancelNotification,
  eventListener,
  checkNotificationPermission,
  isRunning: async () => {
    return await ForegroundService.isRunning();
  },
};

export default ReactNativeForegroundService;
