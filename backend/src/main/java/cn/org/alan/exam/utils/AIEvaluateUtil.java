package cn.org.alan.exam.utils;

import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;


@Component
public class AIEvaluateUtil {

    private static final Logger log = LoggerFactory.getLogger(AIEvaluateUtil.class);

    @Value("${ai.doubao.api-key}")
    private String apiKey;
    
    @Value("${ai.doubao.api-url}")
    private String apiUrl;
    
    @Value("${ai.doubao.model}")
    private String model;
    
    @Value("${ai.doubao.timeout:60}")
    private Integer timeout;
    
    private final ObjectMapper objectMapper;

    public AIEvaluateUtil() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * AI评分结果
     */
    public static class EvaluateResult {
        private Integer score;          // 分数（0-100相对分）
        private String comment;         // 评语
        private Double confidence;      // 置信度（0-1）

        public EvaluateResult(Integer score, String comment, Double confidence) {
            this.score = score;
            this.comment = comment;
            this.confidence = confidence;
        }

        public Integer getScore() { return score; }
        public String getComment() { return comment; }
        public Double getConfidence() { return confidence; }

        public void setScore(int i) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setScore'");
        }

        public void setComment(String string) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setComment'");
        }

        public void setConfidence(double d) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setConfidence'");
        }
    }

    /**
     * 评分主方法
     *
     * @param userAnswer      考生答案
     * @param referenceAnswer 参考答案
     * @param totalScore      题目满分
     * @param questionContent 题目内容
     * @return 评分结果
     */
    public EvaluateResult evaluateAnswer(
            String userAnswer,
            String referenceAnswer,
            Integer totalScore,
            String questionContent) {
        
        try {
            log.info("开始AI评分，题目满分：{}", totalScore);
            
            // 1. 构建评分提示词
            String prompt = buildEvaluatePrompt(userAnswer, referenceAnswer, questionContent);
            
            // 2. 调用豆包API
            String aiResponse = callDoubaoAPI(prompt);
            
            // 3. 解析评分结果
            EvaluateResult result = parseEvaluateResponse(aiResponse, totalScore);
            
            log.info("AI评分完成，得分：{}/{}，置信度：{}", result.getScore(), totalScore, result.getConfidence());
            
            return result;
            
        } catch (Exception e) {
            log.error("AI评分失败", e);
            throw new ServiceRuntimeException("AI评分失败：" + e.getMessage());
        }
    }

    /**
     * 构建评分提示词
     */
    private String buildEvaluatePrompt(String userAnswer, String referenceAnswer, String questionContent) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("你是一位专业的阅卷老师，请对学生的答案进行评分。\n\n");
        prompt.append("【题目】\n").append(questionContent).append("\n\n");
        prompt.append("【参考答案】\n").append(referenceAnswer).append("\n\n");
        prompt.append("【学生答案】\n").append(userAnswer).append("\n\n");
        
        prompt.append("【评分要求】\n");
        prompt.append("1. 给出0-100之间的相对分数（100代表满分）\n");
        prompt.append("2. 提供详细的评语，说明得分点和失分点\n");
        prompt.append("3. 评估你的评分置信度（0-1之间的小数）\n");
        prompt.append("4. 必须返回严格的JSON格式，不要任何其他文字\n\n");
        
        prompt.append("【JSON格式】\n");
        prompt.append("{\n");
        prompt.append("  \"score\": 85,\n");
        prompt.append("  \"comment\": \"学生答案较为完整，抓住了主要知识点...\",\n");
        prompt.append("  \"confidence\": 0.9\n");
        prompt.append("}\n\n");
        
        prompt.append("请直接返回JSON，不要用```包裹：");
        
        return prompt.toString();
    }

    /**
     * 调用豆包API
     */
    private String callDoubaoAPI(String prompt) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            // 设置请求方法和超时
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(timeout * 1000);
            conn.setReadTimeout(timeout * 1000);
            conn.setDoOutput(true);
            
            // 设置请求头
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            
            // 构建请求体
            String requestBody = buildDoubaoRequestBody(prompt);
            
            log.debug("发送评分请求到豆包API");
            
            // 发送请求
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // 读取响应
            int responseCode = conn.getResponseCode();
            
            if (responseCode != 200) {
                BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                errorReader.close();
                
                log.error("豆包API调用失败，状态码：{}，错误信息：{}", responseCode, errorResponse);
                throw new ServiceRuntimeException("豆包API调用失败，状态码：" + responseCode);
            }
            
            // 读取成功响应
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            String responseBody = response.toString();
            log.debug("收到豆包API响应");
            
            // 解析豆包响应格式
            return parseDoubaoResponse(responseBody);
            
        } finally {
            conn.disconnect();
        }
    }

    /**
     * 构建豆包请求体
     */
    /**
     * 构建豆包请求体 (修正为标准 OpenAI 格式)
     */
    private String buildDoubaoRequestBody(String prompt) {
        try {
            // 1. 创建根对象
            com.fasterxml.jackson.databind.node.ObjectNode root = objectMapper.createObjectNode();
            root.put("model", model);
            
            // 2. 创建 messages 数组
            com.fasterxml.jackson.databind.node.ArrayNode messages = root.putArray("messages");
            
            // 3. 创建 user 消息
            com.fasterxml.jackson.databind.node.ObjectNode userMsg = messages.addObject();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            
            // 4. 转为 JSON 字符串
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            log.error("构建请求体失败", e);
            throw new ServiceRuntimeException("构建请求体失败");
        }
    }


    /**
     * 解析豆包响应
     */
    /**
     * 解析豆包响应 (修正为标准 OpenAI 格式)
     */
    private String parseDoubaoResponse(String responseBody) throws Exception {
        JsonNode jsonResponse = objectMapper.readTree(responseBody);
        
        // 标准 OpenAI 响应格式: {"choices": [{"message": {"content": "..."}}]}
        JsonNode choices = jsonResponse.get("choices");
        
        if (choices != null && choices.isArray() && choices.size() > 0) {
            JsonNode firstChoice = choices.get(0);
            JsonNode message = firstChoice.get("message");
            if (message != null && message.has("content")) {
                String content = message.get("content").asText();
                log.debug("豆包AI返回评分内容：{}", content);
                return content;
            }
        }
        
        log.error("无法解析的响应格式：{}", responseBody);
        throw new ServiceRuntimeException("AI响应格式异常，未找到choices字段");
    }

    /**
     * 解析评分结果
     */
    private EvaluateResult parseEvaluateResponse(String aiResponse, Integer totalScore) {
        try {
            // 清理可能的markdown代码块标记
            String cleanJson = aiResponse.trim();
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            }
            if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.substring(3);
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            cleanJson = cleanJson.trim();

            // 解析JSON
            JsonNode resultNode = objectMapper.readTree(cleanJson);
            
            // 提取字段
            int relativeScore = resultNode.get("score").asInt();  // 0-100的相对分
            String comment = resultNode.get("comment").asText();
            double confidence = resultNode.get("confidence").asDouble();
            
            // 转换为实际分数（相对分 * 满分 / 100）
            int actualScore = Math.round(relativeScore * totalScore / 100.0f);
            
            // 边界检查
            if (actualScore < 0) actualScore = 0;
            if (actualScore > totalScore) actualScore = totalScore;
            
            if (confidence < 0) confidence = 0;
            if (confidence > 1) confidence = 1;
            
            return new EvaluateResult(actualScore, comment, confidence);
            
        } catch (Exception e) {
            log.error("解析AI评分响应失败：{}", aiResponse, e);
            throw new ServiceRuntimeException("解析AI评分结果失败：" + e.getMessage());
        }
    }

    /**
     * JSON值转义
     */
    private String escapeJsonValue(String str) {
        try {
            return objectMapper.writeValueAsString(str);
        } catch (Exception e) {
            return "\"" + str.replace("\\", "\\\\")
                              .replace("\"", "\\\"")
                              .replace("\n", "\\n")
                              .replace("\r", "\\r")
                              .replace("\t", "\\t") + "\"";
        }
    }
}