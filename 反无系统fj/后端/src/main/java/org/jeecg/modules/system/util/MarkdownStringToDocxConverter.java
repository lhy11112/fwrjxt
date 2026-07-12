package org.jeecg.modules.system.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Markdown字符串到DOCX转换器
 * 优化版本，支持更多格式和颜色处理
 */
@Slf4j
public class MarkdownStringToDocxConverter {
    // 标题级别对应的字体大小
    private static final int[] HEADING_FONT_SIZES = {0, 18, 16, 14, 13, 12, 11};
    // 正文字体大小
    private static final int NORMAL_FONT_SIZE = 11;
    // 段落间距
    private static final int PARAGRAPH_SPACING = 120;
    /**
     * 将Markdown字符串直接转换为DOCX文件
     */
    public static void convertMarkdownStringToDocx(String markdownContent, String docxPath) throws IOException {
         File file=new File(docxPath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (XWPFDocument document = convertMarkdownStringToDocument(markdownContent);
             FileOutputStream fos = new FileOutputStream(docxPath)) {
            document.write(fos);
        }
    }

    /**
     * 将 Markdown 字符串转换为 XWPFDocument
     */
    public static XWPFDocument convertMarkdownStringToDocument(String markdownContent) {
        XWPFDocument document = new XWPFDocument();
        setupDocumentStyles(document);

        String[] lines = markdownContent.split("\n", -1);
        boolean inTable = false;
        boolean inCodeBlock = false;
        StringBuilder tableContent = new StringBuilder();

        for (String line : lines) {
            // 处理 ``` 代码块
            if (line.trim().startsWith("```") ) {
                inCodeBlock = !inCodeBlock;
                continue;
            }
            if (inCodeBlock) {
                createCodeParagraph(document, line);
                continue;
            }

            // 处理表格
            if (line.trim().startsWith("|") && line.trim().endsWith("|")) {
                if (!inTable) {
                    inTable = true;
                    tableContent = new StringBuilder();
                }
                tableContent.append(line).append("\n");
                continue;
            } else if (inTable) {
                processTable(document, tableContent.toString());
                inTable = false;
            }

            // 空行
            if (line.trim().isEmpty()) {
                createEmptyParagraph(document);
                continue;
            }
            // 常规行
            processLine(document, line);
        }
        // 处理遗留表格内容
        if (inTable) {
            processTable(document, tableContent.toString());
        }
        return document;
    }

    /**
     * 设置文档默认样式
     */
    private static void setupDocumentStyles(XWPFDocument document) {

    }

    /**
     * 创建空段落
     */
    private static void createEmptyParagraph(XWPFDocument document) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText("");
    }

    /**
     * 创建带编号的格式化段落（处理如：1.**基本信息** 这样的格式）
     */
    private static void createNumberedFormattedParagraph(XWPFDocument document, String number, String content) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingAfter(PARAGRAPH_SPACING / 2);

        // 添加编号
        XWPFRun numberRun = paragraph.createRun();
        numberRun.setText(number + ".");
        numberRun.setFontFamily("Times New Roman");
        numberRun.setFontSize(NORMAL_FONT_SIZE);

