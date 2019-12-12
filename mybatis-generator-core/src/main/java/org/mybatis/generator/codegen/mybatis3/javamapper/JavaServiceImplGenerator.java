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
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.config.JavaServiceGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;

/**
 * 生成Service实现类
 * 
 * @Author: javaovo@163.com
 * @Revision: 1.3.8
 * @Date: 2019-12-12 15:09:20
 * @Since: 1.3.7
 */
public class JavaServiceImplGenerator extends AbstractJavaGenerator {

	@Override
	public List<CompilationUnit> getCompilationUnits() {
		FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
		progressCallback.startTask(getString("Progress.20", table.toString())); //$NON-NLS-1$
		CommentGenerator commentGenerator = context.getCommentGenerator();
		// javaServiceGenerator配置
		JavaServiceGeneratorConfiguration javaServiceConfig = context.getJavaServiceGeneratorConfiguration();
		FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getServiceImplementationType());

		TopLevelClass topLevelClass = new TopLevelClass(type);
		// 引入Spring Bean注解
		topLevelClass.addAnnotation("@Service");
		// 是否配置需要开启事务
		if (javaServiceConfig.getEnableTx()) {
			topLevelClass.addAnnotation("@Transactional");
			topLevelClass.addImportedType(new FullyQualifiedJavaType("org.springframework.transaction.annotation.Transactional"));
		}
		// 设置类可视级别
		topLevelClass.setVisibility(JavaVisibility.PUBLIC);
		// 设置类注释
		commentGenerator.addJavaFileComment(topLevelClass);

		// 引入接口类
		FullyQualifiedJavaType superInterface = getSuperClass();
		if (superInterface != null) {
			topLevelClass.addImportedType(superInterface);
			topLevelClass.addSuperInterface(superInterface);
		}
		// 引入超类Service
		String abstractService = javaServiceConfig.getBaseService();
		if (stringHasValue(abstractService)) {
			FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
			FullyQualifiedJavaType superClassImport = new FullyQualifiedJavaType(abstractService);
			FullyQualifiedJavaType superClass = new FullyQualifiedJavaType(abstractService + "<" + entityType.getShortName() + ">");

			topLevelClass.setSuperClass(superClass);
			topLevelClass.addImportedType(entityType);
			topLevelClass.addImportedType(superClassImport);
		}
		commentGenerator.addModelClassComment(topLevelClass, introspectedTable);

		topLevelClass.addImportedType(introspectedTable.getMyBatis3JavaMapperType());
		topLevelClass.addImportedType(introspectedTable.getRules().calculateAllFieldsClass());// 添加import
		topLevelClass.addImportedType(new FullyQualifiedJavaType("org.springframework.beans.factory.annotation.Autowired"));
		topLevelClass.addImportedType(new FullyQualifiedJavaType("org.springframework.stereotype.Service"));

		// 注入MapperC对象
		FullyQualifiedJavaType mapperType = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
		Field field = new Field();
		field.setName(JavaBeansUtil.getFirstCharacterLowercase(mapperType.getShortName())); // $NON-NLS-1$
		field.setType(mapperType); // $NON-NLS-1$
		field.setVisibility(JavaVisibility.PRIVATE);
		field.addAnnotation("@Autowired");
		topLevelClass.addField(field);

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

	private FullyQualifiedJavaType getSuperClass() {
		FullyQualifiedJavaType superClass = null;
		/* introspectedTable */
		if (introspectedTable.getRules().generateJavaService()) {
			superClass = new FullyQualifiedJavaType(introspectedTable.getServiceInterfaceType());
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
