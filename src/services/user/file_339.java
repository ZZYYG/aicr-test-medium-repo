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
