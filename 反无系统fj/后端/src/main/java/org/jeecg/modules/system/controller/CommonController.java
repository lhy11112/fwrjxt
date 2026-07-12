package org.jeecg.modules.system.controller;

//import cn.hutool.extra.pinyin.PinyinUtil;
import cn.hutool.http.HttpRequest;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.sun.javafx.scene.shape.PathUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.*;
import org.jeecg.common.util.wwct.DownLoadUtils;
import org.jeecg.modules.system.dto.ChatContentExportRequest;
import org.jeecg.modules.system.util.MarkdownStringToDocxConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @Author scott
 * @since 2018-12-20
 */
@Slf4j
@RestController
@RequestMapping("/sys/common")
public class CommonController {
    @Value(value = "${jeecg.path.upload}")
    private String uploadpath;

    @Value(value = "${jeecg.path.hyphen}")
    private String hyphen;

    @Value(value = "${bs.KsFxUrl}")
    private String KsFxUrl;
    /**
     * 本地：local minio：minio 阿里：alioss
     */
    @Value(value = "${jeecg.uploadType}")
    private String uploadType;
    @Value("${wjbd.wrj.djms}")
    private String IsDjms;
    @Autowired
    private DownLoadUtils downLoadUtils;

    /**
     * @return
     * @Author 政辉
     */
    @GetMapping("/403")
    public AjaxResult<?> noauth() {
        return AjaxResult.error("没有权限，请联系管理员授权");
    }

    /**
     * 文件上传统一方法
     *
     * @return
     */
    @PostMapping(value = "/upload")
    public AjaxResult<?> upload(HttpServletRequest request) {
        String bizPath = request.getParameter("biz");
        if (bizPath.contains("../") || bizPath.contains("/")) {
            throw new JeecgBootException("参数bizPath不合法");
        }
        StandardMultipartHttpServletRequest standardMultipartHttpServletRequest = new StandardMultipartHttpServletRequest(request, false);
        MultiValueMap<String, MultipartFile> multiFileMap = standardMultipartHttpServletRequest.getMultiFileMap();
        if (multiFileMap.get("file") == null) {
            throw new JeecgBootException("请先上传文件！");
        }
        MultipartFile wj = multiFileMap.get("file").get(0);

        if (wj == null) {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            // 获取上传文件对象
            wj = multipartRequest.getFile("file");
        }
        AjaxResult<?> result = new AjaxResult<>();
        String savePath = "";
        if (oConvertUtils.isEmpty(bizPath)) {
            bizPath = "upload";
        }
        // 限制文件类型
        String fileName = wj.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            result.setMessage("文件名不能为空！");
            result.setSuccess(false);
            return result;
        }
        String extension = fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
        extension=extension.substring(1,extension.length());
        List<String> allowedExtensions = Arrays.asList(
                "jpg", "png", "gif", "jpeg", "bmp", "webp", // 图片格式
                "pdf", "doc", "docx", "xls", "xlsx", "txt", "odt", // 文档格式
                "mp4", "avi", "mov", "mkv", "flv", "mpeg", "wmv", // 视频格式
                "wav", "mp3", "aac", "ogg", // 音频格式
                "zip", "rar", "tar.gz", "7z" // 压缩包格式
        );
        if (!allowedExtensions.contains(extension)) {
            result.error500("不支持的文件类型！");
            return result;
        }
        if (CommonConstant.UPLOAD_TYPE_LOCAL.equals(uploadType)) {
            savePath = this.uploadLocal(wj, bizPath);
        }

        if (oConvertUtils.isNotEmpty(savePath)) {

            result.setMessage(savePath);
            result.setSuccess(true);
        } else {
            result.setMessage("上传失败！");
            result.setSuccess(false);
        }
        return result;
    }

    /**
     * 本地文件上传
     *
     * @param mf      文件
     * @param bizPath 自定义路径
     * @return
     */
    private String uploadLocal(MultipartFile mf, String bizPath) {
        try {
            String ctxPath = uploadpath;
            String fileName = null;
            File file = new File(ctxPath + File.separator + bizPath + File.separator);
            if (!file.exists()) {
                // 创建文件根目录
                file.mkdirs();
            }
            // 获取文件名
            String orgName = mf.getOriginalFilename();
            orgName = CommonUtils.getFileName(orgName);
            if (orgName.indexOf('.') != -1) {
                fileName = orgName.substring(0, orgName.lastIndexOf('.')) + "_" + System.currentTimeMillis() + orgName.substring(orgName.indexOf('.'));
            } else {
                fileName = orgName + "_" + System.currentTimeMillis();
            }
            if (new Boolean(hyphen)) {
                String str = fileName.replaceAll("\\_", "");
                if (StringUtils.isNotBlank(str)) {
                    if (log.isInfoEnabled()) {
                        log.info("替换成功！！！");
                    }
                }
            }
            //fileName = PinyinUtil.getFirstLetter(fileName, "");
            String savePath = file.getPath() + File.separator + fileName;
            File savefile = new File(savePath);
            FileCopyUtils.copy(mf.getBytes(), savefile);
            String dbpath = null;
            if (oConvertUtils.isNotEmpty(bizPath)) {
                dbpath = bizPath + File.separator + fileName;
            } else {
                dbpath = fileName;
            }
            if (dbpath.contains("\\")) {
                dbpath = dbpath.replace("\\", "/");
            }
            return dbpath;
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }

        }
        return "";
    }

