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
