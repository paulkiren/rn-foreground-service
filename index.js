import { NativeModules, AppRegistry, DeviceEventEmitter } from "react-native";

const ForegroundServiceModule = NativeModules.ForegroundService;

class ForegroundService {
  static registerForegroundTask(taskName, task) {
    AppRegistry.registerHeadlessTask(taskName, () => task);
  }

  static async startService(config) {
    console.log("Start Service Triggered");
    return await ForegroundServiceModule.startService(config);
  }

  static async updateNotification(config) {
    console.log("Update Service Triggered");
    return await ForegroundServiceModule.updateNotification(config);
  }

  static async cancelNotification(id) {
    console.log("Cancel Notification Triggered");
    return await ForegroundServiceModule.cancelNotification({ id });
  }

  static async stopService() {
    console.log("Stop Service Triggered");
    return await ForegroundServiceModule.stopService();
  }

  static async stopServiceAll() {
    console.log("Stop All Services Triggered");
    return await ForegroundServiceModule.stopServiceAll();
  }

  static async runTask(config) {
    console.log("Run Task Triggered");
    return await ForegroundServiceModule.runTask(config);
  }

  static async isRunning() {
    return await ForegroundServiceModule.isRunning();
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

const register = () => {
  if (!serviceRunning) {
    ForegroundService.registerForegroundTask("taskRunner", taskRunner);
  }
};

const start = async (config) => {
  if (!serviceRunning) {
    await ForegroundService.startService(config);
    serviceRunning = true;
    await ForegroundService.runTask({
      taskName: "taskRunner",
      delay: samplingInterval,
      loopDelay: samplingInterval,
      onLoop: true,
    });
  } else {
    console.log("Foreground service is already running.");
  }
};

const update = async (config) => {
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
};

const stop = async () => {
  serviceRunning = false;
  await ForegroundService.stopService();
};

const stopAll = async () => {
  serviceRunning = false;
  await ForegroundService.stopServiceAll();
};

const addTask = (task, options) => {
  const taskId = options.taskId || Math.random().toString(36).substring(2, 15);
  tasks[taskId] = {
    task,
    nextExecutionTime: Date.now(),
    delay: options.delay || 5000,
    onLoop: options.onLoop || false,
    onSuccess: options.onSuccess || (() => {}),
    onError: options.onError || (() => {}),
  };
  return taskId;
};

const updateTask = (task, options) => {
  const taskId = options.taskId || Math.random().toString(36).substring(2, 15);
  tasks[taskId] = {
    task,
    nextExecutionTime: Date.now(),
    delay: options.delay || 5000,
    onLoop: options.onLoop || false,
    onSuccess: options.onSuccess || (() => {}),
    onError: options.onError || (() => {}),
  };
  return taskId;
};

const removeTask = (taskId) => {
  delete tasks[taskId];
};

const isTaskRunning = (taskId) => !!tasks[taskId];

const removeAllTasks = () => {
  Object.keys(tasks).forEach((taskId) => delete tasks[taskId]);
};

const getTask = (taskId) => tasks[taskId];

const getAllTasks = () => tasks;

const cancelNotification = async (id) => {
  await ForegroundService.cancelNotification(id);
};

const eventListener = (callback) => {
  const subscription = DeviceEventEmitter.addListener(
    "notificationClickHandle",
    callback
  );
  return () => subscription.remove();
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
};

export default ReactNativeForegroundService;
