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
package org.mybatis.generator.codegen.mybatis3.javamapper;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.codegen.AbstractJavaClientGenerator;
import org.mybatis.generator.codegen.AbstractXmlGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.AbstractJavaMapperMethodGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.XMLMapperGenerator;
import org.mybatis.generator.config.JavaServiceGeneratorConfiguration;
import org.mybatis.generator.config.PropertyRegistry;

/**
 * 生成Service接口
 * 
 * @Author: javaovo@163.com
 * @Revision: 1.3.8
 * @Date: 2019-12-12 15:08:46
 * @Since: 1.3.7
 */
public class JavaServiceGenerator extends AbstractJavaClientGenerator {

	public JavaServiceGenerator() {
		super(true);
	}

	public JavaServiceGenerator(boolean requiresMatchedXMLGenerator) {
		super(requiresMatchedXMLGenerator);
	}

	@Override
	public List<CompilationUnit> getCompilationUnits() {
		progressCallback.startTask(getString("new_Progress.19", //$NON-NLS-1$
				introspectedTable.getFullyQualifiedTable().toString()));
		CommentGenerator commentGenerator = context.getCommentGenerator();

		FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getServiceInterfaceType());
		Interface interfaze = new Interface(type);
		interfaze.setVisibility(JavaVisibility.PUBLIC);
		commentGenerator.addJavaFileComment(interfaze);

		// javaServiceGenerator配置
		JavaServiceGeneratorConfiguration javaServiceConfig = context.getJavaServiceGeneratorConfiguration();

		// 引入继承接口
		String rootInterface = javaServiceConfig.getBaseInterface();
		if (!stringHasValue(rootInterface)) {
			rootInterface = introspectedTable.getTableConfigurationProperty(PropertyRegistry.ANY_ROOT_INTERFACE);
			if (!stringHasValue(rootInterface)) {// 默认继承Mapper接口
				rootInterface = context.getJavaClientGeneratorConfiguration().getProperty(PropertyRegistry.ANY_ROOT_INTERFACE);
			}
		}

		if (!stringHasValue(rootInterface)) {
			FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
			FullyQualifiedJavaType fqjtImport = new FullyQualifiedJavaType(rootInterface);
			FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(rootInterface + "<" + entityType.getShortName() + ">");

			interfaze.addSuperInterface(fqjt);
			interfaze.getImportedTypes().add(fqjtImport);
			interfaze.addImportedType(entityType);
		}

		List<CompilationUnit> answer = new ArrayList<CompilationUnit>();
		answer.add(interfaze);

		List<CompilationUnit> extraCompilationUnits = getExtraCompilationUnits();
		if (extraCompilationUnits != null) {
			answer.addAll(extraCompilationUnits);
		}

		return answer;
	}

	protected void initializeAndExecuteGenerator(AbstractJavaMapperMethodGenerator methodGenerator, Interface interfaze) {
		methodGenerator.setContext(context);
		methodGenerator.setIntrospectedTable(introspectedTable);
		methodGenerator.setProgressCallback(progressCallback);
		methodGenerator.setWarnings(warnings);
		methodGenerator.addInterfaceElements(interfaze);
	}

	public List<CompilationUnit> getExtraCompilationUnits() {
		return null;
	}

	@Override
	public AbstractXmlGenerator getMatchedXMLGenerator() {
		return new XMLMapperGenerator();
	}
}
