import java.io.File;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        System.out.println("-------"+System.getProperty("user.dir"));
        String oldJarPath = System.getProperty("user.dir")+"/res/classes.jar";//原jar路径
        String outClassPath = System.getProperty("user.dir")+"/res/temp";//解压临时文件路径
        String newJarPath = System.getProperty("user.dir")+"/res/classes_crack.jar";//重新压缩后的jar路径
        //第一次破解,主要添加类和方法
        Decompression.uncompress(oldJarPath,outClassPath);
        System.out.println("第一次解压成功");
        CrackClass.crack(oldJarPath,outClassPath);
        Compressor zc = new Compressor();
        zc.compress(newJarPath,outClassPath);
        System.out.println("第一次压缩成功");
        if(StrongFileUtil.deleteDir(new File(outClassPath))){
            System.out.println("第一次删除压缩临时文件夹成功");
        }
    }
}
