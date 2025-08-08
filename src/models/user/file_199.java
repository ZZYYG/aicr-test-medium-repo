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
