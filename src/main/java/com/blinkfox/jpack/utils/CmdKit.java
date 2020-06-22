package com.blinkfox.jpack.utils;

import com.blinkfox.jpack.exception.DockerPackException;
import com.blinkfox.jpack.exception.PackException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.experimental.UtilityClass;

/**
 * 执行命令行的相关工具类.
 *
 * @author blinkfox on 2020-06-18.
 * @since v1.5.0
 */
@UtilityClass
public class CmdKit {

    /**
     * 执行命令行进程中的命令.
     *
     * @param cmd 命令行字符串
     * @return 执行结果
     */
    public String execute(String[] cmd) {
        String cmdStr = Arrays.toString(cmd);
        Logger.debug("【执行指令 -> 开始】准备执行的指令为：【" + cmdStr + "】.");
        try {
            // 阻塞等待命令执行结束.
            Process process = Runtime.getRuntime().exec(cmd);
            int exitValue = process.waitFor();
            if (exitValue != 0) {
                throw new DockerPackException("【执行指令 -> 失败】执行命令行指令失败.");
            }

            // 获取执行结果，并转换为字符串.
            List<String> results = new ArrayList<>();
            convertInputStream(process.getInputStream(), results);
            convertInputStream(process.getErrorStream(), results);
            return listToString(results);
        } catch (IOException e) {
            throw new PackException("【执行指令 -> 出错】执行命令行指令失败，错误原因：【" + e.getMessage() + "】.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PackException("【执行指令 -> 出错】执行命令行指令被中断，错误原因：【" + e.getMessage() + "】.");
        }
    }

    private void convertInputStream(InputStream in, List<String> results) throws IOException {
        try (InputStream is = in;
                InputStreamReader inReader = new InputStreamReader(is);
                BufferedReader reader = new BufferedReader(inReader)) {
            String line;
            while ((line = reader.readLine()) != null) {
                results.add(line);
            }
        }
    }

    private String listToString(List<String> results) {
        StringBuilder sb = new StringBuilder();
        int len = results.size();
        if (len > 0) {
            for (int i = 0; i < len; ++i) {
                sb.append(results.get(i));
                if (i < len - 1) {
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }

}
