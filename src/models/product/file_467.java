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
