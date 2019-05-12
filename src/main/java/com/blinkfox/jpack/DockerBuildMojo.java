package com.blinkfox.jpack;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * 仅仅是构建 Docker 镜像的 Mojo.
 *
 * @author blinkfox on 2019-05-13.
 */
@Mojo(name = "docker-build", defaultPhase = LifecyclePhase.PACKAGE)
public class DockerBuildMojo extends BaseMojo {



}
