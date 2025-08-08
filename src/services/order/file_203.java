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
// 修改于 2025年 8月 8日 星期五 15时40分55秒 CST