        // 处理后面的格式化内容
        createFormattedRun(paragraph, content);


    }

    /* ===================== 行处理 ===================== */

   /* private static void processLine(XWPFDocument document, String rawLine) {
        String trimmed = rawLine.trim();
        // 1. 标题
        if (trimmed.startsWith("#")) {
            int level = 0;
            while (level < trimmed.length() && trimmed.charAt(level) == '#') level++;
            if (level > 6) level = 6;
            String content = trimmed.substring(level).trim();
            createFormattedHeadingParagraph(document, content, level);
            return;
        }
        // 2. 编号加粗 "1.**基本信息**"
        Matcher numberedBoldMatcher = Pattern.compile("^(\\d+)\\.(\\*\\*[^*]+\\*\\*.*)$").matcher(trimmed);
        if (numberedBoldMatcher.matches()) {
            createNumberedFormattedParagraph(document, numberedBoldMatcher.group(1), numberedBoldMatcher.group(2));
            return;
        }
        // 3. 有序/无序/引用 列表 及普通段落
        if (trimmed.matches("^(\\d+)\\. .+")) {
            createOrderedListParagraph(document, trimmed.replaceFirst("^(\\d+)\\. ", ""));
            return;
        }
        if (trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("+ ")) {
            createUnorderedListParagraph(document, trimmed.substring(2));
            return;
        }
        if (trimmed.startsWith("> ")) {
            createQuoteParagraph(document, trimmed.substring(2));
            return;
        }
        // 4. 普通段落
        createFormattedParagraph(document, trimmed);
    }*/

    /**
     * 处理单行内容
     */
    private static void processLine(XWPFDocument document, String line) {
        String trimmedLine = line.trim();

        // 标题处理 - 改进版本，支持复杂格式
        if (trimmedLine.startsWith("#")) {
            int level = 0;
            while (level < trimmedLine.length() && trimmedLine.charAt(level) == '#') {
                level++;
            }

            if (level <= 6) {
                // 提取标题内容（包括可能的空格和格式）
                String titleContent;
                if (level < trimmedLine.length()) {
                    // 去掉#号后的内容，保留可能的空格和格式
                    titleContent = trimmedLine.substring(level);
                    // 如果第一个字符是空格，去掉一个空格
                    if (titleContent.startsWith(" ")) {
                        titleContent = titleContent.substring(1);
                    }
                } else {
                    titleContent = ""; // 只有#符号
                }

                if (!titleContent.trim().isEmpty()) {
                    createFormattedHeadingParagraph(document, titleContent, level);
                    return;
                }
            }
        }

        // 检查是否是带编号的格式化文本（如：1.**基本信息**）
        Pattern numberedBoldPattern = Pattern.compile("^(\\d+)\\.(\\*\\*[^*]+\\*\\*.*)$");
        Matcher numberedBoldMatcher = numberedBoldPattern.matcher(trimmedLine);
        if (numberedBoldMatcher.matches()) {
            String number = numberedBoldMatcher.group(1);
            String content = numberedBoldMatcher.group(2);
            createNumberedFormattedParagraph(document, number, content);
            return;
        }

        // 有序列表处理（标准格式：数字. 文本）
        Pattern orderedListPattern = Pattern.compile("^(\\d+)\\. (.*)$");
        Matcher orderedMatcher = orderedListPattern.matcher(trimmedLine);
        if (orderedMatcher.matches()) {
            String content = orderedMatcher.group(2);
            // 检查内容是否包含markdown格式，如果有就当作格式化段落处理
            if (content.contains("**") || content.contains("*") || content.contains("`") || content.contains("<font")) {
                createFormattedParagraph(document, trimmedLine);
            } else {
                createOrderedListParagraph(document, content);
            }
            return;
        }

        // 无序列表处理 - 改进版本，支持格式化内容
        if (trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ") || trimmedLine.startsWith("+ ")) {
            String content = trimmedLine.substring(2).trim();
            createUnorderedListParagraph(document, content);
            return;
        }

        // 引用处理
        if (trimmedLine.startsWith("> ")) {
            String content = trimmedLine.substring(2).trim();
            createQuoteParagraph(document, content);
            return;
        }

        // 普通段落
        createFormattedParagraph(document, trimmedLine);
    }

    /**
     * 创建格式化的标题段落（支持粗体、颜色等格式）
     */
    private static void createFormattedHeadingParagraph(XWPFDocument document, String text, int level) {
        XWPFParagraph p = document.createParagraph();
        p.setSpacingBefore(PARAGRAPH_SPACING);
        p.setSpacingAfter(PARAGRAPH_SPACING / 2);
        int fontSize = (level < HEADING_FONT_SIZES.length) ? HEADING_FONT_SIZES[level] : NORMAL_FONT_SIZE;
        createFormattedRunForHeading(p, text, fontSize, true);
    }

    /**
     * 创建标题段落（简单版本，向后兼容）
     */
    private static void createHeadingParagraph(XWPFDocument document, String text, int level) {
        createFormattedHeadingParagraph(document, text, level);
    }

    /**
     * 为标题创建格式化的文本运行
     */
    private static void createFormattedRunForHeading(XWPFParagraph paragraph, String text, int fontSize, boolean defaultBold) {
        text = processColorTagsForHeading(paragraph, text, fontSize, defaultBold);
        text = processInlineCodeForHeading(paragraph, text, fontSize, defaultBold);
        processTextFormattingForHeading(paragraph, text, fontSize, defaultBold);
    }

    /* ===================== 颜色标签处理（标题） ===================== */

    private static String processColorTagsForHeading(XWPFParagraph paragraph, String text, int fontSize, boolean defaultBold) {
        // **<font>text</font>** in heading
        Pattern boldColorPattern = Pattern.compile("\\*\\*<font\\s+color\\s*=\\s*['\"]([^'\"]+)['\"]\\s*>([^<]*)</font>\\*\\*");
        Matcher boldMatcher = boldColorPattern.matcher(text);
        int lastEnd = 0;
        while (boldMatcher.find()) {
            if (boldMatcher.start() > lastEnd) {
                processTextFormattingForHeading(paragraph, text.substring(lastEnd, boldMatcher.start()), fontSize, defaultBold);
            }
            String result=createColorRun(paragraph, boldMatcher.group(1), boldMatcher.group(2), fontSize, true);
            if (StringUtils.isNotBlank(result)){
                if (log.isInfoEnabled()){
                    log.error("处理成功！！！");
                }
            }
            lastEnd = boldMatcher.end();
        }
        String remaining = text.substring(lastEnd);

        // <font>**text**</font> and others
        Pattern colorPattern = Pattern.compile("<font\\s+color\\s*=\\s*['\"]([^'\"]+)['\"]\\s*>([^<]*)</font>");
        Matcher colorMatcher = colorPattern.matcher(remaining);
        lastEnd = 0;
        while (colorMatcher.find()) {
            if (colorMatcher.start() > lastEnd) {
                processTextFormattingForHeading(paragraph, remaining.substring(lastEnd, colorMatcher.start()), fontSize, defaultBold);
            }
            String result=createColorRun(paragraph, colorMatcher.group(1), colorMatcher.group(2), fontSize, defaultBold);
            if (StringUtils.isNotBlank(result)){
                   if (log.isInfoEnabled()){
                       log.error("处理成功！！！");
                   }
            }
            lastEnd = colorMatcher.end();
        }
        return remaining.substring(lastEnd);
    }

    /**
     * 为标题处理行内代码
     */
    private static String processInlineCodeForHeading(XWPFParagraph paragraph, String text, int fontSize, boolean defaultBold) {
        Pattern codePattern = Pattern.compile("`([^`]+)`");
        Matcher matcher = codePattern.matcher(text);
        int lastEnd = 0;

        while (matcher.find()) {
            // 添加代码前的文本
            if (matcher.start() > lastEnd) {
                String beforeText = text.substring(lastEnd, matcher.start());
                if (!beforeText.trim().isEmpty()) {
                    processTextFormattingForHeading(paragraph, beforeText, fontSize, defaultBold);
                }
            }

            // 创建行内代码
            XWPFRun codeRun = paragraph.createRun();
            codeRun.setText(matcher.group(1));
            codeRun.setFontFamily("Courier New");
            codeRun.setFontSize(Math.max(fontSize - 1, 10));
            codeRun.setBold(defaultBold);

            lastEnd = matcher.end();
        }

        // 返回剩余文本
        return lastEnd < text.length() ? text.substring(lastEnd) : "";
    }

    /**
     * 为标题处理文本格式（粗体、斜体、删除线）
     */
    private static void processTextFormattingForHeading(XWPFParagraph paragraph, String text, int fontSize, boolean defaultBold) {
        if (text.trim().isEmpty()) {
            return;
        }

        // 处理粗体斜体组合 ***text***
        text = processPatternForHeading(paragraph, text, "\\*{3}([^*]+)\\*{3}", true, true, false, fontSize, defaultBold);
        // 处理粗体 **text**
        text = processPatternForHeading(paragraph, text, "\\*{2}([^*]+)\\*{2}", true, false, false, fontSize, defaultBold);
        // 处理斜体 *text*
        text = processPatternForHeading(paragraph, text, "\\*([^*]+)\\*", false, true, false, fontSize, defaultBold);
        // 处理删除线 ~~text~~
        text = processPatternForHeading(paragraph, text, "~~([^~]+)~~", false, false, true, fontSize, defaultBold);

        // 添加剩余的普通文本
        if (!text.trim().isEmpty()) {
            XWPFRun normalRun = paragraph.createRun();
            normalRun.setText(text);
            normalRun.setFontFamily("Times New Roman");
            normalRun.setFontSize(fontSize);
            normalRun.setBold(defaultBold);
        }
    }

    /**
     * 为标题处理特定格式模式
     */
    private static String processPatternForHeading(XWPFParagraph paragraph, String text, String pattern,
                                                   boolean bold, boolean italic, boolean strikethrough,
                                                   int fontSize, boolean defaultBold) {
        Pattern p = Pattern.compile(pattern);
        Matcher matcher = p.matcher(text);
        int lastEnd = 0;

        while (matcher.find()) {
            // 添加格式前的文本
            if (matcher.start() > lastEnd) {
                String beforeText = text.substring(lastEnd, matcher.start());
                if (!beforeText.isEmpty()) {
                    XWPFRun beforeRun = paragraph.createRun();
                    beforeRun.setText(beforeText);
                    beforeRun.setFontFamily("Times New Roman");
                    beforeRun.setFontSize(fontSize);
                    beforeRun.setBold(defaultBold);
                }
            }

            // 创建格式化文本
            XWPFRun formattedRun = paragraph.createRun();
            formattedRun.setText(matcher.group(1));
            formattedRun.setFontFamily("Times New Roman");
            formattedRun.setFontSize(fontSize);
            formattedRun.setBold(bold || defaultBold); // 标题默认粗体或显式粗体
            formattedRun.setItalic(italic);
            formattedRun.setStrikeThrough(strikethrough);

            lastEnd = matcher.end();
        }

        // 返回剩余文本
        return lastEnd < text.length() ? text.substring(lastEnd) : "";
    }

    /**
     * 创建有序列表段落
     */
    private static void createOrderedListParagraph(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        // 设置缩进
        paragraph.setIndentationLeft(720); // 0.5英寸

        XWPFRun run = paragraph.createRun();
        run.setText("• " + text);
        run.setFontFamily("Times New Roman");
        run.setFontSize(NORMAL_FONT_SIZE);
    }

    /**
     * 创建无序列表段落 - 改进版本，支持格式化
     */
    private static void createUnorderedListParagraph(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        // 设置缩进
        paragraph.setIndentationLeft(720); // 0.5英寸

        // 添加项目符号
        XWPFRun bulletRun = paragraph.createRun();
        bulletRun.setText("• ");
        bulletRun.setFontFamily("Times New Roman");
        bulletRun.setFontSize(NORMAL_FONT_SIZE);

        // 处理格式化的内容
        createFormattedRun(paragraph, text);


    }

    /**
     * 创建引用段落
     */
    private static void createQuoteParagraph(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        // 设置引用样式
        paragraph.setIndentationLeft(720); // 左缩进

        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setItalic(true);
        run.setColor("666666");
        run.setFontFamily("Times New Roman");
        run.setFontSize(NORMAL_FONT_SIZE);

    }

    /**
     * 创建代码段落
     */
    private static void createCodeParagraph(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontFamily("Courier New");
        run.setFontSize(10);

    }

    /**
     * 判断是否为普通段落文本（需要首行缩进的文本）
     * 排除标题、列表、引用、代码等特殊格式
     */
    private static boolean isParagraphText(String text) {
        String trimmed = text.trim();

        // 排除标题
        if (trimmed.startsWith("#")) {
            return false;
        }

        // 排除有序列表（但包含格式化的数字编号不排除）
        if (trimmed.matches("^\\d+\\. [^*`~<]+$")) {
            return false;
        }

        // 排除无序列表
        if (trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("+ ")) {
            return false;
        }

        // 排除引用
        if (trimmed.startsWith("> ")) {
            return false;
        }

        // 排除代码块标记
        if (trimmed.startsWith("```")) {
            return false;
        }

        // 排除表格
        if (trimmed.startsWith("|") && trimmed.endsWith("|")) {
            return false;
        }

        // 排除空行
        if (trimmed.isEmpty()) {
            return false;
        }

        // 其他情况都认为是普通段落
        return true;
    }

    /**
     * 创建格式化段落（支持粗体、斜体等）
     */
    private static void createFormattedParagraph(XWPFDocument document, String text) {
        XWPFParagraph p = document.createParagraph();
        p.setSpacingAfter(PARAGRAPH_SPACING / 2);
        if (isParagraphText(text)) p.setIndentationFirstLine(480);
        createFormattedRun(p, text);
    }

    /**
     * 创建格式化的文本运行（处理粗体、斜体、删除线、颜色等）
     */
    private static void createFormattedRun(XWPFParagraph p, String text) {
        text = processColorTags(p, text);
        text = processInlineCode(p, text);
        processTextFormatting(p, text);
    }

    /* ===================== 颜色标签处理（普通段落） ===================== */

    private static String processColorTags(XWPFParagraph paragraph, String text) {
        // 先处理 **<font ...>text</font>**（已有逻辑）
        Pattern boldColorPattern = Pattern.compile("\\*\\*<font\\s+color\\s*=\\s*['\"]([^'\"]+)['\"]\\s*>([^<]*)</font>\\*\\*");
        Matcher boldColorMatcher = boldColorPattern.matcher(text);
        int lastEnd = 0;
        while (boldColorMatcher.find()) {
            if (boldColorMatcher.start() > lastEnd) {
                processTextFormatting(paragraph, text.substring(lastEnd, boldColorMatcher.start()));
            }
            String result=createColorRun(paragraph, boldColorMatcher.group(1), boldColorMatcher.group(2), NORMAL_FONT_SIZE, true);
            if (StringUtils.isNotBlank(result)){
                if (log.isInfoEnabled()){
                    log.error("处理成功！！！");
                }
            }
            lastEnd = boldColorMatcher.end();
        }
        String remaining = text.substring(lastEnd);

        // 处理 <font ...>内容</font> （新增内部格式识别）
        Pattern colorPattern = Pattern.compile("<font\\s+color\\s*=\\s*['\"]([^'\"]+)['\"]\\s*>([^<]*)</font>");
        Matcher matcher = colorPattern.matcher(remaining);
        lastEnd = 0;
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                processTextFormatting(paragraph, remaining.substring(lastEnd, matcher.start()));
            }
            String result=createColorRun(paragraph, matcher.group(1), matcher.group(2), NORMAL_FONT_SIZE, false);
            if (StringUtils.isNotBlank(result)){
                if (log.isInfoEnabled()){
                    log.error("处理成功！！！");
                }
            }
            lastEnd = matcher.end();
        }
        return remaining.substring(lastEnd);
    }

    /* ===================== 新增：颜色标签中嵌套格式处理 ===================== */

    /**
     * 颜色标签外层：**<font color='red'>内容</font>** 已支持。
     * 新增：<font color='red'>**内容**</font> 及其它格式化符号的支持。
     */
    private static String createColorRun(XWPFParagraph paragraph, String colorName, String inner, int fontSize, boolean inheritBold) {
        // 解析内部格式 ** / * / ~~ 等
        boolean bold = inheritBold;
        boolean italic = false;
        boolean strike = false;
        String content = inner;
        if (content.matches("^\\*{3}.+\\*{3}$")) { // ***加粗斜体***
            bold = true; italic = true; content = content.substring(3, content.length() - 3);
        } else if (content.matches("^\\*{2}.+\\*{2}$")) { // **加粗**
            bold = true; content = content.substring(2, content.length() - 2);
        } else if (content.matches("^\\*.+\\*$")) { // *斜体*
            italic = true; content = content.substring(1, content.length() - 1);
        } else if (content.matches("^~~.+~~$")) { // ~~删除线~~
            strike = true; content = content.substring(2, content.length() - 2);
        }
        XWPFRun run = paragraph.createRun();
        run.setText(content);
        run.setFontFamily("Times New Roman");
        run.setFontSize(fontSize);
        run.setBold(bold);
        run.setItalic(italic);
        run.setStrikeThrough(strike);
        run.setColor(convertColorNameToHex(colorName));
        return "";  // 返回空串，代表已处理完
    }
    /**
     * 转换颜色名称为十六进制值
     */
    private static String convertColorNameToHex(String colorName) {
        switch (colorName.toLowerCase()) {
            case "red": return "FF0000";
            case "blue": return "0000FF";
            case "green": return "008000";
            case "yellow": return "FFFF00";
            case "orange": return "FFA500";
            case "purple": return "800080";
            case "pink": return "FFC0CB";
            case "black": return "000000";
            case "white": return "FFFFFF";
            case "gray": case "grey": return "808080";
            default:
                // 如果已经是十六进制格式，直接返回（去掉#符号）
                if (colorName.startsWith("#")) {
                    return colorName.substring(1);
                }
                // 默认返回黑色
                return "000000";
        }
    }

    /**
     * 处理行内代码
     */
    private static String processInlineCode(XWPFParagraph paragraph, String text) {
        Pattern codePattern = Pattern.compile("`([^`]+)`");
        Matcher matcher = codePattern.matcher(text);
        int lastEnd = 0;

        while (matcher.find()) {
            // 添加代码前的文本
            if (matcher.start() > lastEnd) {
                String beforeText = text.substring(lastEnd, matcher.start());
                if (!beforeText.trim().isEmpty()) {
                    processTextFormatting(paragraph, beforeText);
                }
            }

            // 创建行内代码
            XWPFRun codeRun = paragraph.createRun();
            codeRun.setText(matcher.group(1));
            codeRun.setFontFamily("Courier New");
            codeRun.setFontSize(NORMAL_FONT_SIZE - 1);

            lastEnd = matcher.end();
        }

        // 返回剩余文本
        return lastEnd < text.length() ? text.substring(lastEnd) : "";
    }

    /**
     * 处理文本格式（粗体、斜体、删除线）
     */
    private static void processTextFormatting(XWPFParagraph paragraph, String text) {
        if (text.trim().isEmpty()) {
            return;
        }

        // 处理粗体斜体组合 ***text***
        text = processPattern(paragraph, text, "\\*{3}([^*]+)\\*{3}", true, true, false);
        // 处理粗体 **text**
        text = processPattern(paragraph, text, "\\*{2}([^*]+)\\*{2}", true, false, false);
        // 处理斜体 *text*
        text = processPattern(paragraph, text, "\\*([^*]+)\\*", false, true, false);
        // 处理删除线 ~~text~~
        text = processPattern(paragraph, text, "~~([^~]+)~~", false, false, true);

        // 添加剩余的普通文本
        if (!text.trim().isEmpty()) {
            XWPFRun normalRun = paragraph.createRun();
            normalRun.setText(text);
            normalRun.setFontFamily("Times New Roman");
            normalRun.setFontSize(NORMAL_FONT_SIZE);
        }
    }

    /**
     * 处理特定格式模式
     */
    private static String processPattern(XWPFParagraph paragraph, String text, String pattern,
                                         boolean bold, boolean italic, boolean strikethrough) {
        Pattern p = Pattern.compile(pattern);
        Matcher matcher = p.matcher(text);
        int lastEnd = 0;

        while (matcher.find()) {
            // 添加格式前的文本
            if (matcher.start() > lastEnd) {
                String beforeText = text.substring(lastEnd, matcher.start());
                if (!beforeText.isEmpty()) {
                    XWPFRun beforeRun = paragraph.createRun();
                    beforeRun.setText(beforeText);
                    beforeRun.setFontFamily("Times New Roman");
                    beforeRun.setFontSize(NORMAL_FONT_SIZE);
                }
            }

            // 创建格式化文本
            XWPFRun formattedRun = paragraph.createRun();
            formattedRun.setText(matcher.group(1));
            formattedRun.setFontFamily("Times New Roman");
            formattedRun.setFontSize(NORMAL_FONT_SIZE);
            formattedRun.setBold(bold);
            formattedRun.setItalic(italic);
            formattedRun.setStrikeThrough(strikethrough);

            lastEnd = matcher.end();
        }

        // 返回剩余文本
        return lastEnd < text.length() ? text.substring(lastEnd) : "";
    }

    /**
     * 处理表格
     */
    private static void processTable(XWPFDocument document, String tableContent) {
        String[] lines = tableContent.trim().split("\n");
        if (lines.length < 2) return;

        // 过滤掉分隔符行
        String[] dataLines = new String[lines.length];
        int dataIndex = 0;
        for (String line : lines) {
            if (!line.trim().matches("^\\|[\\s\\-\\|:]+\\|$")) {
                dataLines[dataIndex++] = line;
            }
        }

        if (dataIndex == 0) return;

        // 创建表格
        XWPFTable table = document.createTable();
        boolean isFirstRow = true;

        for (int i = 0; i < dataIndex; i++) {
            String line = dataLines[i];
            String[] cells = line.split("\\|");

            // 清理单元格内容，去掉首尾空元素
            String[] cleanCells = new String[Math.max(0, cells.length - 2)];
            for (int j = 1; j < cells.length - 1; j++) {
                cleanCells[j - 1] = cells[j].trim();
            }

            if (cleanCells.length == 0) continue;

            XWPFTableRow row;
            if (i == 0) {
                row = table.getRow(0); // 使用默认创建的第一行
            } else {
                row = table.createRow();
            }

            // 确保行有足够的单元格
            while (row.getTableCells().size() < cleanCells.length) {
                row.createCell();
            }

            // 填充单元格
            for (int j = 0; j < cleanCells.length && j < row.getTableCells().size(); j++) {
                XWPFTableCell cell = row.getCell(j);
                // 清除默认段落并创建新段落
                cell.removeParagraph(0);
                XWPFParagraph cellPara = cell.addParagraph();
                XWPFRun cellRun = cellPara.createRun();
                cellRun.setText(cleanCells[j]);
                cellRun.setFontFamily("Times New Roman");
                cellRun.setFontSize(NORMAL_FONT_SIZE);

                // 表头样式
                if (isFirstRow) {
                    cellRun.setBold(true);
                }
            }
            isFirstRow = false;
        }


    }
}
