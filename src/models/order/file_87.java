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
// 修改于 2025年 8月 8日 星期五 15时40分52秒 CST
