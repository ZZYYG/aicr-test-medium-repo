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
// 修改于 2025年 8月 8日 星期五 15时40分57秒 CST
