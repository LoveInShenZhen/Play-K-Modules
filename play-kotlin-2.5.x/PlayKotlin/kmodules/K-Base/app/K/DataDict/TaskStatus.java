package K.DataDict;

import java.util.HashMap;

/**
 * Created by kk on 13-12-27.
 */
public class TaskStatus {
    public int code;
    public String desc;

    private static HashMap<Integer, TaskStatus> code_map;

    static {
        code_map = new HashMap<Integer, TaskStatus>();
    }

    public static TaskStatus GetTaskStatus(int code) {
        Integer key = new Integer(code);
        if (!code_map.containsKey(key)) {
            throw new RuntimeException("错误的 TaskStatus: " + code);
        }
        return code_map.get(key);
    }

    public TaskStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
        Integer key = new Integer(code);
        code_map.put(key, this);
    }

    public static final TaskStatus WaitingInDB = new TaskStatus(0, "任务在DB队列里等待被执行");
    public static final TaskStatus WaitingInQueue = new TaskStatus(7, "任务在线程池工作队列里等待被执行");
    public static final TaskStatus Exception = new TaskStatus(8, "任务在执行的过程中发生未处理异常");
}
