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

0p3TuykK2td6N3nB9KOKUn6Z+vPNdOongduEiz2CJxkDJJiWBIlHnu0Y3iJdLuemkYZACLttDrSjFL8B9QVXjEUejGBXUnVJrFirbdEuztO1TppUv443E3Olb3IdUw9g9pyZJvejGOea5c4vQeKz8rTw+gr67AczlqQuCAs2z8VWqXNG2VSk3bSSygmPcPmcRV5JjTK59IxClq/zZonO+x0aufCp3r36ztb7wkl2KziQnZnZ9YewEi98Xon9iNJGC0yotrHC54y8oA0cWaYvnmbf0kYG+DUAPJPXvit6DGANutQs5YU3fy6Tb969RvSQCxqY/asgC3cfYgtVgUW6Odtx3fCDmk+enaU3WXhAW6V97c35f7KzRTvU+4qHAYu864UbpCKA1jptYrqBqJynncGB1Tb4VBnJeqEIQT6YF8imm1ZvCgpRDRmcq7+ZhaKM6+Fd4SYfWaBIYcTCFBsehF8dfAzE6DQrzqM8WzD8L6y7F9zoMopJG5HbxRMbEDIaHBbcuVDYnRk/o7X1z8TnPMSAhfPnk6kq95+G5Jd2fpLaDVeg4c3uN3Y3rTySYeOZfAEvNjILnyo3z2hb2L0OewpVn46JtbKCJF2150ksKZ/swxZTfqFbOGZqFosdFJ8wXscXdDf+F/nqZLAkX6hK62FkUK20rWbc1ynEo8ydy2ZkTPS3PG2XJmMDg0fECKvYp88bSxClYgp0elqS8uV7esttpd9KifMYBYzf3Yf3VaGc15njclQmjw4VNlqwQWxhRqrDjRKBzJy5Gu7Y9zGZlSz2GtnKmCcPWiUUT3Ixv9xiXqApN+U0TOiwoNccqDCna8EwLd9yIndbtscl6ia1i0P9ei142HAxIxlxiOlMTMv0WzrwqfZd9r8NSSKCBD9jqAjMTStrYXKOsxhNMp9rUcEH5bmh2pidgVf+2H57/UfXm8h7HY/Sp6JGIOJzqOy2zOTovy0g1VJ3FutjfyaKvg5hoRlzVvDLuxu3XPrZ3aFkGds5gDV2Pm2EU1LArR9wbWoOMAEAze9EpLxgBRsCMS/pV0w9QeFJL8F02X7nLnQb0NmZAUokOCD5vxzAtzcryqnTjy4eSvKn5t9qKob2rajxLC7bRkcsDj35xjcro9Tq29Bx8j7PgCUZDrTX6x7w8OUhUqEVxcl79+GajGtyRrOdmvFQgjRpodbVHFXbEuymDjXAMFBUGoLfFdIOQAVFY5fwvZtpbJ4twsSOMLox8QMAE14uHV4TUow8h7WhyQAcAagHHxwsTf0yoMcsDbtFtOPUgYsABb5i/VkAccIiGTXzbqZPHrLhrnaliRJEo9qQJm5P/q2k/N+IeaD/lBJoAAuq2KGfUL/zPdz34X3pOCdSVIs08y1uFuMzwLUgQYh0cBVHaLQT+w+GeyBuc2zoNbTmvaSClkBPgavgDo0Jvlwb6CY1MbcJdR/yr1UnynikKffADo4Q0hmfoKyKHCScZ1Y85Re88QUp840l4wlY0rSlNGoTReCCf/svwzAozRukLIyNQvL0EI6afs8S8QqbqcmSkczCvlp7jCdC04EvIhllmTnHjXoyWQ5o73NaHO/Y915Wg7xdaUxHwy7Lq01mwfKI2KtPmSnwUPO3dKM2fAdwsUJGEtUKXFU6m1znVPjeM5UAq6xwA4mnXV/d3iomb+sckTrL/GuIMWPgA43uCBpsT3JRXg9bu6m0tiVSpBprS0edkPdcxAQIcmRoZO/lW/O8slF9hQAOOE0+Qghjmkj2iYmm4Xc/vwJWv370cY5cQNyU8PizPaOSoAOEPyie0/SAg3C3zakeLML9MkbF+p/GLtqFTeDULg7Cu48A0FcNc4zwFriBrmJikYu0PSlHEE6G/pG3PTqH0U18cVCRvQPKwoAAzZOBMFcG2+KA9bB54YrJStQRzZIrJ6z4KwHPxxDaGTGZ3gq/Xmle9nd7aHuUXc/z0mW8ATUEJONupcMGNEHtVm0QlfBr/xXmQVbq9EhsSmXE7ZNi+u45iL5L5QS+LyGWOggvQWE3ndjUwdxj6SZhQfCivrlZrL+NfxcEe2A4W/Wt7TMkJF8/b47m9RZaTK7bhFLTre93jjyjShLtUizqf2b973Cvs9zM2R/f4tXS+4ZnVQaC/yGUKdIRMSsCRmp23lYK/WXwHwGXocvTPdcykxBvxk6/O6NBW8pVQ1E42uJ7/2HPsXQxxEXAoZwfo7mk3p2fYbGY6kOQZ+vTdNEXbndYdbc+/D8WfdZfsguHfZZx8ze9lDlnmbC2snaVsyI6+AbxwUkfy8gBle22jO0eKDWG1J6TBJws3vdVFRioHBYxLwimO8uNN1K9Fayy994pUZ7SVlrfpJcFEykf4QRhSm5/3y5AFy27GbWmF44qRxnSzj7DT6kGnEZ0gJb0DC5Hi59FJqkUTCnclaGCs0Om7T9PV/xc2WKlAUz6nTOR4ynOdb1RqEzdJMVT/v6yz7PQ6OVGH+OGnzlF+M2wg5CgkBKrmjVd/PSag5rQIZ7T5WnTKZ8YKzsxd5/Oo1LD3Ct0mZjGcx+SvFpmrvhFE6ogOntkKpfdgomRU1AbyBswNwHww0jcQvqWh6h7dh+z3LhkDei4Ib8samgygJ5LKkfsQFzj0siW/IGXqDOl6o8kYiVswFGbBXwlRvFb1vjefr27oaC47/q4PH9PgWa4mm/uozTW4F6QLc3zDCv/hQx4zsCR3exRifk8jhzbt86pzlbjoK68Ncaen4d9M7GTel9cZum6jm18l73TXYhg4ZbndI7c9Ht9EdOH/XelaA3XbxI228kJaqoOTvhgHDBPhd8M85x1tCbAte0AaTTCb0hoe88VRYIgUdpeIOwksSL87gcfLSjVQ5KYqX7pDY6Vjmo8Iy+JDsTjaxHxiyoM+ENwFv10OsXROlZ/rHbkKRGwK7gl7k5mYJ9M2Ub0LiC7jGq7+txxu2PH0GJBLXaUIES6F8afUV3xnWPjini7Ho9+Ud+QFLapnLX34TMhUdUfhyyI0E5LDF1wUAOWFiRxMRb/bUO9BOpA2tZcK2gcq1k68rcjBwgt3lEA2DiSQCpqVsTPAehQFoWYpPezQb45WHh/xubyTm7ZlUxVJSnNPOY6Qj9ujVHH8jPXXYWzD/gDV6+4DpAZeP2MbxEZMjFdZeFfLMou7wUlpkxJo4ax52wl9IueS32KVDRgnO9qaJ4LOuXtm1W/kJPh36mRYTrn4o3M1RGRUjXCYdni0OPlqWrOt+rP825JKcpAg5zXB0qbrxIo7TQcQ7ML9J7J2xpHJF+vfbGuGRV9g6zv3ZA+3riqzCh3PcKyt36GVJ+wyn62CTnWd37R+SFoM6x8x/V+WU3nuQPW/7vziFhUItzOC6kT4AfENEPFVORvzwx3oOxmB6/eiJQnFR6NaLO21BS2Tz62oskxSIe2SadByJqMjUHTqeV+hcAP6qoloLHGA5IjGZGIqMXBTY2jxnVbh9mtbh+XQ8tpYo/UQFPR2URRa3HoafSEZMEYa2XB6ebmBWMQITEsTMi/TmwdHRpZ2kL+OH6lU0CK/I/pE92BUVMZ3X7u1TFmMQBjunxA1ML52neq6rSw3WbJreRkPEWgnxC344ttysX5//Js3O8ccZh9Y8i4byWfa6lEvne6KLfPXfDRWkKaMCKZCjXHNLcTWMntjC0O+lwR6A+MDA62cAYiprwCDCAC6Wh/B4VJn589sxNDb2bjX3TGvay0WnEYX1c3Ctg8zSEFN1TYi6I5YHvdePH26je5Ir57JqWL86HIboYhv+Z95qF2iSfzZjXnskwaZr1mk5iXQPjuR1TxcZBpFaaxBVz6MoNwwNdSR4704FrGMHsAaWeB6xmt6Ss0nGs7RQh1DVYnCINXZWcedePghjOP1Sv7kan7gnT2gurR7OM+m6gmq+nRF6eyRW4Q4pBi5oK06wOaEuTF+/g1/7SPpJri56OarcQtK1Oy5HCJnqI/6lB6kqnJ48W4JUsjDMXD4gS3m2vhLMO4yZO4mZ5dZH3qjQZ4Jr8EUVf2vhgQibl5nUIrIfG9kqOaIhGIbM2BVIw/U3eZzPbIqbRoimUZhxVNnBAwaxoLNYeH5MQMFVOe9W4PX+Z3z7SbPquqZlDrJPXqInBTremlYJRuYGUnFDQEElwNZpJAbwp0oumyF6538er5BZdCQwbKFt6QEPbA2yy3D8u9TtYryF29CehFgyBuxfzs7CPrpdjxpJl4nsVIpjC5S/90QHsIxWnDFFrDdXLzfOvgjliqf/uI9cqM+i9b8Zpi5bYAT7uOiB+ohuaSP4W3UD7genM/N4PYhY81GoCS1A9cIZRQcR0UeEbI4fSU2n50L9r5nZSrzjwQkGgbJIi6nIiy+3ZUtbdehrMWWEwDPm91D0d5nlGyKIScyS/sT0/83KFksXcUJYMfPZuWEobi5XlcHn73DfTqW+rPqpdpltmMFivdgjhKkWLhfEVumwVgTa5kKN37MoFE0Zk7fz5X7TgpNTzq7ZyLKqobmSeBfGAqC3gat3iikFXFfycJYkBJMGu5XII4QxKIVnVg+o1akQrVFL6gMBq+1yzmOKBgRaZcRG+V1cbu/x99PUBe+kK6B2OHZZE9ZVblO35N+tctsvwne1y/sz7VH8Fk4zNM0hfp++g+LnYv/7kZjLMJxfgNMzx15efwSvePG32XcbC3YVzKS8tLd3LUHfMXKdqH/hhsGQqaMO9Ca9siOxvWRPjO7B1mn7CQnLq/pSaxCf0fFWv/8HND4v8AL39FNAGHWqM9ihQZpkUwN32/HeT6H0z1JK+N7pw7XdMV41xzPw9kI11baPNB/mNf8PvOqNmjdvmZyCw4dguBpowljbateGvTDH9vqAhKEGTzcI1GSSycWrNnpJTZ2QqvF0nBINcUapB2CXzj7S2R8AX7m0h+3mzTv4a7exnRxgkdvWF+D67UzjSTIFJ72/Q8cR443jItrjBZMOKsWQU4z+b+SIvYN8OJ/22xiqK5na8Dk0Ej/96NglxI+nCX/QzVd88tk+h0Af3mILGDQbGFp5F7InIvJQvgM+2oq4R7smkl4uOtIe/LfNkpQlXYnYg+Xa3Q5JnfCgtMO+1MftWCa+9UiBCts7PGkIWexStPxKNMVJr26Q8IVhRKsqYCDB8+cGYbQ4EXrnJXTAz7r2OYsgzR60oomN7vywhjw2RVmNjQJyiz+RyvCt3H4nROOsz7XY/DtdUFU+1s388b1H/d3vQHstDlE1XLHhdZ6Kf721/ABkZrN665fTAcTC1IdzDZ5vG8hnLV2yzCpoa6HLpHv7/Ail5V1+JJ7MbV/cRzb/CBLnoeZ16p6ALulZHrx44R4c/9NTO/b0shZQWU/opNeRqVyDb0q9Gw2vHJM+uvV6dJScEMgkWteNOsp1A7FQtEoPx7dmh25lpzN5Qbq+RHxr/9xZeekE0zA+gfRKXYZBDt1QIYWfzCCEM1cIx20sX96PCElsgWv4bIA+Is/bJPtP+YQplyzlTxpVMhxKxVmyZxk1PuiABqKMcdnZxaAeyflJ4glevCsXxoF2s+XQgpRGCgUEo0oj+hOo6pZYmlL8+drkGWmGNoAVaAX/IGRMye77R7O1AyVBDDbV8UCxTvxFWtMPs6rN83XqHKiPrC7upPeiAPFgkR/wOZP03YkVXWtID3Mq7Lon9jrgpAIVExcTyLpL1q1Q8A3nRMIO1UeNWpqREFS/CvmiOk0ZMjmHrpdqaAK4rgqkk0PJ1C9phldBbbvHHNRW186DEO10ZfuEdVIX3J79yCejQGp5m8DSFlTFONVdktIngnHo8L8pgp9elcv3CAwSnEoCnModzKGl7Wo5F/7xAeVvDeIJE2NO9J8fyJoUvoFMqxewP6f9Xpd0w1vIlntkqzjSK4QbfaQfCFdheJG5EYnjnGqgINfT8Gj9cOmpFcEQTyNafSE2VJvnLA4H8F6EwFmGByrS9IIM/brFe5+wUeqEx5YXFZF9SM+NkQxeTl95oeevWw21sz9QDSSWbJNSuaajYBuOoBqsMMpg02YPb+7OMQwQFqHkI2NF44j3ddVtw3UViZTT8cbh9zL24h6qGxQxzD95VRF+sfWcK75eUJxWlzl6jKyc1qmjBCq8TqcHnWETfdjuiVZeG1XUThPMw0eaKEwMjdeccDX0pbi/PR/veevm+6SRgxncgDv7kDuWZd1c31vOIFWr8QFwAvxXZI6FL1tfuunsGiu/vp4FVWgfQ9hRYgeicwkZ/COxVkbc1yxTGyM+SC4XapLY3WM6hHbNgDDGmw94r0gnAesx787XE+QWVnyEn8w6HCK60b7c9K27DsEO4AnQK3nNt7MU73fM5a2wgGJpKrscv27WVL0obwItx+3rh09ARi2IbumaLRRgjyrj0ttSo11T9y23uqRblTd+u1WHzKVc4ZYHSsgpWXXwhR2YkGDxw/WEcGLzALsSDTsqDpcDKKdEENZujr5ULkmtYs+oNeYuBhIwrqs35pWL2/gV0hdwLUeKanI6/+87CmZY3cifC8hgAEGQo/4by2nbKp50RzgdVnuEXgemvd5q2iF/UhJv/G5AmZ03bAvxa39jX7QcprCelBeBMrub+I5ncK9gdtZDllGE128lRu20dMsYmt/jhxM71dnrpLZI5Ns3mMbZKEDLuWa2vminKLVIT3JBtfoRB/vEQuTbrVsa/q50vZg5mJrconaELbZRCRlCVBVfb4Mnc9wlGEqFftEqVqbcLDQ9M9NvdLszbG3qZmc5npqPGhGWg7q99nUCODaFtQ60ldqW6vB7K3mttOez+cIIQbwS42TU65n0wpbuIiN+VCA9XJCZWrSrRsdmiK9lYnWggnUmUnwCvQ9ogu0reltQV1zbe7WXohJ5sMRysA1Ff8xkbrI6KK8jm5xs1rmC/vgJQgAz17L/AtS0wPZV0WWXz0LYx5HVYOHjhvH3dOFmLa/FJweqTGDk4F7m6mxKCSzI2XnKoMwwrcGO0xoTp/vmOeQL9KFv0IVz2WUHwaKnNY5vkNSuHKLRWu6t1brirAbjEasMxcoDmSbc4zdopLcxoztrDZsiOObVNy7pOB6iVFhrUsWzF6JxviVqo8WTVxhDI/F2Q0gNoE18oVzQKvxVdHINY1UTgRJPeCxtdrXQGa3ZrJ7lhvBryJfS6UjvRDttWjk1NdMVqp001cxvyWjvaJGq92IxR9fgFtXJ3SW22zkfMZher9WyJHSDVQ6QscSIMexJVbqiijOjkTHbwOD5Bt94cQYV4SE8KguotSBMtQZFLvl0ya+kHmW4BzWrpOv3r0+fpiA6RbhT0DUa17Bkne/fbSSFnaGr8y52+y+w71Lv55qGz07C6h6sbczvEbDgcrtA4zZlmiPe4Xx6gGzIiiVShSLdlcNeNIcWFYYOAXi3Gn+o2q40YvkV128MDMCw8pRdKavqam6eiktHZ7zqwctHO1NUnS3C/8Q/KB8nBu9FwSJyTMCrQzWNrApATs1pl4PL8hTSYkhk9/+0C9dnYMTrKOdo+epaovRNGk+bwBcSQvppwIk3M4kp82hMnHy+Zs8EWHqXxLnN6FT6E9KtGL+FfXbB05f7tukYj5yCcSbfO07pjNKJn4m0yMzgpzsKkCfVIAGV/nRsIwGDP/zUr+QXpWLHq5tdYoOOqHwN5+fuApfrFNPau7Yj+7iSsado/3gh5vNyhI2XJOnAy3K4Iq0T0xthfSRNeOLCTCKJGOgjC9YEnBduNHhhPmJkOU5zAbAFDnqHq9/6O3eqUPec4dC1ku2CZie6D5mdOA3Tq1g992MfeA1lZrBm9h9Zx5x9NI84hlKxyMKPMIdpGqVxGT+cCePldn4OXqHcaX7o6G1mYSh3Rit7HBtUHsZtVothynRKcVOnziGEIG+/D815sHqYb9Wo0PE/YmYG9CHMlwe9QD8N4h9ec5OLL7ORzU+mFUaa94aLRPDf3MVZWfhvMd5luPna6kDdK95mttVyqavjlqIrngJU3myXDReGFlwPLm6+k0zhchXyj7qnyORDhquwGvAocGRpKr9WEpCFZNaCDxMfsE5Xg2MHxRbrUoDlSB8iSoOWUuDCQPIj8eWH6eAx845xhXpspaKJRNK7xkgclMi/GFXt7iw8MXIlkzGnjSuNaJu45umwo/F8j6rkOxdcLQa6IzNLzyMCDQ6MVpHk+hh78vPvRlqEztyEuW1w/QMY8H9Ip87bhGThBahvgdJr+wW2LtJ4+bsaU0F/q9widq8vAYeQN8Bbef78tAnYEiEaHIBwiLI5V3qrJ3yaV35PdThKmyX8F1Wcy5Q75/94hOWECJVoiSDWS2Hs04LQIEs2GedabfnrvEkk+TI1KXGnPRwl3MJibFgT2KUcZHJh0fdYXlsyK+0mxjIArCXzZrn3yVEpDWs4JCjaBjaH05MZ+KWJ3YI8L5RuA0YFXN6Wi9Hn/8Oyhme0YUvqmrV9BH+60SZ7dJrRL0gZRP4fuooHPqsc+UDWeh2U9OeM8PUMyoEI+lFvUGJoxx36bKV5ArEu20hE2QUsVRt2ExfGWmD4951TAeIflYrXxs3YDleALwGuBDW02b2ci7HIMSLla8gI9NpRIo9aC5UM3ZKAcxqSdd/5NxaO3IaICr7iUTWvbwfnJbJBByioj7oHiRq/OFMstGWTKlPrJueJeZPNJYR21bEj+hBKBpll49Bg+6Ul28rTgXFNiWtVHN+7uYIAfm/spe7Uz84vCw4mH+MGLXlXy9kUYyBLtUT5W5JnBZ3lDMsj+AD7Du76/5FMw0LtZfCKmqt9Snzoun6vpjXtVel/dUPRmscBoz90NyII61lGTwgVrKVMlzVc5BGe46Tc4+ZWfefyiYzbNNOKBq9gQ6ne6JJKocR3D03oGxMMxfEaD+Q1qc0g+NuqJz6DbUlG6OLboR/lvKVqZOUXodirqad4OCsWcfSafDtIeIjFSvMlVV0nGE65EzV4hu7o4J4o/mWupa21JIeCBxoMOZJtiTD+GlzIITFJqUpaMpCKe2sLpdEYq0QAump6etDXhzTmFGvLT91phqJJS+VKZdy+UaazL+LMPWxgUg2xrlCTucsQqty+j5eWLf9XWw1ttyXjotFAf+wve9HZp7JpY5y84Hi+zmRnEaTKCwvYPRkLbgo1Rz8wtaRJCVHMJaJOrUokEHaRVOnHp8S4kuZLbwV4M5g4cvi4zBxLQDeUzi6AwG9av+oSuoCv6w/MsL/pmi2I0GcDl68XEh+QG51T3cvJ0NWRKFvwMoHcjmB4H5Sfw/aeblpT0i30uwIL+HwZNhMFmxyCTXuvolJK2p1ODyP5Dg6MmaT2zaV85SZHRkK1kk2Y5z1zzx0TVlMsjS9Li+3NRyKDAUrK+uPz8/H5l+jU6dhMwO0LSBvOoPrnz5sYROwI9DaSt07y/a2/kblWm5aCogxh1ziSDHoMKL6XOPoyHeTqBEHZOt1Q1oyqR7PQWhoH6gc9o0pioG3othKH9BJPWoX3fE9eV//y34XrFBfeyVt9aOHFZj5jTFInIHbIMiJo7NUQ+DLk9nA1/TYcBMg9RzqA4xrxevv6Q9sIDFr89tVoU0/us8RARTq4pm+u/i9ZuUOFnVsyydtX+vHt0sGE3gp62vzLCGQOuSOra6cl1zJeGXj8VZIxiuT0sWSua/nxjPsDt1KwP3mfHKxWZqxbd4+n7okQQEFvVK5BUDim3+W2r3mcHdzOYmuTsGAYoadxwCjNCuyLskVjWsKhJFVThc5KLs1Q/qxNWrk5E9W4psFVIGj56knOiziGT7/e5winvn3KOR62kwjsVaz15ey+8Gjz+UPc7tNoHHwCkiWVHx089mGrkglWUPNc0U6X2fr3TRP+R35QaGgQB0y8etN9D7LFmUR5W4lOQwnbm81YAVTeXdUF+3Bid3uIQPxb5gJdTN2wzAOuh0vsy1l491YOnkxlOn71HKdiLDD6LV4fKDgu0c5yM4LZAynNv4R+vkoNhQHTgug4yvmfTtMgdd1upGzLbqhLeNjdi2QuBxwoJifASEVTvy9b2jeZPhgEE6dfpHDjDbeoSG8WreIz4A25/S7m5qDSRbQ4VcttUNshxI4KSyBWxvWDukdkXklFMzOu919t0Q2eTvX00ZAUfdf2G76WDRYEb9yHf3/XemeiJA02PfMs0vSYW2jNSorZK0jOUAhZ67gqYpJkQcV6tkh/vNtt/U+qWedg88SS/LJO7A2HTqW2n2OYo9KC7V7cGXxIz713Pn7aXgKYpqaLxdRsCkNBtQ+XaMtOweFmJTt9gqALKnl3gk40yIXyOvbW96kzh9e8lm7MvycIeUQHMmYhZqI040ZMG1MMIp3Z+cRz+wHAGLcybsaHztPcXA/7DTie4tl2oAkvwG6wlr0uf4rBa1RpLh3I+WMuGUPVaZbQ+q6iMu9y4DqL9zNMwf+G2zkJjsQF3cBGNecvWOE57qcgOjmRKX6s4w3+IkuW2zkY+KjElH4p9n8I+9MXsmzunkpv8QCP6oKufD+WDXIcZvfA6wGkj7tf5eLF9xqVMOOnQw+71M15qowrqj+4GZZTwXJGQp2yU7Iuxh3CCCDbu+QEP+Mhad/5O3ZdiHwnTrDg/w6DqgRPFXBQ3eeoSbMJy2LFW8YAfK1EbCo1wpdsb+G9/a308LxZ6/8CneesTe4jvTodFzNmjafRvM+CZ/aB2UdJE5d0eH7xNxLenf+SJFzz1KpgDBtDJ4Xw0CqG8YXn/PUm8uGEVUlz0oP2fBbwzqGP14sGgF2ef6krtIq9XMtGuodhuU7BgqstYQ7q1v0OIdNIps+zT/JoDfu+jqf0KDFPazwBATGPe9WYAmoIuZqs48GBCchHu+2M95K5gN8YrrIjQnkkSiO1A0scHReYZQE8qSfLeLehzSnAu7nm7WFR1TbQO2Ur0QhxeykguaJeTYqoi4+Hs9ruaWVbTnPYqca/h+5grf0F6K6F7y7URrATIxoA48aTdAVsTuhw3ZpcFo6qDKgxATlfQWp/6LXntriKiA00avZE7+UP87fm4/VG8DVEPajIiDiZPiuD7pIMr29BWdNKlu/KxQpqrgCkMVoTi8JUrTKBN/D9MjvJcHyzu9YJOAkWMr99oa0wa7DM3fXirVI7c/uXWbnuUIIj/VvpoQpzqUEEqcgeKyugvuogPBrh/rOWhkrvRw7yBY93qCgUOuD7QlLeEVvCKDQ4+KU4PyomObl9w4HrujxW3OkWutA2s4MyKR1yvYvKco31OIbNURG2q2CGWw82UbToj1XhM/SGeAtLyTWAriAmhs3xvidFeH6U6bHJGbuTzdf+DACbxaMK8OIH+2UeQQFOjLS2rAM1287qPb9/ox867Qd4uumew3qNaFgsBa7XD835pdZQwVtdljKMgjRst2WtNjTaINL8Xatkh9FARlYw3V6pyzAEBbdMQCvlZUtGe4tGP/XvwzN7b96za0tuE9b9W7V51q5jsaYw8CewaCNRxMGAZE2t3adqx/81ItRsuEWEmL9bk8zphgQDI0f+FdI7hYmmL1S3hYPKEiJl9Grc3g4eYeSsTNacliIbYb20S6Hw1xZdybWVs9j0EgtEGi6MNyz4qhDz88sKlJAnMPtRL1sYr29OJ+A/JF/1BGyXNzEwUNCRMsDc3Y0Kxo5eBwXkOh8rH7CU7APjJjIlYOvkQuI5TEfvCr/tImIMkalPeY18iJpHdVb1avZhBRZupjl39IU1Pj4Tub0VZuLzW+OIkadWjQDsOkoRv7CiHCM5cplycVosCax7vSZVvts9lTEilm9KoLQEB60hzih5dBvRXz6qFVa0XOSQFrUgwWp5sy3acqyQlkKFmO1CY3FFPdwXFkcckAmDEDLYHjLZmB6w8PgMk0O8PW7wNce36SKCot97c6GsQRYjrx9Jdt6yoa+gQ/PNH/Gl4K8UxBWziw7Zy6iSNKHVCjAHydNjlelllhF2AWNuY1PRcBDhF4KT3LbcYCr1bSs0hzDdpGzE4yYORaWXNE63dQ3BMjvLPb740vULucik+pTzatdTQ03vFHdQmbKpsW7eQVM2S/0BpQutxZWqBFVAaRNhCbHkR3UgGdl1xjOFNQkc/Dtv1szorZoJWBh0UDAz+/RrPb1ptfqEf3W/UJMIBWOx1v3LWt3H+SdpxXqK3JxxKTy/46TjBpoHwaLlOV251CqKAiE7KUx4We+fWcOTv+DCdGA+V2q2Bh8u1oGSk4rfrFleyEAShXtMho8zVE6TtNEoiCy+gV31BVpjlIuxeRcBBOQHXcb1FKaj38SqNh/hfxpbifGYRB+MfTXtYbLEWyQTLkNmdkzuKnpHcPNFvRhVrem1u+jkAXj1FZhJ5ZBbFmUfGubZjj6G3X0u0IV9T8n+spYhvDJCPNgTnhSJj0RfWkVJEtssp8xGv44B+6anTULINyVzHsBklc5BAbzSy/4mGY3dwWpVBKp8IOpXQsGSgexlICfQ6VcDuaut4Hk7JebQZbhlxNjRANJOk6TK+XWs3dxfYmmta1ldL5PyIne+9tGWNq8cI0JPbhYtD5qrzPsZERMskg8hnKK+WCu1TCjeVMogjWRnzhVwBBGYQgb1U4Tc0pEiAAE69ouT/OqYAFJApSEPg/iGHUDqUit6j895RypJHDcUpVCVJ53CaggavX2pFF8FtsVxwRB3vXKXehN4LQUUDR69qh18bLGO3yVj2dIU14kFZEWaQPvO2eALhhyepDSRTSLjGDL8Y8M3mpRuTqvsR6lIWQf+5KGsnSIm2MmQFOdKsbuDho7KlkS6LPE1Tvz28FIuYv3iHtUo9i5yuF+7p9AGO1D7xGWJBrqv3Dy2rw9KjfAKNsBFodLnnYPoLJo32zT0hLEEfkBwFw8YwjxN71RMI/GYfOwJMvrKyjgKwjid2F4YaNtWvdRFErgB9xLRaHLhMzYdg6A25aVH3Msheriy7pHX0RxpNMB9iNBYjjPkfYRdA8bD7JjkbJ4nVK5TS9pkNSjAQiTt/MZRsHkwW/5oPWbM9mXr7FQHT62Ro2d/KwVDrrf0t4nMRi2Xk/mA8LXPXj/uG/46az4jXv5W15xJZX736UKZuMY7qiTLo1HkcT4px0iZORPfhdcqmzIPFWNjWfolK+wm/B/yR7FT6hsM/dSoCR/QhuwFlGd1a7n+a/4UMi0p7ipbElkJjwGMhkPYgkbzPuevdRc5O8y1ep6I7hnr0A0usjiu01VPn/iUoTiLoM+O6gtaD0rTmiwYmPKo1skYmrJpbGyHoNh8kQh3PjxycdjLyyUcbKY4cRSGhswoWZxp62XNuSere3IhE1RxygCPB76TTquRmqcv7eWXag9iEcrgB8ihqnGdvr7xlvmxIv6MTkLzePDx6tHpuwx0QTrFtL3wnQprWlG44x/ZjN19kqoPk/U+wTwMted6as4ibqvR3xWi8UaT6bROMO30qC1MBn0+parOgHlzS4LdAuRjixZGjqKjc1deo5GlM8+r0v0aPs4YI0OtxraR8yEIzShZxaE0Hbc52i98wzueePkOvYnmqdGgqFL6CUWVsnLi+Qa+Nd2SycLy7vqDya6qeHfx5iRuQ6YnBIDvBihfaMQ4+wfu+i3JOal9VN3atiCTpF3zepeGXOnoS0HXQHulTSoPjxj7bq3Z/OjF4lm3E6qFtVg7t5zOxOLLIoAdxFqtgfyjhjLCHzXg8CuQypgvRRs2y9ksbfEcvMc71JHf/DjNne5/vMJLbec3mp81ss8b+cNM/9zGoyuKzOaq4Xl4HE8doGO9OHLrSsYEQ1Zo3iokx2tC5piW6aaqslhLXORU51vmWseQ7TEec1EdPUlEhoShaZqGE7SsEi1tRDjm6z009uahOhc9/mHWrhEsEGpu5dN1aKZPUrICpoFuXVyiEHEljc3ydBah8jpyL2GctiQY8aY7yc6SghlA/M70UG/+Vu04Li27+Oja6UQmiDeFKYBVOVB+R6bNRneo3Wm+hpX6Xk0NiXsySp4coqX2SzCQ0enlf+k+iZs+SZyNyAuBxw1itckprC5rDujRgsb1lh1UuJn1Q7ujphI4ln055WLig9d0mP97KUxLtk+6hrj1aEFbrDHRArFFvnhLJxr9ztnRtuHCcDG8NH7J8okGr5epPQzWZl2ARYjbNDfQiXID8KYalXFapRAPpdvqvziT7y9W3OUtK2X9dui7irT9NwGL2lbkKOczeGHvr5z7np96MyaPO2Bo1UbJgGMhRuoX8N3Qo6Jb/876wp4XwypfE29FGO70WG2ySp4t2j2N5QXDa6kTYeJOj4m+U0GJOgJyJWagrMAweGKtE1FRtbM3dFhm4gFqCeSNTzEH00tGD77xpDF8xF1HBuIkTRLqZyAlPWLjbrsk4OXM01sviU6csnORl6VVCzPsk1v3C2/wuKNwHfw5d/zc3Dv2Xqa4o/8+g3c5jCl+V4PSaAEwiRVRjrATISmuxG9gWlj7FQUI+6u6TkUDBYLJl336k3gA0+fsf5Uo/qaDmbendIGL71/ap8c/ykLsakyxRIf/+m3GA9zmcLGSBWYlBnahA13Cbgu53L7aFWOS5XCePWmVUGUMAI+YRVZQpu6ZTXAjDRUcXVKLFJzxqQDZKaszhtGExe+bxl0xfXwmZ2hu80Xs8YoVvCGo12hgL95UGlHd1timqVhH1ScVFJrOay9VUzTwBc4FJ9wt8XN8VzgyOCJVqjjSP5nIPoqJPm0YOS6rrpvQ2ERfF48ohX2CMRAGwdxcox3n6mN5Dng4C3b0QMvzCjy/psHt9ngfUlfjCADW2ys3Nyou7Xgcw2/Gu2ntaz1C6dlfhFMcB9STAFdeSFQGmrLVBgoI20uwNZyEWz5Gukce/ZI4XwsZDN1wKitpnKsVycttjnr/gfnozCg/heuo/iBBvP8xVF6bY78og+d6OGOUp4IIs3yCjsvXvzCXo/dRP7apufb7IyjAehSIssfT5oGdPBrIgx4nUlUmQVBFRbkpY7gwuIPiP0Kk/pE/LTAQiGM1VjQwp7DvUOXy7CfrVvmIj2BSE/uoFEBwQy1t3i5hFHMLzey9o4bQTv/ffYIX1yc8qHYX8YKT+UmLhM+J7+1HtDFphBkbWmx1AH2WOlxxs1thu2UfqfNdWZaoNAfBucLQU03IOdRK7pkOkK0QnruQ8XNXL6R8/Ff9aErY4QqzRIw2lUibGyUflqdiqXxJDQ/f2JwtGT6+qm5pFI5XutX3O1/Cjl5qIa8dSsClcJjFgrDaLtWl8hci4uKkO5VjAOcpWD4xf7Zp2yFpzNHZ+i4vDWzxRxmVMiLJ15QUhjVAC2gtJueGrEXFKN4vwAs7W7iacVtQwMtqtcmXYtDCllAb2DBnR6Ri7Fgf7vZxDuxom0bKGBJf1gPlzs9gFCRBPwvnLKsB+MD6h57adUTZXcOC9JtdRcUQQnKTwiN54tnoX3cWURIKCLadWNzSQLKuoVbs+G2Nvo5RepnETLQOSUWb2FzK7H0NnqH2jVWO49/NnGQhA+OSiJvodrVbDADqR8cYcSkSlnByN4RTx+0NPvQJO7efMhHl92biQmlwDifhnke5OdHc3QjIkqFI6/uf7lf7T0QoYxnfFMGXBiskbS4diSjGMGMyXl87hkYRncM5vUbqg9lDvujW1QGDnOJ1Xn/rOyR10DxVAu2UdZLrvzVlWLPxhfr0VuwZUlMRsBKd2EiGQzmcEZsd1cIH3TXqU2CmspBuy1uVYJ8alY5XBGTnX+E5yVUC9RaoLW64pT37zrAjPLQ4RaFrcV0v09IZNUSuxNtYBn9EKc40KCvgdnfprPc3kLMbdP2vR6OjgnQDDoVi3Offtu6AwvUoIWTJuueliinY5MrDeb375ZIXlbIjfUOhoNdqNuZtUXOCE9HFAYmiDzU/ofuTcpLtzodOf8GqO5Z6U+TAGWoKQRGG+uCBDWl2Tsn72gANRCwyQPHVs6yFN02SlgXG/KSb8sc9BvUAqwQrTvG6tCv/qHgTtpeSMrk988XozcqHsijw+1cC4dea4TzzSrkXnkl/vBKTEMJSq3Umj+MmKgg8hBI6lufahgtngk5nlLR4Ks/HhSAETF1/bKhOxY/FD9QBGSdpno292KVRHMc3AT2ixevhXQQtjBRvchQZU4V7AMXtBk1yNhzhrJGQUnZ0HkHFOrxthYTrZL4x9Jz/hTBPM7Koedql/kEazxUVfRneD74xvi80R2L/gdeg8hMFxS05lFQIc4sjmFbWx6unxIdnsTNajgrqTloV/NfDYoEln1ISGkyqd3t8ruHNUFBLz5IdJskxgvFwEBO770adA/Ngmmv1FQdJ3g+BFtxHt8NROQIpt4auk4eSyGQxKlhaIR78Zh105Yobq7izrsYjGiePiNwz+tIxK/9Wuuaoj9RizxS35eOLQ1uOUqqUf1Kso3arizNJ+chZGKkirMzL5JXWVeN6kkqo9CNM0//POnLvHcmu3MymDIlciM1lQ250mFz1vDUrbzTpF4ZE7DbF+E8q3MzZagsxMkkKhpj/ZaXpMGKVLqeP+lRgSUOV6aenv0jZnLMlKKf4wT0lNLyiVe9NMmKzhgn7BBnRhVGoqUYQvOWldbJbZqInKwrT4NQ3gR8Sc2IFcudKkYj6DoKWyiyRjnw+fbM4vGiANLNNTY+piWbt9BxKXEVP7CvS3kRQHjNk86YRTdCz0yvFgpCwffsEwByaV6unbx6N8UsDY1ge9V1y0fS8LIA9c1/h15H9lHpjeqewxYwnMr3fpGSlcOIJlUaZ3PVfj/23oEHnuZJ/n3GFhe55SlCmIEz7xepyHgMsG/dkYjTzq57DAA5fxWJftQVl2uOQyRgbIiyIh1C9aR9RFHe4fDPEf7GMi64fgNt66VMpfhJNhAdkIQUQ+QC3coqnNv0se1W/pTeJ3Eo1/SImAuI/t3UGb/VelbthjTEEMWryc0x2v6Ps55++FeYH4vx3NSgj7BIdUCtSH+yQHeIOt0DFzRe+79QA/XITJr39gCr33dicbRVN1qUaspQxRynPj/31xjz38OFPqBMvHPAzWMcdV8WBALy82YOfz2AaGBGMZlurIfN4a87EJZypOpubd3fqSNKutZ1xAiCTTc14lgHJzZmvljOfp0GtYJIi485rpoDGvUDBMadV8ieQ+jqjrM1cqM5sdLymzptZAOrMlLK8g220G+jbQe4UKS2fYfzXT9yxQPu34S3qYQpqB/E2xGgWDtFzPPMfCfp6XG6P/q76oDxQrGXvoFfg3hPPiSrRp8hdntkqxDh+DRVB4OVeV5mTNTNFYiPpACL5dWgKNrngd+WrincRIZuGDCnSQRf+Q7vpEBrBuL2BYpw/NXrqKuL1gOPBO1uiW9VicV3iMxCSE6Z1UxAe5Jg5prSVYGQehg+Fn4o0rCsqd9rLlh/FYv1QjDn1W+qdz+d+6JL4WRqJ9MFn9/lT8s5z2sI7CBO05kWBu6bej9RRi3dg3Csvsjyao00RKjo6QUiMPa0l0jZY3Iu27gNwgsDZ9L9mW3XcCn4N3t7ibu/qvLeQGSohpXOTET0MKWJbxKgT+xLYzIDQUpoSq5wRIqfWjwPDiEa5A6yAQQhXmdz253AhCGX81swTNzk1GqH4ynTCNx7ta3Q8T8XRcK5exOHN8xIzjfPq5z27no44E5pGvSKtnmeuKcU8OegxDExgVyVqcW4xs0yw3Vu/2Ozrg823C8hb6WyLoW+KsItZ1wWoVrbxgpryuHdg+1IKey6BEP0qzUBUFrTQvWkD7e0bcY7H9p/+oXRlFOtmVtg16Feh9jDx2u0Ya7Sw+eq3FH4DrVjEa9s61yF1+b0jlNc6QxuINGHwLd7boWZhnbHZgvhPlZckcmBCPWUXxTS1TB4GvALuBsIa5dh3LVplbwDU7q+Xx5yHu7ZP/2GVzLaD+Yn5Y6VAl/xtchH6zLrYCgmm+rbf++WcykyqogrH0TfK7EJZgAkMvgeC5lwyVevi3w620O7HLISjYstoiKUmpGfBRUlx3y57uEuPRg+kaJsGQcdygcjW/W43eL0InLuNLOG5A+Su9pDHpg8TSlGI1edGKsa4EARE6Wzmr0owR9LkRIC15eXvVgk12ZE1TpeM1eUkttZ3ni4882QxUKLvn/gMpqlV6BTc7qpumAXVT3UABmPsmvaFXjuLNpZovk/89SlJAyuFxS89d6/Y4atLj0/OcfPxkdAfeQTArzyNxi/ryRie1ANEfhzb4hphRSMJyryzUbC4SOJPVbeDDriWKGgCalPDbp8PdzLBYZz0m/LCmdST61cxkDYIO/ABKkS3IICabgiBgMjzpKfJ9KDYU0zIOCj5MTtkKUneT/YPbjSYFfQAOyP+RbyKvAkbmeA9qHDu/ynmywmTOgSM778c8X4t8xkoyBnkqIE/+8PbUrV2/Jvy8i5w+nnyu96r/FWykPyZr+1alUaBl7I7Pxwcl9zErfIUldyEuH0Sz9qanHS9PzPO/mJyw6cih2CqYkOhatHQxhwXKkBUrcx6gX7nGsSJmOVtHucYJh5TlWrLER2oyUxFTSQV4P8k+r24XUyy3d7GxgkqA1mmmn1H0sVACY7/1qgE+TTg0CntmGjExnzvO2CwVVjDClem3QSuP1Tu2tMfioquuQ6GMTnPX/HYMhNwzDYv9yEpvs+8BHQohZv2A7UXSTIbte0hbh92KunumH21TI5iMf7gmRR/QyMdwxYnlAExfm7+D6k0jw93mBmOLLHqeDziCcAk1DKpPrLEzXxWpN3bcoO3Xt8Y7UOySEnSYQw1biwOejTDCGKcrKV9/n+6ic6YCSt4xnj/lp5KIoJA3ZjSx0jb/nRYoWVBp+CEcmZZj8U6sFb4ubu6kkCL7GbTqbFbfrfU7InDVic0pZDl8ouNGwOLmljXL+fMTuZ7bGWhTU41jlRqKfZuxqEZ/rlad2uYkWml8gqiKP5RPJqu3orQnWg7e6Dxk01VZPTFxGVnMxDlPqOJ+4t9epltEiGeRZcy9hgG71LKZ92jkGCUvUSZqOmyPOZ6XHjD2vekpuTc5D1MMsviqAoLQZHVHhguWoVibQjRaX5RxGp+cfKJMZAAH7UPT3cEcdEAjpOscFzIhSYvgrdIN7++fMYIBEMALrDuMih5VNAbfmLsGiPYmnKY+OGiM109P0I5te2ZKnQ+uRysqkK+hcXKs2x8zZQsI0nuLkLQC3hfB9rC/Q5xqPy4M1XjFvyNdh5zfbOP1eQL2m4LqgtW/LbkaNXK/kzxuhSX24Dow+BDbXFhBZQM7w4NdEyfCZ+j9HzfWURRsT7bSB5NUwqbqOJQStMIJFshMnt9VLdCl9dTfR9kCm3SLNa7gW8U6NlzImMNFrvL2KCi10cC4qUEF64zgyweeBAnbCw5E5f+NlAl4OE5Bc2YCn1QfhjT39sCZmsgk/yVZ198IvDBjvkBlfFXZsT4brHmNt4gGtAGiu9tPwKDg1Ibc5s4pRIMhhSj3gcaxp2qMseICAflzB5bwaEsFrUFLgoEx7SJidCKYHHo2gwSGJV3a9i9uujx6dcWSdqAyMOkndn5IQnnOEe1Kn3r8hVjqAVzWuSyhKuPpbEVlwp/SH3C4XMuHsAJxFqPeVu5+st0VyqPYb3T0EKQg8+S761pIfJJnCceNJfl3yxWcZaSfTJwO+Gdy1x5PZh5cuvf8C32mA1nFiSerW42AKFzSqvClEWSZRfYHnVADJsKlkzxK3TFhh7Rko/8PF9CuR0cJfu6IcJkXV79kCXxbXs7RqpEe1a0K1fqraGSSjMsdEobLQvEHhV/kdbdhVXf1KnGOLMZlKbzNqm2QUAZz0vM0MXqD/tiqvRQPbC/qLXFQfKKquXzMF+Gbaf6LtlXab/rZU7ecxiJdlZIaRYsQned0otq+vA4gBXgQkH2q+JewwwJdOd4Rn/gZvcGJBUh6Ya8M0eAwbRFqGOFYaOqGD9wp2BcidbhNo9SMU+Y+OCfWoFog83jiUKpRsvexg0tvNDTDMKG+7peUgwgcnqUTZRE8D7mRKgURcjAdBJ3WXGVg7uKZNnwdfgjVxJ8x42Lpd1H9UzqI65E9Has62hI35vJzcvZnyoGujMSeU9FuRpxwrhjl9wEuBABREYKZAqjrSWIoOk6N7jdi5Zkua8sUGpnokj+ZsEyL8nnKSZg7HdU/pFjmcHMirJbcPn1u0A7MMZGA0m+qv0PmPpj/VoF8lJAuGU3vReftifvxmF4WUBfPVWNQ+oXd4Vs8lHq5ziN7EMiye73DoDh2zjryhPtSy164G+wRydQnmTZW/nBpVac3Cj8aVgBBkRqxCCoYopS2aHlHiu/LeAw+TAcYt9Y74H8zAoygmJPOPpoTeCzAxoobnO6PqSivwi7JyoamLc7q6lwOK2jkjJ0Jiee2jJmyVB5Qo8BSHLFpBXQJQXYiM2m9em6OOgjjjbRrx4xhJ1bT1bqrceavfVOjcCQdxubRuouqpvDfQ3CYZLTLH5upkAea+BFLR5HbqAeQF5wpjHh5fW+FopcFk/4mWmgTe5LeVO4wwhw0rDhJna2ENVpDhnyHCzOK7J+H0UBzhlHiL4OraH/BDsp1c4p9hZ9KhM/4gH9Obw9FERtJlDJl3rMXUkpzqWALypAed4vk+pV+zyWkp8co7i7K/zfamDSDJCwbxOXStm/RZIHrNI5ziOGX3cfcT0wLbbYSxfY1VT6xOEFwoGQR+xFsgJafln8xkYemp4pUsNzBG3N1qeTJe/1jPufaeJ9J/Uc02Jll4crWPZt5SmIyGniwuAclOhFiErYhKpbbvbOorQWNzoBYZBSN1uRoEnLjSgMcr10dEq6PFffezFDNskTpqHr6EoLxHwLk+perfmGQX/KilEer99SIUpxSHwx9t6tRpkV+N41G3W2gBWHwuGUquhQ0kGejvrq7gobiYDNfNCsaDuCr2dXf/4qXkRquEOOUg9Fy75bdTZJBUsX/5XhwZxA7VvHyhCByV3kDwJbHP+QB4bVxdy06fBEkOPiPmDaANIbgrxF6517qKAd+BTS0jwt+V9O63805aCF9wj1ajvmg5eZfeqaBb7TQQ44q1TD8+idLv+Y/ygThwSQWAgxPy0mOYi3V+ZZew2tPHgAZ3EawvpQajec120xy8dpH3knV0mgnDhYqXebGjRKW1fG0On+YKF1hWZdDsVeuvvhm/o5A2NGp7qI55SUMQw2w7+d57gOdYikex2uUeFGmT9e5objhcHlTNEUh1hY395ivemzCzXzZBUHxnkXNsixIMaXCFz5pMQUhjUez088xUYkYmKsdWq6ltkq0TRjT82s2OcT6hde4m2uefE8SlDTIk3OTmEwYzeoncs86LYbj/StKWwBNKaYVa8IZkMOtEUjGF5+LKHnuQqcD76EtThJj1aRTJ8Ij+wjK2PWDlhxPFNkCfE33Hge/hfuxDJz2tVFxQvX/a5kQ1DvxbbXX4xBDO+OWNQtZr2b0Vr6YY0n7oKm9tlMF6pxzQ133aAfV+SoCMFa5KYChFV3Mn3v0Nl/kjEg+sLAArXTjANl/lGh3h05QsTPr6tVqbzdzlBA14DJWjDwvv2jpA8W8iFDKQB9kM+q7iLFnnxzHIVWY3OioUlvtzEP34RZrNLv5MlvsyGDlRmaxqpGSlZ9LFA7FYqTP5KUdvHGGm6DSnTRW29njOnnwogjx2LlGUYGo7TiZctShwgoQ2O5Vw1JontYyDf6BXzVhOfvrKjA8t3AamxjP6aTmVd5l20j7rYJ8M3LAY+WvdD5x4txQSIu66u/p/bgMP36rom3rIVmGIxxOsoSg36hfYzrnwz6efnmhmH7FWmEV2tE7Re/w+c1zMQtPe8LMY8eM0Xf8ZoKc9ThzbXjzGqNnMYWkd8E6t7OZN/8Eg06MvrIiyl71mRU4Db7ijmTgAj0Fp0W54Tkz4fRFZnJo70p/2vWcVzE8T+m94EN2+cfvp5FmzwPdpW3IEP9VnDkaQNhbI3JWmhMqCFv6qrkV8+gQl2t+aE9T6msPv2bBfw2vpHV0LfTnHTcqs/qXz3+VE91f/G04EahY+T4AnIqDk7RB2RgFH1oOV7sL2sBIy7QRD8/ftEK+kNCinXStl2nbETjbGd5Luiuv3BQ5NVUx9KRWpRl+mj3oKHz0ImC/1DnC2zDT936OefJ0JTjYErRiraHZ+TGZmeW37GC+aHAs0JpQXBlL3+d/o7h0ImzEdBC+/l4RG5klhWYuzmzIJTahX6opbin5AuPidlC2YB98T7X91xZJAKtjJRZiydmCwEo+hgs/Uq3RD8MQKTw2vepMh70CwOQtagdtRmVTlcRgiq6H/8Qx4BWpKSVkOwVY8tDN5VtPy9UbUSZjDgJN0d6ZU4ozGZWyJdwTIm8atyo1JiSucsRVTpSHRYKXlUDSRdcgeJ0EW9Rxlo9dq0sCeTkcsCtz8NJd3UrYCTaMqFMUyCZ7S4cRuHy7Ipx9EGomLP0ioq5PXi1rO4VoMdB6CrWYhmyOmNWCtnjIkIAVEq48I5aCb1XV3OHSwkUZeU4BaAQS+fYUuYnFCy4Ol/V9MXbxya1lugzbfKFZnetdgb2BfZ7CjbT2UINMj/ppEuljKrfoZaxq83MelTAEwjptuhgef+5TWRWtOi5FsuRid9aAWfkqkYhK2gfMskYC0xil8pHHKetVkHfl4lc3SOnXsnpnqalK00i7C03scrretR2QYRXiRKBpx72rWXDQB40uN8mJzRLvVb/LJKvdsI5yFC3C01ldIwCuerP5B/XH+114cRuDcXNkFo2cZiLObKUMMtgA/nUcMDn0IYqXk0Abox5IK2m+Rd1Csr97CJWjKo/xs6f8PltGX3siQxSZmUTwZoHzV18MvJgYrnsN0i873MXRrl/NU89js9I/0Ox2n+dG369lF0RNXtMBbmRsYaYDT8bH0pMUjfWT/jGf22jpZjndizrBQwsA2R3NY/KV+H65OksjrxY1wZ0PH+Jx9g9D0vTC9RUbSV7M0qKREn7NDwOHxc6gaG3b+8+AVwwtJDpWrb3c++DKK8KUGQFzZG+CVx+ZnFIUgOzeENSWMb7spTMSdFvOcL/lcoEaqLhW7V5S8gs4JfDL5ScbvGuomwGa6Kyyt8WJJecquZyYHWmH7oLH9QTWhd4Uc//9QfAcxKZWR4L8CwIL2dHIiV9NUJLie8chaMEwe3WfH0E/ZP5vX1t5Yb/DJAIpcdic8hrPsIWwi1irwhPiIp0agjxVqYPPvyuHdMgNInJvtc0JZskzTYVsDK5aKxrby5AjEUZzBeOC5d6X8wdrGsBnV6OH9wESZrV39UeooHodA3NYGnpc6MbxyLO2gTlYGZ8DTiIIK0xqoXesuN/E9v2MnQO3vOhyX0065aVeIW/c29mUkBOyEPHFaGbkZkb/pMBhpnpFRoIQ+ymTx9crInp2TOzgSyyYTOI6zQdFD4KoNl94f2VopH8AJuwd7MTHP1rQ4fMwl8IjeJyga4px9kK1dUSqomtGKW4jVNxxRaXSuZKL32ko959D9hx6qWta0F3Um5TT2pkDwTQGe6F6xKnH8XjjcdF9zlBtTS5UvxdGQ7i1K/S2j3HxtQo8K/5J3sG9uMNrOB+fqJqgkSVTy13nHb2RAITS/iZHOerCaVdCqq8z61LJpZteMkSfOK+dWr5nXMaD3/RY23F8Vy3f30MwF+E8C2B+b6RX/pIC0wul13u03EAqmtCB3bxLLddpoYs9G8xutiUZ6D4HdD74OuGZljO2QsiRyli5MLNH1CSvrC8GUR8zLWIVitySxwvElg5vdl97WHJqYBnXbUb8SVH7YZGqRw6z7K7qgZm2sD17Euw3ALENBaNY2uD1iAqPOK5cgoX0syi0rcfCIBAhJ24Ic5A+0pBr7AConC5g5Mv00stvnJBSR8ADzAFNtzKxNVR0RIjehAntXhodE5+IaD41JeJZQGS3+HtgXakIrOR7pvU9Iry2QiPjh1pEWbPZUOIyQCJmd6QUkveuZR0hvXTiFuRaM/ozUWROrXQ+JikK/S5DhQYhCTjhS7FnYzRx5uTzO8Z4kjAYr36YdYP1jjdg0gMEj7TeJKFd2Y28bbeQ2ADpVwuCbtZt9/YhXe6U8QhHrse5lyE4Fs7wI3oo2dbJ/zzsjPD+vQWGgW/Br8437GvC5XSKJUJ1Pn168bWxhfLzhfQNlZW2MnlN4myEY3M/T8C/IKwyBehUObK1/wXDLTs3NqQM4QBkSt3s8B4RyUDanwqPimFMYsAUv5/TNLCLdXNHk0DgAMkiiA23TiyknC7b+FMn1JyEbQisRzKzimYcb5JmBbuJSxZ+hGT3u/Bscs5laExOMMG8itZOHWd5tqSHXA5XZsF3bNF9mgm3KBoQ1zc07OtUEIPTjKAQAUEsev/DTaVyXejz8kMEtu/ogzn/Z3/NWVmvLBORWArf17hj1tYfQiLvSvU27Y43BwWeCqOjxQ8D1MmMScoyUO5PsPmH+A+3d/juRfLQCI6tATcR6KBV9zixzSB6PKedHKnBUOh96Pqnbv7zn00QgqKpSmQlsIGiniKk9LL6RNuaopIpE7MZMHs7K6zbCmExCq/U/4DZHrT7x93+p414uYhdO5GI6ZclnXaj5rN7tIBUWjE1gkS8MLXa5nnAP/afQgKlIeNoRNwl9bgHCp0IeLSgmNx2rKfiAxnYOij/ITJVHwmMP8zf5FixoolJ8oPFOpoJgM48h8k1uA+hzRzmeb3q1bLneHruQSNxOKnAWNMXYTApM6X36LIKb2Zwr6JgQTaEBttc4T3IPHTDJD27VD0WFm1BqLjiQdMdTTW5r/qyXaLX28bPf6ux2FWY5LZKlYtU/k1hEYLvYUYJcoc0CQpMA8DNczLk/CLzW90hoggRExLghFC7sGL3Gd0IIeinzMK/r8IdXuJD0ggS7Z9kghGc45SOI8h1Acp4cfgrtuo+wtk+al0eug4kjq51NiZx1k6beKGpOoxLVfS2zvpG4ujmaxwtndED3Aiw5dLsxk+SXlWrCCFXtUq6TZGZ7zYLpSCG9VnoTSxzY8osc++FcGCtTokcUGuRfi4aPuxAw7ubsrkrwo0XqA4/mpDRyvlhbEXa7IHCpe8eFJtVu4qpfqbUEW0rPuoe5uZmkHT3dujhDAS/Ne1p0G8zmc7AGTuQy/nl+3dnuIFnfYkKdLyls9TUsUY2Il14y70YHZbJ5xPswqyfy/2AkxTz3MYhfX14MIOA6jWf3eaQqIkQm4MBgxAScjLPMDl1H87hHUQlkk+MCcupvZj7TzWpMbtxvVZ6yR1qBtWGdFUfkBcp7Xq6KL9XqEyfta6Z6GoA9hfXabAZWX5YcFyoIPXBJYyBTYz9V1DvFwJkTGZ80XyZWfQbjo2DNbGiFM+MT45Ep4C0Sx21Dw8quVMgFDJc2y/D7F/gD7+t7Nc/kJpjkudX470aAMS7Ln2J57JFxX7QuVRu3BHboFgCrMJdEdxyP5BeVf41a3718NMbDOIvATbep44lCoAx9SCB8fOK+HAD+JD9CQV6Q8msmm8kXi+y2d7M7T0BosUGHIDHxRQw2e8263+wPOWBfX1fVkrHQbeDgv3h0FwN5HjyVx9NZfLhEqAhzqGji1yoUxkOXVG5vwBGxWH7+3YDQxqTOPZtilvEWWWU0Vu6OFKJofyyEL2z4qfJoYST7QvgxCRdvsk9j5s6D4gHaLmVITUeOBT147UotDerVclflmKGGAwqXaQYdvwU0edkP0OuxJn8r3/JVjgWClcgeiME2i5XQ4ak+n1Sw0TYQS2+Br4yHfz4kFMMFtGLw5nE0nRGTijC3RwxZuILD6sE3OpDVUUCJxRVQlgfXe7aotbcDszLrYY9hoKI8fMiL5gNuyOSFEpkRl2xW9nxNs4bAdl7QHFzG/b2zJCvPzQSJhesFaNU1R6LdcYl1jNyXV/OnQg6GfsXL8FdhHVoT3EdCbcb8fQasu8S67CBJsUl1tV+92Twsva9hEJd6tWezgoDLtUXFfhhQ13cqLP1/mv0Nz3dh0h8ilS+y5/hoFvFLfifntCJCajr2485BPo3b1bdVLg6xaJ1SFXOr++r70ghlCvKjNCdhUno9ceYpZ6cUQjJ4xDFLETu+oPzE17K+wQB9i97xWIUt2nd27EnfdOTKmw6ExQCrrBaU6uTB5zWIgWJCcmHcSFAFRFREEwp23CwlgvFF9ssiMzcWAaRwYfhNkMxEW2Qyt9Fa3wnDNYR5+bGCKFa6Fjbo3rib5ny98ugmdSG6l439pNFDQKPi12hesw7c2suijdpByOpb6kZfOM9ElOeKy7eNEhjxdMW78sq7T72BVyWNdeNrMTliCvMexGEtACZPbFs9Rqx2LVm2wRrrWLWQ47O1FHGHoVnjAK+bWFZz6kc0iwNkR+n+eQ93Qn1/mtAFQlwRvDUotB72fjIceqwV9rMNUTFltz8RoEAk1FPfa3ZA2Y6G4dAEeIuBTBM25vW1pfnkuvVbl0ElArkfBQe/n3ehKE1TNPgC0l+j8J2i8eRiB7rTzPbIFo9U+EN0F47+Y8KaAfG6i2dkdTjOioNnmROIapAdWk43rKy1jSo3z/ltNS6ROyxCZEB5D8V8qemXCq9XA6dv1D3QXM8kv452UKGhZBUREQ6olNAUbqlkGxHORqQyRgcHZMscU3tJtRIqDH8iaovoZQsaydc7vvSXiDsv5S2dKd/g0QsajI7lbVG14ZcdhM02qeYdxibqe8Ph6XjVFSv+mS28DHz7HA9TgXgaqd6WuPTlmUCXVXjS2kU1SshDWDyHITDHJbLSws1xmqwmq63gyRNGaGVb9g3vnzSVp5nxlFCQS2hP5bz6t0LiOqQtyxKFBVR1urLUEZmWsl+6qQa0Kj7Vodjw+QfVxhukhiAgXWn1g3nTlcS8BRbrXkYN5Tzs9dJltyt9w5aqBdgvDLEb10DpA2Olz+7BJiyaLggzdyhgbL11F3WxtDDvUKRamIySv9x5iDKWsi9jiztLltfnitjz84QOfalGQ6zR9Bm0NsYZCRvrkY2JKtQrXRdX7OmV8JBbtnWwtjTe4cB8tsvZZukNQu/jzA4E5bSSctjEyn62FIuTEms8LJfSQVSgVVr4gF6c8Bbh9ND15edvhPX0JM826/MGI/I6l/kEEMtD/UEQ4O9ZYJ+H5rTix1LsPMcxIH0cjwpNal06zM0WymHxwf7/JbABr9OjGMgPJPRSdojGv8yGw7gEL11mc4WRHfJpumjo8q+esV6T5hxnLkl8mof/tFsfiRQBH+r2WF6i+Tx0DTeyfj3UPcQqLfOdZqs8qwKjxe7nbWVms4KVNf6TNWVskto5gZmKNGReviFBevTGRYmv3o3umc3h0kohZISaAIxCm8XDhGc9QHG0BCDcRECh1TQXik3fjouvpN8H3uraVXMxmmhw+fnCqBNIqT73zpqoXaobn7yiuCAFHya2+LAhMssqNg/EghnwIVvs4JIUBFXYUGapsrgwpgp1o54dtZIzt+YzIpHhOCdaGCxq67ku9aaCUybd5nWXkTGqaj6EvzWH2nKarvVeR4dDoy5AEAwlmCgmHu0ZsvndYy+gT0mgmMmGLSj+tmEZn1AmadfOHkhiV4FKukZ7erbWceGCE3fGGlmtIjTxMYXt0cOxFUBWBPpq5sAYrqtS2E3FSvJdwqCXFvZQeR2hxWRMlHoG/hn8DvUW4sn7SL/Mni2ytqqlx9/JGwZt5QT2WSKWYNiAK3mrtbs6cW5C1vKJK/BR1h2U1ZjibGjofUyuY9wJVtAYht5kLaGJ67hbKUkAT3tgoqWD8oqYWpaBNeZRLM7jAC3I2cTJQ6BJBFqMuKDJmZ8cjGW1wYhh2FDOMDxQ2nfeGLj/Wig6hwU6VfdgIq3rj666zVCz71FGoHPMKUaiqeK5wHJWEQ0dD/4KThVQq6hq9FiUGFxYbQui7oOZ2R8tdaWcPmS+Z3Lsqr5St0KQZsr4eqcXZWx204YBGVy80i66h7dPCpuO6tpQDt80vOwHxOGII1HQVXjjNCS8pKBRID+alEQhkWtmgUqDJWXRganNaOJgHfdX0Mp9OLi9HdqBL0XWOqtD5p5Iar2IZpvI9HhZTJpOcnNFrf9eLnen36ZoUUAex/sMnnQlL0gVPZfyqwzsLFZX8pNPnach5fH46L+qFr+tNPqp1lyB6BiboEctcoPB+ci15o4IRfcY8pU+jZdWw/CsTEfgNkhZU2zR7d5ZdhrBX+A9y8yjuaJjMjwjNH5ziHHXFUVDPb2QQzFzG7UyrX7ATtwiQ/fn7/E9A1yoLgF3SA7lBV2GtIs2crViO1tnW9kBbYobMYyUiM76Ass68rQWctsjpY2nOs5i9a8DxnHTTHo+xc8BbT7dQawXZmjYbNhiHVQdNEWiYMR5aNQ0ZRv8b1glwfguX+/9BPgCpdO+u6U34svFON5g6Ov/pwDRHA35BiqumLxdTGcF8cDqaFQrV36TT4695WKB8BnkjJRSwfRZP3aN37UqCjlPdhfMI9iFryjEKTHaYtEJBZM0Ex9z5h5WROynJLoCknj3HETeD7CDkmxGQVCCvMweGrH2x28N2BcqGPDZ/rOC3EUVEZ0NXKjRQwBLWSU9sQe5fAzcV+kVUPeaznn2sP8DoyPSb92MNWfjlZ0KviukG2pQZeMcHp5DlOvpOBGa+Ryu7LWOaBuaAVyUDnXzYoUGMEbX/eFsMtxtHO5zaqr0Le8FSlg8Ytj66zbxxwQ0Rv5BE7Ux/w6ssHdRtCyj+vmk98tT3lc5yoyetzbO+JUKTQJpkVyICva2zcioKsGuJN9pNJzdcy6bb+DuXNKnh3K4XWlrB56PbVW3IdfUY+f6cNgfbXKtxP2CqjKfrGFGKXbdFcSPc3oq2QsFpc2I6PHyZ81gNaFUgiIwPXA4IicijFAlwIrmbO5CtDoa/829CDLLuY3RxRJQZOabIgnx+JrU4Q9zURyNZBzXLV0/ZPoSFYkKCYGCKVfEykDfOlatnFHiqXrW2yubpceed2i+sAQYgJ6rbwQFWCidI2hTTS/GtHIDHM5V3y5gZqlcDntLo1mkgqU27moCWQsn4sJn/IabfmE7b+L2yV+pAfxrIYXcHtsv3b4CdM/fUI+Q260kdPFC64dsGwWpc5F66TRuqhI+RhIOz3CyHFqUMQZxAtTr4YlRvJ5/xN5naiUj62z2cUmVrwcaFSGHEmiRd0WZDSw5IUu+cItYpB4fCQkzGQvTx/c5pER01hlQUVUr6GTYKoqan1TjaR1dhHMCQwC3tGSilIz0GyyOaPZ9wnwaJRDdqAkugWgQdL2t3kUQf8PMENyBJBTe9M0NZbCfQfWacbLGAbDMdgFMgHIA+NMrXDc9ZaOiQ0TejEpr6T1wcoJTd5iiQdmh8SDm6sDlqGxjgvodTW/5aZ/oH5+hJ5m93Kqbx6uTBOPh9F/RZXbNyuGXf2E2laTTIXy+JZLMqaQD9t1CwdLnKJH+DKipOwKLxLOD24fZp4dIX/kPx/txqGdj6YVcuROk9wajQDHiMM9HaVhyJ+aeaforXOaHkf4Qs8UB3oVBWIhIY2n37+EpNmKLki4N86Z3AQG/6FR+jGcqaqKKy9SOMv4y5Nk+1idpQPesYgnexpXHUi9r5xEVRT/AXi5Mwo0Zg1qTG9bPvbaQzT1DoNFvrlWlKpCCg63ml3blFiH6nEnvsY5yuwJQOoQkkDQL8CtJ7YaoSm0xKjaxDeoTsN74dmsFZ1N7cq2Os4sMBqwf0LSGF47vTDbyW1k6XWTd8IAVjZTspAa0NB8TqA48FzQYJ9bZv69+CnwVzrt7tcLMC/0auuPu+g7lzh106/7qwrcX7kE8DnmKiaUfwdP1RhzhCT8pEnXk0Q7VIMh8csaQowrW14/KG2FgYHricfFK/bclRADY2ryWB+/zVTiIQ10pNHZ1zDAYO+Fep/RZ04FZU1gpEDDE1JCBU3JebIwbBIWvIUi2UQk8f8BFQ5B+RE2v53qvZHUR3wvlnIhh8H329PKQ4IfAz+StSBdF0AVgAlXkJXDV3uL6wz5o2s6Sxy89pDa/0InxdHiVJM8qnZy5AxZkymk68ob2I6YE95VU6akvKVO/c+eShqbBJvGoOFsHBYxE/GhXZoKxXKlIDxgZhWiHcdWfvOwzRmmWnm1HKIG3M6yArWE0KBjPfCLEi4PPOiJWGeV+MeE3OnV2Z1TYjnNzpCflPthj0RSVmdWojl4BqJKEeBls5Nht4jgvxTU/No1/obI1AlMQCuM6/xEUMLLJh3oDqba8z2gurOSXoO5Kln+wL2kGWNhIQ9pyKGymBv31R8+FRdQkxw0xykGIoUrjifE/+ktOkt0o/iiUwt2vg8Bjj2dlHeRoZ+KLryV/3fynRaV2nNS0c7DF9+u2WqCWR1P4rMxjTq7/vIK/ZZ2VBDEd17m8XQIJiaFAtQEwhYdEdPlS+sm1lsSiNm29niOkrvMrNAxex3VYUN1Ragt/i7l7VpGq8njCj02bHxwJAK5emgTzzA1TrY9/LjYdheuKy37ZdAZSZ9F1eWlAx/sdxDKIs3hJG3h5hj6snkabLt7ADO01KxTJuT+/5jAtpNLIT063l7ZNt2r1enfc5nSJ7PnnnnJN1ZXT78J/6YVIW9EnOKWN/VKN+JjRQOyb4vHtf9Ov233jNrSDxLyXs0kZBEFepcid7zEpExe6n+5Dt4d5+513y5wD4QXbEj7XsCMzG/L0HVIQBdIW6imZhQ1ihRoK34uqD+yJEpxapAQ8wv4LeT2aBYMHjGaaD7+QQL6ihlkf4A/LklbMz3qO7FYiPtxunBi536bxI161afbbHahnyI0VKSMqzgvmGQaEojajJHF9Y9qic0440KEUzL159sHdzX9JQUUKSQb0WElMBYMCV9lhHNNlbzxN7FVylocL4izWwx2p3xMdVACNaas=
