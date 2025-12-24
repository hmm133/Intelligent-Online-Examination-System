package cn.org.alan.exam.utils;

import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.model.entity.Option;
import cn.org.alan.exam.model.form.question.QuestionFrom;
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
import java.util.ArrayList;
import java.util.List;


@Component
public class AIQuestionGeneratorUtil {

    private static final Logger log = LoggerFactory.getLogger(AIQuestionGeneratorUtil.class);

    @Value("${ai.doubao.api-key}")
    private String apiKey;
    
    @Value("${ai.doubao.api-url}")
    private String apiUrl;
    
    @Value("${ai.doubao.model}")
    private String model;
    
    @Value("${ai.doubao.timeout:60}")
    private Integer timeout;
    
    private final ObjectMapper objectMapper;

    public AIQuestionGeneratorUtil() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 生成题目
     *
     * @param count           生成数量
     * @param quType          题型
     * @param knowledgePoint  知识点
     * @param difficulty      难度
     * @param additionalReq   额外要求
     * @return 题目列表
     */
    public List<QuestionFrom> generateQuestions(
            Integer count,
            Integer quType,
            String knowledgePoint,
            Integer difficulty,
            String additionalReq) {
        
        try {
            log.info("开始调用豆包AI生成{}道{}题，知识点：{}", count, getQuestionTypeName(quType), knowledgePoint);
            
            // 1. 构建提示词
            String prompt = buildPrompt(count, quType, knowledgePoint, difficulty, additionalReq);
            
            // 2. 调用豆包API
            String aiResponse = callDoubaoAPI(prompt);
            
            // 3. 解析返回结果
            List<QuestionFrom> questions = parseAIResponse(aiResponse, quType);
            
            log.info("豆包AI成功生成{}道题目", questions.size());
            
            // 4. 验证数量
            if (questions.size() != count) {
                log.warn("AI生成数量不匹配，期望{}题，实际生成{}题", count, questions.size());
            }
            
            return questions;
            
        } catch (Exception e) {
            log.error("豆包AI生成题目失败", e);
            throw new ServiceRuntimeException("AI生成题目失败：" + e.getMessage());
        }
    }

    /**
     * 构建AI提示词
     */
    private String buildPrompt(Integer count, Integer quType, String knowledgePoint, 
                               Integer difficulty, String additionalReq) {
        
        String quTypeName = getQuestionTypeName(quType);
        String difficultyName = getDifficultyName(difficulty);
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的题目出题专家。请根据以下要求生成").append(count).append("道").append(quTypeName).append("：\n\n");
        prompt.append("【知识点】：").append(knowledgePoint).append("\n");
        prompt.append("【难度】：").append(difficultyName).append("\n");
        
        if (additionalReq != null && !additionalReq.trim().isEmpty()) {
            prompt.append("【额外要求】：").append(additionalReq).append("\n");
        }
        
        prompt.append("\n【输出要求】：\n");
        prompt.append("1. 必须返回严格的JSON数组格式，不要任何其他文字，不要markdown代码块标记\n");
        prompt.append("2. 每道题目包含以下字段：\n");
        prompt.append("   - content: 题干内容（字符串）\n");
        prompt.append("   - analysis: 题目解析（字符串，说明答案和解题思路）\n");
        
        // 根据题型添加选项要求
        if (quType == 1) { // 单选
            prompt.append("   - options: 选项数组，每个选项包含 {content: 选项内容, isRight: 0或1}\n");
            prompt.append("   - 单选题必须有4个选项（A/B/C/D），且只有1个正确答案\n");
        } else if (quType == 2) { // 多选
            prompt.append("   - options: 选项数组，每个选项包含 {content: 选项内容, isRight: 0或1}\n");
            prompt.append("   - 多选题必须有4-5个选项，且有2-3个正确答案\n");
        } else if (quType == 3) { // 判断
            prompt.append("   - options: 包含2个选项的数组 [{content: 正确, isRight: 0或1}, {content: 错误, isRight: 0或1}]\n");
        } else if (quType == 4) { // 简答
            prompt.append("   - options: 包含1个元素的数组 [{content: 参考答案, isRight: 1}]\n");
        }
        
        prompt.append("\n【JSON格式示例】：\n");
        prompt.append(getJsonExample(quType));
        
        prompt.append("\n\n请直接返回JSON数组，不要有任何多余内容，不要用```包裹：");
        
        return prompt.toString();
    }

