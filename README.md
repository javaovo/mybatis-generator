# mybatis-generator
# mybatis code generator 生成统一风格的Service、ServiceImpl和Controller#
**generatorConfig.xml配置如下**
```
<generatorConfiguration>
	<properties resource="generator/generatorConfig.properties" />
	<context id="Mysql" targetRuntime="MyBatis3Simple" defaultModelType="flat">
		<property name="beginningDelimiter" value="`" />
		<property name="endingDelimiter" value="`" />
		<property name="autoDelimitKeywords" value="true" />
		<property name="javaFileEncoding" value="utf-8" />

		<plugin type="tk.mybatis.mapper.generator.MapperPlugin">
			<property name="mappers" value="tk.mybatis.mapper.common.Mapper" />
		</plugin>
		
		<!-- 数据库连接 -->
		<jdbcConnection driverClass="${jdbc.driverClassName}"
			connectionURL="${jdbc.url}" userId="${jdbc.user}" password="${jdbc.password}">
		</jdbcConnection>

		<!-- model生成器 -->
		<javaModelGenerator targetPackage="com.anggle.test.service.entity"
			targetProject="src/main/java">
		</javaModelGenerator>
		
		<!-- mapper xml生成器 -->
		<sqlMapGenerator targetPackage="mapper" targetProject="src/main/resources">
		</sqlMapGenerator>
		
		<!-- dao接口生成器 -->
		<javaClientGenerator targetPackage="com.anggle.test.service.dao"
			targetProject="src/main/java" type="XMLMAPPER">
		</javaClientGenerator>
		
		<!-- service,serviceImpl生成器 -->
        <javaServiceGenerator targetPackage="com.anggle.test.service.modules" targetProject="src/main/java"
        	implementationPackage="com.anggle.test.service.modules" 
        	baseService="com.anggle.test.service.modules.AbstractBaseService" 
        	baseInterface="com.anggle.test.service.modules.BaseService"
        	enableTx="true">
        </javaServiceGenerator>
        
        <!-- controller生成器-->
        <javaControllerGenerator targetPackage="com.anggle.test.rest" targetProject="src/main/java">
            <property name="superClass" value="com.anggle.test.rest.BaseRestController"></property>
        </javaControllerGenerator>

		<table tableName="coupon" domainObjectName="Coupon">
			<generatedKey column="id" sqlStatement="Mysql" identity="true" />
		</table>
	</context>
</generatorConfiguration>
```