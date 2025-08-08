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
