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
