// 窗口加载完
window.onload = function () {
    var vue = new Vue({
        el : '#app', // 元素绑定
        data : { // 数据模型
            loginName : '', // 登录用户名
            redirectUrl : '', // 重定向URL
            outTradeNo : '', // 交易订单号
            money : 0.0 // 支付总金额
        },
        methods : { // 操作方法
            // 加载登录用户名
            loadUsername (){
                // 重定向URL进行uri编码
                this.redirectUrl = window.encodeURIComponent(location.href);
                // 发送异步请求
                axios.get("/user/showName").then(response => {
                    // 获取响应数据
                    this.loginName = response.data.loginName;
                });
            },
            // 生成微信支付二维码
            genPayCode (){
                // 发送异步请求
                axios.get("/order/genPayCode").then(response => {
                    // 获取响应数据 {outTradeNo : '', totalFee : 0, codeUrl : ''}
                    // 获取交易订单号
                    this.outTradeNo = response.data.outTradeNo;
                    // 获取支付总金额
                    this.money = (response.data.totalFee / 100).toFixed(2);
                    // 获取支付URL
                    let codeUrl = response.data.codeUrl;
                    // 生成二维码
                    document.getElementById("qrious").src = "/barcode?url=" + codeUrl;


                    /**
                     * 开启定时器(间隔3秒发送异步请求)
                     */
                    let timer = window.setInterval(() =>{
                        // 发送异步请求
                        axios.get("/order/queryPayStatus?outTradeNo="
                            + this.outTradeNo).then(response => {
                            // 判断支付状态 {status : 1|2|3}
                            if (response.data.status == 1){
                                // 取消定时器
                                window.clearInterval(timer);
                                // 支付成功，跳转到支付成功页面
                                location.href = "/order/paysuccess.html?money=" + this.money;
                            }
                            if (response.data.status == 3){
                                // 取消定时器
                                window.clearInterval(timer);
                                // 支付成功，跳转到支付失败页面
                                location.href = "/order/payfail.html";
                            }
                        });
                    }, 3000);
                });
            }
        },
        created : function () { // 创建生命周期
           this.loadUsername();
           this.genPayCode();
        }
    });
};