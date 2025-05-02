export interface NotificationConfig {
  id: number;
  title: string;
  message: string;
  vibration?: boolean;
  visibility?: "private" | "public" | "secret";
  icon?: string;
  largeIcon?: string;
  importance?: "min" | "low" | "default" | "high" | "max";
  number?: string;
  button?: boolean;
  buttonText?: string;
  buttonOnPress?: string;
  button2?: boolean;
  button2Text?: string;
  button2OnPress?: string;
  mainOnPress?: string;
  progressBar?: boolean;
  progressBarMax?: number;
  progressBarCurr?: number;
  color?: string;
  channelId?: string;
  channelName?: string;
  serviceType?: "default" | "location" | "camera" | "microphone" | "mediaplayback" | "datasynch";
}

export interface TaskOptions {
  delay?: number;
  onLoop?: boolean;
  taskId?: string;
  onSuccess?: () => void;
  onError?: (e: Error) => void;
  loopDelay?: number;
}

export interface Task {
  task: () => Promise<void> | void;
  nextExecutionTime: number;
  delay: number;
  onLoop: boolean;
  onSuccess: () => void;
  onError: (e: Error) => void;
}

export interface NotificationClickData {
  buttonOnPress?: string;
  button2OnPress?: string;
  mainOnPress?: string;
}

declare const ReactNativeForegroundService: {
  /**
   * Registers the foreground service task runner
   */
  register: () => void;

  /**
   * Starts a foreground service with the given notification config
   * 
   * @param config Notification configuration
   * @returns Promise<void>
   */
  start: (config: NotificationConfig) => Promise<void>;

  /**
   * Updates an existing foreground service notification
   * 
   * @param config Notification configuration
   * @returns Promise<void>
   */
  update: (config: NotificationConfig) => Promise<void>;

  /**
   * Stops the foreground service
   * 
   * @returns Promise<void>
   */
  stop: () => Promise<void>;

  /**
   * Stops all foreground services
   * 
   * @returns Promise<void>
   */
  stopAll: () => Promise<void>;

  /**
   * Checks if the foreground service is running
   * 
   * @returns Promise<boolean>
   */
  isRunning: () => Promise<boolean>;

  /**
   * Adds a task to be executed in the background
   * 
   * @param task Function to execute
   * @param options Task configuration options
   * @returns Task ID
   */
  addTask: (task: () => Promise<void> | void, options: TaskOptions) => string;

  /**
   * Updates an existing task
   * 
   * @param task New function to execute
   * @param options Task configuration options including taskId of existing task
   * @returns Task ID
   */
  updateTask: (task: () => Promise<void> | void, options: TaskOptions) => string;

  /**
   * Removes a task by ID
   * 
   * @param taskId ID of the task to remove
   */
  removeTask: (taskId: string) => void;

  /**
   * Checks if a task is currently running
   * 
   * @param taskId ID of the task to check
   * @returns boolean indicating if task is running
   */
  isTaskRunning: (taskId: string) => boolean;

  /**
   * Removes all background tasks
   */
  removeAllTasks: () => void;

  /**
   * Gets a task by ID
   * 
   * @param taskId ID of the task to get
   * @returns Task object
   */
  getTask: (taskId: string) => Task | undefined;

  /**
   * Gets all running tasks
   * 
   * @returns Record of all tasks
   */
  getAllTasks: () => Record<string, Task>;

  /**
   * Cancels a notification by ID
   * 
   * @param id Notification ID to cancel
   * @returns Promise<void>
   */
  cancelNotification: (id: number) => Promise<void>;

  /**
   * Adds an event listener for notification clicks
   * 
   * @param callback Function to call when a notification is clicked
   * @returns Function to remove the listener
   */
  eventListener: (callback: (data: NotificationClickData) => void) => () => void;

  /**
   * Checks if the app has notification permission (Android 13+ requirement)
   * 
   * @returns Promise<boolean>
   */
  checkNotificationPermission: () => Promise<boolean>;
};

export default ReactNativeForegroundService;
