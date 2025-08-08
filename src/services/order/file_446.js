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
// 修改于 2025年 8月 8日 星期五 15时40分56秒 CST
