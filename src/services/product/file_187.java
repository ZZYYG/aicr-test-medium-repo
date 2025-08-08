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
