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
