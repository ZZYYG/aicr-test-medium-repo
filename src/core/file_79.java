package com.example.service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 服务实现类
 */
public class Service {
    private static final Logger LOGGER = Logger.getLogger(Service.class.getName());
    
    private final Config config;
    private final Database database;
    private final Cache cache;
    private ServiceStatus status;
    private Instant startTime;
    
    /**
     * 服务状态枚举
     */
    public enum ServiceStatus {
        STOPPED,
        STARTING,
        RUNNING,
        STOPPING,
        ERROR
    }
    
    /**
     * 配置类
     */
    public static class Config {
        private final String serviceName;
        private final int port;
        private final String logLevel;
        private final DatabaseConfig database;
        
        public Config(String serviceName, int port, String logLevel, DatabaseConfig database) {
            this.serviceName = serviceName;
            this.port = port;
            this.logLevel = logLevel;
            this.database = database;
        }
        
        public String getServiceName() {
            return serviceName;
        }
        
        public int getPort() {
            return port;
        }
        
        public String getLogLevel() {
            return logLevel;
        }
        
        public DatabaseConfig getDatabase() {
            return database;
        }
    }
    
    /**
     * 数据库配置类
     */
    public static class DatabaseConfig {
        private final String host;
        private final int port;
        private final String username;
        private final String password;
        private final String database;
        
        public DatabaseConfig(String host, int port, String username, String password, String database) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
            this.database = database;
        }
        
        public String getHost() {
            return host;
        }
        
        public int getPort() {
            return port;
        }
        
        public String getUsername() {
            return username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public String getDatabase() {
            return database;
        }
    }
    
    /**
     * 数据库接口
     */
    public interface Database {
        CompletableFuture<Void> connect();
        CompletableFuture<Void> close();
        <T> CompletableFuture<T> query(String query, Object... params);
        CompletableFuture<Void> execute(String query, Object... params);
    }
    
    /**
     * 缓存接口
     */
    public interface Cache {
        <T> CompletableFuture<T> get(String key);
        <T> CompletableFuture<Void> set(String key, T value, long ttl, TimeUnit unit);
        CompletableFuture<Void> delete(String key);
    }
    
    /**
     * 构造函数
     */
    public Service(Config config, Database database, Cache cache) {
        this.config = config;
        this.database = database;
        this.cache = cache;
        this.status = ServiceStatus.STOPPED;
    }
    
    /**
     * 启动服务
     */
    public CompletableFuture<Void> start() {
        LOGGER.info(String.format("Starting %s service on port %d", config.getServiceName(), config.getPort()));
        status = ServiceStatus.STARTING;
        
        return CompletableFuture.runAsync(() -> {
            try {
                // 连接数据库
                if (database != null) {
                    database.connect().join();
                }
                
                // 实现服务启动逻辑
                // ...
                
                status = ServiceStatus.RUNNING;
                startTime = Instant.now();
                LOGGER.info(String.format("%s service started successfully on port %d", 
                        config.getServiceName(), config.getPort()));
            } catch (Exception e) {
                status = ServiceStatus.ERROR;
                LOGGER.log(Level.SEVERE, "Failed to start service", e);
                throw new RuntimeException("Failed to start service", e);
            }
        });
    }
    
    /**
     * 停止服务
     */
    public CompletableFuture<Void> stop() {
        LOGGER.info(String.format("Stopping %s service", config.getServiceName()));
        status = ServiceStatus.STOPPING;
        
        return CompletableFuture.runAsync(() -> {
            try {
                // 实现服务停止逻辑
                // ...
                
                // 关闭数据库连接
                if (database != null) {
                    database.close().join();
                }
                
                status = ServiceStatus.STOPPED;
                LOGGER.info(String.format("%s service stopped successfully", config.getServiceName()));
            } catch (Exception e) {
                status = ServiceStatus.ERROR;
                LOGGER.log(Level.SEVERE, "Failed to stop service", e);
                throw new RuntimeException("Failed to stop service", e);
            }
        });
    }
    
    /**
     * 获取服务状态
     */
    public Map<String, Object> getStatus() {
        return Map.of(
            "service", config.getServiceName(),
            "status", status.name().toLowerCase(),
            "uptime", startTime != null ? Instant.now().getEpochSecond() - startTime.getEpochSecond() : 0,
            "version", "1.0.0"
        );
    }
    
    /**
     * 主方法示例
     */
    public static void main(String[] args) {
        // 创建配置
        Config config = new Config(
            "api",
            8080,
            "info",
            new DatabaseConfig(
                "localhost",
                5432,
                "user",
                "password",
                "apidb"
            )
        );
        
        // 这里应该有实际的database和cache的实现
        // Service service = new Service(config, database, cache);
        
        // 启动服务
        // service.start().join();
        
        // 添加关闭钩子
        // Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        //     service.stop().join();
        // }));
    }
}

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
// 修改于 2025年 8月 8日 星期五 15时40分54秒 CST
