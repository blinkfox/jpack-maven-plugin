package com.blinkfox.jpack.utils;

import com.blinkfox.jpack.exception.DockerPackException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;

/**
 * 执行命令行的相关工具类.
 *
 * @author blinkfox on 2020-06-18.
 * @since v1.0.0
 */
@UtilityClass
public class CmdKit {

    /**
     * 执行命令行进程中的命令.
     *
     * @param cmd 命令行字符串
     * @return 执行结果
     */
    public String execute(String cmd) {
        Logger.info("【执行指令 -> 开始】准备执行的指令为：【" + cmd + "】.");
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
            try (InputStream in = process.getInputStream()) {
                Logger.info("【执行指令 -> 成功】执行指令成功.");
                return IOUtils.toString(in, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new DockerPackException("【执行指令 -> 出错】执行命令行指令失败，执行的指令为【" + cmd + "】，错误原因：【" + e.getMessage() + "】.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DockerPackException("【执行指令 -> 出错】执行命令行指令被中断，执行的指令为【" + cmd + "】，错误原因：【" + e.getMessage() + "】.");
        }
    }

}
