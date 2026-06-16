import api from './index.js'

// 查询行为日志
export function getBehaviorLogs(params) {
  return api.get('/log/behavior', { params })
}

// 查询审计日志
export function getAuditLogs(params) {
  return api.get('/log/audit', { params })
}
