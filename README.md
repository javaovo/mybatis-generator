# mybatis-generator1.3.8  需先执行mvn clean deploy -DskipTests将mybatis-generator-core发布到maven私服
# 生成统一风格的Service、ServiceImpl和Controller

## pom.xml配置如下
```xml
<!-- mvn mybatis-generator:generate -Dmybatis.generator.overwrite=true -->
<plugin>
	<groupId>org.mybatis.generator</groupId>
	<artifactId>mybatis-generator-maven-plugin</artifactId>
	<version>1.3.7</version>
	<configuration>
		<configurationFile>${basedir}/src/main/resources/generator/generatorConfig.xml</configurationFile>
		<overwrite>false</overwrite>
		<verbose>true</verbose>
	</configuration>
	<dependencies>
		<dependency>
		    <groupId>tk.mybatis</groupId>
	            <artifactId>mapper-generator</artifactId>
		    <version>1.1.5</version>
	        </dependency>
		<dependency>
		    <groupId>org.mybatis.generator</groupId>
		    <artifactId>mybatis-generator-core</artifactId>
		    <version>1.3.8</version>
		</dependency>
		<dependency>
			<groupId>mysql</groupId>
			<artifactId>mysql-connector-java</artifactId>
			<version>8.0.21</version>
		</dependency>
	</dependencies>
</plugin>
```

## generatorConfig.xml配置如下
```xml
<?xml version="1.0" encoding="UTF-8"?>    
<!DOCTYPE configuration    
    PUBLIC "-//mybatis.org//DTD Config 3.0//EN"    
    "http://mybatis.org/dtd/mybatis-3-config.dtd">

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