    /**
     * 获取JSON示例
     */
    private String getJsonExample(Integer quType) {
        if (quType == 1) { // 单选
            return "[\n" +
                   "  {\n" +
                   "    \"content\": \"Java中哪个关键字用于创建线程？\",\n" +
                   "    \"analysis\": \"Thread类和Runnable接口都可以用于创建线程，但extends Thread是最直接的方式。\",\n" +
                   "    \"options\": [\n" +
                   "      {\"content\": \"extends Thread\", \"isRight\": 1},\n" +
                   "      {\"content\": \"implements Process\", \"isRight\": 0},\n" +
                   "      {\"content\": \"new Task\", \"isRight\": 0},\n" +
                   "      {\"content\": \"create Thread\", \"isRight\": 0}\n" +
                   "    ]\n" +
                   "  }\n" +
                   "]";
        } else if (quType == 2) { // 多选
            return "[\n" +
                   "  {\n" +
                   "    \"content\": \"Java中创建线程的方式有哪些？\",\n" +
                   "    \"analysis\": \"Java创建线程主要有三种方式：继承Thread类、实现Runnable接口、实现Callable接口。\",\n" +
                   "    \"options\": [\n" +
                   "      {\"content\": \"继承Thread类\", \"isRight\": 1},\n" +
                   "      {\"content\": \"实现Runnable接口\", \"isRight\": 1},\n" +
                   "      {\"content\": \"实现Callable接口\", \"isRight\": 1},\n" +
                   "      {\"content\": \"继承Process类\", \"isRight\": 0}\n" +
                   "    ]\n" +
                   "  }\n" +
                   "]";
        } else if (quType == 3) { // 判断
            return "[\n" +
                   "  {\n" +
                   "    \"content\": \"Java中线程的start()方法可以被多次调用。\",\n" +
                   "    \"analysis\": \"错误。一个线程对象只能调用一次start()方法，多次调用会抛出IllegalThreadStateException异常。\",\n" +
                   "    \"options\": [\n" +
                   "      {\"content\": \"正确\", \"isRight\": 0},\n" +
                   "      {\"content\": \"错误\", \"isRight\": 1}\n" +
                   "    ]\n" +
                   "  }\n" +
                   "]";
        } else { // 简答
            return "[\n" +
                   "  {\n" +
                   "    \"content\": \"请简述Java中线程的生命周期及各状态之间的转换。\",\n" +
                   "    \"analysis\": \"答题要点：1.新建状态(New) 2.就绪状态(Runnable) 3.运行状态(Running) 4.阻塞状态(Blocked) 5.等待状态(Waiting) 6.超时等待(Timed Waiting) 7.终止状态(Terminated)。状态转换：new->start()->就绪->CPU调度->运行->完成->终止；运行中可能因同步锁进入阻塞，调用wait()进入等待等。\",\n" +
                   "    \"options\": [\n" +
                   "      {\"content\": \"线程生命周期包括新建、就绪、运行、阻塞、等待、超时等待、终止七个状态。新建线程调用start()进入就绪，获得CPU时间片进入运行，可能因等待资源进入阻塞或等待状态，执行完毕或异常终止进入终止状态。\", \"isRight\": 1}\n" +
                   "    ]\n" +
                   "  }\n" +
                   "]";
        }
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
            
            // 设置请求头（豆包格式）
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            
            // 构建请求体（豆包格式）
            String requestBody = buildDoubaoRequestBody(prompt);
            
            log.info("发送请求到豆包API");
            log.debug("请求体：{}", requestBody);
            
            // 发送请求
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // 读取响应
            int responseCode = conn.getResponseCode();
            
            if (responseCode != 200) {
                // 读取错误信息
                BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                errorReader.close();
                
                log.error("豆包API调用失败，状态码：{}，错误信息：{}", responseCode, errorResponse);
                throw new ServiceRuntimeException("豆包API调用失败，状态码：" + responseCode + "，错误：" + errorResponse);
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
            log.info("收到豆包API响应");
            log.debug("响应内容：{}", responseBody);
            
            // 解析豆包响应格式
            return parseDoubaoResponse(responseBody);
            
        } finally {
            conn.disconnect();
        }
    }


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
            throw new RuntimeException("构建请求体失败", e);
        }
    }



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
                log.debug("AI返回内容：{}", content);
                return content;
            }
        }
        
        log.error("无法解析的响应格式：{}", responseBody);
        throw new ServiceRuntimeException("AI响应格式异常，未找到choices字段");
    }

    /**
     * 解析AI返回的JSON
     */
    private List<QuestionFrom> parseAIResponse(String aiResponse, Integer quType) {
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

            // 解析JSON数组
            JsonNode questionsNode = objectMapper.readTree(cleanJson);
            
            if (!questionsNode.isArray()) {
                throw new ServiceRuntimeException("AI返回的不是数组格式");
            }

            List<QuestionFrom> questions = new ArrayList<>();
            
            for (JsonNode questionNode : questionsNode) {
                QuestionFrom questionFrom = new QuestionFrom();
                
                // 设置基本信息
                questionFrom.setQuType(quType);
                questionFrom.setContent(questionNode.get("content").asText());
                questionFrom.setAnalysis(questionNode.get("analysis").asText());
                
                // 解析选项
                JsonNode optionsNode = questionNode.get("options");
                List<Option> options = new ArrayList<>();
                
                int sort = 0;
                for (JsonNode optionNode : optionsNode) {
                    Option option = new Option();
                    option.setContent(optionNode.get("content").asText());
                    option.setIsRight(optionNode.get("isRight").asInt());
                    option.setSort(++sort);
                    options.add(option);
                }
                
                questionFrom.setOptions(options);
                
                // 验证题目
                validateQuestion(questionFrom);
                
                questions.add(questionFrom);
            }
            
            return questions;
            
        } catch (Exception e) {
            log.error("解析AI响应失败：{}", aiResponse, e);
            throw new ServiceRuntimeException("解析AI生成的题目失败：" + e.getMessage());
        }
    }

    /**
     * 验证题目格式
     */
    private void validateQuestion(QuestionFrom question) {
        if (question.getContent() == null || question.getContent().trim().isEmpty()) {
            throw new ServiceRuntimeException("题干不能为空");
        }
        
        List<Option> options = question.getOptions();
        if (options == null || options.isEmpty()) {
            throw new ServiceRuntimeException("选项不能为空");
        }
        
        int quType = question.getQuType();
        
        // 单选题验证
        if (quType == 1) {
            if (options.size() < 2) {
                throw new ServiceRuntimeException("单选题至少需要2个选项");
            }
            long correctCount = options.stream().filter(o -> o.getIsRight() == 1).count();
            if (correctCount != 1) {
                throw new ServiceRuntimeException("单选题必须有且仅有1个正确答案");
            }
        }
        
        // 多选题验证
        if (quType == 2) {
            if (options.size() < 2) {
                throw new ServiceRuntimeException("多选题至少需要2个选项");
            }
            long correctCount = options.stream().filter(o -> o.getIsRight() == 1).count();
            if (correctCount < 2) {
                throw new ServiceRuntimeException("多选题至少需要2个正确答案");
            }
        }
        
        // 判断题验证
        if (quType == 3) {
            if (options.size() != 2) {
                throw new ServiceRuntimeException("判断题必须有2个选项");
            }
            long correctCount = options.stream().filter(o -> o.getIsRight() == 1).count();
            if (correctCount != 1) {
                throw new ServiceRuntimeException("判断题必须有且仅有1个正确答案");
            }
        }
        
        // 简答题验证
        if (quType == 4) {
            if (options.size() != 1) {
                throw new ServiceRuntimeException("简答题必须有1个参考答案");
            }
            if (options.get(0).getIsRight() != 1) {
                throw new ServiceRuntimeException("简答题答案必须标记为正确");
            }
        }
    }

    /**
     * 获取题型名称
     */
    private String getQuestionTypeName(Integer quType) {
        switch (quType) {
            case 1: return "单选题";
            case 2: return "多选题";
            case 3: return "判断题";
            case 4: return "简答题";
            default: return "未知题型";
        }
    }

    /**
     * 获取难度名称
     */
    private String getDifficultyName(Integer difficulty) {
        switch (difficulty) {
            case 1: return "简单";
            case 2: return "中等";
            case 3: return "困难";
            default: return "中等";
        }
    }

    /**
     * JSON值转义（用于包裹整个文本作为JSON值）
     */
    private String escapeJsonValue(String str) {
        try {
            return objectMapper.writeValueAsString(str);
        } catch (Exception e) {
            // 手动转义
            return "\"" + str.replace("\\", "\\\\")
                              .replace("\"", "\\\"")
                              .replace("\n", "\\n")
                              .replace("\r", "\\r")
                              .replace("\t", "\\t") + "\"";
        }
    }

    /**
     * ==========================================
     * 独立测试入口 (Main Method)
     * ==========================================
     */
    public static void main(String[] args) {
        System.out.println("====== 开始测试 AI 出题功能 (豆包模型) ======");

        // 1. 创建实例
        AIQuestionGeneratorUtil util = new AIQuestionGeneratorUtil();

        // 2. 【重要】配置参数 (请在此处填入你的真实信息)
        // 豆包 API 地址通常是: https://ark.cn-beijing.volces.com/api/v3/chat/completions
        String testApiUrl = "https://ark.cn-beijing.volces.com/api/v3/chat/completions";
        // 你的 API Key (sk-开头)
        String testApiKey = "c12d1b81-c9bf-48da-9d2d-3468637d2926"; 
        // 你的推理接入点 ID (ep-开头)
        String testModel = "doubao-seed-1-6-251015"; 

        try {
            // 3. 使用反射手动注入私有字段 (因为 main 方法运行时 @Value 不生效)
            injectField(util, "apiUrl", testApiUrl);
            injectField(util, "apiKey", testApiKey);
            injectField(util, "model", testModel);
            injectField(util, "timeout", 60); // 设置60秒超时

            System.out.println("正在请求 AI 生成 2 道 Java 单选题...");

            // 4. 调用生成方法
            // 参数: 数量1, 题型1(单选), 知识点"Java集合", 难度2(中等), 无额外要求
            List<QuestionFrom> result = util.generateQuestions(2, 1, "Java List集合", 2, "侧重ArrayList扩容机制");

            // 5. 打印结果
            System.out.println("\n====== 生成成功! 结果如下 ======");
            for (QuestionFrom q : result) {
                System.out.println("【题干】: " + q.getContent());
                System.out.println("【解析】: " + q.getAnalysis());
                System.out.println("【选项】:");
                for (Option o : q.getOptions()) {
                    String prefix = o.getIsRight() == 1 ? "[√] " : "[ ] ";
                    System.out.println("  " + prefix + o.getContent());
                }
                System.out.println("----------------------------------");
            }

        } catch (Exception e) {
            System.err.println("\n====== 测试失败 ======");
            e.printStackTrace();
            System.err.println("请检查: 1.API Key是否正确 2.模型接入点ID(ep-xxx)是否正确 3.网络是否通畅");
        }
    }

    /**
     * 反射工具方法：用于在测试时设置私有字段
     */
    private static void injectField(Object target, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

}