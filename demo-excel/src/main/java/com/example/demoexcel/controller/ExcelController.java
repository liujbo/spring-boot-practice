package com.example.demoexcel.controller;

import com.example.demoexcel.service.*;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/excel")
@AllArgsConstructor
public class ExcelController {

    private final ExcelSplitService excelSplitService;

    /**
     * 传入文件并根据机构号进行数据拆分生成对应的Excel文件并下载zip
     *
     * @param file     原始Excel文件
     * @param orgCodes 目标机构号
     * @return 响应信息
     * @throws Exception 异常信
     */
    @PostMapping("/process/splitAndZip")
    public ResponseEntity<InputStreamResource> splitExcelByOrgCodes(@RequestParam("file") MultipartFile file, @RequestParam("orgCodes") String orgCodes) throws Exception {

        // 解析机构号，支持逗号分隔
        List<String> targetOrgCodes = Arrays.asList(orgCodes.split(","));

        // 拆分Excel文件
        ByteArrayOutputStream zipStream = excelSplitService.splitExcelByOrgCodes(file, targetOrgCodes);

        // 创建ZIP响应
        ByteArrayInputStream bis = new ByteArrayInputStream(zipStream.toByteArray());
        InputStreamResource resource = new InputStreamResource(bis);

        String filename = "org_split.zip";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(zipStream.size())
                .body(resource);
    }


    private final ExcelMergeService excelMergeService;

    /**
     * 合并Excel文件并导出ZIP
     */
    @PostMapping("/merge")
    public ResponseEntity<byte[]> mergeExcelSheets(@RequestParam("files") List<MultipartFile> excelFiles) {
        try {
            // 验证文件
            if (excelFiles == null || excelFiles.isEmpty()) {
                throw new IllegalArgumentException("请至少上传一个Excel文件");
            }

            for (MultipartFile file : excelFiles) {
                if (file.isEmpty()) {
                    throw new IllegalArgumentException("上传的文件不能为空");
                }
                if (!file.getOriginalFilename().toLowerCase().endsWith(".xlsx") &&
                        !file.getOriginalFilename().toLowerCase().endsWith(".xls")) {
                    throw new IllegalArgumentException("只能上传Excel文件(.xlsx或.xls)");
                }
            }

            // 合并文件
            byte[] zipBytes = excelMergeService.mergeExcelSheetsToZip(excelFiles);

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "merged_excel_data.zip");
            headers.setContentLength(zipBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(zipBytes);

        } catch (IOException e) {
            throw new RuntimeException("文件处理失败: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("参数错误: " + e.getMessage(), e);
        }
    }


    private final ExcelMergeService_1 excelMergeService_1;

    /**
     * 优化的合并接口（避免内存溢出）
     */
    @PostMapping("/merge-optimized")
    public ResponseEntity<byte[]> mergeExcelOptimized(@RequestParam("files") List<MultipartFile> excelFiles) {
        try {
            // 使用优化的合并方法
            byte[] zipBytes = excelMergeService_1.mergeExcelSheetsToZipOptimized(excelFiles);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "merged_data.zip");
            headers.setContentLength(zipBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(zipBytes);
        } catch (Exception e) {
            throw new RuntimeException("合并失败: " + e.getMessage(), e);
        }
    }


    private final ExcelMergeService_2 excelMergeService_2;

    /**
     * 合并Excel文件并导出ZIP
     */
    @PostMapping("/merge2")
    public ResponseEntity<byte[]> mergeExcelSheets_2(@RequestParam("files") List<MultipartFile> excelFiles) {
        try {
            // 验证文件
            if (excelFiles == null || excelFiles.isEmpty()) {
                throw new IllegalArgumentException("请至少上传一个Excel文件");
            }

            for (MultipartFile file : excelFiles) {
                if (file.isEmpty()) {
                    throw new IllegalArgumentException("上传的文件不能为空");
                }
            }

            // 合并文件
            byte[] zipBytes = excelMergeService_2.mergeExcelSheetsToZip_2(excelFiles);

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "merged_excel_data.zip");
            headers.setContentLength(zipBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(zipBytes);

        } catch (IOException e) {
            throw new RuntimeException("文件处理失败: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("参数错误: " + e.getMessage(), e);
        }
    }

    private final ExcelMergeService_3 excelMergeService_3;

    /**
     * 合并Excel文件并导出ZIP
     */
    @PostMapping("/merge3")
    public ResponseEntity<byte[]> mergeExcelSheets_3(@RequestParam("files") List<MultipartFile> excelFiles) {
        try {
            // 验证文件
            if (excelFiles == null || excelFiles.isEmpty()) {
                throw new IllegalArgumentException("请至少上传一个Excel文件");
            }

            for (MultipartFile file : excelFiles) {
                if (file.isEmpty()) {
                    throw new IllegalArgumentException("上传的文件不能为空");
                }
            }

            // 合并文件
            byte[] zipBytes = excelMergeService_3.mergeExcelSheetsToZip_3(excelFiles);

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "merged_excel_data.zip");
            headers.setContentLength(zipBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(zipBytes);

        } catch (IOException e) {
            throw new RuntimeException("文件处理失败: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("参数错误: " + e.getMessage(), e);
        }
    }
}