//	@PostMapping(value = "/upload2")
//	public AjaxResult<?> upload2(HttpServletRequest request, HttpServletResponse response) {
//		AjaxResult<?> result = new Result<>();
//		try {
//			String ctxPath = uploadpath;
//			String fileName = null;
//			String bizPath = "files";
//			String tempBizPath = request.getParameter("biz");
//			if(oConvertUtils.isNotEmpty(tempBizPath)){
//				bizPath = tempBizPath;
//			}
//			String nowday = new SimpleDateFormat("yyyyMMdd").format(new Date());
//			File file = new File(ctxPath + File.separator + bizPath + File.separator + nowday);
//			if (!file.exists()) {
//				file.mkdirs();// 创建文件根目录
//			}
//			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
//			MultipartFile mf = multipartRequest.getFile("file");// 获取上传文件对象
//			String orgName = mf.getOriginalFilename();// 获取文件名
//			fileName = orgName.substring(0, orgName.lastIndexOf(".")) + "_" + System.currentTimeMillis() + orgName.substring(orgName.indexOf("."));
//			String savePath = file.getPath() + File.separator + fileName;
//			File savefile = new File(savePath);
//			FileCopyUtils.copy(mf.getBytes(), savefile);
//			String dbpath = bizPath + File.separator + nowday + File.separator + fileName;
//			if (dbpath.contains("\\")) {
//				dbpath = dbpath.replace("\\", "/");
//			}
//			result.setMessage(dbpath);
//			result.setSuccess(true);
//		} catch (IOException e) {
//			result.setSuccess(false);
//			result.setMessage(e.getMessage());
//			log.error(e.getMessage(), e);
//		}
//		return result;
//	}

    /**
     * 预览图片&下载文件
     * 请求地址：http://localhost:8080/common/static/{user/20190119/e1fe9925bc315c60addea1b98eb1cb1349547719_1547866868179.jpg}
     *
     * @param request
     * @param response
     */
    @GetMapping(value = "/static/**")
    public void view(HttpServletRequest request, HttpServletResponse response) {
        // ISO-8859-1 ==> UTF-8 进行编码转换
        String imgPath = extractPathFromPattern(request);
        if (StringUtils.isBlank(imgPath)) {
            return;
        }
        // 其余处理略
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            imgPath = imgPath.replace("..", "");
            if (imgPath.endsWith(",")) {
                imgPath = imgPath.substring(0, imgPath.length() - 1);
            }
            String filePath = uploadpath + File.separator + imgPath;
            File file = new File(filePath);
            if (!file.exists()) {
                response.setStatus(404);
                throw new NullPointerException("文件不存在！！！");
            }
            // 设置强制下载不打开
            response.setContentType("application/force-download");
            response.addHeader("Content-Disposition", "attachment;fileName=" + new String(file.getName().getBytes("UTF-8"), "iso-8859-1"));
            inputStream = new BufferedInputStream(new FileInputStream(filePath));
            outputStream = response.getOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
            response.flushBuffer();
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage());
            }
            response.setStatus(404);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    if (log.isErrorEnabled()) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    if (log.isErrorEnabled()) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }

    }

