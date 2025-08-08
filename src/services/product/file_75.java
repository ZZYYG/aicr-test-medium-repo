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
