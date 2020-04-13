package com.github.myetl.fiflow.core.core;

import com.github.myetl.fiflow.core.flink.FlinkClusterInfo;
import com.github.myetl.fiflow.core.frame.SessionConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.java.StreamTableEnvironment;

import java.util.ArrayList;
import java.util.List;

/**
 * 核心 入口
 */
public abstract class FiflowSession {

    public final String id;
    public final SessionConfig sessionConfig;

    public StreamExecutionEnvironment env;
    public boolean streamingMode = false;
    public EnvironmentSettings settings;
    public TableEnvironment tEnv;
    public FlinkClusterInfo flinkClusterInfo;
    public volatile Boolean closed = false;
    private List<String> jars = new ArrayList<>();
    private int step = 0;

    public FiflowSession(String id, SessionConfig sessionConfig) {
        this.id = id;
        this.sessionConfig = sessionConfig;
    }


    /**
     * 初始化 environment
     * @param streamingMode
     * @return  true 表示新建 false 表示使用旧的 env
     */
    public boolean initEnv(boolean streamingMode) {
        streamingMode = true; 
        if (env != null && this.streamingMode == streamingMode) return false;

        env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(sessionConfig.parallelism);

        EnvironmentSettings.Builder settingBuilder = EnvironmentSettings
                .newInstance()
                .useBlinkPlanner();

        if (streamingMode) {
            settingBuilder.inStreamingMode();
            settings = settingBuilder.build();
            tEnv = StreamTableEnvironment.create(env, settings);
        } else {
            settingBuilder.inBatchMode();
            settings = settingBuilder.build();
            tEnv = TableEnvironment.create(settings);
        }


        return true;
    }

    public void addJar(String jarName) {
        if (StringUtils.isNotEmpty(jarName))
            jars.add(jarName);
    }

    public List<String> getJars() {
        return jars;
    }

    /**
     * 关闭该 session
     */
    public synchronized void close() {
        this.closed = true;

    }

    public String getName() {
        return id + "-" + step++;
    }
}
