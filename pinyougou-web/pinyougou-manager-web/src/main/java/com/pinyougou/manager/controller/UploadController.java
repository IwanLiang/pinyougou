package com.pinyougou.manager.controller;

import org.apache.commons.io.FilenameUtils;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 上传文件控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-21<p>
 */
@RestController
public class UploadController {

    /** 注入文件服务器的访问地址 */
    @Value("${fileServerUrl}")
    private String fileServerUrl;

    /** 文件上传 */
    @PostMapping("/upload")
    public Map<String,Object> upload(@RequestParam("file")MultipartFile multipartFile){
        // {status : 200, url : ''}
        Map<String,Object> data = new HashMap<>();
        data.put("status", 500);
        try {
            // 1. 获取上传文件的原文件名
            String filename = multipartFile.getOriginalFilename();
            // 2. 获取上传文件的字节数组
            byte[] bytes = multipartFile.getBytes();

            /** ############# 上传文件到FastDFS文件服务器 ############ */
            // a. 获取fastdfs-client.conf文件的路径
            String path = this.getClass().getResource("/fastdfs-client.conf").getPath();
            // b. 初始化客户端全局的对象
            ClientGlobal.init(path);
            // c. 创建存储客户端对象
            StorageClient storageClient = new StorageClient();
            // d. 上传文件
            String[] arr = storageClient.upload_file(bytes,
                    FilenameUtils.getExtension(filename), null);

            // e. 拼接访问图片的URL
            StringBuilder url = new StringBuilder(fileServerUrl);
            // http://192.168.12.131 / arr[0] /arr[1]
            for (String str : arr) {
                url.append("/" + str);
            }

            data.put("status", 200);
            data.put("url", url.toString());
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return data;
    }

}
