// import { cfg2D } from '../config';
import config from "@/config"
const webscoket_url = config.VUE_APP_API_BASE_URL.replace("http","ws") + '/wrj-api/websocket/'
export default {
    ws: null,
    initWebscoket(){
        // 创建 WebSocket 连接
        let params="";
        if(localStorage.USER_INFO){
          params = JSON.parse(localStorage.USER_INFO).id;
        }else{
          params = parseInt(Math.random() * 1000000000000)
        }
        const socket = new WebSocket(webscoket_url + params);

        // 连接成功后，向服务器发送数据
        socket.addEventListener('open', () => {
            // alert('连接成功')
          // socket.send('Hello WebSocket!');
        });
  
        // 监听服务器端发送的数据
        socket.addEventListener('message', () => {
          // console.log(`Received: ${event.data}`);
        });
  
        // 监听连接关闭事件
        socket.addEventListener('close', () => {
          // console.log('Connection closed:', event);
        //   alert('连接关闭')
        });
        this.ws = socket;
    }
}