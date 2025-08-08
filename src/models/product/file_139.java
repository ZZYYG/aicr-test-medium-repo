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
