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
