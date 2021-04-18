/*
 * Copyright 2012-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.loader;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.boot.loader.archive.Archive;
import org.springframework.boot.loader.archive.ExplodedArchive;
import org.springframework.boot.loader.archive.JarFileArchive;
import org.springframework.boot.loader.jar.JarFile;

/**
 * Base class for launchers that can start an application with a fully configured
 * classpath backed by one or more {@link Archive}s.
 *
 * @author Phillip Webb
 * @author Dave Syer
 * @since 1.0.0
 */
public abstract class Launcher {

	private static final String JAR_MODE_LAUNCHER = "org.springframework.boot.loader.jarmode.JarModeLauncher";

	/**
	 * Launch the application. This method is the initial entry point that should be
	 * called by a subclass {@code public static void main(String[] args)} method.
	 * @param args the incoming arguments
	 * @throws Exception if the application fails to launch
	 */
	protected void launch(String[] args) throws Exception {
		if (!isExploded()) {
			//扩展 java.net.URLStreamHandler（具体原理可以看 URL#getURLStreamHandler），将org.springframework.boot.loader.jar.Handler替换 jdk默认的jar类型url处理器（sun.net.www.protocol.jar.Handler）
			//原因：java默认除了classpath、file、vfsfile、vfs这几种协议，其他资源都是通过URLResource加载的，而URLResource默认使用的是 URL类加载资源，URL类对于每种协议的处理都是通过 URLStreamHandler机制扩展的
			//spring Boot fat jar 是jar文件，所以不是classpath、file、vfsfile、vfs这几种协议，而spring Boot fat jar除包含传统 java中的资源外，还包含依赖的 JAR文件，换言之，它是一个独立的应用归档文件（内部的boot-inf等目录可以用来查找需要的类），java 默认的处理jar协议的URLStreamHandler （sun.net.www.protocol.jar.Handler）是不知道从jar中的boot-inf等等文件夹中扫描 需要的类的，故需要替换实现。
			JarFile.registerUrlProtocolHandler();
		}
		//创建 ClassLoader(并根据不同的介质类型创建不同的Archive实例并设置符合条件的classpath（对于jarLauncher来说，classpath为BOOT-INF/classes／和BOOT-INF/lib/*；对于warLauncher来说，classpath为WEB-INF/classes/、WEB-INF/lib/* 和 WEB-INF/lib-provided/*（属于 spring BootWar Launcher 定制实现，WEB-INF/lib-provided目录存放的是＜scope>provided</scope＞的 JAR 文件，原因：
		//传统的 Servlet 应用的 Class Path 路径仅关注 WEB-INF/classes／和WEB-INF/lib目录，因此， WEB-INF/lib-provided／中的 JAR 将被 Servlet 容器忽略，如 Servlet api（在传统的servlet项目中由Servlet 容器提供）。因此，spring boot这样设计的好处在于，打包后的 WAR文件能够在 Servlet容器中兼容运行（当然 Spring Boot WebFlux 应用除外）。总而言之，打包 WAR 文件是一种兼容措施，既能
		//通过WarLauncher直接启动，又能兼容 Servlet 容器环境。 因此， )
		ClassLoader classLoader = createClassLoader(getClassPathArchivesIterator());
		String jarMode = System.getProperty("jarmode");
		//调用实际启动类的main方法（实际启动类来自manifest.mf的Start-Class属性（这个逻辑是由ExecutableArchiveLauncher实现，无论是jarlauncher还是warlauncher都没有覆盖，所以无论是jarlauncher还是warlauncher实际运行的类都是来自start-class属性））
		String launchClass = (jarMode != null && !jarMode.isEmpty()) ? JAR_MODE_LAUNCHER : getMainClass();
		launch(args, launchClass, classLoader);
	}

	/**
	 * Create a classloader for the specified archives.
	 * @param archives the archives
	 * @return the classloader
	 * @throws Exception if the classloader cannot be created
	 * @deprecated since 2.3.0 for removal in 2.5.0 in favor of
	 * {@link #createClassLoader(Iterator)}
	 */
	@Deprecated
	protected ClassLoader createClassLoader(List<Archive> archives) throws Exception {
		return createClassLoader(archives.iterator());
	}