//	/**
//	 * 下载文件
//	 * 请求地址：http://localhost:8080/common/download/{user/20190119/e1fe9925bc315c60addea1b98eb1cb1349547719_1547866868179.jpg}
//	 *
//	 * @param request
//	 * @param response
//	 * @throws Exception
//	 */
//	@GetMapping(value = "/download/**")
//	public void download(HttpServletRequest request, HttpServletResponse response) throws Exception {
//		// ISO-8859-1 ==> UTF-8 进行编码转换
//		String filePath = extractPathFromPattern(request);
//		// 其余处理略
//		InputStream inputStream = null;
//		OutputStream outputStream = null;
//		try {
//			filePath = filePath.replace("..", "");
//			if (filePath.endsWith(",")) {
//				filePath = filePath.substring(0, filePath.length() - 1);
//			}
//			String localPath = uploadpath;
//			String downloadFilePath = localPath + File.separator + filePath;
//			File file = new File(downloadFilePath);
//	         if (file.exists()) {
//	         	response.setContentType("application/force-download");// 设置强制下载不打开            
//	 			response.addHeader("Content-Disposition", "attachment;fileName=" + new String(file.getName().getBytes("UTF-8"),"iso-8859-1"));
//	 			inputStream = new BufferedInputStream(new FileInputStream(file));
//	 			outputStream = response.getOutputStream();
//	 			byte[] buf = new byte[1024];
//	 			int len;
//	 			while ((len = inputStream.read(buf)) > 0) {
//	 				outputStream.write(buf, 0, len);
//	 			}
//	 			response.flushBuffer();
//	         }
//
//		} catch (Exception e) {
//			log.info("文件下载失败" + e.getMessage());
//			// e.printStackTrace();
//		} finally {
//			if (inputStream != null) {
//				try {
//					inputStream.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//			if (outputStream != null) {
//				try {
//					outputStream.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//
//	}


    /**
     * 把指定URL后的字符串全部截断当成参数
     * 这么做是为了防止URL中包含中文或者特殊字符（/等）时，匹配不了的问题
     *
     * @param request
     * @return
     */
    private static String extractPathFromPattern(final HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return new AntPathMatcher().extractPathWithinPattern(bestMatchPattern, path);
    }

    /**
     * 文档内容
     *
     * @param path
     * @return
     */
    @GetMapping("/GetWjContent")
    public AjaxResult<?> GetProvince(String path) throws IOException, InterruptedException {
        String filePath = uploadpath + File.separator + path;
        String fileExtension = getFileExtension(filePath);
        String content = "";
        try {
            if ("doc".equalsIgnoreCase(fileExtension)) {
                //System.out.println("Reading .doc file content:");
                content = readDocFile(filePath);
            } else if ("docx".equalsIgnoreCase(fileExtension)) {
                //System.out.println("Reading .docx file content:");
                content = readDocxContent(filePath);
            }
        } catch (IOException e) {
            //System.out.println("An error occurred while processing the file: " + e.getMessage());
            if (log.isInfoEnabled()) {
                log.info(e.getMessage());
            }
        }
        return AjaxResult.OK(content);
    }

    private static String getFileExtension(String filePath) {
        int dotIndex = filePath.lastIndexOf('.');
        if (dotIndex > 0) {
            return filePath.substring(dotIndex + 1);
        }
        return "";
    }

    public static String readDocxContent(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        FileInputStream fis = null;
        XWPFDocument document=null;
        try {
            fis = new FileInputStream(filePath);
            document = new XWPFDocument(fis);
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                for (XWPFRun run : paragraph.getRuns()) {
                    content.append(run.getText(0));
                }
            }
        } catch (JeecgBootException e) {
          if (log.isInfoEnabled()){
              log.error(e.getMessage());
          }
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    if (log.isInfoEnabled()){
                        log.error(e.getMessage());
                    }
                }
            }
            if (document != null) {
                try {
                    document.close();
                } catch (IOException e) {
                    if (log.isInfoEnabled()){
                        log.error(e.getMessage());
                    }
                }
            }
        }
        return content.toString();
    }

    public static String readDocFile(String filePath) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        HWPFDocument doc = new HWPFDocument(fis);
        Range range = doc.getRange();
        String text = range.text();
        fis.close();
        return text;
    }

    /**
     * 扩散分析接口调用
     *
     * @param jd
     * @param wd
     * @param jl
     * @return
     */
    @GetMapping("/KsFx")
    public AjaxResult<?> GetProvince(String jd, String wd, String jl, String isReturnComplexArea, String serviceBufferRadius) throws IOException, InterruptedException {
        String url = KsFxUrl + "?isFromCenter=true&isReturnComplexArea=false&parameter={resultSetting:{returnEdgeFeatures:true,returnEdgeGeometry:false,returnEdgeIDs:false,returnNodeFeatures:false,returnNodeGeometry:false,returnNodeIDs:false,returnPathGuides:false,returnRoutes:true},weightFieldName:length}&centers=[{x:" + jd + ",y:" + wd + "}]&isAnalyzeById=false&weights=[" + jl + "]";
        String content = null;
        try {
            String body = HttpRequest.get(url).timeout(60000).execute().body();
            //System.out.println("地理接口数据"+body);
            content = body;
        } catch (JeecgBootException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage());
            }
        }
        return AjaxResult.OK(content);
    }

    /**
     * @param chatContentExportRequest 请求参数
     */
    @PostMapping(value = "/chatContent/export")
    public void chatContentExport(@RequestBody ChatContentExportRequest chatContentExportRequest, HttpServletResponse response) throws IOException {
        if (StringUtils.isBlank(chatContentExportRequest.getWjmc())) {
            chatContentExportRequest.setWjmc(String.valueOf(System.currentTimeMillis()));
        }
        MarkdownStringToDocxConverter.convertMarkdownStringToDocx(chatContentExportRequest.getChatContentList().get(0), uploadpath + File.separator + "fxbg" + File.separator + chatContentExportRequest.getWjmc() + ".docx");
        File file = new File(uploadpath + File.separator + "fxbg" + File.separator + chatContentExportRequest.getWjmc() + ".docx");
        //下载文件
        downLoadUtils.downFileByContentType(response, file, file.getName(), true);
    }
}
