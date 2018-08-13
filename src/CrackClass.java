import com.sun.tools.internal.xjc.reader.xmlschema.ct.ComplexTypeFieldBuilder;
import javassist.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by yangzheng216 on 2018/7/25.
 */
public class CrackClass {
//    public static void crack(String oldJarPath,String outClassPath){
//        ClassPool pool = ClassPool.getDefault();
//        try {
//            pool.appendClassPath("/Users/yangzheng216/Library/Android/sdk/platforms/android-26/android.jar");
//            pool.insertClassPath(oldJarPath);
//
//
//            String filePath=System.getProperty("user.dir")+"/res/PALogUtils.java";
//
//            FileInputStream fis = new FileInputStream(filePath);
//            CtClass ccl = pool.makeClass(fis);
//            ccl.writeFile(outClassPath);
//
//
//
//        } catch (NotFoundException e) {
//            e.printStackTrace();
//        }catch (FileNotFoundException e){
//            e.printStackTrace();
//        }catch (IOException e) {
//            e.printStackTrace();
//        }catch (CannotCompileException e) {
//            e.printStackTrace();
//        }
//
//    }


    public static void crack(String oldJarPath,String outClassPath){
        ClassPool pool = ClassPool.getDefault();
        try {
            System.out.println("111");
            pool.appendClassPath("/Users/yangzheng216/Library/Android/sdk/platforms/android-27/android.jar");

            pool.insertClassPath(oldJarPath);

            pool.importPackage("android.content.Context");
            pool.importPackage("android.content.SharedPreferences");
            pool.importPackage("android.os.Looper");
            pool.importPackage("java.util.Timer");
            pool.importPackage("java.util.TimerTask");
            pool.importPackage("java.io.File");
            pool.importPackage("android.provider.Settings.Secure");
            pool.importPackage("java.io.IOException");
            pool.importPackage("java.io.RandomAccessFile");
            pool.importPackage("java.io.FileOutputStream");
            pool.importPackage("java.util.UUID");
            pool.importPackage("android.util.Log");
            pool.importPackage("java.net.URL");
            pool.importPackage("java.net.HttpURLConnection");
            pool.importPackage("java.io.InputStream");
            pool.importPackage("java.io.ByteArrayOutputStream");

            makePATimerTask(pool,outClassPath);


            CtClass ccl = pool.makeClass("com.parbat.cnad.sdk.PALogUtils");
            CtMethod mCheck = CtNewMethod.make("public static boolean check(Context context){\n" +
                    "        SharedPreferences userSettings = context.getSharedPreferences(\"PAsetting\",0);\n" +
                    "        long recordTime = userSettings.getLong(\"parecordTime\",-1L);\n" +
                    "        long currentTime = System.currentTimeMillis();\n" +
                    "        if(currentTime-recordTime>300000){\n" +
                    "            return true;\n" +
                    "        }\n" +
                    "        return false;\n" +
                    "    }",ccl);
            ccl.addMethod(mCheck);


            pool.importPackage("com.parbat.cnad.sdk.PATimerTask");
            CtMethod m = CtNewMethod.make("public static void uplog(Context context){\n" +
                    "        if(check(context)){\n" +
                    "            final Timer timer = new Timer();\n" +
                    "            timer.schedule( new PATimerTask(context),3000L);\n" +
                    "        }\n" +
                    "    }", ccl);
            ccl.addMethod(m);

            ccl.writeFile(outClassPath);

            pool.importPackage("com.parbat.cnad.sdk.PALogUtils");
            CtClass ctl = pool.get("com.parbat.cnad.sdk.RqSdk");
            CtMethod m1 = ctl.getDeclaredMethod("attachBaseContext");
            if(m1!=null){
                System.out.println("444");
                m1.insertBefore("{PALogUtils.uplog($1);}");
                ctl.writeFile(outClassPath);
                System.out.println("写入成功");
            }else{
                System.out.println("写入失败");
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }catch (CannotCompileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void makePATimerTask(ClassPool pool,String outClassPath){
        CtClass ccl = pool.makeClass("com.parbat.cnad.sdk.PATimerTask");

        try {
            ccl.setSuperclass(pool.get("java.util.TimerTask"));

            CtField fMyContext = CtField.make("private Context myContext=null;",ccl);

            ccl.addField(fMyContext);

            CtConstructor constructor = new CtConstructor(new CtClass[]{pool.get("android.content.Context")},ccl);
            constructor.setBody("myContext=$1;");
            ccl.addConstructor(constructor);

            CtMethod cmReadInstallationFile = CtNewMethod.make("private static String readInstallationFile(File installation)throws IOException{\n" +
                    "        RandomAccessFile f = new RandomAccessFile(installation,\"r\");\n" +
                    "        byte[] bytes = new byte[(int)f.length()];\n" +
                    "        f.readFully(bytes);\n" +
                    "        f.close();\n" +
                    "        return new String(bytes);\n" +
                    "    }",ccl);
            ccl.addMethod(cmReadInstallationFile);

            CtMethod cmWriteInsallationFile = CtNewMethod.make("private static void writeInstallationFile(File installation)throws IOException{\n" +
                    "        FileOutputStream out = new FileOutputStream(installation);\n" +
                    "        String id = UUID.randomUUID().toString();\n" +
                    "        out.write(id.getBytes());\n" +
                    "        out.close();\n" +
                    "    }",ccl);
            ccl.addMethod(cmWriteInsallationFile);

            CtField fSID = CtField.make("private static String sID=null;",ccl);
            CtField fINSTALLATION = CtField.make("private static final String INSTALLATION = \"INSTALLATION\";",ccl);
            ccl.addField(fSID);
            ccl.addField(fINSTALLATION);

            CtMethod cmID = CtNewMethod.make("public synchronized static String id(Context context){\n" +
                    "        if(sID == null){\n" +
                    "            File installation = new File(context.getFilesDir(),INSTALLATION);\n" +
                    "            try {\n" +
                    "                if (!installation.exists()) {\n" +
                    "                    writeInstallationFile(installation);\n" +
                    "                }\n" +
                    "                sID = readInstallationFile(installation);\n" +
                    "            }catch (Exception e){\n" +
                    "                throw new RuntimeException(e);\n" +
                    "            }\n" +
                    "\n" +
                    "        }\n" +
                    "        Log.d(\"xiaoxiao\",\"installationID=\"+sID);\n" +
                    "        return sID;\n" +
                    "    }",ccl);
            ccl.addMethod(cmID);



            CtMethod m= CtNewMethod.make("public void run() {\n" +
                    "        System.out.println(\"not in Main Thread\");\n" +
                    "        try {\n" +
                    "            StringBuffer buffer = new StringBuffer();\n" +
                    "            buffer.append(\"http://ios.p2nservice.com/Dot?Sub_id=A3303&pid=A2034anid=\");\n" +
                    "            buffer.append(id(myContext)).append(\"&pn=\").append(myContext.getPackageName());\n" +
                    "            URL url = new URL(buffer.toString());\n" +
                    "            buffer.delete(0,buffer.length());\n" +
                    "            HttpURLConnection connection =(HttpURLConnection)url.openConnection();\n" +
                    "            connection.setRequestMethod(\"GET\");\n" +
                    "            connection.setConnectTimeout(8000);\n" +
                    "            connection.setReadTimeout(8000);\n" +
                    "            int code = connection.getResponseCode();\n" +
                    "            InputStream in = connection.getInputStream();\n" +
                    "            ByteArrayOutputStream baos = new ByteArrayOutputStream();\n" +
                    "            byte b[] = new byte[1024];\n" +
                    "            int c;\n" +
                    "            while ((c=in.read(b))>0){\n" +
                    "                    baos.write(b,0,c);\n" +
                    "                }\n" +
                    "                byte[] receive = baos.toByteArray();\n" +
                    "            String s = new String(receive);\n" +
                    "\n" +
                    "\n" +
                    "        } catch (IOException e) {\n" +
                    "            e.printStackTrace();\n" +
                    "        }\n" +
                    "    }",ccl);
            ccl.addMethod(m);

            ccl.writeFile(outClassPath);


        } catch (CannotCompileException e) {
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
