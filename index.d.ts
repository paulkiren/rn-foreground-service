/**
 * Android foreground service types
 * See: https://developer.android.com/develop/background-work/services/fg-service-types
 */
type ForegroundServiceType =
  | 'camera'
  | 'connectedDevice'
  | 'dataSync'
  | 'health'
  | 'location'
  | 'mediaPlayback'
  | 'mediaProjection'
  | 'microphone'
  | 'phoneCall'
  | 'remoteMessaging'
  | 'shortService'
  | 'specialUse'
  | 'systemExempted';

declare const ReactNativeForegroundService: {
  register: () => void;
  start: ({
    id,
    title,
    message,
    serviceType,
    vibration,
    visibility,
    icon,
    largeIcon,
    importance,
    number,
    button,
    buttonText,
    buttonOnPress,
    button2,
    button2Text,
    button2OnPress,
    mainOnPress,
    progress,
    color,
    setOnlyAlertOnce,
  }: {
    id: any;
    title?: any;
    message?: string | undefined;
    serviceType?: ForegroundServiceType | undefined;
    vibration?: boolean | undefined;
    visibility?: string | undefined;
    icon?: string | undefined;
    largeIcon?: string | undefined;
    importance?: string | undefined;
    number?: string | undefined;
    button?: boolean | undefined;
    buttonText?: string | undefined;
    buttonOnPress?: string | undefined;
    button2?: boolean | undefined;
    button2Text?: string | undefined;
    button2OnPress?: string | undefined;
    mainOnPress?: string | undefined;
    progress?: {
      max: number;
      curr: number;
    };
    color?: string;
    setOnlyAlertOnce?: string;
  }) => Promise<void>;
  update: ({
    id,
    title,
    message,
    vibration,
    visibility,
    largeIcon,
    icon,
    importance,
    number,
    button,
    buttonText,
    buttonOnPress,
    button2,
    button2Text,
    button2OnPress,
    mainOnPress,
    progress,
    color,
    setOnlyAlertOnce,
  }: {
    id: any;
    title?: any;
    message?: string | undefined;
    vibration?: boolean | undefined;
    visibility?: string | undefined;
    largeIcon?: string | undefined;
    icon?: string | undefined;
    importance?: string | undefined;
    number?: string | undefined;
    button?: boolean | undefined;
    buttonText?: string | undefined;
    buttonOnPress?: string | undefined;
    button2?: boolean | undefined;
    button2Text?: string | undefined;
    button2OnPress?: string | undefined;
    mainOnPress?: string | undefined;
    progress?: {
      max: number;
      curr: number;
    };
    color?: string;
    setOnlyAlertOnce?: string;
  }) => Promise<void>;
  stop: () => Promise<any>;
  stopAll: () => Promise<any>;
  is_running: () => boolean;
  add_task: (
    task: any,
    {
      delay,
      onLoop,
      taskId,
      onSuccess,
      onError,
    }: {
      delay?: number | undefined;
      onLoop?: boolean | undefined;
      taskId?: string | undefined;
      onSuccess?: (() => void) | undefined;
      onError?: ((e) => void) | undefined;
    }
  ) => string;
  update_task: (
    task: any,
    {
      delay,
      onLoop,
      taskId,
      onSuccess,
      onError,
    }: {
      delay?: number | undefined;
      onLoop?: boolean | undefined;
      taskId?: string | undefined;
      onSuccess?: (() => void) | undefined;
      onError?: (() => void) | undefined;
    }
  ) => string;
  remove_task: (taskId: any) => void;
  is_task_running: (taskId: any) => boolean;
  remove_all_tasks: () => {};
  get_task: (taskId: any) => any;
  get_all_tasks: () => {};
  cancel_notification: (id: any) => void;
  eventListener: (callBack: any) => () => void;
  /**
   * Check if POST_NOTIFICATIONS permission is granted (Android 13+)
   * @returns Promise<boolean> - true if permission granted or not required, false otherwise
   */
  checkNotificationPermission: () => Promise<boolean>;
  /**
   * Check if app can currently start a foreground service (Android 12+)
   * On Android 12+, apps cannot start foreground services from background
   * @returns Promise<boolean> - true if can start, false otherwise
   */
  canStartForegroundService: () => Promise<boolean>;
};
export default ReactNativeForegroundService;
export { ForegroundServiceType };
