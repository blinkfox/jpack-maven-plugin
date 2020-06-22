package com.blinkfox.jpack.utils;

import com.blinkfox.jpack.exception.DockerPackException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;

/**
 * 执行命令行的相关工具类.
 *
 * @author blinkfox on 2020-06-18.
 * @since v1.5.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CmdKit {

    /**
     * 用于判断是否是 Windows 操作系统的全局变量.
     */
    private static final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

    /**
     * 执行命令行进程中的命令.
     *
     * @param cmd 命令行字符串
     * @return 执行结果
     */
    public static String execute(String cmd) {
        Logger.info("【执行指令 -> 开始】准备执行的指令为：【" + cmd + "】.");
        cmd = isWindows ? "cmd /c " + cmd : cmd;

        try {
            // 阻塞等待命令执行结束.
            Process process = Runtime.getRuntime().exec(cmd);
            int exitValue = process.waitFor();
            if (exitValue != 0) {
                throw new DockerPackException("【执行指令 -> 失败】执行命令行指令失败，执行的指令为【" + cmd + "】.");
            }

            // 获取执行结果，并转换为字符串.
            List<String> results = new ArrayList<>();
            convertInputStream(process.getInputStream(), results);
            convertInputStream(process.getErrorStream(), results);
            String result = listToString(results);
            Logger.info("【执行指令 -> 成功】执行指令成功，结果为：\n" + result);
            return result;
        } catch (IOException e) {
            throw new DockerPackException("【执行指令 -> 出错】执行命令行指令失败，执行的指令为【" + cmd + "】，错误原因：【" + e.getMessage() + "】.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DockerPackException("【执行指令 -> 出错】执行命令行指令被中断，执行的指令为【" + cmd + "】，错误原因：【" + e.getMessage() + "】.");
        }
    }

    private static void convertInputStream(InputStream in, List<String> results) throws IOException {
        try (InputStream is = in;
                InputStreamReader inReader = new InputStreamReader(is);
                BufferedReader reader = new BufferedReader(inReader)) {
            String line;
            while ((line = reader.readLine()) != null) {
                results.add(line);
            }
        }
    }

    private static String listToString(List<String> results) {
        StringBuilder sb = new StringBuilder();
        for (String s : results) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }

}
