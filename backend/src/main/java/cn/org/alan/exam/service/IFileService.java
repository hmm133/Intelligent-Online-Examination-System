package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import org.springframework.web.multipart.MultipartFile;


public interface IFileService {

    /**
     * 上传图片
     *
     * @param file 文件
     * @return 返回上传后的地址
     */
    Result<String> uploadImage(MultipartFile file);
}
