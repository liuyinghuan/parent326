package cn.itcast.core.controller.upload;

import cn.itcast.core.entity.Result;
import cn.itcast.core.utils.fdfs.FastDFSClient;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;

    /**
     * 将商品的图片上传到FastDFS上
     * @param file
     * @return
     */
    @RequestMapping("/uploadFile.do")
    public Result uploadFile(MultipartFile file){
        try {
            String conf = "classpath:fastDFS/fdfs_client.conf";
            FastDFSClient fastDFSClient = new FastDFSClient(conf);
            String filename = file.getOriginalFilename(); // 1.jpg
            String extName = FilenameUtils.getExtension(filename);
            String path = fastDFSClient.uploadFile(file.getBytes(), extName, null);
            // java常见维护常量的方式：配置文件、枚举、接口
            String url = FILE_SERVER_URL + path; // 完整的路径
            return new Result(true, url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "上传失败");
        }
    }
}
