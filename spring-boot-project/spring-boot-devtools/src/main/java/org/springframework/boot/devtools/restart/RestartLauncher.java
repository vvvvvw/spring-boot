/*
 * Copyright 2012-2020 the original author or authors.
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

package org.springframework.boot.devtools.restart;

import java.lang.reflect.Method;

/**
 * Thread used to launch a restarted application.
 *
 * @author Phillip Webb
 */

/**
 * 当 spring-boot-devtools 认为应用需要重启 ，将启动
 * org.springframework.boot devtools.restart.RestartLauncher 钱程，并将该线程的名称命名为
 * restartedMain ”
 */
class RestartLauncher extends Thread {

	private final String mainClassName;

	private final String[] args;

	private Throwable error;

	RestartLauncher(ClassLoader classLoader, String mainClassName, String[] args,
			UncaughtExceptionHandler exceptionHandler) {
		this.mainClassName = mainClassName;
		this.args = args;
		setName("restartedMain");
		setUncaughtExceptionHandler(exceptionHandler);
		setDaemon(false);
		setContextClassLoader(classLoader);
	}

	@Override
	public void run() {
		try {
			Class<?> mainClass = Class.forName(this.mainClassName, false, getContextClassLoader());
			Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);
			mainMethod.invoke(null, new Object[] { this.args });
		}
		catch (Throwable ex) {
			this.error = ex;
			getUncaughtExceptionHandler().uncaughtException(this, ex);
		}
	}

	Throwable getError() {
		return this.error;
	}

}
