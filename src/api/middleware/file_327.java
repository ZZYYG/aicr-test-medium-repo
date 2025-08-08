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
// 修改于 2025年 8月 8日 星期五 15时40分53秒 CST
