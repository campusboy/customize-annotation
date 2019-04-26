package com.campusboy.annotations.compile;

import com.campusboy.annotations.StaticIntentKey;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class StaticIntentProcessor extends AbstractProcessor {

    private TypeName activityClassName = ClassName.get("android.app", "Activity").withoutAnnotations();
    private TypeName intentClassName = ClassName.get("android.content", "Intent").withoutAnnotations();

    @Override
    public SourceVersion getSupportedSourceVersion() {
        // 支持java1.7
        return SourceVersion.RELEASE_7;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        // 只处理 StaticIntentKey 注解
        return Collections.singleton(StaticIntentKey.class.getCanonicalName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment re) {
        // StaticMapper的bind方法
        MethodSpec.Builder method = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .addParameter(activityClassName, "activity");

        // 查找所有的需要注入的类描述
        List<InjectDesc> injectDescs = findInjectDesc(set, re);

        // log一下
        System.out.println(injectDescs);

        for (int i1 = 0; i1 < injectDescs.size(); i1++) {
            InjectDesc injectDesc = injectDescs.get(i1);

            // 创建需要注解的类的Java文件，如上面所述的 IntentActivity$Binder
            TypeName injectedType = createInjectClassFile(injectDesc);
            TypeName activityName = typeName(injectDesc.activityName);

            // $T导入类型
            // 生成绑定分发的代码
            method.addCode((i1 == 0 ? "" : " else ") + "if (activity instanceof $T) {\n", activityName);
            method.addCode("\t$T binder = new $T();\n", injectedType, injectedType);
            method.addCode("\tbinder.bind((" + activityName + ") activity);\n", activityName, activityName);
            method.addCode("}");
        }
        // 创建StaticMapper类
        createJavaFile("com.campusboy.annotationtest", "StaticMapper", method.build());

        return false;
    }

    private List<InjectDesc> findInjectDesc(Set<? extends TypeElement> set, RoundEnvironment re) {

        Map<TypeElement, List<String[]>> targetClassMap = new HashMap<>();

        // 先获取所有被StaticIntentKey标示的元素
        Set<? extends Element> elements = re.getElementsAnnotatedWith(StaticIntentKey.class);
        for (Element element : elements) {
            // 只关心类别是属性的元素
            if (element.getKind() != ElementKind.FIELD) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "only support field");
                continue;
            }

            // 此处找到的是类的描述类型
            // 因为我们的StaticIntentKey的注解描述是field，所以closingElement元素是类
            TypeElement classType = (TypeElement) element.getEnclosingElement();

            System.out.println(classType);

            // 对类做缓存，避免重复
            List<String[]> nameList = targetClassMap.get(classType);
            if (nameList == null) {
                nameList = new ArrayList<>();
                targetClassMap.put(classType, nameList);
            }

            // 被注解的值，如staticName
            String fieldName = element.getSimpleName().toString();
            // 被注解的值的类型，如String，int
            String fieldTypeName = element.asType().toString();
            // 注解本身的值，如key_name
            String intentName = element.getAnnotation(StaticIntentKey.class).value();

            String[] names = new String[]{fieldName, fieldTypeName, intentName};
            nameList.add(names);
        }

        List<InjectDesc> injectDescList = new ArrayList<>(targetClassMap.size());
        for (Map.Entry<TypeElement, List<String[]>> entry : targetClassMap.entrySet()) {
            String className = entry.getKey().getQualifiedName().toString();
            System.out.println(className);

            // 封装成自定义的描述符
            InjectDesc injectDesc = new InjectDesc();
            injectDesc.activityName = className;
            List<String[]> value = entry.getValue();
            injectDesc.fieldNames = new String[value.size()];
            injectDesc.fieldTypeNames = new String[value.size()];
            injectDesc.intentNames = new String[value.size()];
            for (int i = 0; i < value.size(); i++) {
                String[] names = value.get(i);
                injectDesc.fieldNames[i] = names[0];
                injectDesc.fieldTypeNames[i] = names[1];
                injectDesc.intentNames[i] = names[2];
            }
            injectDescList.add(injectDesc);
        }

        return injectDescList;
    }

    private void createJavaFile(String pkg, String classShortName, MethodSpec... method) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(classShortName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        for (MethodSpec spec : method) {
            builder.addMethod(spec);
        }
        TypeSpec clazzType = builder.build();

        try {
            JavaFile javaFile = JavaFile.builder(pkg, clazzType)
                    .addFileComment(" This codes are generated automatically. Do not modify!")
                    .indent("    ")
                    .build();
            // write to file
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private TypeName createInjectClassFile(InjectDesc injectDesc) {

        ClassName activityName = className(injectDesc.activityName);
        ClassName injectedClass = ClassName.get(activityName.packageName(), activityName.simpleName() + "$Binder");

        MethodSpec.Builder method = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .addParameter(activityName, "activity");

        // $T导入作为类，$N导入作为纯值，$S导入作为字符串
        method.addStatement("$T intent = activity.getIntent()", intentClassName);
        for (int i = 0; i < injectDesc.fieldNames.length; i++) {
            TypeName fieldTypeName = typeName(injectDesc.fieldTypeNames[i]);
            method.addCode("if (intent.hasExtra($S)) {\n", injectDesc.intentNames[i]);
            method.addCode("\tactivity.$N = ($T) intent.getSerializableExtra($S);\n", injectDesc.fieldNames[i], fieldTypeName, injectDesc.intentNames[i]);
            method.addCode("}\n");
        }

        // 生成最终的XXX$Binder文件
        createJavaFile(injectedClass.packageName(), injectedClass.simpleName(), method.build());

        return injectedClass;
    }

    private TypeName typeName(String className) {
        return className(className).withoutAnnotations();
    }

    private ClassName className(String className) {

        // 基础类型描述符
        if (className.indexOf(".") <= 0) {
            switch (className) {
                case "byte":
                    return ClassName.get("java.lang", "Byte");
                case "short":
                    return ClassName.get("java.lang", "Short");
                case "int":
                    return ClassName.get("java.lang", "Integer");
                case "long":
                    return ClassName.get("java.lang", "Long");
                case "float":
                    return ClassName.get("java.lang", "Float");
                case "double":
                    return ClassName.get("java.lang", "Double");
                case "boolean":
                    return ClassName.get("java.lang", "Boolean");
                case "char":
                    return ClassName.get("java.lang", "Character");
                default:
            }
        }

        // 手动解析 java.lang.String，分成java.lang的包名和String的类名
        String packageD = className.substring(0, className.lastIndexOf('.'));
        String name = className.substring(className.lastIndexOf('.') + 1);
        return ClassName.get(packageD, name);
    }

    private static class InjectDesc {
        private String activityName;
        private String[] fieldNames;
        private String[] fieldTypeNames;
        private String[] intentNames;

        @Override
        public String toString() {
            return "InjectDesc{" +
                    "activityName='" + activityName + '\'' +
                    ", fieldNames=" + Arrays.toString(fieldNames) +
                    ", intentNames=" + Arrays.toString(intentNames) +
                    '}';
        }
    }
}
