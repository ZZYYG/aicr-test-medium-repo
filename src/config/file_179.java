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
