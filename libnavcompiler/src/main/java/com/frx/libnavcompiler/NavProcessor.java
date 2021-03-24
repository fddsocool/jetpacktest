package com.frx.libnavcompiler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.frx.libnavannotation.ActivityDestination;
import com.frx.libnavannotation.FragmentDestination;
import com.google.auto.service.AutoService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"com.frx.libnavannotation.FragmentDestination", "com.frx.libnavannotation.ActivityDestination"})
public class NavProcessor extends AbstractProcessor {

    private Messager mMessager;
    private Filer mFiler;

    private static final String OUTPUT_FILE_NAME = "destination.json";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        //日志打印,在java环境下不能使用android.util.log.e()
        mMessager = processingEnv.getMessager();
        //文件处理工具
        mFiler = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        Set<? extends Element> fragmentElements = roundEnvironment.getElementsAnnotatedWith(FragmentDestination.class);
        Set<? extends Element> activityElements = roundEnvironment.getElementsAnnotatedWith(ActivityDestination.class);

        if (!fragmentElements.isEmpty() || !activityElements.isEmpty()) {
            HashMap<String, JSONObject> destMap = new HashMap<>();
            handleDestination(fragmentElements, FragmentDestination.class, destMap);
            handleDestination(activityElements, ActivityDestination.class, destMap);

            FileOutputStream fos = null;
            OutputStreamWriter writer = null;
            try {
                FileObject resource = mFiler.createResource(StandardLocation.CLASS_OUTPUT, "", OUTPUT_FILE_NAME);
                String resourcePath = resource.toUri().getPath();
//                mMessager.printMessage(Diagnostic.Kind.NOTE, "resourcePath==>:" + resourcePath);

                String appPath = resourcePath.substring(0, resourcePath.indexOf("app") + 4);
//                mMessager.printMessage(Diagnostic.Kind.NOTE, "appPath==>:" + appPath);
                String assetsPath = appPath + "src/main/assets/";
//                mMessager.printMessage(Diagnostic.Kind.NOTE, "assetsPath==>:" + assetsPath);

                File file = new File(assetsPath);
                if (!file.exists()) {
                    file.mkdirs();
                }

                File outPutFile = new File(file, OUTPUT_FILE_NAME);
                if (outPutFile.exists()) {
                    outPutFile.delete();
                }
                outPutFile.createNewFile();
//                mMessager.printMessage(Diagnostic.Kind.NOTE, "outPutFile Path==>:" + outPutFile.getPath());我想买国行，但是国行塞尔达买不了dlc，

                String destMapContent = JSON.toJSONString(destMap);
//                mMessager.printMessage(Diagnostic.Kind.NOTE, "destMapContent==>:" + destMapContent);

                fos = new FileOutputStream(outPutFile);
                writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                writer.write(destMapContent);
                writer.flush();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return true;
    }

    private void handleDestination(Set<? extends Element> elements, Class<? extends Annotation> annotationClaz,
                                   HashMap<String, JSONObject> destMap) {
        for (Element element : elements) {
            TypeElement typeElement = (TypeElement) element;
            //得到全类名
            String clazName = typeElement.getQualifiedName().toString();
            //使用hash获取id
            int id = Math.abs(clazName.hashCode());

            String pageUrl;
            boolean needLogin;
            boolean asStarter;
            boolean isFragment;

            Annotation annotation = element.getAnnotation(annotationClaz);

            //判断注解类型
            if (annotation instanceof FragmentDestination) {
                FragmentDestination dest = (FragmentDestination) annotation;
                pageUrl = dest.pageUrl();
                needLogin = dest.needLogin();
                asStarter = dest.asStarter();
                isFragment = true;
            } else {
                ActivityDestination dest = (ActivityDestination) annotation;
                pageUrl = dest.pageUrl();
                needLogin = dest.needLogin();
                asStarter = dest.asStarter();
                isFragment = false;
            }

            if (destMap.containsKey(pageUrl)) {
                mMessager.printMessage(Diagnostic.Kind.ERROR, "不同的页面不允许使用相同的pageUrl：" + clazName);
            } else {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", id);
                jsonObject.put("needLogin", needLogin);
                jsonObject.put("asStarter", asStarter);
                jsonObject.put("pageUrl", pageUrl);
                jsonObject.put("clazName", clazName);
                jsonObject.put("isFragment", isFragment);
                destMap.put(pageUrl, jsonObject);
            }
        }
    }
}