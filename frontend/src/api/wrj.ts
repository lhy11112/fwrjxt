import axios from 'axios'

// 后端统一响应: { success, result, message }
export interface ApiResponse<T> { success: boolean; result?: T; message?: string }
export interface PageResult<T> { records: T[]; total: number; page: number; page_size: number }

export const http = axios.create({ baseURL: '/api/v1', timeout: 20000, headers: { 'Content-Type': 'application/json' } })

export async function unwrap<T>(req: Promise<{ data: ApiResponse<T> }>): Promise<T> {
  const { data } = await req
  if (!data.success) throw new Error(data.message || '请求失败')
  return data.result as T
}

/** 通用 CRUD 客户端工厂。字符串或数字主键均可。 */
export function makeCrud<T = any>(base: string) {
  return {
    list: (params?: any) => unwrap<PageResult<T>>(http.get(base, { params })),
    add: (d: Partial<T>) => unwrap<any>(http.post(base, d)),
    edit: (d: Partial<T>) => unwrap<any>(http.put(base, d)),
    getById: (id: string | number) => unwrap<T>(http.get(`${base}/${id}`)),
    delete: (id: string | number) => unwrap<string>(http.delete(`${base}/${id}`)),
    batchDelete: (ids: (string | number)[]) => unwrap<string>(http.post(`${base}/batch-delete`, { ids })),
  }
}

// ==================== 各业务模块 ====================
export const fwzfApi = makeCrud('/wrj/fwzf')           // 反无战法
export const fzzbApi = makeCrud('/wrj/fzzb')           // 反制装备
export const bxpzApi = makeCrud('/wrj/bxpz')           // 编携配装
export const zymbApi = makeCrud('/wrj/zymb')           // 重要目标
export const czspApi = makeCrud('/wrj/czsp')           // 操作视频
export const scjApi = makeCrud('/wrj/scj')             // 收藏夹
export const jkglApi = makeCrud('/wrj/jkgl')           // 接口管理
export const jbxxNewApi = makeCrud('/wrj/jbxx-new')    // 无人机详细信息
export const zskApi = makeCrud('/wrj/zsk')             // 知识库
export const zskWjApi = makeCrud('/wrj/zsk-wj')        // 知识库文件
export const dxyyBhApi = makeCrud('/wrj/dxyy-bh')      // 标绘
export const lpwjApi = makeCrud('/wrj/lpwj')           // 录屏文件
export const dxdmApi = makeCrud('/wrj/dxdm')           // 地形地貌
export const yhglCsApi = makeCrud('/wrj/yhgl-cs')      // 用户参数
export const opLogApi = makeCrud('/wrj/operation-log') // 操作日志
export const spectrumResultApi = makeCrud('/wrj/spectrum-result') // 频谱结果
export const kysqApi = makeCrud('/wrj/kysq')           // 空域授权

// 推演计划（带轨迹生成）
export const tyjhApi = {
  ...makeCrud('/wrj/tyjh'),
  generate: (d: any) => unwrap<any>(http.post('/wrj/tyjh/generate', d)),
  changeStatus: (id: string, zt: string) => unwrap<string>(http.get('/wrj/tyjh/change-status', { params: { id, zt } })),
  data: (params?: any) => unwrap<PageResult<any>>(http.get('/wrj/tyjh-data', { params })),
}

// 树形接口
export const treeApi = {
  bxpz: () => unwrap<any[]>(http.get('/wrj/bxpz/tree')),
  scj: () => unwrap<any[]>(http.get('/wrj/scj/tree')),
  zzllBd: () => unwrap<any[]>(http.get('/wrj/zzll-bd')),
}

// 空域授权批量保存
export const kysqSave = (kyid: string, wrjids: string[]) => unwrap<string>(http.post('/wrj/kysq/save', { kyid, wrjids }))

// 地理半径查询
export const geoApi = {
  zymb: (jd: number, wd: number, jl: number, type?: string) => unwrap<any[]>(http.get('/wrj/zymb/by-geo', { params: { jd, wd, jl, type } })),
  dxdm: (jd: number, wd: number, jl: number) => unwrap<any[]>(http.get('/wrj/dxdm/by-geo', { params: { jd, wd, jl } })),
}

// 数据字典
export const dictApi = {
  ...makeCrud('/sys/dict'),
  items: (code: string) => unwrap<{ text: string; value: string }[]>(http.get(`/sys/dict-items/${code}`)),
  itemList: (params?: any) => unwrap<PageResult<any>>(http.get('/sys/dict-item', { params })),
  addItem: (d: any) => unwrap<any>(http.post('/sys/dict-item', d)),
  editItem: (d: any) => unwrap<any>(http.put('/sys/dict-item', d)),
  deleteItem: (id: string) => unwrap<string>(http.delete(`/sys/dict-item/${id}`)),
}

// 用户
export const userApi = makeCrud('/sys/users')

// 文件上传
export const uploadFile = (file: File) => {
  const fd = new FormData()
  fd.append('file', file)
  return unwrap<any>(http.post('/sys/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } }))
}
