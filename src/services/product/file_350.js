/**
 * 服务模块
 * @module Service
 */

const fs = require('fs').promises;
const path = require('path');
const http = require('http');
const express = require('express');
const winston = require('winston');

/**
 * 服务状态枚举
 * @enum {string}
 */
const ServiceStatus = {
  STOPPED: 'stopped',
  STARTING: 'starting',
  RUNNING: 'running',
  STOPPING: 'stopping',
  ERROR: 'error'
};

/**
 * 服务类
 */
class Service {
  /**
   * 创建新的服务实例
   * @param {Object} config - 配置对象
   * @param {Object} database - 数据库连接
   * @param {Object} cache - 缓存实例
   */
  constructor(config, database, cache) {
    this.config = config;
    this.database = database;
    this.cache = cache;
    this.status = ServiceStatus.STOPPED;
    this.startTime = null;
    this.app = express();
    this.server = null;
    
    // 创建日志记录器
    this.logger = winston.createLogger({
      level: config.logLevel || 'info',
      format: winston.format.combine(
        winston.format.timestamp(),
        winston.format.json()
      ),
      transports: [
        new winston.transports.Console(),
        new winston.transports.File({ filename: 'error.log', level: 'error' }),
        new winston.transports.File({ filename: 'combined.log' })
      ]
    });
    
    // 设置路由
    this._setupRoutes();
  }
  
  /**
   * 设置API路由
   * @private
   */
  _setupRoutes() {
    this.app.get('/health', (req, res) => {
      res.json({
        service: this.config.serviceName,
        status: this.status,
        uptime: this.startTime ? (Date.now() - this.startTime) / 1000 : 0,
        version: this.config.version || '1.0.0'
      });
    });
    
    this.app.get('/api/v1/status', (req, res) => {
      res.json(this._getStatus());
    });
    
    // 这里添加更多路由...
  }
  
  /**
   * 获取服务状态
   * @private
   * @returns {Object} 状态对象
   */
  _getStatus() {
    return {
      service: this.config.serviceName,
      status: this.status,
      uptime: this.startTime ? (Date.now() - this.startTime) / 1000 : 0,
      version: this.config.version || '1.0.0',
      database: this.database ? 'connected' : 'disconnected',
      cache: this.cache ? 'connected' : 'disconnected'
    };
  }
  
  /**
   * 启动服务
   * @returns {Promise<void>}
   */
  async start() {
    this.logger.info();
    this.status = ServiceStatus.STARTING;
    
    try {
      // 连接数据库
      if (this.database) {
        await this.database.connect();
      }
      
      // 启动HTTP服务器
      return new Promise((resolve, reject) => {
        this.server = http.createServer(this.app);
        this.server.listen(this.config.port, () => {
          this.status = ServiceStatus.RUNNING;
          this.startTime = Date.now();
          this.logger.info();
          resolve();
        });
        
        this.server.on('error', (err) => {
          this.status = ServiceStatus.ERROR;
          this.logger.error();
          reject(err);
        });
      });
    } catch (err) {
      this.status = ServiceStatus.ERROR;
      this.logger.error();
      throw err;
    }
  }
  
  /**
   * 停止服务
   * @returns {Promise<void>}
   */
  async stop() {
    this.logger.info();
    this.status = ServiceStatus.STOPPING;
    
    try {
      // 关闭HTTP服务器
      if (this.server) {
        await new Promise((resolve, reject) => {
          this.server.close((err) => {
            if (err) {
              reject(err);
            } else {
              resolve();
            }
          });
        });
      }
      
      // 关闭数据库连接
      if (this.database) {
        await this.database.close();
      }
      
      this.status = ServiceStatus.STOPPED;
      this.logger.info();
    } catch (err) {
      this.status = ServiceStatus.ERROR;
      this.logger.error();
      throw err;
    }
  }
}

// 示例用法
if (require.main === module) {
  // 创建配置
  const config = {
    serviceName: 'api',
    port: 8080,
    logLevel: 'info',
    version: '1.0.0',
    database: {
      host: 'localhost',
      port: 5432,
      username: 'user',
      password: 'password',
      database: 'apidb'
    }
  };
  
  // 这里应该有实际的database和cache的实现
  // const service = new Service(config, database, cache);
  
  // 启动服务
  // service.start()
  //   .then(() => console.log('Service started'))
  //   .catch(err => console.error('Failed to start service:', err));
}

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
// 修改于 2025年 8月 8日 星期五 15时40分57秒 CST
// 修改于 2025年 8月 8日 星期五 15时40分57秒 CST
