package com.blinkfox.jpack.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.ClasspathResourceLoader;
import org.codehaus.plexus.util.FileUtils;

/**
 * 模板生成工具类，这里使用高性能的 beetl 来输出模板.
 *
 * @author blinkfox on 2019-03-30.
 * @since v1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TemplateKit {

    /**
     * Beetl 的 GroupTemplate 模版.
     */
    private static GroupTemplate groupTemplate;

    static {
        // 初始化 beetl 模板.
        try {
            Configuration cfg = Configuration.defaultConfiguration();
            cfg.getResourceMap().put("autoCheck", "false");
            groupTemplate = new GroupTemplate(new ClasspathResourceLoader("/"), cfg);
        } catch (IOException e) {
            Logger.error("【jpack -> '初始出错'】初始化 beetl 模版出错！", e);
        }
    }

    /**
     * 根据模版文件的相对路径名及其对应的上下文参数来渲染模版.
     *
     * @param filePath 文件路径
     * @param context 上下文参数
     * @return 渲染的字符串结果
     */
    private static String render(String filePath, Map<String, Object> context) {
        Template template = groupTemplate.getTemplate(filePath);
        template.fastBinding(context);
        return template.render();
    }

    /**
     * 渲染指定全路径下的模板文件到指定的输出路径中.
     *
     * @param template 模板文件
     * @param context 上下文参数
     * @param out 输出路径
     * @throws IOException IO异常
     */
    public static void renderFile(String template, Map<String, Object> context, String out) throws IOException {
        FileUtils.fileWrite(out, StandardCharsets.UTF_8.name(), TemplateKit.render(template, context));
    }

}
