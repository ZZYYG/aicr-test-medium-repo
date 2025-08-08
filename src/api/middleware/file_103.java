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
// 修改于 2025年 8月 8日 星期五 15时40分56秒 CST
