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
// 修改于 2025年 8月 8日 星期五 15时40分56秒 CST
