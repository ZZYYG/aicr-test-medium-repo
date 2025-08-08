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

zDXgiTQdv5z4tiB1tOSmMa0Jo54kw9mUp+gJR8XhfNXSQjsXYjw4b7MBONmAaiiA0hq78wXsliZxBnIrWGnwdO4WOKu0jOTZTlGTBKo3Yxqoe7JP1rne9zngrmDf6GqsP51SGytT2esgVMUEnpNxUV5SurnDKQTkK3WKCtkKk9eyAk55yonnyye99J4IRK2COn6uTBnmEFH1QENzfsfDzIYbwe+TbAzeZTx49RLgi+TjlVYbLeUwQXL0hY8nZZdZWpNPQ9dsroGDZBrwVn4dXtWzqfEx7FBoGCr0u3U2ngWSHhQwqonmQvYCtQ+G89zoFkW9gEkT8dzTj8KgFUHSHnXZDGD128RR+RNLV5YiGPH985ypqQO1VuTaYfuBEgzJc3LK1a76UMlYLLsTqnUthSVHXwoyp2nxaDH+fZvffCrJNFxJPvyx7tXelasDmSmlkZSvB3JFO1u4kzjjWiZFbPNwOMfT3hdnwfbdN1Rc3oPBIGbBzgYtreejhoJr++HVxRANa9D1JXvXM/R7/rkO3J+3/S5+xLjRMI1hgn71w03fIB9WM9I88R+M6XPLs3IeMDkH/ZTVuiG1HXlCbvA01JlGcutAykPUmZvhAC/OQuJ2DhbL6Yl95FhN7RuDaDUukqqOYaTi6sg7XEQ/SRPF2IFXS+55LNpLFU+U1zARVzOHHkSVy31rGaXe+7WmGMoMAJMBSGUN2OGag9GH72a2Tdf+jysMihvKrZ/ch2l67XkoMJoSA5FuiXvqzjTIzXZMbCdxtmUNLRcFVlgYSgrPLNk974hZQjfFzxMdT/JkZjSE3KCdZYzGz78hwA9EfCoOhjJHYSDJMlo8u01gstAy6Ho02hCy55Wm+5j4cyTQK3KnX/cfS0PUw88cb43i32PTxmiyPmMLmunAR1rxJe2kGUXuVjLrtK91fWXJgTPOLAYUCASwCgxNhmGECFpiBpE8Rr6vPh5XzEPOrVj/GL70KOtTcl/9v9PNIGgWZHoTNr7WES3QUEeGEnU51ZHgqBjXzz/NC89NY/DDU+VqrlrD3C5RAKZYsFB7CJgczrodnOh0UoXVZ1thJd2jFmCTo6ZZ7k/C7+C4SLutfAhWS74711rOj2/CMNoQ2WDKZ8BejAV2Ts9unR+YW2qrOgT8nCk1rPNinEKWk3AFqtcFtyy9lUFM6CbvExgVUFW9C1AN477J4TFkqwy6xWV1cXPTgwEaSRgXlRPvUuczifN0CjsCoDfFg+PwR44r9QWCqt1kqAlB5xn69sty2jMsxmLlwyYu2jraSMJNFBOjXewnEM4tIaWbdtO58lvd0qQxDy/8zk9UPusKyy+QrTXuqeH2rjWZg4y21miFieBELEM8EitTRmq7urhXywhoZ8MjLfvgBFOhIAtWu5JJbCXPaHHx4snOppLmVNdfmuEpyFv9no+lilNZoDPgUWKQcTNkh8GzMRcuhqj8x9kfk7rvIMr3xQMJwsHF4bHY05zRJLGcSBzjE/yd8um7KnZ8uc/cbOaC9DIVYsnAk6waluETZa1T76Qk2kNso9ir35QgI0A+XkP3ReDLiKt3VRHn79+GokzjqaCzV5uaQ7LBT/rtkr2mos4Sn94vqwgQRbzJzxseI1gVmhkdG5YUoutiipnDnqGzV8EunyUzlmSbSW39afaN34HrW1LhT1mrhI6ju6eppNW1IMSGnk4gZFqpNKQOS/+pB2mDJ0iw4w5/GrrpmAcJJsA6nYzgoSqQTVEaOkyF4eLAd8qiIfdpmR9dToI8WoR9xtLXk0C5YiAC+ft8zIlAlA9FmtRDn9xOgpO9LskHiUQF4MJ4Yj4dPI/RrF7QrpmlzZfc7WYxCu/TdtNm2rd9AAKJO/wW2sC7OIkfQoVH5UCvsi837Tex22qT4+ZpBwvLUnHCFqxyRtxpY+OCcdM6M7j/7jt2GUhqYo2yWjsQgjA9NQIi4tfa/eMrJTCu/TdWU+VW7781f5qBjZY0X08BuCEa67B3gsGHDNRXq2f30sPAWTxCwE/H+uplig49FE+cYBWPBvAEGc6ChMJNYnvsNZ44VKbsgaukZuFyZJNehFgL/OIoptKffAjaJ9faTUgfVVqF/KirCfddzNgyqNWaMTWX5C6yS3Ijuqtbg7RKDR+pb/s2JFdoW0jNlFY6sul2inrXD/GEgRSgi45M7FnMShk/6BRvrWBH6dlTcMF2yHlqYkhib4xYhJD1FK8++Fn5YkTUzDxcWSm+IR9fi5Asq2Y5nYxfEO9cKbZwTPsQZ9Lv75QU5z8dGllR1ey143lOyBlncrs9bYxhRpdK/FIUQXRWuW2c36zSW6bk2+MyEDXA3JajcbiMAppjO2WUQzpp+A6inElzopL2MM/vCrHX8ZwYC+m2pUTC2MNOtXomq8Ya82JzSyI3lAgcsHg66sTDrdoim/EklFkZlLeCubxiKJUh1lnoEUpSyrz46Mqil0RcV2xmiw3c3KpI/dpBoF+6HofJLrch0WUFsBsNL3y/Yi1BX4rFbJWPwk+IJvLrTdsLIoQzjaMBhBK5X6KjgO+1oLcyQw/85fbk70TcYI03NqrPSRftt4WlioNbtcki0UMh85BqwgiTo+YOK/iBYLM0TjpBY6vkriS73cgomzXU5W6avKvlqOUcrpwt5Z6IvxHpUe+pQ3VNARksywaJgMwy5kZ/3WnjA7Ts1523/eoiUz15pgqIulZaHfXddCOslKeQDHel0P7rh90suXPCcewCOXCa4ezuf480DGv+DHikJan611h4TuCuPUBwVVg8pjVcV/Zta0UD/5R5sK9GRMapiXnfJCv+bOtW54PjM7z32Jjlz6WKc0RYH+j/owj5FmbACtXK7/0ihE1J9lfj9g9oat3OrrSqKroSqOLQiUyx4gDZb7iGFc44XXJVY/DwAsT4r6dYov76/aBLjXTB95zgv4tx56T5RQxrDOpfoj0wp4YWbiHe+rfhQX8Xo0igVBeUjPnVaqNLYHmNLHB49YILeECuHFXK9RXsN5RFR0omrYcsxpVgzRW4N2M9ipU2jR5M3064ld6tkchcOO7IbRw2EftkEq7v6gfA5FQF3GJyxjW9tz11o7Vw18Mv4wyfvCj16BWAKrf+vqkXo9Z8Avd+BcEwVE/2P7j+3J/i5f9chfchld2nfkrfsHm3j3vd7vlpAkKM530bLvJc25WCmC5qhcGVuRcMoYTs6RP33QoEZnp1uhyX0Qol5WmIOW5GhCeuIamUpcwQ1ExffvH8Gyy2qId3mAKsqwq+qqVBXYXpC0542JUDKsiddh8FOI6PSpP+jHL4OU18GMLN4j33X2Z3VxV9EJ0GOyI7d0Us64BSG29AxxaOhVdFFTH+seZphtWF/C6WiomUzjslmtOkp6P4CPtcNL3Yj9PCw3pxgEUv5wXXsm8qEwUxBj38PH6/r8i3eOgh8pkB2PbkVFCtLcYT4B1D0r6Z2PZ8SIdpQYCgioc7OvCReQ7B/KvWqS7v2C27ihiMx94mmMdGVCql3mxaWODniV3382ZQWr5p+hfiBHIqHpIekNXukq9LetN4HwH/6YvsBb5h1XuVDbhTG8gcZKq2FtHPrUvw/+bvXqTvzK2Q42xazVzlHNMglsJTgW9IdsfapoCxNZBFAp8tMqcNCUv7s6vRd6LSNHCGwc3gdyReGVOiRfaVTQFDNRYcnTulD0VEOndjA1DeVy++kyfldKN2ofswu5CQGchUd31KoeWzsBViwxqBRkG4FykMiZGAUJ14KvBm9MwrscFr9SbYFvIEDGKVsKtIfxQWYkwweGp3T3cKM+UqnO0uo6uvmVF1jn8Y5tQSHV0OfQl6iQo4uuLOZeAMK8Q8k6oXG67aQ3TkOTe1ZOKTFOIEn9ucKnjtJPLdp/VqZP1HBk+K3tZpcDyxnRXo7eA6IQTzhFkFCOMzpk/GHXneGjP0O+FT+b/itxG8NVQhJsUhvcxsdS8zwawE/Y4s5aH3zaPZ2fZApXW9PwyNXjof4OieyEhOYcKoYCW1OG+/l4h484irb011rrzBgF5FNyDR/l9uZD5IdxojHDh51gxs48PjqFAQrqgOnINUBF/K/lAiW5BqGZj/YhuHTw2Qn5WUdQbMzkK4+sIbRd2CZ9AkHEmEbeUMPNybL1ulmnkRZOA9CotLgZ/6xfgVs1nDlXBamMIBOmhpSiMisXK9aH31oyD8OXgRZfCk7iP0MDSM6R0Pay9weYBtBCz6EjQTZQ9GVZvaGN793ubnnKm4sNSmdkETXiORSa1lymTx3y7N9FCphaMheaFjbgrifCnLQT8gW58o1ZkAyFZa2evyu/12DAzeJYgBTGrit5/opFPB90jL76wO/TGqJro3VNS6BpYzsJ2kwC+tFp1hLl4hi/uw8F+ckaV6cDGcAP4MnJder51yNCA/ZrsFLPou9YyT78fADoOAHvGo4hZXoF97vMm7E6W/KpdtHd+mnRMnsoaeDP1YPlShDfvopilZo8rkqpLjQnANWMfQvCWMlxYN15xvwzWWyYniB1EistNswXABlhj1A2leAIRfdSaXfIGH1E3Gti6uuvDdtV3M0XoUWGv8hA06btux/bUcGrxTMAsP7vCj+hwDKVYK7XxVmzLdp+lWFc2hoHyLWvh0h5XT41CL6Ey/iKZmyW1tTwFAFSfCRh/aSwJhh8SlCugr1rGiNIC5G4Na5nehcLkc9G/SvVfeN1ozjFAMaY+jI31sUdcKuY1DD7wZm7tkCXfYELI961HPqEqq5udlCuZDPAOUCU4cnMZwaGtUAXq09uLi/HFSY3fEMt7KuNMmnmqC0PFEKXoAkSze4FtKZvrUSSpCJYAC6azfyzV89kcr+nFRNfyGzcLgFlr9Kler/4LIF9R1RGi5vG1cTdYbom/xsbbau/ilWQl6LGJTjy01+m7RlQj5j+b9oscI0nxjxbD6GWdD7K10Kw3UjFauobmZh0bkE6C8EWKyfB89KMvqhi8X1L6LctoBL4z+BQBHhpVlQI+MPHkD0DezdLDBdPXLTN+1dyPDxXqttpe9UYchyDyaj1JpZVearZ+dGyck8juVGEyynuwSeTuBkHDLynLEAXks04I/M8FaiMcvRxrwMzVmzqY8/aibSDelnpwAcE/0H7URcRK/qD9d/QiAFKwhO2KNxcCa7Ojbjc6H4mzU24sVkkt9ua4yB2fppa+hrFIctR+OEYgXxzl5rXZ/60enuSWa8TrjadEJJRfVigCIzE+sBsXBU7RDHn/z+f9KODa+1zTGwPOObguTVtGlHo1aJ4czDnf6UUwv7z2D9Whoj4lCBW4hAK90MEeJfQ49nwjZBF+Wf5U1qzraneBjx9LPWB6CWpPn3t1UQjlfCHVfc6hqNkLhnhLo2Zwu8NFcBOR9kyFUmPRxwTPlFfi9+OP9WzwovqiTNquDRxGo+NTwmHTlqnBZez9UMDV+Wmcvds3BXCMonG9Tf9GseYYsoMT56KJGaFiGX+zi7G8Lvb6ocBLY74NTdkHVm19cZwgWbHZkK4okb27RVxmgug1QwhqL2Smh8aPgFS5BhTh22Okc4tkQry3sGWE38RLDiTjWQF26tOneJavgSfQD3jX6lsFkC9LmTMS+K69gmoo8F5YBrxmstwryrI2i4bRf2YTUgbhQ8LN3MLQkh2iL9CRH1E6UpQgT/kPsLMkLmLYSorebi56A9g/Ub3nVXkNHHRwk+RIIKPoVBUK3JpaX+/90GQIpkMIc5N/QrEggSEKc15kuTj6HXTx7Q1PJQUb+jtgaLHCYum1uTMdBzq6p8UnaCsRcLgA/ZmbC+1POm+2m8q6CNypn2syWyJRoawju1/qy3WRvQ0cXoxJ/qjF86hK0XvF5epMnmdFNYoUJ4p9mUfXf7rtb2Dly0YExPKv/xDJKfy5ur0R0eZTkaj/fmoK6rppi9C38dnGlShk09fZERG0H71BhluWzDpsmQc7w16pPiKzr+p4LM4hYyqXsXyKLrrlOhuksVPByvH0p0Q5hXQkLwM7gWXF7JCxFkcGNOBkdhNilh+s2T6BHzGar9Rsfz52c58RgEJmT4XKeYTe1SGG2qrHqUA6oebVc1u22WFPx8s115UO0ToscLvcjY4NNF6l5RExyDNu9VRnuem8gFRE2Da0r6sU/9CYHl6HPDoIWsH6BHposuKEvsYkALPtfqGvanXIETdC+Fj/k8oMYZwOKc4bMbb9LHjNWuk+X/Q96bS1pfwGZjr5C57x37cFOKLCji+bwJ2S7y/FPEGwCLC86qhUzNYTI6gmS1ZmDIaap6drLteULomrj7E7LdEofi+R6+CnRxe58oKhBe5+Y1gNQ9OCK71j//GPsmCrxaOSF+F/R/mgkNSsidYbyG/rkgcH8elzCX654G95vABqiKLyHyddS3QSTV6sQiIBFsuYKbGXzm8snB8cIrljIb1aQYg+UJksDQJHtuAlvFDaY2zrOTfeUSppNapB4NsWT1XNsU7pgEbzmc4avPC5gjn5h2PRJCAlr7CgpWGQg9S13jWsSYqU+cTMvMMLiNAn2r7SmadWATqbwY1wky2mBK3YdL6ChRV01NRAAA8o5pgoIjiGt+m/HvNVsdomfKx2YQMndBm62SwpBv4azyCfZaz3aG8wKh1TZtmPOp+wPfske1bBjsMoHNZcVGRjOHuCGRCbB5ysFZJTa/IBH45mSr8UsJj4TGe6s01oIbIXvUrUH/KLxUGMpD8AOWh+OTgG5ceeGZYbHsQ9VUdxhp/wMBslX/5/z5eTX+bYRk/dRZIlCyI3Wiuevw7TGY7PUwA3bId4YNr+Wg0P+4qFgU82pJ8+k2tzyB4bFYwrykD+AkFxUko/W0dQNEZNa0Kg5TMB7eXOLWxpKwzUyfhemlF+u6zZd/GBRY3re0Wa+lbIaxVCtlQn+BIC+ewHRy8z9Ey6eYpPxsqVft0GtJxn5FHimYdDwAvTmGHmZh508gxPzCNvgNea4QUrAtv7O1gpbPeHFznNdIa9eDZFyDDgch7HIUUd1kt9pVMi1VgMYD6zTgDmM0OnJ5ZjzcQiVVkYXJ5DdFv/gZ9QYiALs6NQrRUO2bb//lN/If+A50gl/tl9QjGpZ6djqdBR/APezLGAD/voS+PyM0324e7sU9f6PFk3TIU6xGgtVnDXXWxEGWpUSs3sfM+wMOSPRxnuepCf3/qxBj/WP2j15G1OcUAvNAtCgdgTEbvEOaTgMCdR9WXRMV0qHN3JCLuYS9u2/Xv56eJDXks6l9vTBj+mOMyddafyVT9BgO3ILFChHswhhGImG5lXzmumARG/aZqxBKY7pVs071m+xPgiXQeQoyaX41xzm0tk7jPyXmlIWEHiXRfVIeqt4nCwsGjoMbnOeVi0w6sGwGBRRyreC8pArXYB8YzgdTd1a8r0L63C8zhDHzBUiDKwrXOKrhJ3c1ArQzmBs1o/teU4X8Q7QYg8dCaXU06BYDd4NZ4F2ACE+fKimrvedmHRHnognNLUGji+QOc2pEprziFv6K4GJ6CmwLNXwAysN33l+hJfc+suqyI/al0L9usd69t3pmq+vMnI4ZQfzOA4BoGZeorgiFIionbetBjEhtnncGJzLUFTj6PyspAy7QZ0afPyhIIJVX1JyDJjNp59JvwsShT/U2/Pq3vDisnowkHV8+o5xtUEY5W0m3g3Buv4cldyR4PA6vovCwrj6N0U3l0qkqJzH4Ls553ul4qX/AQAD0ZntfWKVZzR2sNr9hDg50KYszq4ZlrEpdx5EH0nrNtDiCL47vWE5gONJsGh6umt6qGn1IZgFiNvkZWf5m0uAm7Q5gxu1e/kBHfQph1/ewfAhCNyHp55nExUKuX/nZvjI/BM2m3pJQT2xRUhXbEsdHWZJVdmjX3YQweh47xhXf0c6RLWphodyxvyLr3mIR6RvFww36l/ykZ/NvORW3N3D/c+WAFBT3hluOxf36E5uGcZLjp02a+aC3T7uwcr0Ro2tL+0xgW6F3fvtAKpUkmcwRwJcPyH/1gYJnw13ecT+rjHyM2xTkTCa0fY8i+OVeLjsnpumufXWHVZ2yNVWStrPYzEBgk09u8C3aLMdolOPR4bGdB1VrXpGdGZB9AVm7OYyfjJqkaQ9fc9PRQXcGHNrqa1/mGbaxOcl2c47qg9xn4jMC573+hBoxqj2waeWjgq6qiBqY6QX69dBT1uUnAkWmxG2HmkLlvMUWBiGyaTAhvnjZaGzfQOlvw6NlSv64sJxFVvYGOQyvNg9RsYC3JK0yu+NwvL4EgLzNhycgAhHpZIM/hK8w32Zxkj4cQvNE4ju6gYnIjA5ZHegihn3fSmmPBPuAYwll+lCKSaaR8/Sh1akv/ATDxChdXrPal2j8cgz+Kzt1PupdNWjOO6qXJ9LD9at/UW8cSqUfnBHnl5Y0G2PLcifCi4bdvAroiAtiWjK5awPEr9KnFmwWsPl/fMUIUVma3ArCStvaVzAslHzGT44tCKWTqJbNKPDUwU7jeFPWJfwddym7x+hsoLVU8V1fxh66iQEhj1nD9VFmo0RkaQbXxRBohpTvSx8ZQNLH84aN4ImSadhn3P8NENHrmLw2kuANUemHQp/QhcBObByV3PK2f4JZ3CM9iDdRyLKLKvCcONAb4NZswtNMYkS5RcdrIkF6aDtkGNDS4Owo1sy8AD9m3Yicvacq1tJC3fHp+aOBqh/XHdIFauZZ+jE+iyFHdr5e5odFsnwHhND6QtBQ9kxo4RDDI47vPLKAL6iFSGW2y3iq0vTzXLsuS7rlRxdAP5mzMr92gg9H7PfVeZ+Yl1LP9fatEE5YUegCv3awFJPdjU+OxvqM1j6/aGuOP/Pyt+GzrN5KRX5SHZCfjSprY0FWR7dGWR7OPChgGvVfSIW+RcDN+ebV6bBfuXI5N6/hjA8A3tVJaQpG2wAXAGAHQoL5YN8MHsA1L3SZYmROJjY5saqf8oXjgfzrq5kId5Ene1BP8TQ/9WlkmRlNu4vbYvCr0DZUVshpTipAApIt48y9am3NkPx4hRyHU1m18YGxlQDsXs+tti2brIAxci8jUN3oWxrtq6C+Ws2hot9v/hm9CPpugcaX5riYbCErd0pCFRKH4otDhzHiawZjJSNi3I1UKqQ9Juq0xTV6ktteio7j7Tmka3eb5WRgPHUr2mS0JswRwZeBYBJDXQ+5ZGCPgplBgJK5ZOHkTMUOb+n9PmhoKipD4eVufcN4yiP0FF/H7mWR3LnyIkoQmSQSXtTaLNk+86iHgu1GEC3tL0iI/DvrTs/Id8sc/tTtqiINnIHtV4Xe9eyq6TgwHLpT4zmBG695vXO3120YZjtDBo/+n/BWKTSKz2caooJJY8qlft04HJ3YsbH0tkFmH3ITJ/IlB3lvLfPH26r48kU7Qcyb7pzvVnSJHSlYHvanKpGFiEKjdwfUhwm11OPya9cvvyb5z/Z6ePFgrjfW/fQsaryq/0Svn3Z6biAc8TC+k+sikNcteq1pw6DTvzvsTIe7Pqi/OuaThCLO59oGRtA6OEFaGrViM+Z900C0IXfPscDOdQqtu+zBi1saI6YxRZRF+kNe5Orjh0Jh5X5Ptxp94g56zvwGBA8opxO3JxhBZqN6UL+m+3kWFnwu7q+zGLEyLJ4j2yxpNG1vAvDBHNcA7XFiOow4j+xgULiBeHRini3aB3GoZy5RPFbp/2RbgTYgw79g1lk65XbKdPjSf7MJpWK0kMWavg1dsLeSXp6HQWK0WrZUr0TWiuyjenoJHH789sO+sHCgiWsriZBfFLzb/buix8FmUqtDI3skk1gdGR851OtkTrHxA1NSLYDon7bkFPVc1hhdO1GSun6Hz3Pu8/d/hFCb102WqV+Ql6hFYHbzfrLAS3XBXroOdsuwZa1u7VwHkvlOOu7a4ZLTyXc5jci63EWwsJ74oGZGCaGB61dzE9F/EXl7wdUpxz/acP9ML5XVptm8SSeiHqO5QPa2GU8C9daDFz6nTYeZTCEzv7nSef1v+uxWzZ/WHyN/BFGEGKZw7fD3X7GvZ5JNEmmBsvs3PHX9TjBlX+mbCSiyR7ZwsSUrb6I6voo0s/DWMo2netuuGjOyM1zQWskMBqZWeIJsisr2aS6BkLPYRJXfVtvOB9hhNJCCV8T35AsXBLjtvftuyLk/5M02jbYgDG0Z06hBVNsGjEWQtTDOu8QcdS9EzGHvSSKPGtHx/9wzQPwuNgDbrtkBU0lf7O2ct9i2dQwGSBItxXz8h2GDyx5lVBegjpnmjJvySaNia887tz44KWyFDh8VK6msHly/tw3hmlQChMaDfycMITBFG79H87V2XhBm+G8cyxuZGUsFLc646l6DMcboSL97SQIeE7C4eFXumMDHfs7GY6I7SOzFkQXbuZjf9HDgH93S5yR3mf5Gs32lu9uNH1GcvnB+M1t2cLdhmM/weICin+qM+d55Y5DcsqJD69IB55MqNfzPjnneG8ORT5Q78GlsqNoqLEG2f/UMBD7LqhGBDvju+EPLN8u2TWmFLugHNdsvVSfYv3oHNGoBShvH0uBsLnQUYwVyFZWDRxmmGmjpRNRAQFTcThe2hs71x8R8KaNb2H39oUOdtNw94u935QZQlCtl46w9wWTksCVTAlim8YE42VIcHqsriAWgSZGiGXRQdKgBWO45RiGqwduMAUovddl7j8niznZV8fAKR8szpPAK/bOEyfOWgLOpwV96g/ChAJQiKeefvtmkgBd0JvcizWggRkJa7vwiMj0Xw0udWfUdvEsvvGrsR07l3Xo0dNiqFTVacK3OISXbwbevJ3W1BrBFgqclJKHSB9JzkeEV9j5k9FqhEMhezN8wWZIBAlIB616hBVwMoPsNQr5UbWBWWwCZQsvxLLDqPe2+0NZR+BYDn7v7oghu0XY+qGa4uzJdY9QDgeBHzwZeMfqVFIgYAkGeh6gikEKi/1IRL0XQ/gBRwhHPUeWWNstz72/KaywzpcZ2CUFdNOm1svyn1ycpiQofjv0Gco/2mZpiH8T4gA31fJksgvOPDXGrD8yDo5FvtPfx49Vt1S/IARKEqRyZv9QYTrvm8wkJSGr985MQEg5SHdqhH0QXJq+UZLD6H81QsblJccaE57mBG0sxbsjUSFO7xoYNewE9/UawtcbIyWuiGgwS1KsfEYYUOAPMcIaU4umOQVw3ZfINFmEQFHZefAdjkt3IJNpb/vPjqI1y2pIiyv1Brmku5P/XMWqWnjAYb43sa64BzKuqMceSfXxKc/vTBLD8YLpgFCdPd3lRKrl24T2eR/FzN4JLWT5TbET9w7SznkuZu8D5jHNU5Xdk+JfKCvzSkjZFSQLgPhBADOm8HGX5waJceGrqZDUvj77al+NdnHCCnGri8YeRz8i5civIJj1WgOQFSTwjOWa091PiBYCIFzhdS3x3RLFA8tOkRTo+FJkZC2Y6EVu0zy0JX7socRw40MhjefQVC0SN+f/c3IPtL8JXTKO5gAM0cxDx4RA3d4+dnq926zsioG4rXQcRsORygowFKwZqinjr3rn0YrgpKSNZ0arzOJ7cMYkg6lUjhiIvnnerU4WeildhQgYKgmBkOvHVNdv6tkDGsuv6NY6F/6ruPfH3bnIoiAXPTeJf2rszpxNnpCL3fPJaQfzWEWw0+d5C3jx+MbnM4CSt0lJT5TLsX4khBvbOzs4928nWjlyBoGI04nctaXM5kV+4oM85o7+SwnhJ4vqqRoNgawuSEP6I3CGbDhw9dgnLIvarQfko+KyOBnC6TQ9vw7yS7UXEM6AmOSHoLsc5RroXSrf2zt922nAXqWEr/GfjaIcyzWWjafkg68nKCDc2PGmNBbNBUGNds/5r+mlhS2IBbBg/FR5VRylUUo0Mb1QB08rla5FRfDCL0qjA28/Fgmv/QBntxkru0FYn4xu/xcdZBtmXWugCTo50C8K/Ag5eKjQNpIMQ+7SHOeT0ViUnGyRjE45H3mWg3NBEgG1BwrYwTvw07dKpjc+FiJPJ3fPn0tahU+NYBvYZtBPN1r3c9wqXUKdAkdxo4/3TPyAjkRonVF04ChWsU3Y6UwbqC5UtOtJ/nSlQ8t/WTXBOydQOE4iMw0NJ3YkyMMdsaW2LejP+fW02ANQa4CFuxvlADutYV2xLiyPxRxwqKGRAAgUCfpIYZ7AIpR0VIfJCl2peInDLwnjAAzU2lsztd2bI23KA51xb2pSBdUzSOb2bg0tjQaIS9wo88SIdzyrcMIqpSqJI3Cf2yKEc6nm+8t5Hd+8h++X92MFlxs36aU1uAo+FqWpzeLiA01d19XIGLuog4oJyg+b17Ab5bDsDdIzl3UMA01KBIcnN+803N7y+heeOqB6sEz7dmEqq5Vom2i1ioA7rtKdlkMbigxtlTqJw95ZVcPwaxR1BpcVzfmKWMCIaCbcgyILKaRvTkBWEdnPdG0KlAslafXZydU8bgV+YtUx6qIe+LaEMibeKi48cvFyu1+7t+4quZ9AhBwxc1t0R2V0vTL4zxLw7wHn9w76nA0ZvGTJoXJ63sIiAxMu/9KtOM9KINTeJZ2+NEJnq21BF/izjPquVAXZXl/vg+GDyiUstM2zNusqmX3QnuwvcWBE1Y2w9vfdygkCYLxdqhPjGupdynMsthcCrjigwr/GSkJEYF/lQ1FvC4U50TEqUZG2kzgh59HKavdBO0fhUCePyBPud7DHB9/AK4G1hXosZzObw8GbKFakcSLP/P5NfaElznDsYiAErzfSl/WnXbyuAfKpsGoISjsbcQvc5qLZhEBYm8Wk1PNE3NQjT/MTKMKNia8lJSGvlclCUMRaZiLRaBJ7rdQwA66EezDPZFUzNkVHjR/dXQofwhq6VNC/L9xnkolCfJuzOIHu6o43VOse3B0VryBABGxOMfYlhFntMcWCVg/RTBV+xWXwB891KctZeddKpSV002S4MYX4+zdGvDL4xghg9T/x4eueGZJp8p0Fpr+NBp27ctDeCHwrcTf3A63q+dSi8rUhNIBvK9hjH7Kn+2QbsVAq6frymPjrUdEkW3vh3ehByfkI8pu4J+RZL54QuKIybTLgE/qBJOyw0hEsP1q+UIUBzvk0AZHfdbB3ih5wCJxsUyKMPNb5d8qoCP8uO6vEhfias2WbOMb6d+4DzSc7UIHlXgmm1YhneL+/6oaPwU8Ji0ojONWeVEwZf5f4A+8UGdZ4Ua46x+E5+KnMH6JdBAHIKMGtITg3FN/DzkPm1iq3dwKpqseuTIjetTryqFvVtvSMhB/hdQWeeoeyk+I4LNB0rrAz/ToMOccm6ueoL8lWV0w5tXeQeVnVtID802WC8k3j+e0kXjaZViUIIXZb0lfgVizMEVNRJRHcN7EkXMKzaeE//zlXSTJSOHowHTNfNQs9Z1n3qrcmwtVMJOX/paJmocdu+cNX4nZqPrrk+9c0/arfTKpLSfAMTRb0j+bR7NMg/hdQAfUxXOEVxU+DDIQq0F0HDV+UJLrD5wYgYEsw/f1gSx2R2tpO3P7F+mJ3/q3iz4cGrNT5tEGdKUpgwdm9vSdUsOEZ7AtpCkEpv3TuTPQyA5+9xO7HiisSJ94RVWttPFRbJn47PA02kzqELgXL4iHbpVTvWg7RbEcqQEdZHwePI8J5kRtuDr9548adVdfVWZGWqakbeLSZkPW7cn5bFyGxXx4AqMg0E/1/ogNaRbMvVdyRz2ye86OQlg2xDSH/4mJfYTAddPoM44DGS2W9z3sjdN/+NwgHL4wpJBviuRilJKd3hYA1E902c43Cb6RIH6/p+O4Sv2fvEPQBAYzTm0BtHRjkoClmbwT/X0Qx/+dnSpZanV81AmALah+2qaMy/Ci6SilCs7BdeJ3lQp6W8v7BfKEfiEFtihYINZuIPGCsPPXvbOnzNaydXMbH8LSbPopGWEo5D7hEjhIU55RjZ48GS8H1UQ3mjyOm3aGnQQ/sUIZmR8XvD5d6UhtlkfPEYL0xHBbchJ55bspBA7qOpRYV8czLFouW3Cbf9dK7ZudbCNl5SL+IXlqoTyeSD4qBOIgBJ/dVTkM1OzX0WCNiXa48M4z7/NA/BUQLGc2Sht2l6hg6OfGL6FUQTtza9MxwAbpzWqtTbUV/dwHECG2olY5shIYd47udUUsOXhR/FRRhzLNeBBf7dUk6wCZk/QgytMZQFDRtQxp1NA4sminD+W3MK1RJgedDJYM5qB8UqFdA64RI0WyVnoRpgmLg8AeIrsveHUorVkE0R0ANMl+46u2Js5eg9ocxeLxGUWZGJMcLXtpnZRxjX2k+XHvJPX1Tafighrh3R3BD0qo5nox79k+ARNJboxFiaCbAfFWNsLV/mZtc4wmmsW+G53ezJzQNa+WINOjUk4r6AcY62lM4NZE/iEPW8/ta7t2kSURcCSWs8GLjHwt79I0B9MG5UqJ4ULChMpVHPPVAkrWZ9iAgDwqyU4xgbrlgKg+rIG+WjuWtXNTGJxFFhREWSWkfAKLrktoWebL5susKAtxUBzwFuieQ3WTwgwisR9o9l3eZbrSC1QCCMppiuAt45s1/9X/Q2VMOkqJGFO+eH4gzjv5ZRbBQruklQ8GhkVqBWRHiq66y3w9Z8HesB7Vj7hX8ccnJGb80AGJ/NfpJwf4CjdwkKQAI62xxdCvb9/m44txMY6RbRLgzmkIjUflRLs223X+VV8+FrWJA3Y2COxQ15piJswOfKsOnZdxHqKSRRf7G8pFdzboefZEhFPqR7tLdrc53sCcnR+UIMgmOZUkSwFQ5+m7m6L+ssKI2Hu+3ioiclT3GW969LTli5QP0nOPcRY48GpHZpenojwRnQ5HoZcUzOF8632ppUXLhOyfuByXe5JH28fSeLv41oUnDF7ZptGQh6NOmxpgA1UzyrAvaNtOAYCbNy9EfcyZtaCGhx+3N7VMKqsNx4JuExxIstO7LbuDKF0uqLZMCeRs/Q1bDG2itdXDRDIiW+bnJG/G63rGY3p6IyFC0kIy9LY0uSgIauf86eNDIFl6ZiQrIHxs7zRzrvdyGTtEsqVaYqnJO3Anj1GXAfNq1fivChgIwoLAaP+WRMTU7eEhvJdSNmOD5s4TCrrxdQskLbhTPQerCJOPFd9wjifaKpCJNvXIXUUyA74YSQfJg8zQgQr5SiA1CwjRsnsaTz11PCQzgcmFcSPOTEItXjE7BS3uqAMmRD0LsQOkpIoTlNntRTxFtYJB4yw1OBO7L39zMhztUg5vD3S9utEumUGtOxp5zdtHMT6ArEhKv48zHGD7c/l4jXTaf2jEdTt7e0QlkxK8YePOUfiB5VbU6udtuSLiPL3I9mjnJXgi76D/6MqgqHn8pgE0ZSNtu1MBWpdFgumDfzst7+W83gidiPf/7tswYuK04c96voHh9XMPy1A39QKRUGk2QP+X3rzWF76py/oN2Fs3Xgfxn1pTtLWJP0kPEWWK75JCM83hcOcrs7C5bkYPyUF15r0jyVDSgWiE4EvkqW3t281HK3nTYpcM5ueev7XQmpJMDfvi6kWGdysJTr0V3UtmHbNlYBONsqrdw07KpidRHqAVFJCaZqYsM+1ES9ZkfkvPCcuSV5/OH8GLctLGahlYAaYwShHpRWZu5T+tVOv0xvtsinkbL2nQY8bd8+WH1j9eAeZ4hNDF0XYkFRbNzNNogMBRc6iROoCy6K60+ZStC4BTP0U5ubqkcSvM4N+yjNC3e5AW63xW4BLC7sHvyj20Mb9hfrAi1ADbnjIeNjnI+RdCDrQC7dzY3ABrWG3uJmHl700He79O+kfVahA3KojHoeDhlwd+l+xNa0HHNOT36R87xESYkVSY5fcaEEmIOO3KBqCkIH+NWaWXE18uFrwOtm3dSWhZjFsEG3aTjXILHs6INmRr0qna7LmHTVHpVEuJa18EkvDQ95hm4YxBOypwJjjzliKx2bAKWrAQ99T0k/tk6YU2PwhA1ct7L1S5gjRM9ypVw/Op/gqP9Uv7LosBQ8TBbBAD8lrL9O+GrEwfYlr3LKe/MsYLrgxig5Wxtev7qjit1e+58rbKKHXqn/crJCBNqOF/BFT34tiQplgVY6CskeZxyS0EzlKKQPylKz4OKkD9+jk9AB4CCwEhHz95sxIwWBH3/p6j7oQFe4yBRRPXm1vewAuLK61cswsXnkNP7O0phF8/Uv9AskbH99Ln/NFoQET4pi9FukJRDVOXbimsecUE3P7tMM0ERSOt2xinoxmPJR5eplly01lhwTAv0t7HHzvnRW/j9bisbRA+QyjPyEoekkMCoFEZN4ehXAjtON368OBm15HaxwUUzLnUD8YYrXj67nlDPOsh0YsyU/TgwHu6B5JOcNMsRoVQ6+oVbDqYvolRPdq2ziOrxWn9ynoeHlqjf2N0OTryN3LE6krtMJ38aX7qmY6U0lKqppc3A/4l1+mMEm123vXdbFSaxZUO1R59umYm9Xt9JRQT5lqYUBNqBX6OfUg8MtvoSfx8swGnFyOj4KiYzBUW9W5AmjUCTjoyyAvRs96sj21/ohXt9PtygjgEhsT5IqEX/NMDMRSx8O1OWv9ICsv/AfiYDLKjB/2jZHHm+sPrR50MWXlB8qKogK45VVh4NHzzTuQf7iRA+kQjOxyoocvf5nv/3NTvFbOvAn3H0gyVEhy4B3WcAd3ef2jBU4U0iRGOQ2kXfuXWkX1Xo/v4ud5aD7cPRMuoRs5CMDl1vl/POBRLgQw7wK7fgDafG87zD/60w/r5em7y4enHwhpMaZzQVfBYqZYGXZ/ymGs3T2kEmhLvAg1e0s1/FuwcJ2MknqWPfOp2tjI3MYAP6EwCoCmlWXgyaOM2XZ7DopUdxl5xNty4Uxue0CktNzeLsAvr/bAnjA9ZpYjKdSsKDMYWbQ13ARNMM8bMd7V5xheu6hmLhGecYNssMKls3Co0frxkngzjBMzMbvUMFsZoaYo5cyIwVqbPxXM+nfZJim0T1XzZxE05frDYbWI7Ehfd+gaTxClSyTguBQL8foIYwEQTjNPzKuGgchY1jfmzJarGZRlUA+84/JruPOTmCyE+xGffDxkUVsQQwJbqAiqQvj8/h+xR0e5yJZmtZjv8U7zGCiSiFlmnzvn9GNVaCZtPmjLKaVHZ6+/HqKqXpWF8lhmbznd/dULBPTHHWu55wdFzWNN22eaO6h7nWCWVLkOlRUdndudgVLKwq3+1ieC0JiIcq0FRQRzgaAs/G/bHTK5arH2RXsJQWtknpIn1q8t4+48ppV8RLnzhEFRcPFKHfMFznP7ZS8xLDl8Q6ILM4KINpERpTqjHByV4joJo1G/qoI1YZBaKAj7/O7d4fZNuQxz3DznLVI19DeQZBEL0jTqqe0EsqNr+0jnLcJocOoJ2ZmtbnuuFi5L7dbU/ccuIy3LpF7shM/G5l4Nttx6Hb6H2V/94Avj/R1w9Rkg17nkr1hg+wI765lLwkAc0S4UWOFNqlmckgJrmGdyJcrVyGextYp2MYn6Jhco3ql8zDCA+5WWHbeSumXTMDUnqDKQ6MfiStxTg1B/bIWTIVbLBXyjmUwuVdnYA+ItiMRzQ68DQm1WQCAXffV27tCY6tWFimaQt+5XGhkJLDyhbKOHmNvTdePGDAhe9UESr/dvHoaKqgUhRNPgVeyj8Zl0rM2cP5VXHrtuLRWACJKNXBw0+AVR93dW7gT07nNRYhdg17vWsRL/dO6EbXlD1ALfLp0Llt0q8Y9Lq4HX2z/FyYX052rLSP1+oi8TGh6e7Ji2Bb2Ug7Yd2H6g+P/0CJfpSSqLnx36tHR9DOYi4BHmg9a5AkVWWsAIn7ygjPL5Ix8j3xtARiiwub4tBNXqHfGYMBrqB1dezW0xzqN2s9kDlZC9ZyKJWY3fvjizHA2x3yqYcB27h7mleAPuSnJfFo4GZ1L1vvArJ7gv2P0g56g78C+G3FLYHgiebq+T1nviOISphnyOg962FkhSebe+CRgzeNuDO51ooBfPX5GOt3g3N8WdoZki0qAxbxBAdzVJIayk1y56q1Xz0WSqKYGC181f/h0OqIfDYw+ReMe97GlEk5n+nXrW9xu8MKEewQDpCJ8OOrBE6rwqVMeA5/vyopFnYz710BhkOrdd/Wxh4ebjPVYs07QWP7KYUA0tTHiT03h4rOKI6w+ZMhPOlPp2ljdvaoMiQGsmoT4VYsT/yWcZ4muAq/hdGlj6HrRIMOUQwKyV22OD0t+GPBAoCXLobXH08Kwe+9gIK8Luj/0V/vZ4Ec2el8iAgzTD3VB7s3aE2VV5lCk34W0i2K3XBoLaWU5nsIBI6XqBf9NDDhQ6Q3itOKl7mrEwyRAa6XUuyW2CRP1RtQI92nA8vZMzMw6yUBMJM+zRIl7vl5fldSfaTBUEOqEpJ19J1RkdqGte5otff7BJ1GRALfwWModMYz51/IfRH7PxPVSzOrmW/nakWkKkNUOcqOTqbWwPIZ0fFkaHphM6o40L8LsrAZuwAOckzvKu+bZZj4KDIt72mvemTV6kJZkK92cFjpPfvr2EdmV7DA/Pa1Vg96A9kmMBpaK/awMTk7GyEIvTaaJqFRMP/e7JcHRhzpKaDsg8LDI9RmNWUgNg2YFz3R8VOw/JMPIVpmYkIE65rrj6m2+kJl6HHdseVoDDCdvpRF3aVOkBznoPC9+GDrFLTZ3naye2La5gAIFieT5C31vhFm/AYlePbU0PIJ3qq/DTxsbwDYEgR790rL+BB42CpovTYMiMc7Il7iKJsCadcYLJEDQKyT8rOLCPnTVWHqJpUNeVX4uUkQ2My1toj7sNv2c/V3YUslvdSyOR4uWf8k15RnLCu1cETgigVyGVJOhTxGAo0HXXQ7JImKT/YMaB2JZ28HsHwSanu3xiHiUfVJB2LcrG5ZXD8g8newyZiUazV20GiC/WB2tk4LvlSuS5aOsHLo+aWMF5PPKSmn4LFm9rN3CF8eTz+DmaUoXz87/YEzBgW2gWFJFbjih1Onh8yykKhxapWQBFR3mYy4sHpLEr4IdGMvKDuFYSLapmjRQ6GFqw9txo7GjFLpih29mz3IUkG0ztawO7CW7jigcg8qpDlmUPEX+jxmRUnZbFZ0DQNcv8tBhv2eQ2+7j+0JOittG3a/ggOTthEqq+7jNfXB5VodZx0qhrLBsv1r10l/1eSxSop5WoflBv18lOSLPKuk/AbeRmTSD8/e4TmoDuAu3uptEab+avdZtoLe1/4D4pBcx6hhpP6SlwyB5UpH6qjdlYZRL1ZJlp0MAENsSok5/4iSs/lEA5vp4SZ4I7PM4Os/6Ey8/4fSR5k+j8XR9j2kcHECLPsiDNPTZJrHas4MxVqY4jIcslMsFEZExJhZPrq1oEsS4Qe8+RZ9VCILeROLCPK6YvwjbVzU3NNIPmyEMKJRpWfJM5o2XEE90JuTrFGhYHUcn/7qtiM3J64KaYgNRvrZThlcGpEpNZPkDwGZZfs5S+b9VG+DHgkEYOvsZd5ZeSIyt8ljsUULTYLRYj7xIWwA4FOS9WOE/zcvwuudc3re1He+WEoMmlT526LGSZ9Q6Zv+8IhhuBBLTIS+rpa6iA7Yw0bME/IOksKw3XNfXr5WoMHUfKH5ROELLz1w8S6r/AMBQQfnBcUFlIa0N7VOjUCI0GS4QRehDfVhfEXCc3AeFK7AU9uskUk3IFfFQHSgAElpc5hADuavupE/bO7t5rDYRPPjYcMTmvjxJXeaHcrparXqO/JYp9Dy1UCQyZiYL/EXXeqlxF1Pivzr7eNc0AaCYskvyr9fMArBzXSC8TjkZhyqCNl73XPaBbWPzGw3EDvLUtebjpideA8GqlxurMm9mZR1H9P9LhKvd5T+SOSsnB4RjlNEuPvbCkOgYIW4YIA44LrSYVb80hu9eVqd7Waj/urhrnbrMgwwkhJCUkkwbIsj5PxV9mSO0YJP8qsOWLmJ4R9MkpSyQ57dG1c6TmwJJquTjVtHhqEFd2WfHxKuCosV0ARmzMhadFqDbm/UoUYqzDkNYX6VkTlLxujRm5WDbJLaekkIVmAptoDZTXqtzr0mpvP7WEC7J4fo8tzbSpUOCvQjxA0xGup8o7kU13mwXYi9QUIGTsRKunOGUbp56a7yYeP+C59O79DCsBIvp2EAZ3lI7u904BSt9Y8DqN/89cs/VObghypVEpB0JQb7nwIURHpA13sRLgcYL61kBX06rDAeGaTKOmYzSn6OzxtfKiv1ExhdY8+yAY+FsqBt4vwkv5YRzo45WyHfGIFuu8LdsCRno9diXad/tLeGm8A+lphD6m4mLa/XomS97LBhRUnIBvwvVX2GwSoMqCYorhMwbBa+5te5FXEakp3kFXR92FhavYWJ0Ylgxt1UbkXk+g/ssWzYHB9aYYptSHK3teVeJzTw+qEXnCKJysaaJfz/bp225VLAU4tj4J42X2I37pCDOyDu/qIzkV16kAr8g+YVm8Ew5gPRjkRGB/0SUXi+tBeLChRluljwGHfwxIeWDWyY2/ZU9RItGnw7QVb2Q1jYaIIhXu7yyf3Onte3DvS1NOgHXVZxDC4L1oQn7No52KVLCuAMTtMUUUV3ABlOCHBk/7zkazvfzVpw16EAfy3/DdZ53OXCLcv38aMoSc7Citpkm0laNHKwgwOX+kUpejH10CC5vmJXHHiVs4UUltoFNT+WJhHinxM3VgvT1RMi5M1wyBsVLtCyErZ/5BoVp+TNfTZxOKM98Kp+LkpQQ3Q5/DT/uVQrTMv+8OW+OkuRWYSq9t7dVi+Z7sTyfXy5aRehFaLGKxGhWmaTsiYyQQEOO9W8eMSbGD8OUQ2rrZeoUFfQJ8l/szTY+HC/U+bxGkRWqGGpqL1Q+ADNHmYHO8QnAJPruH5LJ2U7N6/knIsCNOVIc3YcoSKeUYZXE+iDvwgH2ch3ivSDcvswNK4cTdE8+Sn3bwqFS9vXPRSk1qQu0KLcI4eIpe1ANjpim77QNSKKJQR1bD8dywUmkfSCGypdh2K0xQPKC3fYmGGKM1M+wASLbDq8NX0mqApAQDAzpNcxrhLEGKOX2wnd5zSMz/Vo26X4Hl2dVgFIsC6AYdhKKhVnZArOZO35lhZ1/Rm2ygIGxK7/08ZELnFTBHVkP01idTYY/UmPrRF0q1Gz0BWzZKRq17Pj3bUqX3Bq5yOTX9llsFTfzDPNaeHIx9yfyANoiFzG3Z6S0z7iOmGTA2p0ZfStgRs4carcMg9rHUOWd2NPG0c/A61hBMDgULfL67a2bcFt7OXpyo6sGCNCnEX90bLCDnxFuZvEOigz0SJTzZGDyi9ZkVRF0mNOibJghQcFSGqhSTOodOPRTVMlw0PHy5wVz9A+1Fn/4eogEOgia4eQ9btydWO7aEIrb0HP2ydqtuz6C7Uqqi5mTqQqB9IYfBYEx0ol7wJafpxbUd7jI6fT+DlhXAVQj1p6955Xp9wVlH8nIVhkWxA5deErceWZE7Q5c/mOXnAz3+s1iqhJMSWuOl3u/RUBJmQSogKCxoUrEEMhAQU/l+Ld+rvDs3aRjaDkzLavkQwBiM6i7fa0uiY2Kt55VOcZg34pens2SDCc3bWLJ96HmwO6TefqnbLT1WqKfwS7nyMPSu+5PSUv0Y8dqPlkyLpr6bVCaaiBcTQkEuDqSs6re1nPGzkJRRMswRt/bFKrpxPsNgnNpfpfTrqXGsdBpQZLSwz9O5SGrDbih7SkE5ZWPS2t9aY285b8DJh5zyKIfe15RASytHeznni1x1D8Rxh/JfUzc3Ynh+qLaD84e9ZWoYZulpwlEBjd30rwopjwI+h3EH51XBFeyZgjxuhGG160bi5jKy8gew/utubJD1FPs/lgG9i9CQjIK5wy7qeSG7zryPzOeAFv1686ZDZ8lWfSBIX09wRxf5Rc3UjWxYY5viOT6/RVXqfttztRu47j3bSbVc88vPSmNENueTiEF5cBdQkBu8F+0cdmd+7B+mc1SGvATZJJcYE2VPTZ9GagS4g/hqBpATU7rDeF6xuCax04bzFzqwq/l/hz8K+9mIhZ0TjEj/h1B+bY+vtLn1ajfzv4Prf7lLIxup9IaESZ9rnJ1Jn15ChDw6E/+hkFkEaP3iaBCmIPIdu484/tMxufYBrzRU9GPAESEHLL1JdJ/zkQtRl9KvjSsb5xyeh507LrYzekNH/2bJouYTQBitRhDjpmtfE6EkCgWGl7liWeJ6D5TtwsYniVTymhtMHjGJev7dorTBszZOZs2eOSl88nM+hD1V/alvw1BwRZqD2AOggEYdhKMiUbf5jq9fzTvCQ4s6YItKhjgV+4F6OJ8WI+wI3YW0grpdnVrKw+aoDO1WGYwljtmQv4T/Vyn3kIY05SsuCAqRYR4Xh6McmxpTR20SNJ2MLIVC1lC7BxL/HpM8HrY0dwhQYoeh5RysJafnWzH9mPRnaFu8ymY+cr2vBUy7JkuXGvyytMMzgBze+PBNhwLMCBpCaWdJN7tozB7BSidvJutATVNmsQNwJj23FZTDDzr0FviBG0UoVfBTGlBmz7GPe0AUDmsRzMn3e37eITAzNIeOo7x1NdBZH7MZXJNQ7DwM+7kSZhOQKhG+hRAqhEzZwPhGvlPuvpBhBYOhaWsel/8fDwCA4LiU8D5kKl+f1ONRprJcauT/PSd0bEraqsLsWtkDXzHFCUdo7emE3fbnzN3nC2FV3zmsxAMdGWy1FKWIytTK2znqNOJUEY4w5Un4ihjJRM6qS/iQuTMLXAwfbCnDMeNxlMjVsPxd2XPBq5VJ6L4ERhK8ui3EkGJF0jnCh0nuxLPGMKQFYhC09iAlWpWEKDt3+YlSvBHpGhRn/K6Os4asnkq1DY83XA/An2MbMpi3NXXP2BLE09Sll2qc/Q/O7MYVX8U2/+SftW203zNjaDQqgtCKdArxSX8shQzaugatCF5Fgnr9FJ+8DVBaGLPuvFFL2XikzOanGfUpg0q2CcTeodWOJrbFSAT9WxhAhJkfkqiF8RxqGXuf92MxQKbap/i1MTuBB8Q5PQglQvdoTh0RvkeYxKVugAmAP4d2+wpa0bmXHA7Su/4YOBDnswXq/IdBnO7DbWeSsa8CgpZGWEkDN5bbmd2Ed2dnJ4xbkp5VUfpYCqcN9GzyEoRy2Bc6Fh4ZWqo72iHll0ziilL4OrisMgipSjqJsID7yVXg+DUuu51AzJoFkpkbwJSqZ0UITw6Jyq5X0UjsuxK2M1nXng0bggUPWP88fPMsvWT4MfT4Dk2uCB9g7WtymwtrDQk05NgIHaf8jmoZGoqKlPqaBV2IvzhdJNTgk29n+VSeJrn1IX0WHec3i2ZPEQlJv8Hy86d/jPi+dQLa+Ny3Ii7NtiFSOdmGdnqBG1K1EqTCf1AUHLBUnGSuwJVBr2HrO2PBuQDO6LasGYO4s5NRWYukMoexpxcoI1Gh/zzXwyVQWuzcKhfvVLa2n1Nt5RtLoZD+D38pZGKiS+Oed9agVifFTyGnAQaRrZCdNx3PezV7jrq1fdhM/PU7f5yYbhP+JmULfhbOO09gp7a3J2R6imRwmtan97MtYtGiFwoDqsYVpD3jqZi259zZ2hq5mJW4v11K77J07sE7D1s/ZidqrJv0OXtestVk4XQyTd76wWmhYCMC6h5SgnQNQQh2F2XeKxxZSfX41FUMsBvTgDwSsev7vQ1455xbeM0hYa2AY8+BW9dO88Jl66P247WS/Qa9ptmdpMrJHhk/ZRFyh+pjY5FVo8lR970IL7PFSzXehN4Rt0k1Ybt2k3MS/Yo3zPteP24H/tQCPyHaPPCdj9YTqB3qF9i9bii4vRe8c5emagk8tOBvsJkpILrbd5KevvwBsjC0mVG3rcLGnh6SUEdNjc9brwAf150+LV9rnp8n63LwUeAc60rtlgpWRU13mOw9DVlT76mPw3AjGRYrla5mIvWK++7wC5y2SzlvAWJAG/o9JS8p3WWjmoCbVGZ+Em5yf+wQVbh39map3dZZNoK+1JRI1qw7zhF6wjlN5WJkPDIaVrwcNBgFl3besk+eWjKq7iIjLGaF+hSaG8rx1bB6CseSojQlkl35Rogj/lZb+TB6WWcoXcSlySAsE4Ky0Dtm1fmYvp3A9EL+41L8drbTyt2QGYA+qSuZT2lFOObGkOI703bZMYc9ukzKVMu/lNUrDipKzYJ7z17cUM+/94i3GMPU3BWsAmPOIpltuK1/eF6ewvHZwF+jYSs2HhG55MWu/dcKWrISg0TxT/gbnh0I4jJ8KKX4rpQKKL4K7BMFaU7w8jIInx6adGj3I2QSEgdhkL8MuGdRNxUf/3yWF3hp4vjkZN2ywInpBrDclE17AcBceWneWQI+V8evHldxEmZ+4CioQsIzALwpVraA2Bwl3dThRAR3ONKl/pi20ElzVufslBsmGgYizRZ3o3kvzt6tDBD/og7h84ki3xsielualzpTpY8RzaP0FB5/VNSgRCIJiUD1cuvwVjGaJWLcTD0v3tvuz+k9Jn9SumRHw55aIwPrJcAZOmfZFzeRp0cmACZx8Y6Y81a0m/jBLMcSMw+hb9iMIGtQySqBZRnWz+J9wtHJmsXju6HmRICGW7Ve9FmqfvMXYc85iFvb4jPchORieYnzd/j/HyoNwBknIwq/LhfmyXC6fgSkwKcd1GcoNLuRUMoN5k93Sbxa5ye5VvlNc32FcB43T/CbyghH1asnjq2ESm19rZgG2kzDShTGRWRxyYz6I3gtOfzh6MYcjCykACNQXRfF2W0NABy4ub119Ede2pZ5ZwpucWpfiibJ/l7RLPQIj+k8cO/I1yeZMHecCz1i0Sekv8FM6k9K+drH2egCqFO1/vKW9qLblIGUhEH65hkc9WA1LOfCdTMteWQ+0osKXMvc6E35NX9o4HtVhvkd4KWd59ZuHjPD5sSO0q9me/EeZEs5qzIz+BSvQ7kEeXcsDcDq9/wsP8sAOCtxoxXZgA5rk15LLjvH64eIwXczZLXU5HTK1w+3DBpUjeFiHDd1jrEuLEykha294sYniBh1pz+OOuCkOTB4rQGgP5ygmobvEeYlJNdx4TbcSnnNew+G2kADWbrK3wq2Qm9bG4Xi9qJ1iXDMIFlQPp0PP3A8ohxvRWAeroMVaFbjvHvq0/WXu0oDLmqIosuCp21YaTQXguHMFokKml5/TDznCCDOmuUKbN4J/1JX8wmnAdBuSbA2RBUxL1szxX6eC0OToCDuegDV7lV1haF6EE0V6PQTq83DPYP3DNPHaSzMWBQEx9vbRhY5U64UNmt+T8uQJGR6EKHHbZxfXgQtjru3eRpjDnZy4plUBXfs5O+QB9ZyFrvFikaFa/hFpbjBrWCA0y5p4sn0y2EBEn2TNtXWaliRs15UDkiKt64f70bOYIGG3LAGWY7OHghppQLCoq9NceRT0O3TUnIQJC643pjsKl4Ebst4lZuMi4oWEMi9CWorWkc/LCT1H2RxypgsDywuEi3+z3JIBG6N5W3khQdlr4d3T30ysg4N2Xeeo+x5+fZ7YdzRE+OLNEWmSqGBmcKztcUkKOjjmnkfEM7hTMfGP+2w8dhJWzkcZkdN1cDcD/XnGRlMZZVFPLE+xCLHl5LlLDi4UwM15jx4vD5k2Cj6iQkPWaHhw3WzcFIkNPBgoTkPn3QKBOgJTe/vrnX09q2slfV4XGrtTXRKNa+w1irgrZultDQEYBWNO2tPQEyMfQRq6/FvIHGaEyPzM58dyOJ7IFS01IPJbZcmQfB27eRgJpkJoHBW5d0EXqoZCdfkpSZ2to+OT6PfQuuFDxKCs3yMJD106FjNX2y1w6/MV3sD+TzetpPc4ZU2j/kxaGfeR+1L3syZYTwX3hq2+wzu7gjBXu9HWeUIL54oJDghZ01EbOxxr7wzhwUqGaXMwdjVWpkb5eaBUuMEuuLbj/B7S3VxeRrGscIHK8Y4D3usMqpCpoAfM7LAzKWE587BX5cQGCYqncJuoVAXiT5EyzD8iupZ0w6gIVLtC3+/6DvPAfDikw0smx/sdPRZe3+iACo44hezblb6JEPZjQvywneGSKA87D3pXPGUyedz7M+KJpBQ1WzZn49v26QfbpErVHjhuVVek8Lak8YpbopqSxsXkxpiVNpjoYsKcgZj3rWBfxHjc4EqYas3U4vmBNJPZSxTPnvJ4S4AsFBxW8Cuud8pJBIzMFSPF6Q/kLJ6KZBzmO7eel89gKU1iaxECwJY8elB7P/zargjS8LQSZXYB+IXCbxuQQx34SvFR2XTAhlT7/42B3u0bTsonUE4xKNEMh8Ld46O697P9aSgnVLIoqlQ0d+7uN9Vk0Y+6xemVW3yNCYJyOwnZONSrM/PNYkK4kOo7lWceWitWNfPqwESQHFbjbRiEpJ7n6j8Zq5PDXpjEWfNRPH+XQR04pHJwPN9JxLu4zd6Tw3ETP9FmrB+tdhS6S5tlphLDi4xRqAhoOJaloVPlaXkSQ+Y0tO0hLSIJUwfaaXAI96AVVCaQFxuLVyccodCcq/Qeqrkff80FUWaD7xsq9M/8oQqZ4UPwIETEgA/IKDyxYHiWOTFk0AL5ymAJ4HHigDGKjil594jT42M/dq6LbDT0+boVl+DBBflHMK+/BdSZBfHMJgyNPQePrAWXdwqlAla/eQJsBll8qDNNaCy3ELsPsn2RsQo6NW6B3+7/Rs586Ougy9fCpaEoUJfAZU72TssWTWUSyfE77Dm3QOzytZZv375cEqzuSd8kDOLub9CK1zyxHjvr5mJV+8uUJTGd2n2rFPp5ABMrrAf4dLhGEcTuAgXxaLP63cXDGSK6QDegYJfrRmMCXnvgd7Jm4l4IGRzxAYkrD0WLJoI/RX4IJdEVbtEAkFRyNAi7Mn/i3pWoJajQT7kvCHYfNQctiCz0WyILT1yNbiruiyWyqjKCbuRnNWbBhAPd1XCvP7qvo6N8GOF9Bf+ElSmJ1FCLDBM0IWJzqRGiw0bookxPdu7wVwpfmNAr77QdqZm6DSQA13IX8lb8k2n/T9+fxGOUe90T8I0NJB6b0LxVK/3mu7Oe2id2OdhHbnbBDQgk4YBGRAXW53FWnx+A8VaeDyA0f6ezhsapEnCfrI0sZ6ZLZbaTbmVCvUlaacAm0EtTGMUS6A4b9JCH+rS3nLWgGxPPkZlHPkMp5V57Fsepi3IsCO+ah7h7nupMOWetmYh3IpV8ATbY50cCXRRMmTuBDMc0E46VT/mZwFxfo4EBO58zJIML2pjd6914RW2KSGOkMbgYuDwAsGu1M1FVp5xLriY/bh1cj+KaZ+PsXnszwhCJDM4dyeXHWbEy894Sz2upP4YYYkf/A1MU2ev0oru1k8cH0KzPZWtJpUsLWaH8EtciwDtj2fM0LrfgFGnfI/PV/XeQvZmO5csKsw+GnJhhnE6trxPQj/jHw6MLTDJNER+jR8MaAujlzuGXe3ihAXcolByL/iM06MEQ5JqFG1dl/oxhEJzuqnu/WSu3Vz19d3BT1O20d86A/vtrLW25y+WCz8fdDkXBvXeFbJYE/3JbzhtprHvIzc6zuVSfa9xbUoZ5L3i1Dx+JatkTBdQUiGXrht0YQVOPNjiG/CfEkWNExdYtMmBA86GxH5XNIUZNT7CVAe2e3K6+1DfK7UvUMVNBjLPSX6B1/YycygW4jMKe2FDashdPVqSIOlAkQEGfGMJQ1jQaC+jtFisegqIlXT4DF0zCc2rl3ksnBQGxye4R0PCm9BmVJ4phNOJ0ipWAhARVHaWhcTh0leUvj6eCOOG0Z37fiv0/R2ok3FZQwldGSkX+/uAdGr9MN99yAf+UiNKz4TvqVHDF0SQH9rHWoMHljwiHx34daSA7LWOqwa2xVYGseqddnrdlYv2A8e7AvIW71uqVXCuimRQl3nvUIO/BnenquBawPKxdDJfl26TINkOipoVwTdwabFNxr/DZ+vt7UD3AaF+QCNsNRHSGNo6jK4SSiIWcCRJUkx+2E1p4CWZFh4XaBgDJQ0GgfupDwxFYUWnehQce08Zr3jCRGFTojLZltFEEt94lmIHSHtVUBk1R4Dg+hChG+XZu8cUa1U6u/eV4fOw/oJOqCssJggEiYPPJOyq/y1uFHMRb/XDh9BSHHxI0jj8TEC+JNmCC4aiyLhzfd4WXyGEAlL/52VYJwxB3OT5eSPnXCwtRIyj3XjZ2uHqj6XSmcefTALHA6LRI8ZYgNlEj/RJmTFrwtQ8LyIiwAmkrCBDZr36pzm6SwlrMJ4lDMydRwU/OYPvNjfaUbljoj8eH1G9nLNxLpkVKSn+pgLZzT1iaBYZkOEulUWlyMqZKLhavA9MnFbpwFoEiMBsf19lASa4r0YacFGSJi7Mg6w4hCHmfVgCC66MZBd7mxt9rHln8hO70QLKqSdwDrMyOgBgjgrgi7YHcNLVxnqY56L2dxzJNVS8UQxg15eB4kKlLFsVAuUlA5Hgq2ZKNKa8j9NAfTOr/lr2vK/tyYzsGyo0pK5yCD0SM9pptD7VkMx9Gx3PIsEebm1vjcomOyr+tgDtMJNPiiKClpVWUHJAsd+A6vcWOS0Qd1Wp1jQz/4UV5bzbX6xom+dC1sMgq8xyGIloryS01CEPB+QkDMzIjI1l4gVpthS/aKHhKS10o3O4/iE6VVqFMLG8weadFtjI9kJkRWTDxj6lKvxhhIrJKGWGGWvkaDPoPo+3Y71RL0/ejjfH8vU1dgrMKSCaA7RmgK/0cPUd8J9PHxfbqI2VwV9lPEHMho93j/g2E3+MECBB69rq3uVfQq26SMyj0IuitM5jqSo0ISsYkWSK+mdl6efv/h/De5ziaP56sRWospjQEnos5AQQQQh1JlENfgL8KWtvvXesZjPGEZh5t60gFFH2nRXolM/NqFXWNa4B5frhKqksv61GqvGEmUZ0zg4AXZsU025UYp8c2hGTO4O20jv520GnG5NzqgPxc8eyO4oah4bfUchWBFeAOmeL07MD54cuElRq67chAjiMDzHQTR5EHiVgPiRJUAB42w3Ngcl8v4kYDKxknQdkpzLsmGtS+4z8HDX5nYxizro1UijHhUfOt6wQgb0Q1COM+b0QHOe7PIHZjTuRXbxYcXfDlrc1wmvhauL1KIiIuYjHUmnIGNtd0mDtxcsMZ8wfyWLtFryLbp0ztrJRepitae8awHek6dt+TLaiB9yPZ4mllhFlrKrugdmG9mWGefdXUBQTXl5LZaLydEpzvfDmmgAyBhTbnTU4LIrJW8pm/Lm4PU7o4GZfLLgJvo1jRnrk5SMTkv87V2P7yo/xtxzX18MVa1byOzKbPdTAOWxxvqzDneDWh9+6qwCe9L5XdZayosPJARL6usppoREpcP1VlpywtvIor9HgKKo6U7tbZIpDRhlHQktbUoNANRB9dqS3bqvOAKtJunh5/RAkGhTpGJFURrmZixLEk9jRGvueAAKnNfTPMs5FrZBkuTLj0AkTVFMyJg0mu22xbMbPsXj3zSpPBZ4rcQni5kMWswPTgxm2EB0qxHxyM8micx9STPdhL/Y8pNP6hImhA9HNQs91FDeN6OnRKrzjQA9n5kCHGT1TP5W1OV5IoGLw9mbKhJ39vIXsPzaSwVBwsQ5sv0v6CzZXYCbiwLHNSZ0wJayFALOgt61kEwGuDvRWepe65tIUvr2gLHmBbfMDhT77jJia1AFarGf27zKNSDk2Ku4OI+vcE7jZ9xzNyJbu1f1Ah8scFveX2o5KuX0ClSSHdCixx2AdgAi2LH/toGrj/pCvC59Q3YhypqHPjmsNUxutcTXSw8IDjjd+Qe+PhoWdYOYd6g2nOe0vBA+F1hTrEANcxjF3CvbLngvU89K/xWLo0hP2VX5Rqdo03nbLqAGG5OFeQGoCPoHQr4VbjSnQX7PtghFRFxZyBTOrlEcPUB8aREFEyFYjvD8PZ5kS6pZ0v3gBPXTdtQBY+wxW86rpwcJyL2R0Dq/+q9mnuzX15ctaN0HJ+FES9T0VoVKBNrFUGpmrCU5mdvqV8msibeYHzAp18Cr1pjTOP7NtFrOhWypd7fX1MOw5E/FPZNy79O9XzIkmCOakG35AhawfUY3JW2ChaRPd5yzuTJgkruvcg9LjyICb4qzxmsAzTdA7mtCC6rGdt/rbhQW/qWNYe8Umj6R/1Qcswq7nHqM+2jbC+5q6OaUcY0IaL6svcEBtSOMoSk+U0IHWgkgX8l9ngDfKwU78JLwteoVwWJLZ3zDBBu8oG/4KQ+lanZHlBLu2oaWbBD71zmVVjeNz4CHZPTN355tLDB49x1H06+CMrXR4XZp1m2NEFLGsVnLLiy2u3etUfMX0bkC1NzM88FKPqS1UqAAZ0pPTWrmQRnJY/a/XtPnJqBeEmvMrM5B2y2xEJPItmxPulsnL0J26pm+FDqrHGWt57Vp04rCfJVuXr2CTeFIdVsqHjHa8olApkotkpSeS9O+VsE3vPA3S9IoV2R4m6XhGsH2Vx603KbEm8/FE+eq7GobtWqA/BoHgQXiBU/qSL46aPk/aRS8AyRWpl8l2idPRoycFJBYCkoBCYqXM45rIr6rK13//eB62EktCjLmuXALhkwuvjWa7x9tI5LS/gF4FxApwV/K1cNzcWkx4rgM7qryD6OHU4haI1TMnIbf0su+3NuvhUHKvwdGGoCh43KN2wTP1B0o72W/PFCxSZkAwf2U1S/lMYciBFMX5oeeqKI5QfJ01BqUiJejGeRjkvt7xoWm5A0PNT7r+/pW+Eki6CaEPN0kdsG/kQZpsNQ5HP3TjYiqwB0ldASSc4Fe+LyQf7hFyG2zzMW3+brzOhOQ+XV3jc8t0lsbAg/DUkHKxvAubpC32HVw0zk4V5ZWzSe1Qe9CZARQfGrvCbXDD0C2X778+6vx2PwdSTeJgLu5s36RiqDB/aQ6aqOQNoy7ijzsk2w632sCiGvM4J9JBOnWc/M/f5ZzVvbdHJXr7WhRNPlmWeYLIdwTimJQXKuh+LFQlMVepUVK1kfyvN5JKThW6IrdPM/t0kurTg2jcR63BqxldXuVp4kcucGU9+CIiaH+EPBwuiuamZETFD2/IMIDW5b9FDQ3mYUI2R6mp/zenoQ2J73hWEh9OaIqu3etMcW3Ko58WtWdl77QkIp+27o3P9gwYc9gCK+xy/Kx5Vr6Reo6cEPUgvUgksqbOtoKouxpq1i0r+3N7Z4kMVlshQ1PZUrD5WagOkRmMBFIWaJGZODsanDxvaJMNAnFdhcLgPsrQpTDICngBuld6rAa2SfsiR1eNuvPk8hNySVVyyBBHMbbBie6XdWRD0s2EdeEE/60mlxnJu/J4wtoZizACplXQUaGWqbiFynSAIAwcDX1joOO44sTasL200QZrMSagr093/ql7zlLJi0DP76h7PRUpHcZTurKhCKs+C9uBUTkJQWcI2jl/IuRMEezCRJHVoa6Q8cnHIq0vYEsLfmODrFnFHPdeB5YhQ6Mvm53DBM0juOMq6niv+JcWZkyYoNNqRJCm5R+kyocIgyGHZI3/hPqysWYTeosqXaGzmCkgifJMnhpoYRdcNHdPqPTziSx9f9UltdAHsjNwvWMmnfxLvTCKtLjrW/fHAm0397g4Agh/yvuQsZM5nbF3/UQrlcokh63/fy/soeV3Vcd+IHDk+eIeo4AY42+74TneYWauwKWuFeXhyeuqX/Ig3SR7foxF+iebyDz1Ix7rzcqIiaOXj7d2nub954V4wBoXnFmlzC0dbGPmJnlvL4eHKmdnBUkYosnK+GNkrMNDaYaTFaYO9HfFSgJ/CIUtWzMakacqX5dVPIlEAg2hWx4tJoSt007WmHO4+U62f1HGSxFGRciuAT1CZuHT1lKL97NJ5WNVHNmNP/4s2SAKLb2ivQJDSpZXFUZQyY0Fyg/jmGHPhzXIzEjxQtHzZ7cZA5QIzJI0siyWV8sSCLWOTLijzsyAhfYMjRiO3jkw6e4+iMFClrNDLNfeviqpSevSO4xjUXbgM0ex/6KxCV1dmxHqiTpfgv6G2DsbLVUz/IU3k3QkXaPGmUmenTw9BG4UdtdJUZoyxziLhIscLhsSu3T1FKXIy4MiPDrBebmX/H5i1s+f0LUE9l+p2NQ3VrFU4sz1I/toXbKR6d+2ZWC6pLKORbYEmv+tInaIUKgwHW0TJ4wuuX/00zPEB+ugyIRM7q/DWk8qW3Z3wn7mDZNOvCoT9HJkWDT97BmGyiGs2/7y2JA44Wl2bXDM6mIdxkrPwDasZ6PlDbDpKtYxQSHnH4GFCCT+5MzBh1A0Sa5UjEsD2UlEwujeo58CmItZxpEIBQ2WkcM6obxdDoSeyJ8IpWmeZdwcDhLqaJQ20WbT7bICr5+js8SeAgALwvDXKyYftgEwgvynj+AuU8tRA3InZaJJwfuFcXGsVq05WsxJZ5pAUW4eJriM/SbG7U/fzpIp5l9ENnCbfoYD68NFR/0DS5UK4/wxpPnbunnQ6/PTS9z2mf1N/ayzglRJgKi2szk0HDBlfrRbgi3PXu+qU8H1vNGbcummRsUgZTFskjai39CBrJIoF12awMUnqRFDqEiz4dpkbOlvBejSAiea+ju/JsrW2guc+45EB0a+IiylyLcmwgU4a4yyQsKSsDLMaU49lu2GYvYj3l151Lqv603Y624zWqMrb+TuL+A1rnglF613QjDQVWsXwyUQDWkt+O9Uj98C6CaTGnIQdaJ1lj34zC1G4v2k302CEZCR8LqYgwIUjWQclegVuVZyzbxVUyYude0vh1p29l5+F9ZbxP6IRSWvIc2SdcsTiZtXAp2U3qPcKFyhMe9ESQqdqw9CMuxXvrrV9yRhx3SPjaOudkLBDNCShmWheBE9E7XeWuGV0t770YlpHLNnhVBvVUktoGBFIehpVeGHdhHZKG+fb8ONe2jKydKZJJSiPmUdkR7F30G6xKcoOEVzQyqNRDtCl0JR+hb9IAE8i8+a5fC93T/SMhUdGAZ6Mq+KCJnn37zOrFqOKDQr7EUfcBHybYAu1JVy8QitOaQf4dJU2i9G66iKXZPGYYeIsH/pHbamp8P9kx53FOO82X7zGa10KNfoEsvi86/4fNdvgq2NLDh1oK3B9jvMQZr6txFxTGdDbmpynyRhN6a7s4vz9inyyJvwsCmkmTsIRTLuDY5Gdmbcc0lD2OzE9punV0LoZDQ/LlEok5m7FQh51ftJsBeRgboCEqK0EoX2Cw3lxQrOmA71VcHyTNQQ5WmW1GfxO/PAsNFQPP8SrcVhM3WrWSY4ZEMeoGmQCOFFFVgC3lKPna42YhrAA9w/4oZXY32l7mQ1pgxEu9gL44vMbDuBL3vnQY4uAhqHKFuDShiEf2xARluZSMMeEIoDfUdAcUI+5KEbKyIq1QWVC4x6up01P/p2zjX1iSMSMiT3vsPQEuZ5LsGmcLTYgA+TKqIrzWAux1tiMJYGREnpsHJtpiMaKZDnWssyM0K/uLsW+nD2FAP9KVmHK/VXjdxQR1ju9mRJgX/hQYgel9jPeXTF89dGsmr9hnVo35FvrnLmBF0i2rliMP7z0kqrskQZq8r5IkHj6g7NODOZZgLZjZeN7CXxsD7IcrZWFgJ9URx22BpMLKFVb1IyUpEWSIzLKA3t9GN7tNy2qRnM4hKuVsuNNkYrVhlbprAgrnw8v27sY+KSZI0awJfSrPlH37zhtFjysWTE/g+SOct7XnnA9G7IANOUCiWsjwvX/CD2G6L8tf0PnOWzicb8ljHy4nb7jCP7yo/yl7VMNPLpm/PglyjHo4X0QzwqYtHOf8tusRXgCX3tn8hLe6TKxCEu33IV4DURd62F9IDtOkcb8HUji+Ib/2ujvZoQCiLIp+mAL1rcreSjjog5Smwo6dwhBk4W57L0UXK/1N/i4RHSU2iITjYcdtB2XsKWA4BF24rFHslwDxtItYGz1WfqDvyuN/EbhjUTc5hnQLgb3M6OqprxFgNTC3Ral9YDLgYTBdl1AC+uafwY3C+pA88LHFC8plav9cyV26aCUebwEEKEB/kBEXOjDSf46xCZ99TpLMtLFuWoBanVQkTkJ7kkTUZKeAxvfyarOIjJW1J3PezAXx1Ryq3wabZB2NDdhXw+YlzjFAsld9qmaxye1covTZ/EYg8gSA/Q77UBOd/oOJborPE0EYh1SBLlan1URhht9VjPk80LOTC0GHYpFtdFu1wVRCeDXdAdUXjHIascxpdA2dqNEZ8CNY0HJGbncSKB+3qKz2eyLHIck53+7Lf42/WjBWsdJBhVDdEddbHlqhv/z/MTi+5GxMEIRuWNDBMRFrCLzhixAN1XFIRyrLyLuDRU/wJ7B22noQULT4+MF7Wl3dYse9wL/ygaPhce+UhL5wL5MNcpLh1yOKS4B0AJWvN8C9h5pwSdtscsamdpIQjkJ9H0Y94juuS+KU2i0udeKbL5lmWl5bvVFuhzoAdzBmLw7d7ZZsHNST17kCMnH+lE63HLz21baBkV37qrMB/UkkcTcLB+GGXvLjR6VcV8yRWApqguXuwnoapkly6KMHLtFuMMr77rwL1sYXewxs70g4OTjucFlOu219MoGVcZyio1iIcOcBxrKIaMqQYkRiKbXHdBTjxV34q0xbjlB4y8DzaYUjFld40vDOp/QkjiQpf9e+rgykErUpypu3eCAzYXVsfQnZFW2qxDutLq5enPz2q1yce02cmgwtMouAxws/V5BSI0cBMXzbhgkhETL1BJM/6ZbFlo9t40GVgkcVTO/XH1GiS3zvWWcBhpQwK3lTzG2+7G/qLHMt7l9fr19uRRsWWlJk2/lLDaiozErf5iMAgTZJYC+hXLEdFHEY5Z97ZP8/fi7GmXt4yEuc7TFUwFOrKue46D7DmwjPBhAuouvY+3knazcdmpRHtkL2Xu/vwkf0uTJA/P84wytbx8jvZ/uvPk4bzUZhv9XmeNxU8tcyXNE7opJ2Hex+yaKfj6QkW4ZRy6wVUChTvJM9ysPY0tih7Q0fuXS02qQ43FgMC93IgP2i2/zMNO3K7fsycSYhUAjtp4Nm4l7cBk0D4MCPBw81445LUmhSgGJhBiNTl/bKkW/BOL8YeVXl6qFlqxl4VKjJxIkpvN2ejN/z6S6NSqFLgjWDfJ7cCBtIxeFeioi4ZJL4fmSM5iLqR7UOzoqOeNFwlwSyMBgpnFNm/FYwSU2+smbjGkuqm5P9pFyQiQESNCY4gNC/HVuUDYfrjhwP0HwzZHS/DvLU5PudpXXC9azJaKycMYqZkdAzqp97MQoO9nDQinnCklvphJ7MmhPLfrkIQD1EJ0dRd94IUeUIN44sQsY6RheK1duYoGM6peEKGF5oS0cSu3rf/yvQmrYhsrgvDHgk7jBylDiJVFhXikQm7H7jG0QBu9RGlY8UVH0tY0Whlq7k21iMCotDqghoynAAxvm89jG9/gG637JnlBSgEYpGqqkf7q9upm8eyw2rIEEOsRzYVB22J7t+FmFqEAx5rYNEYwHqTSfcknTwr84q0exaCFLP0zbYRNGBEEQoh1OazGlvMIRcL/VcG8uPl4rSwOGMsV1zUoU3Iegmun40JZkSKDZdyhTeDD3UW8iGh9hFHOQp3b13ndPEmRO4snQ+D01xOya3rxFAicBgqzhF/HVNPppYpvsvOwUkPKObrp/CRpOSVyKbAwSO/sY5lygVVrEBTSgA+ZWhQP/Tp72UT74yBRoat5pVUfhvUOsVpYS/ayJCei6qWVYWUcZ0qYcR95Xy6zEAUZGozhGyuL810xuyCTeQbfDf8i6rudhIqboRhPslw0303NGvnxIeU7u7N9gmkauGqtbTLcfoGikqm6uCP1F7Sg/2t9HtsUansPOe9vJf/OeoyUOUKDfui4j0BDNYklsg3BKK1UoOTSg2Gs2iVgUnk+9DXH5hqJvlkcaCZ/jNaiHPVaqdNjaaMPm8L1cfHQcu6Ej3wJ6zxBxg35W3ZCuB81A9/wixjClFXzWMoLFh8OmTqqgxVSvW0+84Z0NkBq3qRVEkNcVB7KyB+8cnh/lypjKEj04UQuuBRbTtCa4lGul2hCzJtKtxBtauc57imtVPaYUVcOdxnZIkqMN9h6B2zuLMfsi7N8S02BjWFQ2IEmQFdRYpyiC8iehi2swDKjnBCDo6bKKfnP4XXo6uhJXAP97DAjJbrhyohG08K6jiFhiUQEGdilqO3se6Rk2VFGPcPF8rR2o90EwUWrY190oQebpM//ayyyjx2O4JbFUXnnVSEyeeF//gxDh3h8QUuikumAa8GlrnRpgquTdKwyDQOC38pTzKU57E1l560Zl1JLnP6ylioXGTPmBLzcVPoBIUIOQLVpX5u8t7dE9ONmeqXpB6+uAdADPLCwZNQSLKwMFc36WMIzwVOswLpaPuSRJd6AwDLO0P+wlj68S0pSi2IyAzC0MzE4EiiGCVtmHM6R/piJRg+m7ukqq+eylG7Ua680gv89fJhzibHU773BHWfrtNmAeOQTSOFivu/38uflte6bEGbN8gASjP6qu3rIrsVWJqH9qvSYIu9qJ8AjlqxQ93TWe/Bk/ChKYwZbtFK7Icd1BQkVDShyxeewABADg1LxJ0Pq4HYAgY6lWGWYaJdkcvQHsvT57joEb9WiYfaZPc8enCgejKYvt7as4W937lcWZYCR5J/p1oEU3a6sSXq0QkRY6arixYDEW6+FX4kACWPv4qwn2gJzQPZ9VyQ1XlW6tD8MFn58vP090oHC45wYut8wxow/vYS0xh2FIG3tTq7vq+7vnBnpVTWKpcyP6njU7E6Y+y3srzuJ6vgNjJDnB1G2Em0PYnFPSNP7Tx6EIveROlJ4Ort7cpp2vddvr1RsEBZXTyTWlMW0OiwKbFWMCsSZmigb0p0BrBfPIHysfhg3jCB3pBU3W1pskUdq7N1rYEoapUrTZrSMtqCPfmomyReALckGCmnQS6oGQlcY/p0HfOKgLf6USyYIExmSe8yn410zZMmR1KDcX0h8VjTi5Q6BdrpdVEstc9buW1xE28W4s27osV/jOj2yUb1nst87hws4FYWsegbaPfbt6nyXH3Z8rPbus=
