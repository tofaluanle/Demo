package cn.temp;

import cn.temp.ITaskCallback;

interface ITaskBinder {
    boolean isTaskRunning();   
    void stopRunningTask();   
    void registerCallback(ITaskCallback cb);   
    void unregisterCallback(ITaskCallback cb);   
}  