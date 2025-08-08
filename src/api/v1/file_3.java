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
