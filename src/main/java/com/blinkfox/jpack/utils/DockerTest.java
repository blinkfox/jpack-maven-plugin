package com.blinkfox.jpack.utils;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.ImageSearchResult;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.io.RawInputStreamFacade;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * DockerTest.
 *
 * @author blinkfox on 2019-05-08.
 */
public class DockerTest {

    public static void main(String[] args) throws DockerCertificateException,
            DockerException, InterruptedException, IOException {
        DockerClient docker = DefaultDockerClient.fromEnv().build();

        // 构建镜像.
        final AtomicReference<String> imageIdFromMessage = new AtomicReference<>();
        String dockerDirectory = "/Users/blinkfox/Documents/dev/gitrepo/2019/sbtest";
        String returnedImageId = docker.build(Paths.get(dockerDirectory), "my-web-demo:1.1.0", message -> {
            final String imageId = message.buildImageId();
            if (imageId != null) {
                imageIdFromMessage.set(imageId);
            }
        });
        System.out.println("returnedImageId: " + returnedImageId);

        // 导出镜像.
        File destFile = new File("/Users/blinkfox/Downloads/dtest/my-web-demo.tar");
        try (InputStream imageInput = docker.save("my-web-demo:1.1.0")) {
            FileUtils.copyStreamToFile(new RawInputStreamFacade(imageInput), destFile);
            System.out.println("--------导出完毕.");
        }

        docker.close();
    }

}
