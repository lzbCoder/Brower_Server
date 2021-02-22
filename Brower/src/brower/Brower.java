package brower;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

public class Brower {

    //定义一个Scanner对象，接收用户输入的URL
    private Scanner scan = new Scanner(System.in);
    private Socket socket = null;
    private String ip;
    private int port;

    /**
     方法1：打开浏览器，输入URL
     */
    public void startBrower(){
        //提示用户输入
        System.out.print("URL:");
        //接收用户输入  ip:port/资源名
        String url = scan.nextLine();
        //调用方法2，解析URL
        this.parseURL(url);
    }

    /**
     * 方法2：解析用户输入的URL   ip:port/资源
     * @param url  用户输入的url
     */
    //将URL按照:和/分割成三部分：ip、port、资源
    private void parseURL(String url){
        //返回":"对应的索引号
        int colonIndex = url.indexOf(":");
        //返回"/"对应的索引号
        int slashIndex = url.indexOf("/");
        //分割":"之前的部分，获取ip
        ip = url.substring(0,colonIndex);
        //分割":"与"/"之间的部分，获取port
        port = Integer.parseInt(url.substring(colonIndex+1,slashIndex));
        //分割"/"之后的部分，获取资源
        String contentAndParams = url.substring(slashIndex+1);
        //调用方法3：将解析后的资源发送给服务器
        this.createSocketAndSendRequest(ip,port,contentAndParams);
    }

    /**
     * 方法3：将解析后的资源部分发送给服务器
     * @param ip     请求服务器端的ip
     * @param port   请求服务器端的port
     * @param contentAndParams  向服务器发送的请求内容和参数
     */
    private void createSocketAndSendRequest(String ip,int port,String contentAndParams){
        try {
            //创建socket对象，与服务器建立连接
            socket = new Socket(ip,port);
            //将contentAndParams发送给服务器
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            //发送一行数据。 资源名?key=value&key=value
            out.println(contentAndParams);
            //刷新
            out.flush();
            //浏览器等待响应信息
            this.receiveResponseContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设计一个方法 接受服务器回写的响应信息
     */
    private void receiveResponseContent(){
        try {
            //创建BufferedReader对象，读取服务器回写的响应信息
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String responseContent = reader.readLine();
            //解析响应信息并展示
            this.parseResponseContentAndShow(responseContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设计一个方法 解析响应信息并展示
     * @param responseContent 从服务器中响应回来的内容
     */
    private void parseResponseContentAndShow(String responseContent){

        //创建两个新的变量 用于存储新一次的请求和参数
        //存储请求名
        String content = null;
        //存储参数
        HashMap<String,String> paramsMap = null;

        //解析一个<br>标签
        responseContent = responseContent.replace("<br>","\r\n");
        while(true){
            //解析其他的标签
            int lessThanIndex = responseContent.indexOf("<");
            int greaterThenIndex = responseContent.indexOf(">");
            //如果两个符号成对 证明存在一个有意义的标签
            if(lessThanIndex!=-1 && greaterThenIndex!=-1 && lessThanIndex<greaterThenIndex){
                //输出小于号前面的内容
                System.out.println(responseContent.substring(0,lessThanIndex));
                //分析标签是什么类型 做相应的处理  <input name="" value="">
                String tag = responseContent.substring(lessThanIndex,greaterThenIndex+1);
                if(tag.contains("input")){
                    String value = scan.nextLine();
                    if(paramsMap==null){
                        paramsMap = new HashMap<String,String>();
                    }// <input name="xxx" value="">
                    String[] keyAndValues = tag.split(" ");//将一个大的标记按照空格拆分
                    for(String keyAndValue : keyAndValues){//循环每一组键值对
                        if(keyAndValue.contains("=")){//如果当前的一组中包含有等号 证明是正常的参数
                            String[] KV = keyAndValue.split("=");//按照等号拆分
                            if("name".equals(KV[0])){
                                paramsMap.put(KV[1].substring(1,KV[1].length()-1),value);
                            }
                        }
                    }
                }else if(tag.contains("form")){//<form action="" method="">
                    String[] keyAndValues = tag.split(" ");//将一个大的标记按照空格拆分
                    for(String keyAndValue : keyAndValues){//循环每一组键值对
                        if(keyAndValue.contains("=")){//如果当前的一组中包含有等号 证明是正常的参数
                            String[] KV = keyAndValue.split("=");//按照等号拆分
                            if("action".equals(KV[0])){
                                //产生一个新的请求
                                content = KV[1].substring(1,KV[1].length()-1);
                            }
                        }
                    }
                }
                responseContent = responseContent.substring(greaterThenIndex+1);
            }else{//如果符号不成对 证明不存在其他标签
                //则直接输出全部的内容
                System.out.println(responseContent);
                break;
            }
        }
        //------至此将所有的响应信息解析完毕--------------------------------------
        //如果标签中遇到了<form>表示我还有一次新的请求
        this.sendNewRequest(content,paramsMap);
    }

    /**
     * 设计一个方法，接收并发送新的请求
     * @param content  请求的内容
     * @param paramsMap 请求的参数
     */
    private void sendNewRequest(String content,HashMap<String,String> paramsMap){
        if(content!=null){//遇到了一个form标签 还需要发送下一次请求
            StringBuilder url = new StringBuilder(ip);
            url.append(":");
            url.append(port);
            url.append("/");
            url.append(content);
            if(paramsMap!=null){//证明新请求还有参数
                url.append("?");
                Iterator<String> it = paramsMap.keySet().iterator();
                while(it.hasNext()){
                    String key = it.next();
                    String value = paramsMap.get(key);
                    url.append(key);
                    url.append("=");
                    url.append(value);
                    url.append("&");
                }
                //循环执行完毕后 最终多了一个&符号 将其删除
                url.delete(url.length()-1,url.length());
            }
            this.parseURL(url.toString());
        }
    }

}
