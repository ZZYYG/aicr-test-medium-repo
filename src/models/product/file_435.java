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

+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
// 修改于 2025年 8月 8日 星期五 15时40分50秒 CST
