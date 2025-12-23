package cn.org.alan.exam.utils.agent;


public interface AIChat {

    /**
     * 向大模型输入消息，返回结果
     *
     * @param msg 输入消息
     * @return 结果
     */
    public String getChatResponse(String msg) throws Exception;
}