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
