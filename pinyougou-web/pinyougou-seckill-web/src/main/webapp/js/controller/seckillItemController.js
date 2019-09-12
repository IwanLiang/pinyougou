// 窗口加载完
$(function () {
    var vue = new Vue({
        el : '#app', // 元素绑定
        data : { // 数据模型
            loginName : '', // 登录用户名
            redirectUrl : '', // 重定向URL
            entity : {}, // 秒杀商品对象
            timeStr : ""  // 时间字符串
           
        },
        methods : { // 操作方法
            // 加载用户
            loadUsername () {
                // 定义重定向URL
                this.redirectUrl = window.encodeURIComponent(location.href);
                // 获取登录用户名
                axios.get("/user/showName").then(function(response){
                    vue.loginName = response.data.loginName;
                });
            },
            // 根据秒杀商品id查询秒杀对象
            findOne (){
                // 获取秒杀商品id
                var id = this.getUrlParam("id");
                // 发送异步请求
                axios.get("/seckill/findOne?id=" + id).then(response => {
                    // 获取响应数据
                    this.entity = response.data;
                    // 倒计时
                    this.downcount(this.entity.endTime);

                });
            },
            // 倒计时方法
            downcount (endTime){
                // 计算出相差的毫秒数
                var milliseconds = endTime - new Date().getTime();
                // 计算出相差的秒数
                var seconds = Math.floor(milliseconds / 1000);
                if (seconds >= 0) {
                    // 计算出相差的分钟
                    var minutes = Math.floor(seconds / 60);
                    // 计算出相差的小时
                    var hours = Math.floor(minutes / 60);

                    // 定义数组拼接字符串
                    var resArr = [];
                    // 添加小时
                    resArr.push(this.calc(hours) + ":");
                    // 添加分钟
                    resArr.push(this.calc(minutes - hours * 60) + ":");
                    // 添加秒
                    resArr.push(this.calc(seconds - minutes * 60));
                    // 把数组中的元素转化成字符串
                    this.timeStr = resArr.join("");

                    // 开启定时器
                    setTimeout(() => {
                        this.downcount(endTime);
                    }, 1000);
                }else {
                    this.timeStr = "秒杀已结束！";
                }
            },
            // 计算不够两位前面补零
            calc (num){
                return num > 9 ? num : "0" + num;
            },
            // 立即抢购
            submitOrder () {
                // 判断用户是否登录
                if (this.loginName){ // 已登录
                    // 发送异步请求，实现秒杀下单
                    axios.get("/order/submitOrder?id="
                        + this.entity.id).then(response => {
                            // 获取响应数据
                        if (response.data){
                            // 跳转到支付页面
                            location.href = "/order/pay.html";
                        }else {
                            alert("秒杀下单失败！");
                        }
                    });
                }else { // 未登录
                    // 跳转到单点登录系统
                    location.href = "http://sso.pinyougou.com/login?service=" + this.redirectUrl;
                }
            }
        },
        created : function () { // 创建生命周期
           this.loadUsername();
           this.findOne();
        }
    });
});