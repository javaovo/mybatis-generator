/**
 *    Copyright ${license.git.copyrightYears} the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.config;

import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class JavaServiceGeneratorConfiguration extends TypedPropertyHolder {
	private String targetPackage;
	private String implementationPackage;
	private String targetProject;
	private String baseService;
	private String baseInterface;
	private Boolean enableTx;

	public JavaServiceGeneratorConfiguration() {
		super();
	}

	public String getTargetProject() {
		return targetProject;
	}

	public void setTargetProject(String targetProject) {
		this.targetProject = targetProject;
	}

	public String getTargetPackage() {
		return targetPackage;
	}

	public void setTargetPackage(String targetPackage) {
		this.targetPackage = targetPackage;
	}

	public XmlElement toXmlElement() {
		XmlElement answer = new XmlElement("javaServiceGenerator"); //$NON-NLS-1$
		if (getConfigurationType() != null) {
			answer.addAttribute(new Attribute("type", getConfigurationType())); //$NON-NLS-1$
		}

		if (targetPackage != null) {
			answer.addAttribute(new Attribute("targetPackage", targetPackage)); //$NON-NLS-1$
		}

		if (targetProject != null) {
			answer.addAttribute(new Attribute("targetProject", targetProject)); //$NON-NLS-1$
		}

		/*
		 * if (implementationPackage != null) { answer.addAttribute(new Attribute(
		 * "implementationPackage", targetProject)); //$NON-NLS-1$ }
		 */

		addPropertyXmlElements(answer);

		return answer;
	}

	public void validate(List<String> errors, String contextId) {
		if (!stringHasValue(targetProject)) {
			errors.add(getString("ValidationError.2", contextId)); //$NON-NLS-1$
		}

		if (!stringHasValue(targetPackage)) {
			errors.add(getString("ValidationError.12", //$NON-NLS-1$
					"javaClientGenerator", contextId)); //$NON-NLS-1$
		}

		if (!stringHasValue(getConfigurationType())) {
			errors.add(getString("ValidationError.20", //$NON-NLS-1$
					contextId));
		}
	}

	public String getImplementationPackage() {
		return implementationPackage;
	}

	public void setImplementationPackage(String implementationPackage) {
		this.implementationPackage = implementationPackage;
	}

	public String getBaseService() {
		return baseService;
	}

	public void setBaseService(String baseService) {
		this.baseService = baseService;
	}

	public String getBaseInterface() {
		return baseInterface;
	}

	public void setBaseInterface(String baseInterface) {
		this.baseInterface = baseInterface;
	}

	public Boolean getEnableTx() {
		return enableTx;
	}

	public void setEnableTx(Boolean enableTx) {
		this.enableTx = enableTx;
	}
}
