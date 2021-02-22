package controller;

import server.HttpServlet;
import server.HttpServletRequest;
import server.HttpServletResponse;

public class IndexController extends HttpServlet {
    //一个控制层

    public void service(HttpServletRequest request, HttpServletResponse response){
        //1.获取请求发送过来携带的参数？
        System.out.println("控制层执行啦");
        //2.找到某一个业务层的方法，让其做事

        //3.将最终业务层执行完毕的结果，交还给服务器，让服务器写回给浏览器
//        response.write("响应信息");
        response.sendRedirect("index.view");
    }

    /**
     * 引用类型的参数也可以充当返回值
     *
     * FileInputStream fis = new FileInputStream(file);
     * byte[] b = new byte[1024];  //是一个参数，目的是为了装东西用的
     * int count = fis.read(b);
     * b数组起初是空的，经过fis.read(b)之后，里面就有东西了
     */

}
