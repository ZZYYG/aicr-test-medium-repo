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
