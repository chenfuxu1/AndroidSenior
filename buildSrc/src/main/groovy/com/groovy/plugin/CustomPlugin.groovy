package com.groovy.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class CustomPlugin implements Plugin<Project> {
    @Override
    void apply(Project target) {
        // 创建一个扩展的对象
        def extension = target.extensions.create("cfxExtension", CustomExtension.class)

        // 整个文件执行完才会执行
        target.afterEvaluate {
            println("Hello ${extension.name}")
        }
    }
}