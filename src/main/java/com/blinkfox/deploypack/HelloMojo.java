package com.blinkfox.deploypack;

import com.blinkfox.deploypack.utils.CompressKit;
import com.blinkfox.deploypack.utils.TemplateKit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.resource.ResourceManager;
import org.codehaus.plexus.resource.loader.FileResourceCreationException;
import org.codehaus.plexus.resource.loader.ResourceNotFoundException;
import org.codehaus.plexus.util.FileUtils;

/**
 * HelloMojo.
 *
 * @author blinkfox on 2019-04-22.
 */
@Mojo(name = "hello", defaultPhase = LifecyclePhase.PACKAGE)
public class HelloMojo extends AbstractMojo {

    /**
     * bat 文件的名称数组.
     */
    private static final String[] BAT_ARR = {"install", "uninstall", "start", "stop", "restart"};

    /**
     * 日志对象.
     */
    private static Log log;

    @Parameter(defaultValue = "${project.build.directory}", required = true)
    private File targetDir;

    @Parameter(defaultValue = "${project.artifactId}", required = true)
    private String artifactId;

    @Parameter(defaultValue = "${project.version}", required = true)
    private String version;

    @Parameter(defaultValue = "${project.build.finalName}")
    private String finalName;

    @Parameter(defaultValue = "${project.description}")
    private String description;

    /**
     * JVM 相关的参数.
     */
    @Parameter(property = "vmOptions")
    private String vmOptions;

    /**
     * 程序运行时的其他自定义参数.
     */
    @Parameter(property = "args")
    private String args;

    /**
     * 消息.
     */
    @Parameter(property = "msg", defaultValue = "你好 Mojo，当前版本是:${project.version}.")
    private String msg;

    /**
     * bin 文件路径.
     */
    private String binPath;

    /**
     * 执行的方法.
     */
    @Override
    public void execute() {
        log = getLog();
        log.info("执行了我自定义的 Maven 插件方法，得到的msg是:" + msg);
        log.info("开始生成 Windows Service 必要的文件, targetDir: " + targetDir);
        /*创建文件夹*/
        File distDir = new File(targetDir, File.separator + "mydist");

        if (distDir.exists()) {
            try {
                FileUtils.cleanDirectory(distDir);
                log.info("清空文件夹成功！");
            } catch (IOException e) {
                log.error("删除目录失败！请检查文件是否在使用", e);
            }
        } else {
            FileUtils.mkdir(distDir.getPath());
        }

        // 初始化创建 bin 目录.
        initCreateBinPath(distDir);


        FileUtils.mkdir(new File(distDir, File.separator + "logs").getPath());
        try {
            ResourceManager resourceManager = (ResourceManager) new DefaultPlexusContainer().lookup(ResourceManager.ROLE);
            FileUtils.copyFile(resourceManager.getResourceAsFile("windows/README.md"),
                    new File(distDir, File.separator + "README.md"));
            this.renderXml(new File(distDir, this.binPath + this.getProjectName() + ".xml"));
            FileUtils.copyFile(resourceManager.getResourceAsFile("windows/bin/winsw.exe"),
                    new File(distDir, this.binPath + this.getProjectName() + ".exe"));
            FileUtils.copyFile(resourceManager.getResourceAsFile("windows/bin/winsw.exe.config"),
                    new File(distDir, this.binPath + this.getProjectName() + ".exe.config"));
            log.info("copy并读取资源文件成功.");
            FileUtils.copyFile(new File(targetDir.getPath() + File.separator + getFullJarName()),
                    new File(distDir, File.separator + getFullJarName()));
        } catch (IOException | FileResourceCreationException | ResourceNotFoundException
                | PlexusContainerException | ComponentLookupException e) {
            log.error("读取并 copy 资源文件出错!", e);
        }

        // 创建 `.bat` 文件.
        this.createAllBat(distDir);

        log.info("正在制作压缩包....");
        try {
            CompressKit.zip(distDir.getPath(), targetDir.getPath() + File.separator + getProjectName() + ".zip");
            CompressKit.tarGz(distDir.getPath(), targetDir.getPath() + File.separator + getProjectName() + ".tar.gz");
            log.info("正在清除临时文件....");
            FileUtils.forceDelete(distDir);
        } catch (IOException e) {
            log.info("已清除临时文件.");
        }
        log.info("制作压缩文件完成.");
    }

    /**
     * 属性转化.
     *
     * @param xmlFile xml文件
     */
    private void renderXml(File xmlFile) {
        Map<String, Object> context = new HashMap<>(8);
        context.put("projectId", this.artifactId);
        context.put("name", this.getFullJarName());
        context.put("description", this.description);
        this.vmOptions = StringUtils.isBlank(this.vmOptions) ? " " : " " + this.vmOptions + " ";
        this.args = StringUtils.isBlank(this.args) ? "" : " " + this.args;
        context.put("arguments", vmOptions + "-jar ..\\" + this.getFullJarName() + this.args);

        String content = TemplateKit.render("windows/bin/winsw.xml", context);
        try {
            FileUtils.fileWrite(xmlFile, content);
        } catch (IOException e) {
            log.error("写入文件出错!", e);
        }
        log.info("生成的内容:\n" + content);
    }

    /**
     * 创建所有的 bin 目录下的 bat 文件.
     *
     * @param distDir 主文件
     */
    private void createAllBat(File distDir) {
        // 创建各个 .bat 文件.
        for (String str: BAT_ARR) {
            createBat(distDir, this.binPath + str + ".bat", str);
        }
    }

    /**
     * 初始化创建打包时的 bin 目录.
     *
     * @param distDir 打包时的主目录
     */
    private void initCreateBinPath(File distDir) {
        // 先创建 bin 目录.
        this.binPath = StringUtils.join(File.separator, "bin", File.separator);
        try {
            FileUtils.forceMkdir(new File(distDir.getPath() + binPath));
        } catch (IOException e) {
            log.error("创建 bin 目录失败", e);
        }
    }

    /**
     * 创建 bat 文件.
     *
     * @param outDir 输出目录
     * @param fileName 文件名
     * @param text 命令文本
     */
    private void createBat(File outDir, String fileName, String text) {
        if (!outDir.exists()) {
            FileUtils.mkdir(outDir.getPath());
        }

        File file = new File(outDir, fileName);
        try (FileWriter w = new FileWriter(file)) {
            String content = "@echo off\n"
                    + "%1 mshta vbscript:CreateObject(\"Shell.Application\")"
                    + ".ShellExecute(\"cmd.exe\",\"/c %~s0 ::\",\"\",\"runas\",1)(window.close)&&exit\n"
                    + "%~dp0" + this.getProjectName() + ".exe " + text + "\n"
                    + "echo The " + this.getProjectName() + " service current state:\n"
                    + "%~dp0" + this.getProjectName() + ".exe status\n"
                    + "pause";
            log.info("生成的 .bat 内容:\n" + content);
            w.write(content);
        } catch (IOException e) {
            log.error("创建 .bat 文件异常.", e);
        }
    }

    /**
     * 获取打包时 jar 包的名称.
     *
     * @return jar包名
     */
    private String getProjectName() {
        return StringUtils.isNotBlank(this.finalName) ? this.finalName : this.artifactId + "-" + this.version;
    }

    /**
     * 获取打 jar 包时的全名.
     *
     * @return 全名
     */
    private String getFullJarName() {
        return this.getProjectName() + ".jar";
    }

}
