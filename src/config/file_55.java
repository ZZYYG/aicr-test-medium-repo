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
// 修改于 2025年 8月 8日 星期五 15时40分49秒 CST
// 修改于 2025年 8月 8日 星期五 15时40分52秒 CST
