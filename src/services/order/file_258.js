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
