
import request from '@/utils/request'

export function quAdd(data) {
  return request({
    url: 'questions/single',
    method: 'post',
    data
  })
}
export function quPaging(params) {
  return request({
    url: 'questions/paging',
    method: 'get',
    params
  })
}

export function quUpdate(id, data) {
  return request({
    url: `questions/${id}`,
    method: 'put',
    data: data
  })
}

export function quDel(ids) {
  return request({
    url: 'questions/batch/' + ids,
    method: 'delete'
  })
}

export function quDetail(id) {
  return request({
    url: `questions/single/${id}`,
    method: 'get'
  })
}

export function importQue(id, data) {
  return request({
    url: `questions/import/${id}`,
    method: 'post',
    data
  })
}

// fetchDetail, saveData
/**
 * 题库详情
 * @param data
 */
export function fetchDetail(id) {
  // eslint-disable-next-line no-undef
  return post('/exam/api/qu/qu/detail', { id: id })
}

/**
 * 保存题库
 * @param data
 */
export function saveData(data) {
  // eslint-disable-next-line no-undef
  return post('/exam/api/qu/qu/save', data)
}


/**
 * AI生成题目
 * @param {Object} data - 生成参数
 * @param {Number} data.repoId - 题库ID
 * @param {Number} data.count - 生成数量
 * @param {Number} data.quType - 题型 (1:单选 2:多选 3:判断 4:简答)
 * @param {String} data.knowledgePoint - 知识点
 * @param {Number} data.difficulty - 难度 (1:简单 2:中等 3:困难)
 * @returns {Promise}
 */
export function aiGenerateQuestions(data) {
  return request({
    url: 'questions/ai/generate',
    method: 'post',
    data,
    timeout: 60000 // 设置超时时间为60秒
  })
}
