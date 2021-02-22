package server;

//这个类的目的是为了管理findController方法
//1.该方法与之前服务器Handler做的事情不一致，将其抽离出来
//2.每一次找寻Controller类的时候都需要参考一下web.properties
//      读取文件性能会比较低，增加一个缓存机制
//3.每一个Controller类都是由findController方法来找寻
//      找到了Controller类的目的是为了执行里面的方法
//      让类中的方法有一个统一的规则----便于查找和使用
//4.发现Controller类与之前的Service和Dao相似，只有方法执行，没有属性
//      让Controller类的对象变成单例模式

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

public class ServletController {

     //添加一个缓存 用来存储web.properties配置文件中的信息(请求名字=真实Controller类名)
    private static HashMap<String,String> controllerNameMap = new HashMap<>();
     //添加一个集合 存储被管理的所有Controller类对象
    private static HashMap<String,HttpServlet> controllerObjectMap = new HashMap<>();

    //创建一个静态块 在当前类加载的时候将配置文件中的所有信息读取出来存入缓存集合中
    static {
        try {
            //创建Properties对象，读取配置文件
            Properties pro = new Properties();
            //加载项目中的配置文件
            pro.load(new FileReader("src//web.properties"));
            //通过pro对象的propertyNames方法，返回Enumeration对象所有键的枚举
            Enumeration en = pro.propertyNames();
            //循环，依次查找出配置文件中的每一对的值
            while (en.hasMoreElements()){
                //该方法获取的是请求的名字
                String content = (String)en.nextElement();
                //通过请求名字获取真实的类名
                String realControllerName = pro.getProperty(content);
                //将每一对content和realControllerName存入controllerNameMap集合中
                controllerNameMap.put(content,realControllerName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //找人干活---控制层   (controller 或 action 或 servlet，更习惯称controller)
    //content----index     map-----{{name,zzt},{},{}}
    public static void findController(HttpServletRequest request,HttpServletResponse response){
        //获取request对象中的请求名字
        String content = request.getContent();
        try {
            //首先在controllerObjectMap集合中寻找controller对象
            HttpServlet controllerObject = controllerObjectMap.get(content);
            //若对象为空(不存在)，证明之前没有使用过
            if(controllerObject == null){
                //参考配置文件(缓存)，获取真实类名
                String realControllerName = controllerNameMap.get(content);
                //请求对应的真实类名是否存在  若不为空，说明真实类名存在
                if(realControllerName != null){
                    //通过反射来找寻该类。
                    // (由于反射是需要类全名，而请求过来的资源名并不是我们想要的结果，因此才有了配置文件将资源名与类名产生映射关系)
                    Class clazz = Class.forName(realControllerName);
                    //通过反射创建该类的对象(为了下面执行方法时需要)
                    controllerObject = (HttpServlet) clazz.newInstance();
                    //将新创建的对象放在上面的对象集合内
                    controllerObjectMap.put(content,controllerObject);
                }
            }
            //----以上可以确保controllerObject对象肯定存在-------------
            //找寻类中的方法(找寻到该类的目的是为了执行类中的service方法)
            Class controllerClass = controllerObject.getClass();
            Method serviceMethod = controllerClass.getMethod("service",HttpServletRequest.class,HttpServletResponse.class);
            //执行类中的test方法
            serviceMethod.invoke(controllerObject,request,response);
        } catch (ClassNotFoundException e) {
            response.write("请求的"+content+"Controller不存在");
        } catch (NoSuchMethodException e){
            response.write("405 没有可以执行的方法");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
