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
