import request from '@/utils/request'

export function answerExamPging(params) {
  return request({
    url: 'answers/exam/page',
    method: 'get',
    params
  })
}

export function answerUserPging(params) {
  return request({
    url: 'answers/exam/stu',
    method: 'get',
    params
  })
}

export function answerDetail(params) {
  return request({
    url: 'answers/detail',
    method: 'get',
    params
  })
}

export function correct(data) {
  return request({
    url: 'answers/correct',
    method: 'put',
    data
  })
}

// export function scorePaging(data) {
//     return request({
//       url: 'answers/correct',
//       method: 'put',
//       data
//     })
//   }


// ==================== AI评分相关接口 ====================

/**
 * 单题AI评分
 * @param {Object} data - { examId, userId, questionId }
 */
export function aiEvaluateSingle(data) {
  return request({
    url: 'answers/ai-evaluate',
    method: 'post',
    data,
    timeout: 80000 
  })
}

/**
 * 批量AI评分
 * @param {Number} examId - 考试ID
 */
export function aiEvaluateBatch(examId) {
  return request({
    url: `answers/ai-evaluate/batch/${examId}`,
    method: 'post',
    timeout: 100000
  })
}