	/**
	 * Create a classloader for the specified archives.
	 * @param archives the archives
	 * @return the classloader
	 * @throws Exception if the classloader cannot be created
	 * @since 2.3.0
	 */
	protected ClassLoader createClassLoader(Iterator<Archive> archives) throws Exception {
		List<URL> urls = new ArrayList<>(50);
		while (archives.hasNext()) {
			urls.add(archives.next().getUrl());
		}
		return createClassLoader(urls.toArray(new URL[0]));
	}

	/**
	 * Create a classloader for the specified URLs.
	 * @param urls the URLs
	 * @return the classloader
	 * @throws Exception if the classloader cannot be created
	 */
	protected ClassLoader createClassLoader(URL[] urls) throws Exception {
		return new LaunchedURLClassLoader(isExploded(), getArchive(), urls, getClass().getClassLoader());
	}

	/**
	 * Launch the application given the archive file and a fully configured classloader.
	 * @param args the incoming arguments
	 * @param launchClass the launch class to run
	 * @param classLoader the classloader
	 * @throws Exception if the launch fails
	 */
	protected void launch(String[] args, String launchClass, ClassLoader classLoader) throws Exception {
		Thread.currentThread().setContextClassLoader(classLoader);
		createMainMethodRunner(launchClass, args, classLoader).run();
	}

	/**
	 * Create the {@code MainMethodRunner} used to launch the application.
	 * @param mainClass the main class
	 * @param args the incoming arguments
	 * @param classLoader the classloader
	 * @return the main method runner
	 */
	//方法的实际执行者为 MainMethodRunner的run（） 方法
	protected MainMethodRunner createMainMethodRunner(String mainClass, String[] args, ClassLoader classLoader) {
		return new MainMethodRunner(mainClass, args);
	}

	/**
	 * Returns the main class that should be launched.
	 * @return the name of the main class
	 * @throws Exception if the main class cannot be obtained
	 */
	protected abstract String getMainClass() throws Exception;

	/**
	 * Returns the archives that will be used to construct the class path.
	 * @return the class path archives
	 * @throws Exception if the class path archives cannot be obtained
	 * @since 2.3.0
	 */
	protected Iterator<Archive> getClassPathArchivesIterator() throws Exception {
		return getClassPathArchives().iterator();
	}

	/**
	 * Returns the archives that will be used to construct the class path.
	 * @return the class path archives
	 * @throws Exception if the class path archives cannot be obtained
	 * @deprecated since 2.3.0 for removal in 2.5.0 in favor of implementing
	 * {@link #getClassPathArchivesIterator()}.
	 */
	@Deprecated
	protected List<Archive> getClassPathArchives() throws Exception {
		throw new IllegalStateException("Unexpected call to getClassPathArchives()");
	}

	//通过当前 Launcher 所在的介质（是 JAR 归档文件实现 还是解压 目录实现）来返回相应的archive实例（JarFileArchive或者ExplodedArchive）
	protected final Archive createArchive() throws Exception {
		ProtectionDomain protectionDomain = getClass().getProtectionDomain();
		CodeSource codeSource = protectionDomain.getCodeSource();
		URI location = (codeSource != null) ? codeSource.getLocation().toURI() : null;
		String path = (location != null) ? location.getSchemeSpecificPart() : null;
		if (path == null) {
			throw new IllegalStateException("Unable to determine code source archive");
		}
		File root = new File(path);
		if (!root.exists()) {
			throw new IllegalStateException("Unable to determine code source archive from " + root);
		}
		return (root.isDirectory() ? new ExplodedArchive(root) : new JarFileArchive(root));
	}

	/**
	 * Returns if the launcher is running in an exploded mode. If this method returns
	 * {@code true} then only regular JARs are supported and the additional URL and
	 * ClassLoader support infrastructure can be optimized.
	 * @return if the jar is exploded.
	 * @since 2.3.0
	 */
	protected boolean isExploded() {
		return false;
	}

	/**
	 * Return the root archive.
	 * @return the root archive
	 * @since 2.3.1
	 */
	protected Archive getArchive() {
		return null;
	}

}
