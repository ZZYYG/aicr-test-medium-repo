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
// 修改于 2025年 8月 8日 星期五 15时40分52秒 CST
// 修改于 2025年 8月 8日 星期五 15时40分54秒 CST
