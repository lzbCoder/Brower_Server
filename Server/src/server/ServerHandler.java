package server;

import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.HashMap;
import java.util.Properties;

public class ServerHandler extends Thread{

    //属性
    private Socket socket;

    //构造方法
    public ServerHandler(Socket socket) {
        this.socket = socket;
    }

    //重写run方法
    public void run(){
        this.receiveRequest();
    }

    //读取消息
    private void receiveRequest(){
        try {
            //获取最基本的字节流(socket对象中的方法)
            InputStream is = socket.getInputStream();
            //将字节流转化为字符流
            InputStreamReader isr = new InputStreamReader(is);
            //由于isr对象中没有读取一行信息的方法，因此将isr对象包装为高级流
            BufferedReader br = new BufferedReader(isr);
            //调用br对象中的readLine方法，读取一行来自浏览器的数据 content?key=value&key=value
            String contentAndParams = br.readLine();
            //调用解析方法来解析读取过来的信息
            this.parseContentAndParams(contentAndParams);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //解析
    private void parseContentAndParams(String contentAndParams){
        //创建两个变量，用于存储请求过来的资源名和携带的参数
        //存储资源名
        String content = null;
        //存储携带的参数
        HashMap<String,String> paramsMap = null;
        //找寻问号所在的位置。(问号之前是资源名，问号之后是携带的参数)
        int questionMarkIndex = contentAndParams.indexOf("?");
        //判断是否携带了参数
        if(questionMarkIndex != -1){  //表示找到了问号，说明携带了参数
            //截取问号前面的信息-->请求资源名
            content = contentAndParams.substring(0,questionMarkIndex);
            //截取问号后面的信息拆分存入map集合里  key=value&key=value
                //创建一个HashMap对象
            paramsMap = new HashMap<String,String>();
                //截取问号后面的信息，先存入一个String类型的变量里
            String params = contentAndParams.substring(questionMarkIndex+1);
                //将params变量按照&进行拆分,形成一个个key=value,将其存入字符串数组中
            String[] keyAndValues = params.split("&");
                //使用增强for，对keyAndValues进行遍历
            for(String keyAndValue : keyAndValues){
                //再将每一个keyAndValue按照等号("=")进行拆分，将其存入字符串数组中
                String[] KV = keyAndValue.split("=");
                //最后将key和value存入HashMap集合中
                paramsMap.put(KV[0],KV[1]);
            }
        }else {  //没有携带参数
            //请求发过来的信息就是完整的资源名
            content = contentAndParams;
        }
        //-----至此将请求发送过来的字符串解析完毕(content,paramsMap)-----------------
        //将解析出来的字符串包装成一个request对象(放在对象中存储)，方便以后改变资源名和参数的个数，也增强可读性。
        //自己创建两个对象 request  response
        // 一个是为了包含所有请求携带的信息
        HttpServletRequest request = new HttpServletRequest(content,paramsMap);
        // 另一个是为了接受响应回来的结果，创建时是空对象，在Controller执行完毕后将其填满
        HttpServletResponse response = new HttpServletResponse();
        //调用控制层方法(从配置文件中通过反射机制找资源)
        ServletController.findController(request,response);
        //上面这个方法执行完毕，真实的Controller里面的那个service方法执行完毕
        //这时，response对象中就有响应信息啦
        //将响应信息返回给浏览器
        this.responseToBrowser(response);
    }

    //将最终的响应信息 写回浏览器
    private void responseToBrowser(HttpServletResponse response){
        try {
            //创建字符输出流对象
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            //将response对象中的内容输出到浏览器
            out.println(response.getResponseContent());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
