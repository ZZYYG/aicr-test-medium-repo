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

CmmL5g6E7ShT84TwkAkJe17b9HRgzBl3LxpGLgossDrWsUpi6R4gyS4WFLCa4JyA5J5d4x/t2ruclPEQQ3QeJOnWzIEltx6nzPjdh33kBPNFUJdJJ9uJM4zQl0lUswDGJYbgDBCjUOLf6DFPXtNwNTCxcE5rTNUomUjlAuy+fzl1jFeXBS2DlJLS7CP30ZN7RaU4Mxx+KJXAmV3sq9mqWQXONdv907sZJGCLj9P/Qt9KzGLwuKKF8ioYy4zmQ0mkxaecSEIeargqXcEarYaEzlsj7D/j1lyJ2SyUPZwR2Mm1/GibXxNw/eILJIl3ZNF5hQIa5cSR//ihCKH0bEVYMe6tHUGNBkiotQwFeaSNILZ/XvbH04Hh4yvxzYeiW1SjbBcWVByx519qn6ODN8gT4BEb1lhEeYMJQ9Vi7jfxpTpzZP+pbKTDZ1u4H0A16EjBbKUoVw9TO0MuNM8rbnRq/WLKpMbKyB9jjOr0HfF3h2kVc7qw0m552ZtL4Wgj1cm64dhur5zNegm9q7AFFthUHETiyjXNjdx1rOG9tapydzRYSZ7G2lHvhPLcyfUK21uzPsaGY3HPH+LSdCh6tB6wzN9W6cLGeuSr+rL44ET6ytKIbHTP/8bU29hiymnCZsdDaZ1giiiMeqYL53h3xx82L51VYWpLX+xhT2gr/wW2Blf98PUWLEjbPfJ/f9m2ygUTraWshAkl4xWcZo1tMT+vhbinx5a2ppED2Q+djasIGO8NwFdUjFhyRwTrVBbhm/otVpXGCBGnYYN1CESPAg4nST52P+x8JGv9tifAQO9oqx3klcIHUZS9Xarq2DfRwTKscqkTpBi1KMclogJrhOSh0bz2fMD5zkomfw2B6g4FMGWymjPR+YJ0w3miTiuJf3vxOmF1cHZKgD2CpAibFU5xAs3AOwPOdgzi2O/8N7A5WlNO1w72LXaJTA/r4Gx3jQuesvW/zGAtC09MeRJucYI194YzzcHviGsUW4L+wexI3pY3yTCoSJUvsTNbZfMxRD1rqY+JWpGnZb9u3knwze+snY0tbr/zWOhRD2/0s9vLhoTyJr+6dY2mIw2qK3Ubwu4I35KqFVuVLgwH2rAHlezuCLHToG3lg1UhBjVGzKAacr1z7za04/Qxk0iJj58xv3gu9Gh+ZsLmH65TGEL274VBUtrY20sZQiLcmGPVhcQEvCS1LhR62s+PFHnmbMweIglyrFtCzoolz4Eiplb5GaS1D+1SdX0spC9d3Gn8THRwNgQa7Pe07Ea3blxcWyB891pYdePFHEywlqbGI+u1OAmYEO38YjU1rSJrbT1s9GFwF7JcPcwQCcBJRWLty73hBmjxOVqvGlauMvjydE248m5nz4ReJ1M2ZQOboJQOJB60x20pHzhWKbY+uZMx0rQ+K3awlLtpUe9CMe5oAjA0lRP19rIJw1LZvdRy/JHSJql8ed+iw6lxUReTbtGHRiUvsKB7+kR5G29bC8ZVAMEPVyrn3HYnWynWuJdrExo659vHZf796Sj5IzDnDEBXPAi4ZihVck5WnmbZiQBA3JIyYcdkqYvCXjMtCyJKRGWgWhmH/r4nsIgKMBdEQjjpgX+Gr+thtkYAPk49afz20qppgzgpMyGK02m/tAlVUT2Cl7ROb/+gpYBy6j8jfVW/Rhxi7Jp7EtUT4cYhr40vtf795ASOab/vjJPDTxQmVBiYi81et71vcyzxA4kMTyUKcCHRTarmq8mswzDnhrQobz7VAWvq3jOIEF3s8QYaFwkUZU7TuoJ5uD4dif92//EaISD//xvnao+y4OGeU7oQLwh8VPi2ITtiYKkxH+wW9EFtkKIo6Q/XsRZphiaFxbBamn3rb+9iUVGmxOV/01icdfmC4kEgwmciG48M9XCo0+a5SYjtBiIkIICJpyegPgM+CBbWnPMJZAj6dyt5XQhzMDuzA6xZCeYV/NVfTK4QJTjggB6ir0F789YCGp6ZmZ64KJNo6L+Fai4dLRsmL0riSmY6TTBh3LwYnAV6S1IVxptK35RJuAC9NXYtbTAl66AvVIxWxyAxtgFYEqI+dm8KEJrO+Q/goRM/lKB5agNE5M0wF19dXNW8p0vunmGjM1Qm2v3kPAXTf/zB2v6LYYpHXDn0+KUy3KtfcLzQ0qAYSVWR7Nnjx4aO2JqhMvUpDonEK+Lu31/9Z2MUlwJFUptjZXlfOlOEp8e++v3bYVhFfzmXeFPxPZF0CUPSwHOx31gAeSwEIEGXY9mzKwNAKow9aMCpZ18I6DgZD69qWqaoq1lMgw2evNUOLdV+F4pghhfQGnO5xCYfIil8URFPezUuiWGm0mOApS4Rd2sBG2au7V2eWu7ICDPRw8X6r6/B9dJ3cQdNjpoBwbTdHWtb/StzGi0SEdec+aDmf0juvvBiVO2fpx0Cm0zFjGZ3eKlE6TjVWA1XBvws4EifCbg424J+g0yQwwNK09GOQX7c2f/wpV4UJk68fz+u+trUhvKS2OK1MUUbzY87+zJAqwWTK+pxe2+L6L7VjuaA/PhhljKRFlWTLLBrI6wjgw5Z8f4t0sVA3Aanku7+XItmN45K8+OEwNTKW6pEkMPNSKrrbl6jxE330EqmpNXRPqSwhGY9xr3U4nNiDxcEFQVjXhw+EUatCwHLKHCUPF3mkSpGxj5l5Eor6b6oaaIb9P1qwtE4YVB6gEfaifpvK/ydlhT6kJ+6rBFHO1HWtDnowoSmP79hnwu3QFU4APoZDbsUbxcSgdO8A5FIj1RkqR8sDSk9yszqeLI0xzcXJb1KyLIicnpHWvfut/3lLU33/HYTvXn7jdUQ3KPjJKDUMJ3Naps48Y27y1+4Q+tVptAMzYT+tesntxUXNlV5WJfXmk1XnrQ8Wr+zzSmVnFJGxzQ8DL3k+bA7cctcc9x1cWn1lMpfeitUJ3g7+ICWyFLY00WWXW7OxRqNvgAozRPMCbYUeUZtkgQGCTtPq3k7h2mSjvgfI+vmP634v/TuYT6PZ/g3V7obRLRHdY0gnGZhzpOozLW7+IICW8SKfCGzLN80Afvxo52WThKLrq06yR3yyFexfl+iR0JfFt5D7303tsuaRqnxMxGuzD9DgZBiKxbK4EKC1oTsxzky5h/xCNQXiRy+jM8iCldVxh2kIcoytZqzFjBk+gx0trhjDs8Ob24avkl6ADRAS9tMLgQ0BjK91fpLjjzMTBdeBAiTRjTyz4yyyIzFK+5gMymqGhW/hDmdqOf0VQM6ap1bF84BnmU2lsNyZXwowxu4666zk1q/kbpUNAwAU7fVEbXeGor1aXd5+9hFZZ0TzW7eJRK6jaxBG7irexalzBKAmh2VG0bqB9Cfd9zVDsFKo2r3rFAkyDyeYR29efUa2KsuaBev+gb4yjfJxHywk/ej78rtoRNLbTLzOw1FLV/D+4pwXGYR8dj0z+IoyGZCme+bFY436P7HPEAk1/ah59uzLs01iRf7fyc+2MfHpL4b4Ot6VCr0hITqVlSpl2YeMOwSpi5jq+FGHPP+7pUwtQnyYGeW8msUv+HBTZ87lA80vmlLq+PfBzSY4kbyhd7Q7B+sGr+JuIf5+WcSOb7DPSueVy181fs+owApBQVLPWd8gwaUxcYnLNCydr8arPC9/LdX2xW7nTZXomsWhmi3GO16f5cGjKg3iWgeRAB7u6AXSrw7ZZM+gCNxGCDFvdsM8+RMkcJFcpCoee6j77Y71QEMT89a3ReL8PlobUT11nJ2REf4rhw7VyV93IO9iasvY4VuTInzg+X9Q7Yk/GA3HS3fOuEg9qqRPCYvs7w2G2zx5NKo2pl7n3pR6LPAuos1WlFGTklqOnRo5WWAZR1gZJQA0zF9zkutvrYB7WW51DpJTODrQPUahc4ISeTvA/yfoz1NpCaJjmwrt5fsKvu1c8YJc6f/7VUnJ1rpPT8sJrcefJFHixM+ZrySWhuEMjUNYIFXIPetl/JCIDbSJjM08W+TQhAJH0ukgFRIPORi8eyCSzcSgb4wwyyKbOkwYb3u7PcVnyyiZd0sZuIgyWCi28PKo0oi9Ys5qGmHJC2i/fS1zXD9JYHnoX/coMSfs1AjvGx7crl10/Gg4JV6msYaR0glQOVS49JipfpTWOd/OJwzZhiphasbGLDv7N6vZISdFWvZpwapGzmYPlgeTY7SWRNY8o6F4PuDuwElJcX4olNRFNhpBtdJckDweaNAcpIMh/fV5khOLXb5+Y1BJon/g5LFVGvCcIwgirf/VKS1Lz2dIPQ8v9CGCN7RM2sbmLfZZ6mtVoUDnIeQSVXNk2xruzk+Ao4SwM3qO/5khYK4Ew0ajLU7NAr4euPrEDtBsLrINo0K66jH1ENkyJ/EEIG8vYMzBnwcUpUlI2mpcOcTyRwz9HlVR3+83pvvoHdU9JOzSHZ/oqwsSjFGqH1DBrTMH8I5v5i64Z/Ie1aobtyI6hURAIvHNLyIcL8k9qJj/FTgYXhqDdVsOOUgMI2RTCfGakcHbWQm7y7xRBF40/kQAaaQPhXujULvfREMGhLYslEKssIbS6GTKZ5nooJQGwnz5NOmoCiExJBToF8o5krG0JfM2zpmXCK5xNOzpVmaC9lzTmWXKlItIGBPifW3+Y60ZjlMphTv0oEBcmnLDu3mdWlN15rqFH8562thC6OaZAnT5cKbDHvDSwt7RTl/71OpN1Qz+kGJDj929VVV6v0HOPWONxU1AONnWPTXRbdIgC6lWCYKgBvUe9GvUYwRnllXC7wI7RJYA6E0iscwUbKihRwsl8p63PzjALxZFMa5F3aHOOBHzUMXdofv+tSGtGMUk3MfgvxVETjiNQ2TkNlzUiwKW8h6Y5vcSl3KaayVANLp93leUm9arOTD7U3trqpiL3JZJkl1VaC/LzOzG6DqEZcMdmC53xoEedzYHbjXzxg2VojqVxwnM+/6041xlT6VQu42aJuBah6ofJc9IK8uGblycZ78Ob4KwLx4XTSAxvPH0O8ecxinmw4aWlkhncHkdvOI5OUjGMJ8UZ6zPrfV6Crb3zqXTID0YtVzmyLs+juOCQj8b7dNKeBWgnM7QF3rwl1EaVt7C2thSz1zgAhqElA/0H1yqQ+ZD9NQ3UMW18NXTm4RpZAlC/WVxuE9Mtm1GlvpAib9SawA/fR0SwcG73uC+9zFuy41reSUU1ryeclXHSNDRZSETcvTuasa/O1Cn3f5mjE7Tt1AaFlhBbe8xzUhzE0GF/og8wpTwah2SDIBYjzZK1MCNSyi8vzJ5NvxVg4udU/s9Nlviq6aSsJREmRhYt3GJGYcLQOdz5T5dnE6aY5Yx0fPqbAk7sZnAw6PSr/GQZeJZ0MCgnWFoXmQfwZh7q93bW3tRzWlEqLhYEGpq6AolIpT29LS0bqU2F/mQmdoWG1h0M2NypE3NdCRR5W4OxzfHa5EbawT9baVXb07NFM0Ig956Q8UJF99A4gPxMaW3NFwSTd6nsn1s1iodBYYNtPE3MsA0tkdcxztPDQ6yDRsBRYSI7CzsF60nFSQyDwmYsECiole8Xiyz6ViqQh0LLs35GyOYlHSak2yXyQNMFnXmZ+eFe72q+pixBHrNb+b+8wjSutFUXFgOmtAfVuibsLl2N3R5ZaBtLhUDBeZ8cVh0CScOZY9t2qxLoFsoR1lkKJCEViMGxNxsCDsPhkb7ZZiPi9/p9/Slh0r/y5YRMcvAOyFaeCwbHLkutUTgLDkkZq9uaNNF7wEXVvGFGE+TD96LLIgHwoQl0LTr0AG1JH4kmf2fl366bzx6mQqaDU7BF261fOx/1RtUUl5IXmJS/OjyOp5mYC1nVmbEf3d2ouU9idgo0Z+1exHG4EMxSujDWmsaBBL/y2FWZW8Htw56Pb9JRmJ2bqrg3ARzKKe9YVs6KDMFufOLTRpLgEbmKpfPaOnAMc52dQF4kMdOnVi6hJFlXCegPUI0iIxylv8snu72T9osWJBejLhQKIw84Q3F33zmrHsxI3uzhlYwH4zHXkRevI4mAqWEEYaV7rPT99I3JZfV1T8AW1q8zBFd9OAAKjzpx7it5vtHk9EqZPe1uF/iC5Wzn/1y6NXpwg5xvfYpCj/ZKBhk5B33db1CVPDOjCncGtmOMvG95dyOdaHae2ygsLK31IpC9bKdTS/bzjDXPLpObEsdXaNFqN6pEJQPuZdzpN0gyFFo+tqFKHcJKUzuWDI9vJk9e0/7/3Bu1ahdccOKzxJ+1c+96oIw76RWmHx7ZMSRyMbGOP9NxJ46ogu8ciF0fivX0RRcK0wEAEdzFzHgBbsOk+nE4px3YEvDDjDznPFQqY60fK4qSX4Igfz3EJXh/BDyffgbwOxWcdLl9N8ifB6moL0XIQ2ZsPdeSY1YGvdnZC/6MN/bDiSp0WY9jIbx2hTffJqXEm2zKngVBBx0rVOrGA653zQ70zkyXr3YSZB7cgU5PPZdZ4xlaNFFifeBpLI9D7DDya6bKgiWSA1+iP+5jhsbv30zkfSFj/oYmRhSbB5rR4ZqOzAvvjDZc4OGTo7lYk+hIS3+IBaAxKs7TSzJOJufuxBczW73i3peD7m04VSPgaD1tLvBkMpsl6tCQ0UZkbxotNHk3AZlgut7cJC88QvBi1gSbkvO5GpDRjgAkz9Cx4ehhPMSZKu6pMhKTqg/ATOpLqKTR4vrkcZPnJpAb/KzKq2E7ngFUmgQY0TZtbhFWQ4XjTiNi+Ufr1Fk//ZvYy3XVoRTgYtP1+OdiX/TmN2Kfjo+VCvXrCw1quR6uSm3u+3knJuaIrOUEy4Cltxfa1wtKVtLKoKzWrZC1rDIPDLf39lsAr7bYcPhs8F4PMSVd40zmF6y7QAE8SiH6JL8eoucMGW7ER0BlBVXM1qN8LOnCFyoHnnmNqx2EJ7yQWXFjNjwaX3U810tmmyu1qrbAWOtMrzy+E2Nwsdp2FB4uLpvBvKMfSMzjCOnEAyi0iyVVKzGRXnjGtvzf4bD7Kyv3qI0Gw+au7qwQCFpX6EvYvyHbRkuvX8oWgWQJlQqFv+7iAZGm9vtQispS3P5O+hzaAkabMyJCTEvBlhIx2HJMppq8Hd0JrNNalZsyGFFTT9M6bwK6TOJW4287ktSGggsl0Ud/DxGVfNwP90pz6DPcBNDD05c6G6NkFNbaobn7Yk+VM5q8TY+DyZWjtedxliPJ1B0sBAumycJ8NVo/q0soLDr4/MHoQufF9R52zA6fXIq4YAPBPbw+TMbTJOVSV9JH/k0ZMEblJr9P9awt9ujKMlV7xg/yyAAmDYvqAehT7e3Jwf08mt9Xt0EXLsLlqMFGNKtgVUmwMRHL0LNWohYi8oZ+nIVvS/E7H0ScwRX1A4qEz1c0T+9rD90NLOs751+YjaOilQnI1GQ9QnOeQ1q300V7v/u8TrBzvnVDOH+YlxbA95ZHORsqePY0G6nFPwwxRQ6oU5YSElaeFkhtJHsEfoWzLjDGn7yydoxqeAIwUwHiQsxtmKYFMNLv8ZqZPtHrhzmKZ6tJj1ZNNb8i4FpmSKXCDEEDXC04Ns2bF2nXa9THWRjF/oZk42tkRKUcglymBmDutvYlU5E0gjORZ9ze0xstsuswb+pYYgRhVXXLgpsX3EZ6Py4l6evAq7isMK2b4b9WeiK9X3uBG+EszlT0tyUi9EWb+rhdJrz4fjFpH1dvJeXBqms6V350sVo7rm7X4B+Y6xm9S05cPF3N6rcMcTTbyvoTr3YkleLouxFBwpFXXHnYvKqB+KXepkMjrYfyiV2a57DJ+7LzzzGVjGyakpN7PcRuLR2pAykleabfkh6OcYlND1KvtHM3+JWo+TH8TsnchrgnF5FjhXXsFlCOyj+HAk4BBCw6skLitK9MJKlDAK0bCamG+mJJIwlkn1F/AkWIcdhlVho28stJY/pH8VQDeMb/xPnPzblG9Y2gb2eR7pxjvIodEJrETL7DTlYMfPozvYP0R6D7MmvXGrNjUYEam2aXfKblA/lbYp6Y0bHZcvbXqBBOxkDC2qDYFFOep63uU2crAYgf9AujBIWWXjBhBdBT4caCvfa9V6ApLNU9Y5f/auJBqHxN9VZZ8hw4w4KjChoZfY0jTYNn+jsFMET8DsaGYgvT1G9MIu9boyyIHAqI/mlnip0phqdpAsXMiXTkhzQ+d8F99B3oy15u+Vu+9AMbl28GLv0fFK2nAsfkVhInQbx6TsVdJ8v/Sd/xAeKZ6Wdyhbjm3fsCLeM5RZp4dS41JNdplMmICWc/zwxbrt1XX/C4AvD42T04AH5fZc7bGecOks64nn7pdBW3W9SCqspTS0f8PW8ysLw86owvwIUsELUgZEzIm+iL9Bp/KMUv97j5pOCM/NAyGuixUk0SOxeI6Qgz+UxRqq2yCsmlmt8Iixv5/bOMX4oscYXdaY0Jjemhv7bQ7QT6laUMwTa0eZ4Dt6/PBEEo2ibVCGnJECfxRh9/eBW/ehQH4hhwLawU+gxtC0U7NJ6w+XoNw9qLeh/FYFkDn5sh7eiY5Y9wtYjYmgcqbBGeo1pTardBmayrcTyZlcuEX2j4VqDKLIkeDmKFjkamSf6z/s8BLHYvW4hEJ/Jp2xY+R+KXZkZEeq1mVbtjJKkMtRgd/JuMNQCLDYRsNv00Q5yoTqJdvkgijuPCPLXuBE1WbY/lWAe5q6JOdFlx9JE7i/66EAMmasag2fnnXBz6uZblKvSv2BNoxG7NQ74KXxiyHdbg/o/6vJHRRccMhc8ihsyj7iSks1OAwyPd2p1Pvby40NhT9bpKOVDRjdDbmfBmJ5izrSNEGvfOmsc72s22RaPV5WKFlgKXLm3LvRBwPkwKYhU502xr2qP+l7MtVLKWuptC5OzlWJrrcEI+Msl1m4/lkbxVSkFKUzdaQd6AuxCbppXQbMMoR35en6imU9SXIQi1+k0MH5KvjwELLjp7JZIiDSK1dbZYBHBTP3x8kidW0b3ScHlnqFrzm/RVlmjA5q0J5PmCjmxGuWyIlje+qXw+d1h1qhHNRQhN2hplqMNbOUnYy+xP6hzQOH0+T1hO70oliUt/EkqHQUS+G/kUKMiNBlDi3GUs29juMaeC/Y36T6R4I1dqiORK8rzJNld+mmVmo0sy4x4iNWhPyWo5uZqr6vkno2XEZIcgW45f3LXcm2ESEk9ht0b3KIi6Gj+wTBxEd2e9nyvcAqoIzMvViqeGd/i0yuGISt9zfhroVw35AzV/D5UFVva1+zbv9PmKxpMNu8CxELQhXovlYRPbvC9ZUSvgTiqWc9V4ly4xLsfdXcaBBZdLdAZSe9kNI1hXtUP6Sg5jqvTCVr8EbVbaOqjpHDIblCy6KMFyN5JLZlPYVMo82O9lUbPOAMxtbtTSxokFLL/xF4baDGeaPYwojxNK6Zz9BCiK6rLDq1a4V9pHwGBWM1dpyvoiIPdBFaSH5Gfwdc3jxeQzEJonp5e4fVGSMzEu5oSiBQS77/ZhK9nCoKgCgpppw5Tp+xNZDhuxu60WzGQFqO9GX8lhLQitbUV1QFVugL1JXQ2qUNng3TbOfHTjDsAbT4MaVi16u9Bgw+B/k0RTE6nrYVl708wJclUW/wJSgNf5XSVjSV6WgE3jhPDiWEv7Sm08prX9F3CZBzwLjxxcatm2UXZ+JfnwmkzD0o+O2gk/qdpu9kdp+dasXgiOFNfvhDDuRDbSQEvTetd7Sa3NO7I4vbx1JpfYANEJ6q/mk4Ycb1XTB7NYM7GV5NSQaEz0NSxGC+CHWIyLDYq5FVzOIU7Ze8sb8ZFujYMiF759IaWzSzspCLg2QC40n8zcLTV99Rln2icL0pK5QlhUxDADLJiYjEggG1YjffTnxO/Wn79M6LCtmIbWx2Up5Ft3bXp6sYjbcdd7UZyY4ur4c8XliaGtVlupNu6K89WUYEJ1zUuUMc46M62WqZaz2A8v9YL+cx4fuavPNvbEf39HyeQsTq3tlpTOOGhfMRekwYYKbQrBkYqhGrFVyfYq2geKuGtm5CDDMz6+eqgXDzVGDzrm1NvUOspfpu7g8QeVKervyMB5kVCOGEeQRRtG1QYBg1+IX8wRlhk/Wa6yb2uThxogNUIjj6bHB3WpIMWoh1NGWzgCXkuDZ/FJyfbpDz13MMsEfPMZeRM8y4QF974GBYdKatKfig8bhG31XcNVSWotdm9sJ9rq+BoG6E57iSnOZafXmk4zF3WET4XzNw1zajIGq/YiElMB5XaadmwV9Va+v632qyT2Q3m68yviPGrIer0qdBna/sRaYGJN6gjxXUuWcSk5iwM+b4krzqAbzgrXas2S8f+q7UCXqIfii8GbUNEFi7TuMgwFl2uYc8ZBqhNQLI6hWEDBi+vFIBwyAfkImJfncJSXQViudf2yAV0MWmWMW1kaUQBH1N/O1vsRrSzvrKPMDuBViK+l+mQJ8R1oTyer+zfP4t7mrwWzEFnqrSXgDU8wzhnR8HgYU4WgRahtZPU/ZRKDZmlhWqljYKsROhZZhXCrT6/2JYBXcjqn0GU5PbdqESKE2Uw0m6rgSUlwfIdTtEEW4dTmJOcM+N4ZQT4zkjYoj2wpDqImjD2jfZ7TOtX5kGq9QKqimLcCds6C1ZW++cSVlR/J28zTUln2AttgXC9XWdLnCnFqycWW4xf4RU0FxPrfeerC0O8/bOCnNPKNK6ifL36XUR1/t5ILF8kn13y7wlreiFelQpPMrfIAx5SBVzjInVBv7GdFEUXV2UPYX4Fk9djkjwQ3jBc2uEAftbIqEu5nY2qx5uInFpB3j4p0SyOppXnFe+ebYiDqLWQc51PFoBJGhll87oRDc/FuoZ5n4RS3us2TNWSEKPAnlQfs47HZjcKfDRHyGf7Plo/bA8lRZn5fjVqgis3n2pMhVZKFWAf2XIGma7tl5HJtrTu0Ur7cfvqBJHN10X2TX67wexRwtQQn57XNmHnRKJV6TpmY0OXsXJkBndQcGRwP4XPcA61kSboNsXHRRjzAiuVRdx069/BXeu45855NQ3GNQgIp4kSczLHaI411tKtssoUzeC6KC38PHgTHp5cNdP8DcHDuVgxi68U7P9wHHNFuyX1Si320KI/lsA+wvAlXiJhnkFt8+JDh9ha7/eiOh7qDhbf//cwqugApX12eu8/nNRlTf8zZ5SnlwxbldIMxwFyOjAGh4vcG3kviESTAAppPIfbhSMpdVZdC4r58pRvSHa8sS80Ev81w3/Ww2NjxZ40v+R3sgodPHznUU+8xgFloJNcPolEuY8Mkx2njEwg5tmCmQBoPmjLr3lNLyIfP3BqlSQgLJyipqtBq5XfuWBDuyMW1gYxlQYtRNx1iCEwwhoeaJZhvWPNohxS7pO71Sgixm8J6ELa4Fvi2iTAx5J+6sZLs9nfJyLI7ELZH+qQul842jq6cv0eWjDKFbgzz1aLrwzIbjDrGpHna4BDfbUk0EgdB2OGUJfmzlOaagQxCK7mEVXFgoJO5bKbVLn4R+2kOGgfD5L9yiDfWE0BCuN6iN7db0sBg+omvIrLREZB9hmYiwqXJUMmkImisR+00i2WFe8FIZk88TMzwoyFph8DUJpr5leOF9OOw7dVjTIET768jLliInP4HI/IBIqXT/MjHMUMqk5CVP7VtlFN/ebkS25ua78VK080pusZwbc9eRXuU88bqAaQ2ItIJoO+2/wRJNUFYXJ2Y7Wd5MNB0lsMmqeFZthQ57ISxxasMaE6bmmaB093VtVSH4/dKUzRHa8CbpmfWM1o/jcqlYrdIYOFANsyh1p++uebgLjp9jfMPZg0k3uTBWBe2n+Jxplss0tZ7RkKy0BHd/2VjGALkXJPUevq6klXm4hzV0j66Jk6Nn+SjDjWnC9iTN6vaPM3z8kOEQB8/GhuOp60v98rKMBvreCzsWaJihzq/KBVVpjuLHB8g1HlBrcmx7ao4DAE/f8aeraBw++qJnBgnnFW3Pm4l+D972F7QlNYbK54bevj1K3ndqhJ1eVNu4WWOM2kaZuqyWdyenMJV0NCo4B9v6nm8rMFQQko+LgJqF8RS6eoEzd8lyyikc97ibdvtgIUf62l+ECprH/wW5YXB9G2wTsvdY5hVU2NPGEh+zEECNNlm/ogq3dIRHCT9rU+fWC1TBWQMpeOT1RebgrOtFqZRQ0MXjN1WRUTftLIIESocrBEnh59ofMTM6kOOwOtS9BBbx5+krsHBzoRlopdvwNGcznsBHLY0bw4EoxMBNAoUBBQLFMrvCURnEh0Wqb0F77zfWnP6JSoCM7WxvhTNEjYyE0JREL+tf0GKxN4ZrtwS8Cwa7+q7wsHb8JmWTo7TyDvzMimorCTIcZu5FSw+pMWKXkW20iKBixfGs9thuXfNiKIeg4wV39N0H+572bJCzENTYnPW4aGB+c2ww+10n8hXJZTCo4D39KSoD8QgavFcwSYLDA6ew3hg4kVNff1lQrH8Xbla6NOQHRMPVltlvKHU5tTcdYeD4PHmOXq7R357DS84GnRWN/NN4oPwDnwfcsAcW2NSYH8aD2r8tbvF79nnRwAx7xsudTABGfXeFxU5wcubculx1coYwrsWK4lGKi9gst4PhMIOVmPg8sGizTNHgZ4PujYgmNRx7+8u8TwhKocFGrSzNNR4W+C30/wOTosXVOl97Lm+ysAKVBFdfAjp/oVCzVOTmaIDvNf5yzgPE+pZ+pohTV8fZN9nODLCWeoAPkk2PmZtU1dXU8ovFqp/yATDLA7TTdbczQbmDhR7hyRuqA3TKN0Hm8Dn3a8Dkkps0f3ZbMCOICE/6ZLHPBKYE/wHd9WbCn/5GrGSo/dEKpxxJqqZCN6ogUYpD7jPTKu8Jz8WT3HNA7bW5PTddul5ZMyvTp2rc9sce18mCWamvUazppYmHi6Bc3PobSn40Zh++6vrOCPyeTRb9n4rYZK9K/Ch36T5GnZriZxpOqytx0T1INVxZMe4IJBTHHut5fBF6IeJJlaA2G/uucskcb3WeBBGTZqNoBxspAHh+HOej1GnICH5VExqvxvVZbuzYcAoQ8RUi9Js0ZS8+D5oKIAkCAJgudekJ2qZxriVwirRTxTYUXV4842iR6UFkBk7XRdqaydIDeGot8Ph6E7eTiGKoBQpwWc8TJ98XdH3qfB/JYZXMP7A6PViVzFzDkZR33+EzZhkxSSmyXQT1y/6YMmRyzt0BWax0uuejaK56yMQ5QjDuyeV7DngkGQVtpYhIolXDgvkxpE4dpUGcJ6Pxcdp6ejQJwr3WPrClR0u0GaxgZ/RWCjXz1QanfLm9s+Fi5MqeYQyFDD/U1bTolT5kUaTdA7+sC/b8iZ+K181y00CGRsO2VVVyVUbSo5wZiQDos5SnYGaNn5SmNy/yHNde/iXvkIOKzywcRNw6jOdDZA3zVFjdPdgM7DBy+/HTS3jdzib5F7lcqMMaifdiqjoGdxoZaUO0Ag+jhN33RuI6aenCSAPSA/h3nBCCj2HMqGsQNhCg+XHKAi2v3LSDPLqwn40jsqS8GBU3ftAj3GFBPe4xe5w8vlnTAEGIZU/xWmvW708WjF9MP5LM86LYvzEPmUWFBcH02gJJqOjj5OrE4b7ycVSv0aPd/0G8yXOqBUWQNNnUWoG9p3ubJsHeFihKPgauCB1HRtNvbaquxntl/ipuMGXIsH3dbDpEJl9fW9CXnxUD/Z7j3tbkbsfzXSR3h5PBkaSK6vb6+nFDYP61NnU85JnkapbpU5CwXNBN3RAeWcw937vU3agn3PGXw9+jq/H+5RA8Wl8cWZTAHHft7JaX4zHTM9Iz1UelusVE4LKks332jrT4Fu5eK/lWyGFWnvzEtRR7xetzvnB5dmIx9LFcki58H3kXrDl7GuvRbymHv3wDgjHUJp+0H8iPGSMUgw7WCmyVJgiOEcC8ohdPrn3Nb3SNwdPNtvHf1fDgSznulJ3aX1N8/zf/6Yfl9Ntc9hERA1j/Sa31mP07yUEU7C8aKJu4v2Cd1wc1zc1FOVDBgFQd/qk2/zuV/6PLeHSOsO7ARej7dA93d/fUOOrZT7kp3VBN8vBSQ1glHv7X85fghZUuJs1wnL8JJ+LAqABDkXzAYfWLfjeeNDt7UvPF4MoA4DwVHjnGF3mYe4f0BSk/kaJ19Y6Ok6dI5oYNvePeyRSNlYck1bs1EfgJ26W1XGNFYo6fC7A9DKpbQPGQr7YgBn3yPK10pOSzkm27IBcHv0A4GlE+90t3JQoWt5UcnDbGLk62sxEGKciGruf9IJWCquep/Tl/tl8UD77ZYyNe1ptu3CXXH7QcXQ/RCWTPtdI5FJNLQyD52+D+DR8YfagdgizmyHPOpNohRwDqsG/eX2YnQkCHK2lOQgvKgNmfRISF3EGy8onFQq0YtqcwHAMJnznOn19giUN3KYpattZrcO7RHx4/79E8oFd76GGw7jRGWmYR/ATKVO23NKg9DQrfu2bvSaLH9wv9ijWwWABdQtT0bkDIUU4w3F0fp3Rs1ydR9OXhJhVqrw7iS7+m1GsGUGyFPai/TX8VjaEvHf42vByr9qTHStbSgIGeM64I07xV2IZUnedH6zlDDvZUkFg18VKGu28+cdUitYNgnvI4bHM3rxbF5xXGY0jWiySrxlX/U73FWziuLHIxoNr7i2clVfmM5auZVu8SiR9I/16Y/a2Hg98exb/cCOF3Z4hl/v4vSjxQKZJ4087MIeGr4EIvphlp3Z93zJFY+smHpdUXqU8PRcFckwZrl0HahNDjSyBdIT3wImix1x8I/FT/9mbsw4vddQ/EKU/RdRMxY/FJRo8+9dqkvvNFdDifGzbBtWhXCougYzC+aogWfIFt8Xunn1LuRZUAaSa+K70vOpXVdkEjyGNejZOP97+OEX3S2wG0yJthHViTcmVW5u4f6dvIrvxHvDR0NfGm6N36MAsxHOpdMm4H28UP7+a+o/QmBeaMNodL/0AEMtaBO7j9ruodreUvF9zfsnu0GQEuSEqbMsZ1Ye8rhWYKjKffSxkja6DXxrP+Bme1ECThk4CO3oJ96J6nq51C9zFoDxknDFeF1Vy/J5mO5XHeh0K3xHd4nt/wmjZoLEqcgM7KgRREkEWCggS1cHrhNbDf+8oB/lbKi2X+1dimE9xhjgahyD4ymgKRF+tYiDCCCq51DKmzWdAkPRm1d8VD0hzbDOqoDaZMM/K600I8EP+MW/y7L24H+XosAbJI3ibdbK5Ik2RvTdQsxGahnqZdfPYrL8Ehcw6S6JvO1Nuh8+kqOy2n8ubUtxTIJFEc0GqKxjx/DBVfhNwsgzVvssNDvJv1tZJqeiPtqcxLy+hHLuj+NH8s1lvrnWHQ7ArfGoqYN9xkdw88XzDkyZ23FRfTbaEP/hwinC51nR4X2HLEMuTCeU8heYruqgkJwK13D4Avzjyf9aK5oXV2O/bzX9H1eaJHlu0B7i6mycf9MAVbKOy5ICU3VNfWshH39nAnl0lNMk6I6KaZClRkQUUNEOzfLm/KBiwKS220N0mF+/vZE6+0JhvcVTILg6DDK7ksksrWcE/KohYCbUYQNAKGnSwNeMXaPf16D0CJdwN1z9Sy/ivY+J5wI6TN/Gznr1PrSTMmaJ+xz3r7ZIIN+ZERy88qb4Xof+bF2NqKRydXcmgO9sObq9XclH+NCUINk5niOjS41CDHVmaEOdkEH5hcCsMkq0bgvqlzfJ+QS048XT2GVVBe9A8yaVIgW09nnZnshIXsaLDt56Hv+BMQ3p4fObYvjprq0LwZunM4HFkz6sOjPHDiekEWXuNG9TODz5BHQEHJjpfNPPVZ8qws0y+/awTxqs1Cj6kh1eJkT1O81v5qYV0NjY6BxWfpY4ph502Ao+xt6+8lJCVf8VrJZQ0ZgR9X56Rw0pFb0mrwjEnUAjghe2bge5Q8MZ3ACBbZs39iW0uYRoNiosbeYCdFHGMKFb9aAXXRVXhIyym6LBCUJoBfFVJzSsQIka5A9P5LEQITfoDR1fbH3q38RK1Di0M/f/Wc7j9xP9rB9FhUA3VkQkdGAu6cWDRSOhsy9pK8JEVbgrrkfJa4ko5F00k4YG0+ywcWBQSekbzBq8KlMx6lvYwDXHxnF1PjtxzsJMlRUBmm+zXAEWQyzRc/XJ9XE0mVskAGE0ScMr9IRhDNQ3vjWotfoVtTtPxOob3swWzi7Sc1vKvWjO3JaXkRk24jyNTwhvuPDaCD3Vt/T8/dHzgpEYjV08fROZrMSUYhSjRKJuVGhHphG/qjy78Fl58U5ZwalA6ch2V4NbYYZj6IL98nNqEVPNZ3WHjMr7c2wLTrKTTbYO9m/bLPer7h6wvE2ec3b1sOke/48Cfo0ooeAeQDmxc62SvET3RY9SywkmYN5nwx95mmKAKBT9UBQlCeCUNsNCQX9rLL8owN3PLINjMktTtt+6hOuhrownO/zLYBiUSMxedEURbD+sj4vtoXLBksnqGhxLY3EbFIT1oJ9YPDwRJO9EbiFar47A8BMWbzjQmRzoGUX4gSAx/hc1k0atJ9+tGOcpAwryFeTUMvOQbzBZHqi9SpwalsZzUgZ/kj9ZyAOjcWCY3NjaijA7+8I3LYY0W95Fcnf2fRF1MileJ4h5+TXA63ezXav8y3p797zq8xzIj3WhczVNgN1sOmvHBPobR9Met2RhxqphSi/YXRzl0koJKjUrBpC0xX9cm37oA67FsjGDPtLvtgn3YdYqttlKjffhBuWPEzXTZp7dOkhuYdzVWHFvPUxYHZdY6inv8d6e9MGlgi9mq5X90zBfVqRtU3dzZFBmefShQjj24zI3IuohXJC13xHAHgh2WkDTbFvYdkQtf/UjNoIwm7cR9o/wZkTlmc4a4OI/zulRXQNINxTcm6As6bCJtiy71h9aweqX7TCiVltIdMAhMn0+ZSUbOXjpMO+zGgJuFbCJAckmSLHyd/SvbfuDP46tar2xpgQEtDxbxqWBrowgZeWLMbEFAQol7C64Whs95w31WOtRX445f0G7FFncIVs4pHlw789AZUQzGXubjfg/KJv82Dcf5Q1s6zglw7BAJ3shXrIRFO0GK8F+I2PvvYYdKnUDXELXAZXmvc7lWOcyIwcubOeWKiNKdBFVlOWg8hGMMs9XWINpAtTF257FMmyDWym6U+62GUaQclbXPyRYeZg1oKQChWYVVt20TsObscuEkbAoeaMI34ZzAHDI5KFXFR/8TG92A+w+d8Q0VTwX8LNIGjpN3cNa/U7P9AGGhNELb3HBAbDchHHG111AdosdhkfsTJWOREPnFisvFY+DSNiWxM2aVXIM8gEhtB83N9/sLLy0ASZbGRmTIocZvkgd2us/HhlM31yO+JHhIsdSIwsawwCaeGxbKVDD1xv8YzpPZCIH38oC+WgLwpHRfTE8zyppHefBWR5xa+M9sYWQUni7ixB91OCQzEJ6VirIYhBI2aMIMNFZ9oxmg1t3QeDtaV9NN/k/T8DLtRJ8XYjWEd/O+vJ0tKQ6jnqUD1UQzNLOvOsj8ai/E6nvCIA4Equ2y5QDnaquqVkSHv+W1SstpHq+7tkmW+pCRzikToBT7XAsnOA4PQPVexEy5HQeNhalvqO7TGLVGPFSttOteYpz1Ehb+yfhJoK3Ah2yvTPfq69qXgAEPmFfc3fOUHBP6Qy91/b+9ztjPc5eq2jEWTI0LztzlI61EqOrg/2RX0osA7GyHfXACN3akU8G8eWb2w1aTrv7zuIZxY1uerhzboAhbzNllTabdpkRKGGlBAGsQoDP+Nsg9xDlcMhhfKUiih+i/wSlfKH71SRJz7cdt4mDLve2CEIERLaHoZAHdTGiog5oh9eFwqztfg+UGHqomj0q3pGv0V72mvD53psnlmRPRtXWSgmEzFvd/aonjw87xn12ADU08OWUt58/cTnd7zdq0vvb6pFfEk3zFtIyu147eH2EApLAUqj6GT/ZlWVSB7lFdHsekCVdJtQxaCvZ4WckJZD3OBeHys3hWlaPUwWrLPrQ4VL5U2kDbAxHX8PPfjAPKQ5IDPFplDt72qVvQg4ElgZNeOrqw1U2hx+IgYj6IOkLaYiLh2lPblc06ge8jNvMbPPRKBbSoTfmlrpdChYCZXstCtoWwbSEEPCGzzK0Wiasg47SxmOogY/S/i6SBPkZIBw+UOlC0Bn2R9DCjjGdB3PJrVdXrUV7E+zCjigJdwgmcRl59NFTDafV2hY7rH+Vd+SePChPOQ+Sdv7OR5Wffw05loP9xCokjoeg9X+zU9XnIl1pBu+u2+kN+8wwww7raqQivu3JgOvME5RrPxmr/2iO+2ptAcbJVgb2RUZZQ8LSRmEcIFp1IaQ2fJhXEYy4+GDK90u412n4zh1qeMOwswFmgyM9WL9hf/0Ejmqb0LP5bwuwnnbzDofizjgENSO6csxtp5X8Bsar/5Jhc4XWALxIIwpDKf9AaRHKupv35khlTjajG2UX0DjOoqumyxIstSMdXQ7qf+jzEQrOGq4t71BKo/0gt/ojrIfgo1aR8/Ku+TstWVMHCGQ4VrY3tGGlqGb8FWdbJRvNtR0XyyhqqGIg23f1bHC500VYfNBETP74guk3O7m7Qg0Ygc7UJI2rYD82vHOhf4c0ucWgANXGvjLbED3V9A27WnCn8u0b+sUMFRqTfaN14QU3MWgQ2ycRWhrHSjnGnvXaLcMXL/rWeUN8gDZVDOTm6ajlYAyuaRfqv2je2m/koDNiDdyXUks1EAVYJIl7M2ktGmycejjV0zVyFtmaoHt9esYCgT4FjxuvwBTYg/JDAGVfyKXmUVsbcP4K7Mn8bANIDhlMYkk0FBbtGNo4GGNeKtoZcmbTTEk2htM8yRmXiMG6Rt8ig9ejN/FrHbD1VODaWAGXswUyYbWY6BL3tnJ6N87pDA10bYn5hp+yxci3o6fIBDewmdkzJvgXd+6pzKqshhIrt4kGSlt6DD7Y3fsvocg70BYMVSKCkQLCPr1NHJsL3FUj6ah2MFRnyH4Q7dBXA2nYlcgLI0H3KA65Bw3R+HAt2BTb/FM1qcPoqBKPVrHSzMeYyJjfmaVKYK2Z5ODYqUVi+X31C79BgSg4IlHr30KLoHhlFzxCIAIb8pWITEWlzKY35KRy6AWrYIt6M702qqqHOfzIdGKfz6SMdKWtSa4fWH56uKIDxSdeoCMB7nnvZtc4NsasyieLIcG99TFVgF6VMPvdRMnIWlWRvkhH6RscUHDfZSDIxg662P+uc5/1HD2FyMzUU9M/vBL6nAL5bq936dGdsWWj2BDv8tzIhg2NyYY41HW8olI9sGVoPSU6i849QySQNrHzsoMuTLJJzuoUd6PusTELyQ+N1MS2azBmaYKJGrvetje3P9IKd8UXzYaow3uPN7atDpl4obFGH96fl7xQQrU9NA60DQ043JnRIwjmDwcVz5PZJnAokQytfrBF34jtpo1VWeoc5CyfOgK8N8ydrKyEJQmHLBKGNC7eQluZZuG1r1rfYK1od4qk5AIsONoHrFE0TX+Sx5jiktm3oRGx90ng0qn9BOKzZhI3/Jf1cPx5kP1hBZjuSAK2rgyJQEO6xtco5HwRMSHriTBZKvNoSTHOcuNudt/RObQvI9EHxoSwQS7SBSmnqSgI23Nm5ooQ3p3jFKPUadNlZoj3/9r+QNTgBdO/E/pw9UUa7WbNDZ7RBKx+U2qQdO807rVCsTUVvQVl/2QmtIrv6wu/33EPO5R3xhxeTll92nNqq+Qdg4bKB17Bn9l/xGVDZz0A5sc2eJWvxvjqWAkcVuYChHr3XqURNjKxB8cS1TQdaCCBu4O6ubOWq9taPaCilTKmts5YvGbN5zlNHBJgm4eaU6bP3jFQ7cinX5i37hqCnLveRDqZAZcmfRaEYo3tHtF9Jh8bW2S8GLtrSlqM8r79799VUrbDKHirUFtkGrfGwQlT+c/UpEGonCNPd+BBXTK+xxqLCVrEa3pnEryanTverZYZivkxBwGYyo236LcGIZgXtTmYxSTrWUH7R/lw9cK1SDAtGNSzuNjg6kQ9GDktI6hr2xZu8RLi2EEWJX2A+SQbyN6Zzam/fetp4nvMMuhgO0TWULk2601KPoCIHXI4zcYwljREstSr5Vc/BIs04JB/iUf1PdDNBJp+E1FwPixr5LjXd0P8GO1VOg53twrS2yLUSAteoscY5Yz+pvay7xeBhx9OMwD9mnHw+stJ1bOGbeM6vv2kAmaF1j5kAfMX/u1HmqhOX9GTqGdrjCZnKum5Wgrb4kXI20e2cTNBbKuYV/yl5Rxn4TboBEQciSKEokYdAatDKhbaUPdJ3rxFLDSPlWkuRYg73oXyOvNRBFqMzE/9tiHk1wgbT/kH5r8aviXLW7bnO1THPf6amwn+GyjURU/GMw8oLdDbY83pO91qSYRch760WUD0D2Xugv81BH0+ObiE/by2ShWW6Ux2Csf3Rdm0WCVKCnD1wngW3jZUBvqtmvBye/Aw0vsTlJhs1zi28hIfQNwq6MyoitF4vsh7bfYxiSmJ53f+f7TqOXkxTDws7SMGEEhQtqeHO5PF7DH/Mfkh7nWml3+tFZ1uLAqswnmV5cyAPsU8kiaqNKzR97VbtCc9Uq94oFoeriaVRhp7UH/o55G4u6QlaM6sEu7/MlRoT8ODu1sITQ2I0t8RRXFHxzZkchiZuxmlWASOLBmDwKvKuJoZMkQUISw6hBRKUhTgNgYCfMSEiYM99LUDXMfCKNtuL2b94wNuHLshMnqVOzhDekoRYjQXAg+zF1IvTlkHL/t1UpdndDL9SU+QAvcEj9ZNqhNzCmjMVGF8gi+pn8Fg8tPAHqq5V6o308QifzGP8227COPjL/ZVWJjd8FBxvybaAS3UKh/JsKCHiMDRoBPWISlE/RH9DPPN/wWy2yugEkqHltq6IV+Je8zXX25ScbqvICgdA00CbxmrXn7w6WtGX8Ym53HZge68/kM45aG+a2rB3WPuXd7QUDJ8hYhhCbkTrycQq+2BD2DkJN+We0GdX+aB1CCe2qQwiNHq8wj3fzee44Co/vMmEN0Zidjcarn8AmiaOViWukf2LH5Z6BYcANhXE5r62dA+8mphPZ0WwqwjoZdCNd3jCRoAHdtmYbUdBFIV2h6xx0el24m/GbeI7PsxWtX1uCPMri2kQUbF85pjKSO5TuYHPYDkPk1H7jMOheKVxMH1v+SQ/HsqoHPtD6wVimjCUCRXAtm8PSGPSPL6/LYlTPrTmRd2G/WVSGAximDB2eg/VC9VKxhuam70jTUfGYArN995gxzf/js0ySi1xw12rMe9YE0OBD4XBjQJgsMV5ZpqgWq/EkO0v8rgzRprDyWEcHYxobCO7ED4poO9kDCtbbzU8t+f6MKnDH5HjjqnWjU28w2z9Bbp+eOLov6OLdUH2IUdRRDR2hS8SPrqgHUg+4ECQcJnoO8uIW6oKp3Axf6KrkaihVuiAo6hLK2W9JLqdFqJ1fy5jlWXhKhv9OeJFy+n2zZhJ2cwCNpbe5/9WDSdHq0Ph7E2FsfJKCloBqyzBmTieEvN/XEghSdpZuJdxSLrYP8RcfGzqOQVdZAGIrHbw1Nx4BGB8FYyizWOvxySr3T/rOkh6kGrYuyNSvJPZmx5i1+iyi2Zep778NCf3o89on8SZcMeOjW2aA7KhAWES4HCAq8ypcOXfJ8w6nO1n/ALM6QNFDsHt3mie3zWcL67omFF7CAkasY0SmtN/tzAJe6VopNvVcvAGbdjSON+eiFQK77kviFUPL01hJFmAQl90ZSrYuKgQ7o+S4X+0wuvUWQTz4AnqYH5zflxd1JeC1jylj/XXfZqE+MCRgw5hovzw4CMcJZRLfWtGeP4DYonKxefmMRUa7aYSyUdj4HSeZpCMA8aKSWLBbxAzLXQXX1VuUfwxcbxu98VFWjrUCLGxt/OReNg/hFvJwiJIbqsQDWTRNUpGuuhi1q2C5JgbBE+QVzJEf9J5Goq37qUR5PsG35xhLT8IrqfaYseFQdY1JktydWt4aN0sbihW7AGUXykzKfTIs8cyTQgDfYABuKiKftEWKsUm2zfKvEiTTRhWmcOmXkzl0wWtn8+zzDgTjsKzstGQh7BZbEHMYF+kUF9ofEDgeuUKkL+iYa0h9a/AEIaRrGHvoyNU3crACRNMCHCUTRfKZTK/ykR0ZprgUyvrShJdqEKKtYOAGIAQBmKqRZs1GAbRun0Tsg8mM6Eqb5kkw384GA1E8RPkUMHOO7zl139SshLz4ZK2Rb02vm6kVa0ASMV9WUux+PgdX6nh8Nq+/Y/XfgeKXDJiy0IkEUnxM9/IVyBIyzKxnjOUb8FnKoYj9RwD/Ule+JHs0GmDw8Z6cRTKUMMPyQthMqmKQ7+tDA5ijs+QUgV9mOFDRITIvJJjyhKmGWZvnvl9y+YLsuYQEFeCVP9i04d713laX8Hvtb9cCy0GBH6Ios4KBlF3yE3krZcNooW2PlMkZY6N8gdF6zQ5vrOqKD0qf1204LgHVajUsBmMORTyc/Ac20h543mJT/tuKeb2nsCaOLGap57K5ABkUY2Q2ohsvqZiMSLTT+5nvxreQwDjCNhevE+4Pb6dfGl0hFs1IzNk10UkM7GqwCUUCzlxSrO7YmQenbCM9OfJd/iDzgcAe8JfHO3cxse6IsLF7DBumJmG6FDrXNagK7LVdyIhLkkxY6gV8Eb+rtwgighouVm8mMFKO/g6YcszuH+3oNGvay3KkXRKKgg67iopStkH6TpBIRvHWvMKIxvQuEJdfKJAqmII5gJ1W4jNPJ0kOzFKqiJCs2Pq7m3J9c6rKb1prUJKp+XUeGiuWUJXrmJ6eRXUq1Ls69oLRWevBfUABuaEVUgTpXADkIMx8oYRZSCnFqlPe/qePHRia53CMfMkRfYACP7dQZBaQG/SPqVYcXVVv0yORUqWstzG3D9szno33AAt0fUY4uJbx/zN9dzl1g4i+C/AWUgYmRPK+l1AxNJF8k9/B21FEdfLEjDePCFw6Ig41mSVhnQ0c7HaWHHfiaXUEVlzRDY1NFLzMhj76Yf5DVyTjnAwunBlTvxo3eLwLqXiQGaJMyct1YJYKJQvdo0tRyuDIqqXQ/b4N0ZWoScYPGuCtBkw6+ES8lz6rjg1pspyw7JXMZCF9z+g+2uMozExK0tmMrP6Xfu7vYaoHtdO/TrmOvd7vEHJOtY+p5e1p88DG6Q+mY3C0xFuJnc4De1fd79ZW9RLpmGKt1YyUgGWhpcLVducR/RDnRkfkCc/XdimE+79bqSzBj+ASA+7Wzv4+P7MILrvL7WvMiPSM2QBHmGkUIebqSf0DpzsekZ/56y0xgFxEP3Ai0PWVzbeUIYMY2R7oG6SK95nWXiBJRXCkpNecM45E2R75WhMiK2zcy01uRH0W8/Av+3ARipYcmBQBV0Hb9HpJiXIvcPL1T2wCbEawNGjQD3A3tvzvPzd3hhpdlzY10bQ3intx+HoJ52LDg36snk/1uVYesqbBygO237NXJf+BTS4VqDS4080j3TWcupbdDUyGK4FNnyGbo2CSQO86pAQfmSb9azkiGJ1eCyhtEQ7RMQEJ289PTAc1LxW7TcXlo3ixMoQDwkkpWQYbWwUC52sxp1me8IIgHCDTo22uZpnwEodP5GmBoAoCMlIO26O/NbiFeVBpRw5AD+ethsUxyjDUWXvTxWC2UeRIhJ/z+ip8z6R6JTrmzvHTJM1rmwIj7kaeu1OXyiR80kxMDyJ8D/9EvhK/MFx6ivRcnpyBM04s1AZMHx00AnO47tYbyeLnSxa4xb5ANZ+jSJpl/XZbunXGtB7NyRxQLdMRPi7aSaXpBcZiT3L46FoVUqEMiXTi6a3gqRZDLY5IUav5bZ5Y9/P27g/edGwBTSQ+QT3LlDkVQCExeCkfcA/ju4eQLUD7E7i62i1WsMD3ZYueQJgWW3ivlMspixDudpoNnlVvSen/pMsSOJr4ago0M0pP9ZVRMpJI9x83yLLMuE4EzmMXJwJeSyMXw8HYPw0aQLtWcdFSnJk1hzCrZzRjkPGIdkvDBuRWwA11I250X0MdZhSebVZ2xUmRbM60qGPBvdZRUTq3mr/bkNIDIyzIarNXEXFv2a/mEUi9tKDD6Y8DzRJ2NAkroGsDCFjEW1U95aRgaQYNtm9x872+Qg1jl8b7L0Fc/zWHSEAYngjwRr9ifJcwfajdb+BoaFl7N+aaeA3HoZvaIAYqtSbfczvl8sDGkoE3zZnQQmVDRnr76V2iMCHtMXsh5xWkWF12a1WSOWZUH6muiwpfL6bXRuqVIZawbl39syEJiLd4=
