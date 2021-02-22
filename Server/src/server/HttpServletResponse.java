package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class HttpServletResponse {

    //创建一个StringBuilder对象，用来存储响应回来的信息
      private StringBuilder responseContent = new StringBuilder();

    //方法：创建一个向response对象中写入信息的方法(向后拼接)
      public void write(String str){
         this.responseContent.append(str);
      }

    //让response读取一个文件  文件中的内容是响应信息
    public void sendRedirect(String path){
        try {
            File file = new File("src//file//"+path);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String value = reader.readLine();
            while(value!=null){
                this.responseContent.append(value);
                value = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //方法：将响应信息输出
    public String getResponseContent(){

          return this.responseContent.toString();
    }
}
