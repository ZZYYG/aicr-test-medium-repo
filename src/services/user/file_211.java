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
