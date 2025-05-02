declare const ReactNativeForegroundService: {
  register: () => void;
  start: (config: {
    id: number;
    title?: string;
    message?: string;
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
    progress?: { max: number; curr: number };
    color?: string;
    setOnlyAlertOnce?: boolean;
  }) => Promise<void>;
  update: (config: {
    id: number;
    title?: string;
    message?: string;
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
    progress?: { max: number; curr: number };
    color?: string;
    setOnlyAlertOnce?: boolean;
  }) => Promise<void>;
  stop: () => Promise<void>;
  stopAll: () => Promise<void>;
  is_running: () => boolean;
  add_task: (
    task: () => Promise<void> | void,
    options: {
      delay?: number;
      onLoop?: boolean;
      taskId?: string;
      onSuccess?: () => void;
      onError?: (e: Error) => void;
    }
  ) => string;
  update_task: (
    task: () => Promise<void> | void,
    options: {
      delay?: number;
      onLoop?: boolean;
      taskId?: string;
      onSuccess?: () => void;
      onError?: (e: Error) => void;
    }
  ) => string;
  remove_task: (taskId: string) => void;
  is_task_running: (taskId: string) => boolean;
  remove_all_tasks: () => void;
  get_task: (taskId: string) => any;
  get_all_tasks: () => Record<string, any>;
  cancel_notification: (id: number) => Promise<void>;
  eventListener: (callback: (data: any) => void) => () => void;
};

export default ReactNativeForegroundService;
