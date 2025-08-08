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
