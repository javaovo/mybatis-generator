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

import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.internal.util.JavaBeansUtil;

/**
 * 生成SpringMVC Controller
 * 
 * @Author: javaovo@163.com
 * @Revision: 1.3.8
 * @Date: 2019-12-12 15:08:28
 * @Since: 1.3.7
 */
public class JavaControllerGenerator extends AbstractJavaGenerator {

	private String requestMappingName;

	private String serviceName;

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getRequestMappingName() {
		return requestMappingName;
	}

	public void setRequestMappingName(String requestMappingName) {
		this.requestMappingName = requestMappingName;
	}

	@Override
	public List<CompilationUnit> getCompilationUnits() {
		FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();

		progressCallback.startTask(getString("Progress.21", table.toString())); //$NON-NLS-1$
		CommentGenerator commentGenerator = context.getCommentGenerator();

		FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getControllerInterfaceType());
		FullyQualifiedJavaType serviceType = new FullyQualifiedJavaType(introspectedTable.getServiceInterfaceType());

		requestMappingName = JavaBeansUtil.getFirstCharacterLowercase(table.getDomainObjectName());
		serviceName = JavaBeansUtil.getFirstCharacterLowercase(serviceType.getShortName());

		TopLevelClass topLevelClass = new TopLevelClass(type);
		topLevelClass.addAnnotation("@Controller");
		topLevelClass.addAnnotation("@RequestMapping(\"" + requestMappingName + "\")");
		topLevelClass.setVisibility(JavaVisibility.PUBLIC);
		commentGenerator.addJavaFileComment(topLevelClass);

		FullyQualifiedJavaType superClass = getSuperClass();

		if (superClass != null) {
			topLevelClass.addImportedType(superClass);
			topLevelClass.setSuperClass(superClass);
		}
		commentGenerator.addModelClassComment(topLevelClass, introspectedTable);

		if (superClass != null) {
			topLevelClass.addImportedType(introspectedTable.getServiceInterfaceType());
			topLevelClass.addImportedType(introspectedTable.getRules().calculateAllFieldsClass());
		}
		topLevelClass.addImportedType(new FullyQualifiedJavaType("org.springframework.stereotype.Controller"));
		topLevelClass.addImportedType(new FullyQualifiedJavaType("org.springframework.web.bind.annotation.RequestMapping"));
		topLevelClass.addImportedType(new FullyQualifiedJavaType("org.springframework.beans.factory.annotation.Autowired"));

		Field fieldService = new Field();
		topLevelClass.addImportedType(serviceType);

		// 注入Spring Service
		fieldService.setName(JavaBeansUtil.getFirstCharacterLowercase(serviceType.getShortName()));
		fieldService.setType(serviceType); // $NON-NLS-1$
		fieldService.setVisibility(JavaVisibility.PRIVATE);
		fieldService.addAnnotation("@Autowired");

		topLevelClass.addField(fieldService);
		if (introspectedTable.isConstructorBased()) {
			addParameterizedConstructor(topLevelClass);

			if (!introspectedTable.isImmutable()) {
				addDefaultConstructor(topLevelClass);
			}
		}
		List<CompilationUnit> answer = new ArrayList<CompilationUnit>();
		if (context.getPlugins().modelBaseRecordClassGenerated(topLevelClass, introspectedTable)) {
			topLevelClass.getAnnotations().removeIf(s -> s.startsWith("@Table"));// 删除不必要的Annotation
			topLevelClass.getImportedTypes().removeIf(t -> "javax.persistence.*".contentEquals(t.getFullyQualifiedName()));// 删除不必要的import
			answer.add(topLevelClass);
		}
		return answer;
	}

	public static Field getJavaBeansField(IntrospectedColumn introspectedColumn, Context context, IntrospectedTable introspectedTable) {
		FullyQualifiedJavaType fqjt = introspectedColumn.getFullyQualifiedJavaType();
		String property = introspectedColumn.getJavaProperty();
		Field field = new Field();
		field.setVisibility(JavaVisibility.PRIVATE);
		field.setType(fqjt);
		field.setName(property);

		context.getCommentGenerator().addFieldComment(field, introspectedTable, introspectedColumn);

		return field;
	}

	private FullyQualifiedJavaType getSuperClass() {
		FullyQualifiedJavaType superClass = null;
		/* introspectedTable */
		String superClassStr = introspectedTable.getContext().getJavaControllerGeneratorConfiguration().getProperty("superClass");
		if (superClassStr != null && !"".equals(superClassStr)) {
			superClass = new FullyQualifiedJavaType(superClassStr);
		} else {
			String rootClass = getRootClass();
			if (rootClass != null) {
				superClass = new FullyQualifiedJavaType(rootClass);
			} else {
				superClass = null;
			}
		}

		return superClass;
	}

	private boolean includePrimaryKeyColumns() {
		return !introspectedTable.getRules().generatePrimaryKeyClass() && introspectedTable.hasPrimaryKeyColumns();
	}

	private boolean includeBLOBColumns() {
		return !introspectedTable.getRules().generateRecordWithBLOBsClass() && introspectedTable.hasBLOBColumns();
	}

	private void addParameterizedConstructor(TopLevelClass topLevelClass) {
		Method method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setConstructor(true);
		method.setName(topLevelClass.getType().getShortName());
		context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

		List<IntrospectedColumn> constructorColumns = includeBLOBColumns() ? introspectedTable.getAllColumns() : introspectedTable.getNonBLOBColumns();

		for (IntrospectedColumn introspectedColumn : constructorColumns) {
			method.addParameter(new Parameter(introspectedColumn.getFullyQualifiedJavaType(), introspectedColumn.getJavaProperty()));
			topLevelClass.addImportedType(introspectedColumn.getFullyQualifiedJavaType());
		}

		StringBuilder sb = new StringBuilder();
		if (introspectedTable.getRules().generatePrimaryKeyClass()) {
			boolean comma = false;
			sb.append("super("); //$NON-NLS-1$
			for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
				if (comma) {
					sb.append(", "); //$NON-NLS-1$
				} else {
					comma = true;
				}
				sb.append(introspectedColumn.getJavaProperty());
			}
			sb.append(");"); //$NON-NLS-1$
			method.addBodyLine(sb.toString());
		}

		List<IntrospectedColumn> introspectedColumns = getColumnsInThisClass();

		for (IntrospectedColumn introspectedColumn : introspectedColumns) {
			sb.setLength(0);
			sb.append("this."); //$NON-NLS-1$
			sb.append(introspectedColumn.getJavaProperty());
			sb.append(" = "); //$NON-NLS-1$
			sb.append(introspectedColumn.getJavaProperty());
			sb.append(';');
			method.addBodyLine(sb.toString());
		}

		topLevelClass.addMethod(method);
	}

	private List<IntrospectedColumn> getColumnsInThisClass() {
		List<IntrospectedColumn> introspectedColumns;
		if (includePrimaryKeyColumns()) {
			if (includeBLOBColumns()) {
				introspectedColumns = introspectedTable.getAllColumns();
			} else {
				introspectedColumns = introspectedTable.getNonBLOBColumns();
			}
		} else {
			if (includeBLOBColumns()) {
				introspectedColumns = introspectedTable.getNonPrimaryKeyColumns();
			} else {
				introspectedColumns = introspectedTable.getBaseColumns();
			}
		}

		return introspectedColumns;
	}
}
